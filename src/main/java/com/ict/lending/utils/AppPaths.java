package com.ict.lending.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves application data directories (portable ./data preferred).
 */
public final class AppPaths {

    private static final String APP_FOLDER = "ICTLending";

    private AppPaths() {
    }

    public static Path dataDir() {
        Path portable = Paths.get("data").toAbsolutePath();
        try {
            Files.createDirectories(portable);
            return portable;
        } catch (IOException e) {
            String appData = System.getenv("APPDATA");
            if (appData == null || appData.isBlank()) {
                appData = System.getProperty("user.home");
            }
            Path fallback = Paths.get(appData, APP_FOLDER);
            try {
                Files.createDirectories(fallback);
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot create data directory", ex);
            }
            return fallback;
        }
    }

    public static Path databaseFile() {
        return dataDir().resolve("lending.db");
    }

    public static Path backupDir() {
        Path dir = dataDir().resolve("backups");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create backup directory", e);
        }
        return dir;
    }

    public static Path exportDir() {
        Path dir = dataDir().resolve("exports");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create export directory", e);
        }
        return dir;
    }

    public static Path deviceImagesDir() {
        return DeviceImages.imagesDir();
    }
}
