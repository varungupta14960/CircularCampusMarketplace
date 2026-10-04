package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
import com.campusmarketplace.dao.StudentDAO;
import com.campusmarketplace.model.Listing;
import com.campusmarketplace.model.Student;
import com.campusmarketplace.util.AppConstants;
import com.campusmarketplace.util.CookieUtil;
import com.campusmarketplace.util.PasswordUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Surface a one-time flash success message (e.g. "registration successful") if present.
        HttpSession existing = request.getSession(false);
        if (existing != null && existing.getAttribute(AppConstants.ATTR_SUCCESS) != null) {
            request.setAttribute(AppConstants.ATTR_SUCCESS, existing.getAttribute(AppConstants.ATTR_SUCCESS));
            existing.removeAttribute(AppConstants.ATTR_SUCCESS);
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        request.setAttribute("emailValue", email);

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Email and password are required.");
            forward(request, response);
            return;
        }

        try {
            Student student = studentDAO.findByEmail(email);
            if (student == null || !PasswordUtil.verifyPassword(password, student.getPasswordHash())) {
                request.setAttribute(AppConstants.ATTR_ERROR, "Invalid email or password.");
                forward(request, response);
                return;
            }

            // ---- Session creation ----
            HttpSession session = request.getSession(true);
            session.setAttribute(AppConstants.SESSION_STUDENT, student.withoutPasswordHash());

            // ---- Guest cart (Cookie) -> logged-in cart (HttpSession) migration ----
            migrateGuestCartToSession(request, response, session, student);

            String returnTo = request.getParameter("returnTo");
            String target = sanitizeReturnTo(returnTo, request.getContextPath());
            response.sendRedirect(target);

        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "A database error occurred. Please try again.");
            forward(request, response);
        }
    }

    /**
     * Reads the guestCart Cookie, validates every ID against the database
     * (never trusts the cookie), drops invalid/unavailable/self-owned/
     * duplicate items, merges the rest into the HttpSession cart, then
     * clears the guestCart cookie. This is the concrete demonstration of
     * Cookie (client-side, guest) -> HttpSession (server-side, logged-in).
     */
    private void migrateGuestCartToSession(HttpServletRequest request, HttpServletResponse response,
                                            HttpSession session, Student student) throws SQLException {
        List<Integer> guestCartIds = CookieUtil.readIds(request, AppConstants.COOKIE_GUEST_CART);

        @SuppressWarnings("unchecked")
        LinkedHashSet<Integer> sessionCart =
                (LinkedHashSet<Integer>) session.getAttribute(AppConstants.SESSION_CART);
        if (sessionCart == null) {
            sessionCart = new LinkedHashSet<>();
        }

        if (!guestCartIds.isEmpty()) {
            List<Listing> resolved = listingDAO.findByIds(guestCartIds);
            for (Listing listing : resolved) {
                boolean available = listing.isAvailable();
                boolean ownListing = listing.getSellerId() == student.getStudentId();
                if (available && !ownListing) {
                    sessionCart.add(listing.getListingId()); // LinkedHashSet -> no duplicates
                }
            }
        }

        session.setAttribute(AppConstants.SESSION_CART, sessionCart);
        CookieUtil.clearCookie(response, AppConstants.COOKIE_GUEST_CART);
    }

    /** Only allow redirecting to a same-app relative path, to avoid open-redirect issues. */
    private String sanitizeReturnTo(String returnTo, String contextPath) {
        if (returnTo != null && returnTo.startsWith("/") && !returnTo.startsWith("//")) {
            return contextPath + returnTo;
        }
        return contextPath + "/marketplace";
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/login.jsp");
        dispatcher.forward(request, response);
    }
}
