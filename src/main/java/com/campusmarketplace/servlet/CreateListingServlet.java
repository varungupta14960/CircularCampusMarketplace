package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
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

@WebServlet("/listing/create")
public class CreateListingServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categories", AppConstants.CATEGORIES);
        request.setAttribute("conditions", AppConstants.CONDITIONS);
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // AuthenticationFilter guarantees this is non-null, but we defend anyway.
        Student student = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);
        if (student == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String category = request.getParameter("category");
        String condition = request.getParameter("condition");
        String priceRaw = request.getParameter("price");

        request.setAttribute("categories", AppConstants.CATEGORIES);
        request.setAttribute("conditions", AppConstants.CONDITIONS);
        request.setAttribute("titleValue", title);
        request.setAttribute("descriptionValue", description);
        request.setAttribute("categoryValue", category);
        request.setAttribute("conditionValue", condition);
        request.setAttribute("priceValue", priceRaw);

        BigDecimal price = parsePrice(priceRaw);
        String validationError = validate(title, category, condition, price);
        if (validationError != null) {
            request.setAttribute(AppConstants.ATTR_ERROR, validationError);
            forward(request, response);
            return;
        }

        try {
            int listingId = listingDAO.createListing(
                    student.getStudentId(), title, description, category, price, condition);
            response.sendRedirect(request.getContextPath() + "/listing?id=" + listingId);
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not create listing. Please try again.");
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

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/create-listing.jsp");
        dispatcher.forward(request, response);
    }
}
