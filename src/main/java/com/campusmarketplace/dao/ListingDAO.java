package com.campusmarketplace.dao;

import com.campusmarketplace.model.Listing;
import com.campusmarketplace.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the listings table. All dynamic search/filter SQL is
 * built with PreparedStatement placeholders only - user-supplied values
 * (keyword, category, min/max price) are NEVER concatenated into the SQL
 * string itself, only appended as '?' parameters.
 */
public class ListingDAO {

    private static final String SELECT_BASE =
            "SELECT l.listing_id, l.seller_id, s.name AS seller_name, l.title, l.description, "
            + "l.category, l.price, l.item_condition, l.status, l.created_at "
            + "FROM listings l JOIN students s ON l.seller_id = s.student_id ";

    public int createListing(int sellerId, String title, String description, String category,
                              BigDecimal price, String itemCondition) throws SQLException {
        String sql = "INSERT INTO listings (seller_id, title, description, category, price, item_condition, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'AVAILABLE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sellerId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setString(4, category);
            ps.setBigDecimal(5, price);
            ps.setString(6, itemCondition);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Listing creation failed: no generated ID returned");
    }

    public Listing findById(int listingId) throws SQLException {
        String sql = SELECT_BASE + "WHERE l.listing_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Resolves multiple listing IDs at once (used for cart/recently-viewed cookie resolution). */
    public List<Listing> findByIds(List<Integer> listingIds) throws SQLException {
        List<Listing> result = new ArrayList<>();
        if (listingIds == null || listingIds.isEmpty()) {
            return result;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < listingIds.size(); i++) {
            if (i > 0) placeholders.append(',');
            placeholders.append('?');
        }
        String sql = SELECT_BASE + "WHERE l.listing_id IN (" + placeholders + ")";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < listingIds.size(); i++) {
                ps.setInt(i + 1, listingIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        // Preserve the caller's original ordering (SQL IN() does not guarantee order)
        List<Listing> ordered = new ArrayList<>();
        for (Integer id : listingIds) {
            for (Listing l : result) {
                if (l.getListingId() == id) {
                    ordered.add(l);
                    break;
                }
            }
        }
        return ordered;
    }

    public List<Listing> findBySeller(int sellerId) throws SQLException {
        String sql = SELECT_BASE + "WHERE l.seller_id = ? ORDER BY l.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Listing> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        }
    }

    /**
     * Server-side search/filter over AVAILABLE listings only.
     * All parameters are optional; null/blank means "no filter" for that field.
     */
    public List<Listing> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE l.status = 'AVAILABLE'");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (l.title LIKE ? OR l.description LIKE ?)");
            String likeTerm = "%" + keyword.trim() + "%";
            params.add(likeTerm);
            params.add(likeTerm);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND l.category = ?");
            params.add(category);
        }
        if (minPrice != null) {
            sql.append(" AND l.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND l.price <= ?");
            params.add(maxPrice);
        }
        sql.append(" ORDER BY l.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Listing> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        }
    }

    /** Updates a listing. Ownership is enforced in the WHERE clause (defense in depth alongside servlet-level checks). */
    public boolean updateListing(int listingId, int sellerId, String title, String description,
                                  String category, BigDecimal price, String itemCondition) throws SQLException {
        String sql = "UPDATE listings SET title = ?, description = ?, category = ?, price = ?, item_condition = ? "
                + "WHERE listing_id = ? AND seller_id = ? AND status = 'AVAILABLE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.setBigDecimal(4, price);
            ps.setString(5, itemCondition);
            ps.setInt(6, listingId);
            ps.setInt(7, sellerId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Soft-deletes a listing (status -> REMOVED). Only the owner can remove, and only while AVAILABLE. */
    public boolean removeListing(int listingId, int sellerId) throws SQLException {
        String sql = "UPDATE listings SET status = 'REMOVED' "
                + "WHERE listing_id = ? AND seller_id = ? AND status = 'AVAILABLE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, sellerId);
            return ps.executeUpdate() == 1;
        }
    }

    private Listing mapRow(ResultSet rs) throws SQLException {
        Listing l = new Listing();
        l.setListingId(rs.getInt("listing_id"));
        l.setSellerId(rs.getInt("seller_id"));
        l.setSellerName(rs.getString("seller_name"));
        l.setTitle(rs.getString("title"));
        l.setDescription(rs.getString("description"));
        l.setCategory(rs.getString("category"));
        l.setPrice(rs.getBigDecimal("price"));
        l.setItemCondition(rs.getString("item_condition"));
        l.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            l.setCreatedAt(ts.toLocalDateTime());
        }
        return l;
    }
}
