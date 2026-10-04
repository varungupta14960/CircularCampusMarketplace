package com.campusmarketplace.dao;

import com.campusmarketplace.model.Transaction;
import com.campusmarketplace.util.AppConstants;
import com.campusmarketplace.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class TransactionDAO {

    /** Result of a checkout attempt. */
    public static class CheckoutResult {
        public boolean success;
        public String message;
        public BigDecimal totalAmount = BigDecimal.ZERO;
        public List<Transaction> transactions = new ArrayList<>();

        static CheckoutResult failure(String message) {
            CheckoutResult r = new CheckoutResult();
            r.success = false;
            r.message = message;
            return r;
        }
    }

    private static class ListingRow {
        int listingId;
        int sellerId;
        BigDecimal price;
        String status;
    }

    /**
     * Performs checkout for the given buyer and set of listing IDs as a
     * single atomic JDBC transaction:
     *   - locks every listing row (SELECT ... FOR UPDATE) in a fixed
     *     ascending order to avoid deadlocks with concurrent checkouts,
     *   - re-validates availability/ownership/price from the DATABASE
     *     (never trusts any client-supplied price),
     *   - locks the buyer's and every distinct seller's wallet row
     *     (SELECT ... FOR UPDATE) before mutating balances, again in
     *     ascending student_id order,
     *   - deducts the buyer, credits each seller, marks listings SOLD,
     *     inserts one transaction row per listing, and awards
     *     sustainability points to both sides,
     *   - commits only if every step succeeds; rolls back completely
     *     otherwise, so no partial checkout can ever be persisted.
     */
    public CheckoutResult performCheckout(int buyerId, List<Integer> listingIds) throws SQLException {
        if (listingIds == null || listingIds.isEmpty()) {
            return CheckoutResult.failure("Your cart is empty.");
        }

        // Lock listings in a fixed order (ascending ID) to avoid deadlocks
        // between two concurrent checkouts that share an item.
        TreeSet<Integer> sortedIds = new TreeSet<>(listingIds);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Map<Integer, ListingRow> lockedListings = new LinkedHashMap<>();

                // 1. Lock + validate every listing.
                for (Integer listingId : sortedIds) {
                    ListingRow row = lockListingForUpdate(conn, listingId);
                    if (row == null) {
                        conn.rollback();
                        return CheckoutResult.failure("Listing #" + listingId + " no longer exists.");
                    }
                    if (!"AVAILABLE".equals(row.status)) {
                        conn.rollback();
                        return CheckoutResult.failure("Listing #" + listingId + " is no longer available (already sold or removed).");
                    }
                    if (row.sellerId == buyerId) {
                        conn.rollback();
                        return CheckoutResult.failure("You cannot purchase your own listing (#" + listingId + ").");
                    }
                    lockedListings.put(listingId, row);
                }

                // 2. Compute authoritative total from DB prices only.
                BigDecimal total = BigDecimal.ZERO;
                for (ListingRow row : lockedListings.values()) {
                    total = total.add(row.price);
                }

                // 3. Lock buyer wallet row, verify sufficient balance.
                BigDecimal buyerBalance = lockWalletForUpdate(conn, buyerId);
                if (buyerBalance == null) {
                    conn.rollback();
                    return CheckoutResult.failure("Buyer account not found.");
                }
                if (buyerBalance.compareTo(total) < 0) {
                    conn.rollback();
                    return CheckoutResult.failure(
                            "Insufficient wallet balance. Total is " + total + " but your balance is " + buyerBalance + ".");
                }

                // 4. Lock every distinct seller's wallet row, in ascending student_id order.
                TreeSet<Integer> sellerIds = new TreeSet<>();
                for (ListingRow row : lockedListings.values()) {
                    sellerIds.add(row.sellerId);
                }
                Map<Integer, BigDecimal> sellerBalances = new LinkedHashMap<>();
                for (Integer sellerId : sellerIds) {
                    BigDecimal bal = lockWalletForUpdate(conn, sellerId);
                    if (bal == null) {
                        conn.rollback();
                        return CheckoutResult.failure("Seller account #" + sellerId + " not found.");
                    }
                    sellerBalances.put(sellerId, bal);
                }

                // 5. Deduct buyer.
                updateWallet(conn, buyerId, buyerBalance.subtract(total));
                awardPoints(conn, buyerId, AppConstants.SUSTAINABILITY_POINTS_PER_TRANSACTION * lockedListings.size());

                // 6. Credit each seller with the sum of items they sold in this checkout,
                //    mark listings SOLD, insert transaction rows.
                Map<Integer, BigDecimal> sellerCredit = new LinkedHashMap<>();
                Map<Integer, Integer> sellerItemCount = new LinkedHashMap<>();
                for (ListingRow row : lockedListings.values()) {
                    sellerCredit.merge(row.sellerId, row.price, BigDecimal::add);
                    sellerItemCount.merge(row.sellerId, 1, Integer::sum);
                }
                for (Integer sellerId : sellerIds) {
                    BigDecimal newBalance = sellerBalances.get(sellerId).add(sellerCredit.get(sellerId));
                    updateWallet(conn, sellerId, newBalance);
                    awardPoints(conn, sellerId,
                            AppConstants.SUSTAINABILITY_POINTS_PER_TRANSACTION * sellerItemCount.get(sellerId));
                }

                CheckoutResult result = new CheckoutResult();
                result.success = true;
                result.totalAmount = total;

                for (Map.Entry<Integer, ListingRow> entry : lockedListings.entrySet()) {
                    int listingId = entry.getKey();
                    ListingRow row = entry.getValue();
                    markListingSold(conn, listingId);
                    int txnId = insertTransaction(conn, buyerId, row.sellerId, listingId, row.price);

                    Transaction txn = new Transaction();
                    txn.setTxnId(txnId);
                    txn.setBuyerId(buyerId);
                    txn.setSellerId(row.sellerId);
                    txn.setListingId(listingId);
                    txn.setAmount(row.price);
                    result.transactions.add(txn);
                }

                conn.commit();
                return result;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private ListingRow lockListingForUpdate(Connection conn, int listingId) throws SQLException {
        String sql = "SELECT listing_id, seller_id, price, status FROM listings WHERE listing_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ListingRow row = new ListingRow();
                row.listingId = rs.getInt("listing_id");
                row.sellerId = rs.getInt("seller_id");
                row.price = rs.getBigDecimal("price");
                row.status = rs.getString("status");
                return row;
            }
        }
    }

    private BigDecimal lockWalletForUpdate(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT wallet_balance FROM students WHERE student_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("wallet_balance") : null;
            }
        }
    }

    private void updateWallet(Connection conn, int studentId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE students SET wallet_balance = ? WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    private void awardPoints(Connection conn, int studentId, int points) throws SQLException {
        String sql = "UPDATE students SET sustainability_points = sustainability_points + ? WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    private void markListingSold(Connection conn, int listingId) throws SQLException {
        String sql = "UPDATE listings SET status = 'SOLD' WHERE listing_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.executeUpdate();
        }
    }

    private int insertTransaction(Connection conn, int buyerId, int sellerId, int listingId, BigDecimal amount)
            throws SQLException {
        String sql = "INSERT INTO transactions (buyer_id, seller_id, listing_id, amount, status) "
                + "VALUES (?, ?, ?, ?, 'COMPLETED')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, sellerId);
            ps.setInt(3, listingId);
            ps.setBigDecimal(4, amount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // ---- History queries (read-only, no locking needed) ----

    public List<Transaction> findPurchases(int buyerId) throws SQLException {
        String sql = "SELECT t.txn_id, t.buyer_id, t.seller_id, t.listing_id, t.amount, t.txn_date, "
                + "l.title AS listing_title, s.name AS seller_name "
                + "FROM transactions t "
                + "JOIN listings l ON t.listing_id = l.listing_id "
                + "JOIN students s ON t.seller_id = s.student_id "
                + "WHERE t.buyer_id = ? ORDER BY t.txn_date DESC";
        return queryTransactions(sql, buyerId, false);
    }

    public List<Transaction> findSales(int sellerId) throws SQLException {
        String sql = "SELECT t.txn_id, t.buyer_id, t.seller_id, t.listing_id, t.amount, t.txn_date, "
                + "l.title AS listing_title, b.name AS buyer_name "
                + "FROM transactions t "
                + "JOIN listings l ON t.listing_id = l.listing_id "
                + "JOIN students b ON t.buyer_id = b.student_id "
                + "WHERE t.seller_id = ? ORDER BY t.txn_date DESC";
        return queryTransactions(sql, sellerId, true);
    }

    private List<Transaction> queryTransactions(String sql, int id, boolean isSellerView) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaction> list = new ArrayList<>();
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTxnId(rs.getInt("txn_id"));
                    t.setBuyerId(rs.getInt("buyer_id"));
                    t.setSellerId(rs.getInt("seller_id"));
                    t.setListingId(rs.getInt("listing_id"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setTxnDate(rs.getTimestamp("txn_date"));
                    t.setListingTitle(rs.getString("listing_title"));
                    if (isSellerView) {
                        t.setBuyerName(rs.getString("buyer_name"));
                    } else {
                        t.setSellerName(rs.getString("seller_name"));
                    }
                    list.add(t);
                }
                return list;
            }
        }
    }
}
