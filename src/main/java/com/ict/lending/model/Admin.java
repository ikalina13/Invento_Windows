package com.ict.lending.model;

/**
 * System administrator account.
 */
public class Admin {
    private int adminId;
    private String username;
    private String passwordHash;
    private String salt;

    public Admin() {
    }

    public Admin(int adminId, String username, String passwordHash, String salt) {
        this.adminId = adminId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
