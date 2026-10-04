package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.StudentDAO;
import com.campusmarketplace.util.AppConstants;
import com.campusmarketplace.util.PasswordUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = trim(request.getParameter("name"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("nameValue", name);
        request.setAttribute("emailValue", email);

        String validationError = validate(name, email, password, confirmPassword);
        if (validationError != null) {
            request.setAttribute(AppConstants.ATTR_ERROR, validationError);
            forward(request, response);
            return;
        }

        try {
            if (studentDAO.emailExists(email)) {
                request.setAttribute(AppConstants.ATTR_ERROR, "An account with this email already exists.");
                forward(request, response);
                return;
            }

            String passwordHash = PasswordUtil.hashPassword(password);
            studentDAO.registerStudent(name, email, passwordHash);

            request.getSession().setAttribute(AppConstants.ATTR_SUCCESS,
                    "Registration successful! Please log in.");
            response.sendRedirect(request.getContextPath() + "/login");

        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "A database error occurred. Please try again.");
            forward(request, response);
        }
    }

    private String validate(String name, String email, String password, String confirmPassword) {
        if (name == null || name.isBlank()) return "Name is required.";
        if (name.length() > 100) return "Name is too long.";
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) return "Please enter a valid email address.";
        if (password == null || password.length() < 8) return "Password must be at least 8 characters.";
        if (!password.equals(confirmPassword)) return "Passwords do not match.";
        return null;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/register.jsp");
        dispatcher.forward(request, response);
    }
}
