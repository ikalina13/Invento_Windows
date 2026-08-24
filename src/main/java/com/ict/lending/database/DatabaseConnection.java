package com.ict.lending.database;

import com.ict.lending.utils.AppPaths;
import com.ict.lending.utils.IdGenerator;
import com.ict.lending.utils.PasswordHash;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * SQLite connection manager, schema bootstrap, and seed data.
 */
public final class DatabaseConnection {

    private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";
    private static DatabaseConnection instance;
    private final String jdbcUrl;

    private DatabaseConnection() {
        this.jdbcUrl = JDBC_URL_PREFIX + AppPaths.databaseFile().toAbsolutePath();
        initialize();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Re-open after restore. */
    public static synchronized void reset() {
        instance = null;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcUrl);
        try (Statement pragma = conn.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    private void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQLite JDBC driver not found", e);
        }
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS admins (
                    admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS devices (
                    device_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    device_name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    brand TEXT NOT NULL,
                    serial_number TEXT,
                    quantity INTEGER NOT NULL,
                    available_quantity INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    date_added TEXT NOT NULL,
                    image_path TEXT
                )
                """);
            migrateDevicesImagePath(conn);
            st.execute("""
                CREATE TABLE IF NOT EXISTS borrowers (
                    borrower_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    position TEXT NOT NULL,
                    grade_level TEXT NOT NULL,
                    section TEXT NOT NULL,
                    purpose TEXT NOT NULL
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id TEXT PRIMARY KEY,
                    borrower_id INTEGER NOT NULL,
                    device_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    borrow_date TEXT NOT NULL,
                    borrow_time TEXT NOT NULL,
                    return_date TEXT,
                    return_time TEXT,
                    status TEXT NOT NULL,
                    FOREIGN KEY (borrower_id) REFERENCES borrowers(borrower_id),
                    FOREIGN KEY (device_id) REFERENCES devices(device_id)
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    action TEXT NOT NULL,
                    details TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """);

            seedAdminIfNeeded(conn);
            seedDevicesIfNeeded(conn);
            syncTransactionSequence(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private void migrateDevicesImagePath(Connection conn) throws SQLException {
        boolean hasImagePath = false;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(devices)")) {
            while (rs.next()) {
                if ("image_path".equalsIgnoreCase(rs.getString("name"))) {
                    hasImagePath = true;
                    break;
                }
            }
        }
        if (!hasImagePath) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE devices ADD COLUMN image_path TEXT");
            }
        }
    }

    private void seedAdminIfNeeded(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM admins");
             ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        String salt = PasswordHash.generateSalt();
        String hash = PasswordHash.hash("admin123", salt);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO admins (username, password_hash, salt, updated_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "admin");
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, LocalDate.now().toString());
            ps.executeUpdate();
        }
    }

    private void seedDevicesIfNeeded(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM devices");
             ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        String today = LocalDate.now().toString();
        Object[][] seeds = {
                {"Wireless Keyboard", "Keyboard", "Logitech", "KB-1001", 15, 15},
                {"Optical Mouse", "Mouse", "Logitech", "MS-2002", 20, 20},
                {"USB Headset", "Headset", "Jabra", "HS-3003", 10, 10},
                {"Dell Latitude Laptop", "Laptop", "Dell", "LT-4004", 8, 8},
                {"65W Laptop Charger", "Charger", "Dell", "CH-5005", 12, 12},
                {"HDMI Cable 2m", "Cable", "Generic", null, 25, 25},
                {"CAT6 LAN Cable 3m", "Cable", "Generic", null, 30, 30},
                {"Epson Projector", "Projector", "Epson", "PJ-6006", 3, 3},
                {"Power Extension Cord", "Extension", "Omni", null, 10, 10},
                {"USB Flash Drive 32GB", "Storage", "SanDisk", null, 15, 15}
        };
        String sql = """
            INSERT INTO devices (device_name, category, brand, serial_number,
                                 quantity, available_quantity, status, date_added)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : seeds) {
                int qty = (Integer) row[4];
                int available = (Integer) row[5];
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.setInt(5, qty);
                ps.setInt(6, available);
                ps.setString(7, "Available");
                ps.setString(8, today);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void syncTransactionSequence(Connection conn) throws SQLException {
        String prefix = "TXN-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT transaction_id FROM transactions WHERE transaction_id LIKE ? ORDER BY transaction_id DESC LIMIT 1")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    String[] parts = id.split("-");
                    if (parts.length == 3) {
                        IdGenerator.syncSequence(Integer.parseInt(parts[2]));
                    }
                }
            }
        }
    }
}
