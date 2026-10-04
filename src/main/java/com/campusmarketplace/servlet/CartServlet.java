package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
import com.campusmarketplace.model.Listing;
import com.campusmarketplace.model.Student;
import com.campusmarketplace.util.AppConstants;
import com.campusmarketplace.util.CookieUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Cart handling for BOTH guests and logged-in users, deliberately in one
 * servlet so the Cookie <-> HttpSession distinction is explicit:
 *
 *   - Guest cart lives ONLY in the "guestCart" Cookie (listing IDs only).
 *   - Logged-in cart lives ONLY in the HttpSession ("cart" attribute, a
 *     LinkedHashSet<Integer> of listing IDs).
 *
 * Neither ever stores prices client-side; prices are always re-read from
 * the database at view/checkout time.
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = getLoggedInStudent(request);
        try {
            List<Listing> cartItems;
            if (student != null) {
                LinkedHashSet<Integer> sessionCart = getOrCreateSessionCart(request.getSession());
                cartItems = resolveAndCleanSessionCart(sessionCart, request.getSession());
            } else {
                List<Integer> guestIds = CookieUtil.readIds(request, AppConstants.COOKIE_GUEST_CART);
                cartItems = resolveAndCleanGuestCookie(guestIds, response);
            }

            BigDecimal total = BigDecimal.ZERO;
            for (Listing l : cartItems) {
                total = total.add(l.getPrice());
            }
            request.setAttribute("cartItems", cartItems);
            request.setAttribute("cartTotal", total);

        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load your cart right now.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/cart.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        Integer listingId = parseId(request.getParameter("id"));
        Student student = getLoggedInStudent(request);

        try {
            if ("add".equals(action) && listingId != null) {
                handleAdd(request, response, student, listingId);
            } else if ("remove".equals(action) && listingId != null) {
                handleRemove(request, response, student, listingId);
            } else if ("clear".equals(action)) {
                handleClear(request, response, student);
            }
        } catch (SQLException e) {
            request.getSession().setAttribute(AppConstants.ATTR_ERROR, "A database error occurred. Please try again.");
        }

        String redirect = sanitizeRedirect(request.getParameter("redirect"), request.getContextPath());
        response.sendRedirect(redirect);
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response, Student student, int listingId)
            throws SQLException {
        Listing listing = listingDAO.findById(listingId);
        if (listing == null || !listing.isAvailable()) {
            request.getSession().setAttribute(AppConstants.ATTR_ERROR, "That item is no longer available.");
            return;
        }
        if (student != null) {
            if (listing.getSellerId() == student.getStudentId()) {
                request.getSession().setAttribute(AppConstants.ATTR_ERROR, "You cannot add your own listing to your cart.");
                return;
            }
            LinkedHashSet<Integer> sessionCart = getOrCreateSessionCart(request.getSession());
            sessionCart.add(listingId); // Set semantics -> no duplicates
            request.getSession().setAttribute(AppConstants.SESSION_CART, sessionCart);
        } else {
            List<Integer> guestIds = CookieUtil.readIds(request, AppConstants.COOKIE_GUEST_CART);
            List<Integer> updated = CookieUtil.addIfAbsentBounded(guestIds, listingId, AppConstants.GUEST_CART_MAX);
            CookieUtil.writeIds(response, AppConstants.COOKIE_GUEST_CART, updated);
        }
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response, Student student, int listingId) {
        if (student != null) {
            LinkedHashSet<Integer> sessionCart = getOrCreateSessionCart(request.getSession());
            sessionCart.remove(listingId);
            request.getSession().setAttribute(AppConstants.SESSION_CART, sessionCart);
        } else {
            List<Integer> guestIds = CookieUtil.readIds(request, AppConstants.COOKIE_GUEST_CART);
            List<Integer> updated = CookieUtil.removeId(guestIds, listingId);
            CookieUtil.writeIds(response, AppConstants.COOKIE_GUEST_CART, updated);
        }
    }

    private void handleClear(HttpServletRequest request, HttpServletResponse response, Student student) {
        if (student != null) {
            request.getSession().setAttribute(AppConstants.SESSION_CART, new LinkedHashSet<Integer>());
        } else {
            CookieUtil.clearCookie(response, AppConstants.COOKIE_GUEST_CART);
        }
    }

    /** Resolves the session cart's listing IDs to full Listing objects, silently dropping any that
     *  became unavailable/deleted since being added, and writes the cleaned set back to the session. */
    private List<Listing> resolveAndCleanSessionCart(LinkedHashSet<Integer> sessionCart, HttpSession session)
            throws SQLException {
        List<Listing> resolved = listingDAO.findByIds(new ArrayList<>(sessionCart));
        List<Listing> valid = new ArrayList<>();
        LinkedHashSet<Integer> cleaned = new LinkedHashSet<>();
        for (Listing l : resolved) {
            if (l.isAvailable()) {
                valid.add(l);
                cleaned.add(l.getListingId());
            }
        }
        session.setAttribute(AppConstants.SESSION_CART, cleaned);
        return valid;
    }

    /** Same cleanup as above, but for the guest cookie: rewrites the cookie to drop stale/invalid IDs. */
    private List<Listing> resolveAndCleanGuestCookie(List<Integer> guestIds, HttpServletResponse response)
            throws SQLException {
        List<Listing> resolved = listingDAO.findByIds(guestIds);
        List<Listing> valid = new ArrayList<>();
        List<Integer> cleanedIds = new ArrayList<>();
        for (Listing l : resolved) {
            if (l.isAvailable()) {
                valid.add(l);
                cleanedIds.add(l.getListingId());
            }
        }
        if (cleanedIds.size() != guestIds.size()) {
            CookieUtil.writeIds(response, AppConstants.COOKIE_GUEST_CART, cleanedIds);
        }
        return valid;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<Integer> getOrCreateSessionCart(HttpSession session) {
        LinkedHashSet<Integer> cart = (LinkedHashSet<Integer>) session.getAttribute(AppConstants.SESSION_CART);
        if (cart == null) {
            cart = new LinkedHashSet<>();
            session.setAttribute(AppConstants.SESSION_CART, cart);
        }
        return cart;
    }

    private Student getLoggedInStudent(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Student) session.getAttribute(AppConstants.SESSION_STUDENT);
    }

    private Integer parseId(String raw) {
        try {
            return raw == null ? null : Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String sanitizeRedirect(String redirect, String contextPath) {
        if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")) {
            return contextPath + redirect;
        }
        return contextPath + "/cart";
    }
}
