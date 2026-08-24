package com.ict.lending.model;

import java.time.LocalDateTime;

/**
 * Admin activity audit entry.
 */
public class AuditLog {
    private int logId;
    private String action;
    private String details;
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(int logId, String action, String details, LocalDateTime createdAt) {
        this.logId = logId;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
