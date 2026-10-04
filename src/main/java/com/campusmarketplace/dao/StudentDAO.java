package com.campusmarketplace.dao;

import com.campusmarketplace.model.Student;
import com.campusmarketplace.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Data access for the students table. All queries use PreparedStatement;
 * no user input is ever concatenated into SQL.
 */
public class StudentDAO {

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a new student and returns the generated student_id. */
    public int registerStudent(String name, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO students (name, email, password_hash, wallet_balance, sustainability_points) "
                + "VALUES (?, ?, ?, 500.00, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Registration failed: no generated ID returned");
    }

    public Student findByEmail(String email) throws SQLException {
        String sql = "SELECT student_id, name, email, password_hash, wallet_balance, "
                + "sustainability_points, created_at FROM students WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Student findById(int studentId) throws SQLException {
        String sql = "SELECT student_id, name, email, password_hash, wallet_balance, "
                + "sustainability_points, created_at FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setWalletBalance(rs.getBigDecimal("wallet_balance"));
        s.setSustainabilityPoints(rs.getInt("sustainability_points"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            s.setCreatedAt(ts.toLocalDateTime());
        }
        return s;
    }
}
