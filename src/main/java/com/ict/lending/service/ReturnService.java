package com.ict.lending.service;

import com.ict.lending.database.DatabaseConnection;
import com.ict.lending.database.TransactionDao;
import com.ict.lending.model.Device;
import com.ict.lending.model.Transaction;
import com.ict.lending.utils.Validators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReturnService {

    private final TransactionDao transactionDao = new TransactionDao();
    private final AuditService auditService = new AuditService();

    public List<Transaction> listActive(String search) {
        try {
            return transactionDao.findFiltered(search, "Borrowed");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load active borrows: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies borrower identity fields then completes the return.
     */
    public Transaction returnDevice(String transactionId, String fullName, String idNumber,
                                    String position, String gradeLevel, String section,
                                    String conditionReport) {
        Validators.requireNonBlank(fullName, "Full name");
        Validators.requireNonBlank(idNumber, "ID number");
        Validators.requireNonBlank(position, "Position");
        Validators.requireNonBlank(gradeLevel, "Grade level / Department");
        Validators.requireNonBlank(section, "Section");

        try {
            Transaction existing = transactionDao.findById(transactionId)
                    .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
            if (!"Borrowed".equalsIgnoreCase(existing.getStatus())) {
                throw new IllegalArgumentException("This transaction is already returned.");
            }
            if (!matches(existing.getBorrowerName(), fullName)
                    || !matches(existing.getIdNumber(), idNumber)
                    || !matches(existing.getPosition(), position)
                    || !matches(existing.getGradeLevel(), gradeLevel)
                    || !matches(existing.getSection(), section)) {
                throw new IllegalArgumentException(
                        "Borrower information does not match the original borrow record.");
            }

            try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                conn.setAutoCommit(false);
                try {
                    LocalDate returnDate = LocalDate.now();
                    LocalTime returnTime = LocalTime.now().withNano(0);
                    String report = Validators.isBlank(conditionReport) ? null : conditionReport.trim();
                    transactionDao.markReturned(conn, transactionId, returnDate, returnTime, report);
                    restoreInventory(conn, existing.getDeviceId(), existing.getQuantity());
                    conn.commit();

                    existing.setReturnDate(returnDate);
                    existing.setReturnTime(returnTime);
                    existing.setStatus("Returned");
                    existing.setConditionReport(report);
                    auditService.log("RETURN",
                            transactionId + " — " + existing.getQuantity() + "x "
                                    + existing.getDeviceName() + " from " + existing.getBorrowerName());
                    return existing;
                } catch (Exception e) {
                    conn.rollback();
                    if (e instanceof RuntimeException re) {
                        throw re;
                    }
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Return failed: " + e.getMessage(), e);
        }
    }

    private void restoreInventory(Connection conn, int deviceId, int quantity) throws SQLException {
        String select = "SELECT quantity, available_quantity FROM devices WHERE device_id = ?";
        int total;
        int available;
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setInt(1, deviceId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Device not found for return.");
                }
                total = rs.getInt("quantity");
                available = rs.getInt("available_quantity");
            }
        }
        int newAvailable = Math.min(total, available + quantity);
        String status = Device.computeStatus(newAvailable, total);
        String update = "UPDATE devices SET available_quantity = ?, status = ? WHERE device_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setInt(1, newAvailable);
            ps.setString(2, status);
            ps.setInt(3, deviceId);
            ps.executeUpdate();
        }
    }

    private boolean matches(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }
}
