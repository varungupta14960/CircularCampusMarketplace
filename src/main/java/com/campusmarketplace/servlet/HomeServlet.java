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

@WebServlet("")
public class HomeServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Integer> recentIds = CookieUtil.readIds(request, AppConstants.COOKIE_RECENTLY_VIEWED);
            List<Listing> recentlyViewed = listingDAO.findByIds(recentIds);
            request.setAttribute("recentlyViewed", recentlyViewed);
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load recently viewed items.");
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/index.jsp");
        dispatcher.forward(request, response);
    }
}
