package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
import com.campusmarketplace.model.Listing;
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
import java.util.List;

@WebServlet("/marketplace")
public class MarketplaceServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = emptyToNull(request.getParameter("q"));
        String category = emptyToNull(request.getParameter("category"));
        BigDecimal minPrice = parsePrice(request.getParameter("minPrice"));
        BigDecimal maxPrice = parsePrice(request.getParameter("maxPrice"));

        // Keep raw values to repopulate the filter form.
        request.setAttribute("qValue", request.getParameter("q"));
        request.setAttribute("categoryValue", category);
        request.setAttribute("minPriceValue", request.getParameter("minPrice"));
        request.setAttribute("maxPriceValue", request.getParameter("maxPrice"));
        request.setAttribute("categories", AppConstants.CATEGORIES);

        try {
            List<Listing> listings = listingDAO.search(keyword, category, minPrice, maxPrice);
            request.setAttribute("listings", listings);
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load marketplace listings right now.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/marketplace.jsp");
        dispatcher.forward(request, response);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            return value.compareTo(BigDecimal.ZERO) < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null; // ignore malformed price filter rather than erroring out
        }
    }
}
