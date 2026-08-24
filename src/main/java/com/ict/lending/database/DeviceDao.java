package com.ict.lending.database;

import com.ict.lending.model.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceDao {

    public List<Device> findAll() throws SQLException {
        return findFiltered("", "All");
    }

    public List<Device> findFiltered(String search, String category) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM devices WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(device_name) LIKE ? OR LOWER(brand) LIKE ? OR LOWER(serial_number) LIKE ?)");
            String q = "%" + search.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        if (category != null && !category.isBlank() && !"All".equalsIgnoreCase(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY device_name COLLATE NOCASE");

        List<Device> list = new ArrayList<>();
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

    public List<String> findCategories() throws SQLException {
        List<String> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM devices ORDER BY category COLLATE NOCASE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cats.add(rs.getString(1));
            }
        }
        return cats;
    }

    public Optional<Device> findById(int id) throws SQLException {
        String sql = "SELECT * FROM devices WHERE device_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Device device) throws SQLException {
        String sql = """
            INSERT INTO devices (device_name, category, brand, serial_number,
                                 quantity, available_quantity, status, date_added, image_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, device);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void update(Device device) throws SQLException {
        String sql = """
            UPDATE devices SET device_name = ?, category = ?, brand = ?, serial_number = ?,
                               quantity = ?, available_quantity = ?, status = ?, image_path = ?
            WHERE device_id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, device.getDeviceName());
            ps.setString(2, device.getCategory());
            ps.setString(3, device.getBrand());
            ps.setString(4, device.getSerialNumber());
            ps.setInt(5, device.getQuantity());
            ps.setInt(6, device.getAvailableQuantity());
            ps.setString(7, device.getStatus());
            ps.setString(8, device.getImagePath());
            ps.setInt(9, device.getDeviceId());
            ps.executeUpdate();
        }
    }

    public void delete(int deviceId) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Remove returned history first so FK does not block device deletion
                try (PreparedStatement clear = conn.prepareStatement(
                        "DELETE FROM transactions WHERE device_id = ? AND status = 'Returned'")) {
                    clear.setInt(1, deviceId);
                    clear.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM devices WHERE device_id = ?")) {
                    ps.setInt(1, deviceId);
                    int removed = ps.executeUpdate();
                    if (removed == 0) {
                        throw new SQLException("Device not found: " + deviceId);
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean hasActiveBorrows(int deviceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE device_id = ? AND status = 'Borrowed'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deviceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void bind(PreparedStatement ps, Device device) throws SQLException {
        ps.setString(1, device.getDeviceName());
        ps.setString(2, device.getCategory());
        ps.setString(3, device.getBrand());
        ps.setString(4, device.getSerialNumber());
        ps.setInt(5, device.getQuantity());
        ps.setInt(6, device.getAvailableQuantity());
        ps.setString(7, device.getStatus());
        ps.setString(8, device.getDateAdded() != null
                ? device.getDateAdded().toString()
                : LocalDate.now().toString());
        ps.setString(9, device.getImagePath());
    }

    private Device map(ResultSet rs) throws SQLException {
        Device d = new Device();
        d.setDeviceId(rs.getInt("device_id"));
        d.setDeviceName(rs.getString("device_name"));
        d.setCategory(rs.getString("category"));
        d.setBrand(rs.getString("brand"));
        d.setSerialNumber(rs.getString("serial_number"));
        d.setQuantity(rs.getInt("quantity"));
        d.setAvailableQuantity(rs.getInt("available_quantity"));
        d.setStatus(rs.getString("status"));
        String date = rs.getString("date_added");
        if (date != null && !date.isBlank()) {
            d.setDateAdded(LocalDate.parse(date));
        }
        try {
            d.setImagePath(rs.getString("image_path"));
        } catch (SQLException ignored) {
            d.setImagePath(null);
        }
        return d;
    }
}
