package com.ict.lending.service;

import com.ict.lending.database.BorrowerDao;
import com.ict.lending.database.DatabaseConnection;
import com.ict.lending.database.TransactionDao;
import com.ict.lending.model.Borrower;
import com.ict.lending.model.Device;
import com.ict.lending.model.Transaction;
import com.ict.lending.utils.IdGenerator;
import com.ict.lending.utils.Validators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BorrowService {

    /** One basket line: device id + quantity to book. */
    public record BasketLine(int deviceId, int quantity) {
    }

    private final BorrowerDao borrowerDao = new BorrowerDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final AuditService auditService = new AuditService();

    public Transaction borrow(int deviceId, int quantity, Borrower borrowerInfo) {
        List<Transaction> all = borrowMany(List.of(new BasketLine(deviceId, quantity)), borrowerInfo);
        return all.get(0);
    }

    /**
     * Books multiple devices for one borrower in a single checkout (no payment).
     */
    public List<Transaction> borrowMany(List<BasketLine> lines, Borrower borrowerInfo) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Add at least one device to the booking basket.");
        }
        Validators.requireNonBlank(borrowerInfo.getFullName(), "Full name");
        Validators.requireNonBlank(borrowerInfo.getPosition(), "Position");
        Validators.requireNonBlank(borrowerInfo.getGradeLevel(), "Grade level / Department");
        Validators.requireNonBlank(borrowerInfo.getSection(), "Section");
        Validators.requireNonBlank(borrowerInfo.getPurpose(), "Purpose");

        Map<Integer, Integer> merged = new LinkedHashMap<>();
        for (BasketLine line : lines) {
            if (line.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }
            merged.merge(line.deviceId(), line.quantity(), Integer::sum);
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int borrowerId = borrowerDao.insert(conn, borrowerInfo);
                borrowerInfo.setBorrowerId(borrowerId);

                List<Transaction> created = new ArrayList<>();
                StringBuilder auditNames = new StringBuilder();

                for (Map.Entry<Integer, Integer> entry : merged.entrySet()) {
                    int deviceId = entry.getKey();
                    int quantity = entry.getValue();
                    Device device = lockDevice(conn, deviceId);
                    if (device.getAvailableQuantity() < quantity) {
                        throw new IllegalArgumentException(
                                device.getDeviceName() + ": only "
                                        + device.getAvailableQuantity() + " unit(s) available.");
                    }

                    Transaction txn = new Transaction();
                    txn.setTransactionId(IdGenerator.nextTransactionId());
                    txn.setBorrowerId(borrowerId);
                    txn.setDeviceId(deviceId);
                    txn.setQuantity(quantity);
                    txn.setBorrowDate(LocalDate.now());
                    txn.setBorrowTime(LocalTime.now().withNano(0));
                    txn.setStatus("Borrowed");
                    transactionDao.insert(conn, txn);

                    int newAvailable = device.getAvailableQuantity() - quantity;
                    updateDeviceQty(conn, deviceId, device.getQuantity(), newAvailable);

                    txn.setBorrowerName(borrowerInfo.getFullName());
                    txn.setDeviceName(device.getDeviceName());
                    txn.setPosition(borrowerInfo.getPosition());
                    txn.setGradeLevel(borrowerInfo.getGradeLevel());
                    txn.setSection(borrowerInfo.getSection());
                    txn.setPurpose(borrowerInfo.getPurpose());
                    txn.setCategory(device.getCategory());
                    created.add(txn);

                    if (!auditNames.isEmpty()) {
                        auditNames.append(", ");
                    }
                    auditNames.append(quantity).append("x ").append(device.getDeviceName());
                }

                conn.commit();
                auditService.log("BORROW",
                        created.size() + " item(s) — " + auditNames
                                + " to " + borrowerInfo.getFullName());
                return created;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Borrow failed: " + e.getMessage(), e);
        }
    }

    private Device lockDevice(Connection conn, int deviceId) throws SQLException {
        String sql = "SELECT * FROM devices WHERE device_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deviceId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Device not found.");
                }
                Device d = new Device();
                d.setDeviceId(rs.getInt("device_id"));
                d.setDeviceName(rs.getString("device_name"));
                d.setCategory(rs.getString("category"));
                d.setQuantity(rs.getInt("quantity"));
                d.setAvailableQuantity(rs.getInt("available_quantity"));
                return d;
            }
        }
    }

    private void updateDeviceQty(Connection conn, int deviceId, int total, int available)
            throws SQLException {
        String status = Device.computeStatus(available, total);
        String sql = "UPDATE devices SET available_quantity = ?, status = ? WHERE device_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, available);
            ps.setString(2, status);
            ps.setInt(3, deviceId);
            ps.executeUpdate();
        }
    }
}
