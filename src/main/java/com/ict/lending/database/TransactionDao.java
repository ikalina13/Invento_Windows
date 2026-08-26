package com.ict.lending.database;

import com.ict.lending.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDao {

    private static final String JOIN_SELECT = """
        SELECT t.transaction_id, t.borrower_id, t.device_id, t.quantity,
               t.borrow_date, t.borrow_time, t.return_date, t.return_time, t.status, t.condition_report,
               b.full_name, b.id_number, b.position, b.grade_level, b.section, b.purpose,
               d.device_name, d.category
        FROM transactions t
        JOIN borrowers b ON t.borrower_id = b.borrower_id
        JOIN devices d ON t.device_id = d.device_id
        """;

    public void insert(Connection conn, Transaction txn) throws SQLException {
        String sql = """
            INSERT INTO transactions (transaction_id, borrower_id, device_id, quantity,
                                      borrow_date, borrow_time, return_date, return_time, status,
                                      condition_report)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txn.getTransactionId());
            ps.setInt(2, txn.getBorrowerId());
            ps.setInt(3, txn.getDeviceId());
            ps.setInt(4, txn.getQuantity());
            ps.setString(5, txn.getBorrowDate().toString());
            ps.setString(6, txn.getBorrowTime().toString());
            ps.setString(7, txn.getReturnDate() != null ? txn.getReturnDate().toString() : null);
            ps.setString(8, txn.getReturnTime() != null ? txn.getReturnTime().toString() : null);
            ps.setString(9, txn.getStatus());
            ps.setString(10, txn.getConditionReport());
            ps.executeUpdate();
        }
    }

    public void markReturned(Connection conn, String transactionId,
                             LocalDate returnDate, LocalTime returnTime,
                             String conditionReport) throws SQLException {
        String sql = """
            UPDATE transactions SET return_date = ?, return_time = ?, status = 'Returned', condition_report = ?
            WHERE transaction_id = ? AND status = 'Borrowed'
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, returnDate.toString());
            ps.setString(2, returnTime.toString());
            ps.setString(3, conditionReport);
            ps.setString(4, transactionId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Transaction not found or already returned: " + transactionId);
            }
        }
    }

    public Optional<Transaction> findById(String transactionId) throws SQLException {
        String sql = JOIN_SELECT + " WHERE t.transaction_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Transaction> findActive() throws SQLException {
        return findFiltered("", "Borrowed");
    }

    public List<Transaction> findRecent(int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = JOIN_SELECT + " ORDER BY t.borrow_date DESC, t.borrow_time DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Transaction> findFiltered(String search, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(JOIN_SELECT);
        sql.append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append("""
                 AND (LOWER(t.transaction_id) LIKE ?
                      OR LOWER(b.full_name) LIKE ?
                      OR LOWER(d.device_name) LIKE ?)
                """);
            String q = "%" + search.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        if (status != null && !status.isBlank() && !"All".equalsIgnoreCase(status)) {
            sql.append(" AND t.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY t.borrow_date DESC, t.borrow_time DESC");

        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public int countBorrowedUnits() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE status = 'Borrowed'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countReturnedToday() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE status = 'Returned' AND return_date = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Transaction map(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getString("transaction_id"));
        t.setBorrowerId(rs.getInt("borrower_id"));
        t.setDeviceId(rs.getInt("device_id"));
        t.setQuantity(rs.getInt("quantity"));
        t.setBorrowDate(LocalDate.parse(rs.getString("borrow_date")));
        t.setBorrowTime(LocalTime.parse(rs.getString("borrow_time")));
        String rd = rs.getString("return_date");
        String rt = rs.getString("return_time");
        if (rd != null && !rd.isBlank()) {
            t.setReturnDate(LocalDate.parse(rd));
        }
        if (rt != null && !rt.isBlank()) {
            t.setReturnTime(LocalTime.parse(rt));
        }
        t.setStatus(rs.getString("status"));
        t.setConditionReport(rs.getString("condition_report"));
        t.setBorrowerName(rs.getString("full_name"));
        t.setIdNumber(rs.getString("id_number"));
        t.setPosition(rs.getString("position"));
        t.setGradeLevel(rs.getString("grade_level"));
        t.setSection(rs.getString("section"));
        t.setPurpose(rs.getString("purpose"));
        t.setDeviceName(rs.getString("device_name"));
        t.setCategory(rs.getString("category"));
        return t;
    }
}
