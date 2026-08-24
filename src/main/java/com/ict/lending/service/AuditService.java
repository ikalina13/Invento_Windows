package com.ict.lending.service;

import com.ict.lending.database.AuditLogDao;
import com.ict.lending.model.AuditLog;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class AuditService {

    private final AuditLogDao dao = new AuditLogDao();

    public void log(String action, String details) {
        try {
            dao.insert(action, details);
        } catch (SQLException e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }

    public List<AuditLog> recent(int limit) {
        try {
            return dao.findRecent(limit);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
