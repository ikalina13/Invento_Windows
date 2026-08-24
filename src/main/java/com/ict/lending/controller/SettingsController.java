package com.ict.lending.controller;

import com.ict.lending.model.AuditLog;
import com.ict.lending.service.AuditService;
import com.ict.lending.service.BackupService;

import java.nio.file.Path;
import java.util.List;

public class SettingsController {

    private final BackupService backupService = new BackupService();
    private final AuditService auditService = new AuditService();

    public Path backupNow() {
        return backupService.backupNow();
    }

    public void restore(Path backupFile) {
        backupService.restore(backupFile);
    }

    public List<Path> listBackups() {
        return backupService.listBackups();
    }

    public List<AuditLog> recentAudit() {
        return auditService.recent(50);
    }
}
