package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.StudentDAO;
import com.campusmarketplace.model.Student;
import com.campusmarketplace.util.AppConstants;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student sessionStudent = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);

        try {
            // Re-read from the DB so wallet balance / sustainability points are always current,
            // even if they changed via a purchase/sale made in another tab or session.
            Student freshStudent = studentDAO.findById(sessionStudent.getStudentId());
            if (freshStudent != null) {
                request.getSession().setAttribute(AppConstants.SESSION_STUDENT, freshStudent.withoutPasswordHash());
                request.setAttribute("profileStudent", freshStudent);
            } else {
                request.setAttribute("profileStudent", sessionStudent);
            }
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load your profile right now.");
            request.setAttribute("profileStudent", sessionStudent);
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/profile.jsp");
        dispatcher.forward(request, response);
    }
}
