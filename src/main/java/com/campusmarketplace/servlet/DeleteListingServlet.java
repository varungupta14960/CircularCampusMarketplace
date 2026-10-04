package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
import com.campusmarketplace.model.Student;
import com.campusmarketplace.util.AppConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/listing/delete")
public class DeleteListingServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);
        Integer listingId = parseId(request.getParameter("id"));

        if (listingId != null) {
            try {
                // DAO WHERE clause enforces seller_id = ? -> a student can never remove someone else's listing,
                // even if this ID were tampered with client-side.
                boolean removed = listingDAO.removeListing(listingId, student.getStudentId());
                if (!removed) {
                    request.getSession().setAttribute(AppConstants.ATTR_ERROR,
                            "Could not remove that listing (not found, not yours, or already sold).");
                }
            } catch (SQLException e) {
                request.getSession().setAttribute(AppConstants.ATTR_ERROR, "Could not remove listing. Please try again.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/my-listings");
    }

    private Integer parseId(String raw) {
        try {
            return raw == null ? null : Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
