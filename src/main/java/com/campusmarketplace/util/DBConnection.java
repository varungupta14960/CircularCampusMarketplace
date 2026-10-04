package com.campusmarketplace.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection factory. Every DAO obtains connections through
 * this class only - no connection code is duplicated elsewhere.
 *
 * Configuration: reads DB_URL / DB_USER / DB_PASSWORD from environment
 * variables first (recommended for real deployments); if any are absent it
 * falls back to the DEFAULT_* constants below so the project is easy for a
 * student to run locally by just editing three lines here.
 *
 * To configure via environment variables (recommended, especially outside
 * of local development):
 *   export DB_URL=jdbc:mysql://localhost:3306/circular_campus_marketplace?useSSL=false&serverTimezone=UTC
 *   export DB_USER=root
 *   export DB_PASSWORD=yourpassword
 *
 * SECURITY / GITHUB NOTE: the DEFAULT_* constants below are placeholder
 * values for a fresh local MySQL install only (root/root is MySQL's
 * well-known local-dev default, not a real/deployed secret). They exist so
 * the project runs out of the box for grading. For anything beyond local
 * development, set the DB_* environment variables above instead of editing
 * these constants, and never commit real credentials to source control.
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/circular_campus_marketplace?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root"; // local-dev placeholder only - see note above

    private static final String URL = firstNonEmpty(System.getenv("DB_URL"), DEFAULT_URL);
    private static final String USER = firstNonEmpty(System.getenv("DB_USER"), DEFAULT_USER);
    private static final String PASSWORD = firstNonEmpty(System.getenv("DB_PASSWORD"), DEFAULT_PASSWORD);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String firstNonEmpty(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }
}
