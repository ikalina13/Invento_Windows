package com.ict.lending.service;

import com.ict.lending.database.DatabaseConnection;
import com.ict.lending.utils.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BackupService {

    private final AuditService auditService = new AuditService();

    public Path backupNow() {
        try {
            Path db = AppPaths.databaseFile();
            if (!Files.exists(db)) {
                throw new IllegalStateException("Database file not found.");
            }
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path dest = AppPaths.backupDir().resolve("lending_backup_" + stamp + ".db");
            Files.copy(db, dest, StandardCopyOption.REPLACE_EXISTING);
            auditService.log("BACKUP", "Database backed up to " + dest.getFileName());
            return dest;
        } catch (IOException e) {
            throw new RuntimeException("Backup failed: " + e.getMessage(), e);
        }
    }

    /** Creates an automatic backup if none exists for today. */
    public void autoBackupIfNeeded() {
        try {
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            try (Stream<Path> stream = Files.list(AppPaths.backupDir())) {
                boolean hasToday = stream.anyMatch(p -> p.getFileName().toString().contains(today));
                if (!hasToday) {
                    backupNow();
                }
            }
        } catch (Exception e) {
            System.err.println("Auto-backup skipped: " + e.getMessage());
        }
    }

    public void restore(Path backupFile) {
        if (backupFile == null || !Files.exists(backupFile)) {
            throw new IllegalArgumentException("Backup file does not exist.");
        }
        try {
            Path db = AppPaths.databaseFile();
            // Safety copy of current DB before overwrite
            Path safety = AppPaths.backupDir().resolve(
                    "pre_restore_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db");
            if (Files.exists(db)) {
                Files.copy(db, safety, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.copy(backupFile, db, StandardCopyOption.REPLACE_EXISTING);
            DatabaseConnection.reset();
            DatabaseConnection.getInstance();
            auditService.log("RESTORE", "Restored from " + backupFile.getFileName());
        } catch (IOException e) {
            throw new RuntimeException("Restore failed: " + e.getMessage(), e);
        }
    }

    public List<Path> listBackups() {
        try (Stream<Path> stream = Files.list(AppPaths.backupDir())) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".db"))
                    .sorted(Comparator.comparing(Path::toString).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list backups: " + e.getMessage(), e);
        }
    }
}
