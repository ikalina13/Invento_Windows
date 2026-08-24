package com.ict.lending.database;

import com.ict.lending.model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class AdminDao {

    public Optional<Admin> findByUsername(String username) throws SQLException {
        String sql = "SELECT admin_id, username, password_hash, salt FROM admins WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updatePassword(int adminId, String hash, String salt) throws SQLException {
        String sql = "UPDATE admins SET password_hash = ?, salt = ?, updated_at = ? WHERE admin_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setString(3, LocalDate.now().toString());
            ps.setInt(4, adminId);
            ps.executeUpdate();
        }
    }

    private Admin map(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("admin_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt")
        );
    }
}
