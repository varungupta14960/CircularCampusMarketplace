package com.campusmarketplace.servlet;

import com.campusmarketplace.dao.ListingDAO;
import com.campusmarketplace.dao.StudentDAO;
import com.campusmarketplace.dao.TransactionDAO;
import com.campusmarketplace.model.Listing;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private final ListingDAO listingDAO = new ListingDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute(AppConstants.SESSION_STUDENT);

        try {
            // Always re-read the wallet balance from the DB - the session copy
            // can be stale (e.g. right after a purchase from another tab).
            Student freshStudent = studentDAO.findById(student.getStudentId());
            request.setAttribute("walletBalance", freshStudent.getWalletBalance());

            LinkedHashSet<Integer> sessionCart = getSessionCart(request.getSession());
            List<Listing> cartItems = listingDAO.findByIds(new ArrayList<>(sessionCart));
            List<Listing> available = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (Listing l : cartItems) {
                if (l.isAvailable()) {
                    available.add(l);
                    total = total.add(l.getPrice());
                }
            }
            request.setAttribute("cartItems", available);
            request.setAttribute("cartTotal", total);
            request.setAttribute("insufficientFunds", freshStudent.getWalletBalance().compareTo(total) < 0);

        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Could not load checkout details right now.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/checkout.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Student student = (Student) session.getAttribute(AppConstants.SESSION_STUDENT);
        LinkedHashSet<Integer> sessionCart = getSessionCart(session);

        try {
            TransactionDAO.CheckoutResult result =
                    transactionDAO.performCheckout(student.getStudentId(), new ArrayList<>(sessionCart));

            if (result.success) {
                // Remove purchased items from the cart; refresh the session's Student
                // copy so the navbar/profile immediately reflect the new wallet balance.
                session.setAttribute(AppConstants.SESSION_CART, new LinkedHashSet<Integer>());
                Student refreshed = studentDAO.findById(student.getStudentId());
                session.setAttribute(AppConstants.SESSION_STUDENT, refreshed.withoutPasswordHash());

                session.setAttribute(AppConstants.ATTR_SUCCESS,
                        "Checkout complete! You purchased " + result.transactions.size()
                        + " item(s) for a total of " + result.totalAmount + ".");
                response.sendRedirect(request.getContextPath() + "/transactions");
            } else {
                request.setAttribute(AppConstants.ATTR_ERROR, result.message);
                doGet(request, response); // re-render checkout page with current cart + error
            }
        } catch (SQLException e) {
            request.setAttribute(AppConstants.ATTR_ERROR, "Checkout failed due to a database error. Nothing was charged.");
            doGet(request, response);
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<Integer> getSessionCart(HttpSession session) {
        LinkedHashSet<Integer> cart = (LinkedHashSet<Integer>) session.getAttribute(AppConstants.SESSION_CART);
        return cart == null ? new LinkedHashSet<>() : cart;
    }
}
