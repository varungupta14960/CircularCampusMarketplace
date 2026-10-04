package com.campusmarketplace.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private int txnId;
    private int buyerId;
    private String buyerName;   // joined, display only
    private int sellerId;
    private String sellerName;  // joined, display only
    private int listingId;
    private String listingTitle; // joined, display only
    private BigDecimal amount;
    // Timestamp (not LocalDateTime) deliberately - it IS-A java.util.Date, which is
    // what JSTL's <fmt:formatDate> requires in the JSP views.
    private Timestamp txnDate;

    public Transaction() {
    }

    public int getTxnId() {
        return txnId;
    }

    public void setTxnId(int txnId) {
        this.txnId = txnId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(Timestamp txnDate) {
        this.txnDate = txnDate;
    }
}
