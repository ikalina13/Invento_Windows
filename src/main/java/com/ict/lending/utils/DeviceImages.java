package com.ict.lending.utils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stores device photos under {@code data/device-images} and loads them for UI.
 */
public final class DeviceImages {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "gif", "webp", "bmp");

    private DeviceImages() {
    }

    public static Path imagesDir() {
        Path dir = AppPaths.dataDir().resolve("device-images");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create device images directory", e);
        }
        return dir;
    }

    /**
     * Copies a chosen file into the app data folder and returns a portable relative path
     * (e.g. {@code device-images/abc.jpg}).
     */
    public static String storeCopy(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IllegalArgumentException("Please choose a valid image file.");
        }
        String ext = extension(source.getFileName().toString());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Unsupported image type. Use PNG, JPG, GIF, WEBP, or BMP.");
        }
        String relative = "device-images/" + UUID.randomUUID() + "." + ext;
        Path dest = AppPaths.dataDir().resolve(relative);
        Files.createDirectories(dest.getParent());
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        return relative.replace('\\', '/');
    }

    public static Path resolve(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        Path p = Path.of(storedPath);
        if (p.isAbsolute()) {
            return p;
        }
        return AppPaths.dataDir().resolve(storedPath);
    }

    public static void deleteQuietly(String storedPath) {
        Path file = resolve(storedPath);
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    public static Image load(String storedPath, double width, double height) {
        Path file = resolve(storedPath);
        if (file == null || !Files.isRegularFile(file)) {
            return null;
        }
        try {
            return new Image(file.toUri().toString(), width, height, true, true, true);
        } catch (Exception e) {
            return null;
        }
    }

    /** Thumbnail or a placeholder tile when no image is set. */
    public static Node preview(String storedPath, double width, double height) {
        Image image = load(storedPath, width * 2, height * 2);
        StackPane box = new StackPane();
        box.getStyleClass().add("device-image-box");
        box.setPrefSize(width, height);
        box.setMinSize(width, height);
        box.setMaxSize(width, height);

        if (image != null && !image.isError()) {
            ImageView view = new ImageView(image);
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            box.getChildren().add(view);
        } else {
            Label placeholder = new Label("—");
            placeholder.getStyleClass().add("device-image-placeholder");
            box.getChildren().add(placeholder);
            StackPane.setAlignment(placeholder, Pos.CENTER);
        }
        return box;
    }

    public static boolean looksLikeImage(Path path) {
        if (path == null) {
            return false;
        }
        return ALLOWED_EXT.contains(extension(path.getFileName().toString()));
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** Optional bundled fallback (unused when absent). */
    public static Image bundledPlaceholder() {
        try (InputStream in = DeviceImages.class.getResourceAsStream("/images/device-placeholder.png")) {
            if (in == null) {
                return null;
            }
            return new Image(in);
        } catch (Exception e) {
            return null;
        }
    }
}
