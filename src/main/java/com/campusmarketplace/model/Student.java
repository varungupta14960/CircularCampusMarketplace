package com.campusmarketplace.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a registered student/user. This object is stored in the
 * HttpSession (as the "student" attribute) after a successful login, so it
 * implements Serializable in case the session is ever persisted/replicated.
 *
 * The password hash is intentionally kept out of this object once it is
 * placed in the session (see StudentDAO - the session copy has the hash
 * cleared) so it never leaks into JSP EL output or session serialization
 * unnecessarily.
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    private int studentId;
    private String name;
    private String email;
    private String passwordHash;
    private BigDecimal walletBalance;
    private int sustainabilityPoints;
    private LocalDateTime createdAt;

    public Student() {
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public int getSustainabilityPoints() {
        return sustainabilityPoints;
    }

    public void setSustainabilityPoints(int sustainabilityPoints) {
        this.sustainabilityPoints = sustainabilityPoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Returns a copy of this student with the password hash cleared, safe to store in session/EL. */
    public Student withoutPasswordHash() {
        Student copy = new Student();
        copy.studentId = this.studentId;
        copy.name = this.name;
        copy.email = this.email;
        copy.passwordHash = null;
        copy.walletBalance = this.walletBalance;
        copy.sustainabilityPoints = this.sustainabilityPoints;
        copy.createdAt = this.createdAt;
        return copy;
    }
}
