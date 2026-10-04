package com.campusmarketplace.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Helpers for reading/writing the two cookies used by this application:
 *   - recentlyViewed : bounded, most-recent-first list of listing IDs
 *   - guestCart      : bounded list of listing IDs for guest shopping cart
 *
 * Cookies only ever store listing IDs (ints). Never prices, never
 * passwords, never wallet data. The database is always re-consulted as
 * the source of truth before these IDs are trusted for anything.
 */
public final class CookieUtil {

    private CookieUtil() {
    }

    /** Finds a cookie by name, or returns null if absent. */
    public static Cookie findCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Safely parses a comma-separated cookie value into a list of listing
     * IDs. Malformed/non-numeric tokens are silently skipped rather than
     * throwing, since cookie content is client-controlled and untrusted.
     */
    public static List<Integer> readIds(HttpServletRequest request, String cookieName) {
        List<Integer> ids = new ArrayList<>();
        Cookie cookie = findCookie(request, cookieName);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return ids;
        }
        String[] tokens = cookie.getValue().split(",");
        LinkedHashSet<Integer> deduped = new LinkedHashSet<>();
        for (String token : tokens) {
            try {
                int id = Integer.parseInt(token.trim());
                if (id > 0) {
                    deduped.add(id);
                }
            } catch (NumberFormatException ignored) {
                // skip invalid/corrupted token - never trust cookie content
            }
        }
        ids.addAll(deduped);
        return ids;
    }

    /** Writes the given list of IDs to a cookie, comma-joined, path "/". */
    public static void writeIds(HttpServletResponse response, String cookieName, List<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(ids.get(i));
        }
        Cookie cookie = new Cookie(cookieName, sb.toString());
        cookie.setPath("/");
        cookie.setMaxAge(AppConstants.COOKIE_MAX_AGE_SECONDS);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /** Immediately expires a cookie (used to clear guestCart after migration). */
    public static void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * Moves listingId to the front of the list (used for "recently viewed":
     * re-viewing an item bumps it back to the top), removing any existing
     * occurrence first, then truncates to maxSize.
     */
    public static List<Integer> moveToFrontBounded(List<Integer> existing, int listingId, int maxSize) {
        List<Integer> result = new ArrayList<>();
        result.add(listingId);
        for (Integer id : existing) {
            if (id != listingId && result.size() < maxSize) {
                result.add(id);
            }
        }
        if (result.size() > maxSize) {
            result = result.subList(0, maxSize);
        }
        return result;
    }

    /**
     * Appends listingId to the list if not already present, bounded to
     * maxSize (used for the guest cart, where order of addition matters
     * more than recency).
     */
    public static List<Integer> addIfAbsentBounded(List<Integer> existing, int listingId, int maxSize) {
        List<Integer> result = new ArrayList<>(existing);
        if (!result.contains(listingId) && result.size() < maxSize) {
            result.add(listingId);
        }
        return result;
    }

    /** Removes a single listingId from the list, if present. */
    public static List<Integer> removeId(List<Integer> existing, int listingId) {
        List<Integer> result = new ArrayList<>();
        for (Integer id : existing) {
            if (id != listingId) {
                result.add(id);
            }
        }
        return result;
    }
}
