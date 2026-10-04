package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.TransactionDAO;
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

@WebServlet("/transactions")
public class TransactionHistoryServlet extends HttpServlet {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);

        HttpSession session = request.getSession();
        if (session.getAttribute(AppConstants.ATTR_SUCCESS) != null) {
            request.setAttribute(AppConstants.ATTR_SUCCESS, session.getAttribute(AppConstants.ATTR_SUCCESS));
            session.removeAttribute(AppConstants.ATTR_SUCCESS);
        }

        try {
            request.setAttribute("purchases", transactionDAO.findPurchases(student.getStudentId()));
            request.setAttribute("sales", transactionDAO.findSales(student.getStudentId()));
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load transaction history right now.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/transaction-history.jsp");
        dispatcher.forward(request, response);
    }
}
