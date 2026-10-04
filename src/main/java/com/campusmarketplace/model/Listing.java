package com.campusmarketplace.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Listing implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_REMOVED = "REMOVED";

    private int listingId;
    private int sellerId;
    private String sellerName; // populated via JOIN for display only, not persisted from here
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private String itemCondition;
    private String status;
    private LocalDateTime createdAt;

    public Listing() {
    }

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAvailable() {
        return STATUS_AVAILABLE.equals(status);
    }
}
