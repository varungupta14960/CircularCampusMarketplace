package com.campusmarketplace.util;

/**
 * Central place for session attribute names, cookie names, and fixed
 * lists (categories/conditions) so every servlet and JSP agrees on the
 * same names. Avoids typo-mismatches between producer and consumer code.
 */
public final class AppConstants {

    private AppConstants() {
    }

    // ---- Session attributes ----
    public static final String SESSION_STUDENT = "student";
    public static final String SESSION_CART = "cart"; // LinkedHashSet<Integer> of listing IDs

    // ---- Cookie names ----
    public static final String COOKIE_RECENTLY_VIEWED = "recentlyViewed";
    public static final String COOKIE_GUEST_CART = "guestCart";

    // ---- Cookie bounds ----
    public static final int RECENTLY_VIEWED_MAX = 8;
    public static final int GUEST_CART_MAX = 20;
    public static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 7; // 7 days

    // ---- Domain fixed lists ----
    public static final String[] CATEGORIES = {
            "Books", "Electronics", "Hostel Essentials", "Furniture", "Clothing", "Other"
    };

    public static final String[] CONDITIONS = {
            "New", "Like New", "Good", "Fair"
    };

    // ---- Business rules ----
    public static final int SUSTAINABILITY_POINTS_PER_TRANSACTION = 10;

    // ---- Request attribute keys (used for flash-style messages across forward/redirect) ----
    public static final String ATTR_ERROR = "error";
    public static final String ATTR_SUCCESS = "success";
}
