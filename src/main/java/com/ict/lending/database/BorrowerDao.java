package com.ict.lending.database;

import com.ict.lending.model.Borrower;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BorrowerDao {

    public int insert(Borrower borrower) throws SQLException {
        return insert(null, borrower);
    }

    public int insert(Connection conn, Borrower borrower) throws SQLException {
        boolean owned = conn == null;
        if (owned) {
            conn = DatabaseConnection.getInstance().getConnection();
        }
        String sql = """
            INSERT INTO borrowers (full_name, position, grade_level, section, purpose)
            VALUES (?, ?, ?, ?, ?)
            """;
        try {
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, borrower.getFullName());
                ps.setString(2, borrower.getPosition());
                ps.setString(3, borrower.getGradeLevel());
                ps.setString(4, borrower.getSection());
                ps.setString(5, borrower.getPurpose());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            return -1;
        } finally {
            if (owned) {
                conn.close();
            }
        }
    }
}
