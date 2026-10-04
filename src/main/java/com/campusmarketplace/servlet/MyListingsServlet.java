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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/my-listings")
public class MyListingsServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);

        // Surface any one-time flash error set by DeleteListingServlet.
        HttpSession session = request.getSession();
        if (session.getAttribute(AppConstants.ATTR_ERROR) != null) {
            request.setAttribute(AppConstants.ATTR_ERROR, session.getAttribute(AppConstants.ATTR_ERROR));
            session.removeAttribute(AppConstants.ATTR_ERROR);
        }

        try {
            request.setAttribute("listings", listingDAO.findBySeller(student.getStudentId()));
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load your listings right now.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/my-listings.jsp");
        dispatcher.forward(request, response);
    }
}
