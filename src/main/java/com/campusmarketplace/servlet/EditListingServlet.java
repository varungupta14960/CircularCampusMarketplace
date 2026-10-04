package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
import com.campusmarketplace.model.Listing;
import com.campusmarketplace.model.Student;
import com.campusmarketplace.util.AppConstants;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;

@WebServlet("/listing/edit")
public class EditListingServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);
        Integer listingId = parseId(request.getParameter("id"));
        if (listingId == null) {
            response.sendRedirect(request.getContextPath() + "/my-listings");
            return;
        }

        try {
            Listing listing = listingDAO.findById(listingId);
            // Server-side authorization: only the owner may edit, regardless of what the UI shows.
            if (listing == null || listing.getSellerId() != student.getStudentId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not own this listing.");
                return;
            }
            if (!listing.isAvailable()) {
                request.setAttribute(AppConstants.ATTR_ERROR, "Sold or removed listings cannot be edited.");
            }
            request.setAttribute("listing", listing);
            request.setAttribute("categories", AppConstants.CATEGORIES);
            request.setAttribute("conditions", AppConstants.CONDITIONS);
            forward(request, response);
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load this listing right now.");
            forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);
        Integer listingId = parseId(request.getParameter("id"));
        if (listingId == null) {
            response.sendRedirect(request.getContextPath() + "/my-listings");
            return;
        }

        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String category = request.getParameter("category");
        String condition = request.getParameter("condition");
        BigDecimal price = parsePrice(request.getParameter("price"));

        String validationError = validate(title, category, condition, price);
        try {
            Listing existing = listingDAO.findById(listingId);
            if (existing == null || existing.getSellerId() != student.getStudentId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not own this listing.");
                return;
            }

            if (validationError != null) {
                existing.setTitle(title);
                existing.setDescription(description);
                existing.setCategory(category);
                existing.setItemCondition(condition);
                existing.setPrice(price);
                request.setAttribute("listing", existing);
                request.setAttribute("categories", AppConstants.CATEGORIES);
                request.setAttribute("conditions", AppConstants.CONDITIONS);
                request.setAttribute(AppConstants.ATTR_ERROR, validationError);
                forward(request, response);
                return;
            }

            // DAO also enforces seller_id = ? and status = 'AVAILABLE' in its WHERE clause (defense in depth).
            boolean updated = listingDAO.updateListing(
                    listingId, student.getStudentId(), title, description, category, price, condition);

            if (!updated) {
                request.setAttribute(AppConstants.ATTR_ERROR,
                        "Could not update listing - it may have already been sold or removed.");
                request.setAttribute("listing", existing);
                request.setAttribute("categories", AppConstants.CATEGORIES);
                request.setAttribute("conditions", AppConstants.CONDITIONS);
                forward(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/listing?id=" + listingId);

        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not update listing. Please try again.");
            forward(request, response);
        }
    }

    private String validate(String title, String category, String condition, BigDecimal price) {
        if (title == null || title.isBlank()) return "Title is required.";
        if (title.length() > 150) return "Title is too long.";
        if (!Arrays.asList(AppConstants.CATEGORIES).contains(category)) return "Please choose a valid category.";
        if (!Arrays.asList(AppConstants.CONDITIONS).contains(condition)) return "Please choose a valid condition.";
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return "Price must be a positive number.";
        return null;
    }

    private BigDecimal parsePrice(String raw) {
        try {
            return raw == null ? null : new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseId(String raw) {
        try {
            return raw == null ? null : Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/edit-listing.jsp");
        dispatcher.forward(request, response);
    }
}
