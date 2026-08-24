package com.ict.lending.service;

import com.ict.lending.database.AdminDao;
import com.ict.lending.model.Admin;
import com.ict.lending.utils.PasswordHash;
import com.ict.lending.utils.Validators;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final AdminDao adminDao = new AdminDao();
    private final AuditService auditService = new AuditService();
    private Admin currentAdmin;
    private boolean usingDefaultPassword;

    public boolean login(String username, String password) {
        try {
            Validators.requireNonBlank(username, "Username");
            Validators.requireNonBlank(password, "Password");
            Optional<Admin> adminOpt = adminDao.findByUsername(username.trim());
            if (adminOpt.isEmpty()) {
                return false;
            }
            Admin admin = adminOpt.get();
            if (!PasswordHash.verify(password, admin.getSalt(), admin.getPasswordHash())) {
                return false;
            }
            this.currentAdmin = admin;
            this.usingDefaultPassword = PasswordHash.verify("admin123", admin.getSalt(), admin.getPasswordHash());
            auditService.log("LOGIN", "Admin '" + admin.getUsername() + "' signed in");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    public void changePassword(String currentPassword, String newPassword, String confirm) {
        if (currentAdmin == null) {
            throw new IllegalStateException("Not logged in.");
        }
        Validators.requireNonBlank(currentPassword, "Current password");
        Validators.requireNonBlank(newPassword, "New password");
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }
        if ("admin123".equals(newPassword)) {
            throw new IllegalArgumentException("Choose a password different from the default.");
        }
        if (!newPassword.equals(confirm)) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }
        if (!PasswordHash.verify(currentPassword, currentAdmin.getSalt(), currentAdmin.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        try {
            String salt = PasswordHash.generateSalt();
            String hash = PasswordHash.hash(newPassword, salt);
            adminDao.updatePassword(currentAdmin.getAdminId(), hash, salt);
            currentAdmin.setSalt(salt);
            currentAdmin.setPasswordHash(hash);
            usingDefaultPassword = false;
            auditService.log("PASSWORD_CHANGE", "Admin changed password");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
        }
    }

    public boolean mustChangePassword() {
        return currentAdmin != null && usingDefaultPassword;
    }

    public Admin getCurrentAdmin() {
        return currentAdmin;
    }

    public void logout() {
        if (currentAdmin != null) {
            auditService.log("LOGOUT", "Admin '" + currentAdmin.getUsername() + "' signed out");
        }
        currentAdmin = null;
        usingDefaultPassword = false;
    }
}
