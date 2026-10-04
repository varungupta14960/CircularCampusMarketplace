package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
import com.campusmarketplace.model.Listing;
import com.campusmarketplace.util.AppConstants;
import com.campusmarketplace.util.CookieUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/listing")
public class ListingDetailsServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer listingId = parseId(request.getParameter("id"));
        if (listingId == null) {
            response.sendRedirect(request.getContextPath() + "/marketplace");
            return;
        }

        try {
            Listing listing = listingDAO.findById(listingId);
            if (listing == null) {
                request.setAttribute(AppConstants.ATTR_ERROR, "That listing could not be found.");
                request.setAttribute("listing", null);
            } else {
                request.setAttribute("listing", listing);
                // Update the recentlyViewed cookie: move this ID to the front, bounded size.
                List<Integer> current = CookieUtil.readIds(request, AppConstants.COOKIE_RECENTLY_VIEWED);
                List<Integer> updated = CookieUtil.moveToFrontBounded(
                        current, listingId, AppConstants.RECENTLY_VIEWED_MAX);
                CookieUtil.writeIds(response, AppConstants.COOKIE_RECENTLY_VIEWED, updated);
            }
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load this listing right now.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/listing-details.jsp");
        dispatcher.forward(request, response);
    }

    private Integer parseId(String raw) {
        try {
            return raw == null ? null : Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
