package com.campusmarketplace.filter;

import com.campusmarketplace.util.AppConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Guards routes that require an authenticated student. Runs on every
 * request; only the exact paths below are protected, everything else
 * (home, marketplace browsing, listing details, cart, static assets) stays
 * public so guests can browse and build a guest cart.
 *
 * This is a SERVER-SIDE check - JSPs may also hide buttons for
 * unauthenticated users, but that is a UX convenience only. The real
 * authorization boundary is here and re-checked per-owner inside the
 * relevant servlets/DAOs.
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/listing/create",
            "/listing/edit",
            "/listing/delete",
            "/my-listings",
            "/checkout",
            "/profile",
            "/transactions"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();

        if (PROTECTED_PATHS.contains(path)) {
            HttpSession session = request.getSession(false);
            boolean loggedIn = session != null && session.getAttribute(AppConstants.SESSION_STUDENT) != null;
            if (!loggedIn) {
                String returnTo = path;
                if (request.getQueryString() != null) {
                    returnTo += "?" + request.getQueryString();
                }
                String encoded = URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/login?returnTo=" + encoded);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
