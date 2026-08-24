package com.ict.lending.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

import java.util.prefs.Preferences;

/**
 * Light / dark theme manager. Preference is saved between sessions.
 */
public final class ThemeManager {

    public enum Theme {
        LIGHT, DARK
    }

    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String PREF_KEY = "theme";
    private static Theme current = loadSaved();

    private ThemeManager() {
    }

    public static Theme getCurrent() {
        return current;
    }

    public static boolean isDark() {
        return current == Theme.DARK;
    }

    public static void setTheme(Theme theme) {
        current = theme == null ? Theme.LIGHT : theme;
        PREFS.put(PREF_KEY, current.name());
    }

    public static void toggle() {
        setTheme(isDark() ? Theme.LIGHT : Theme.DARK);
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }
        applyStylesheets(scene.getStylesheets(), scene.getRoot());
    }

    public static void apply(DialogPane pane) {
        if (pane == null) {
            return;
        }
        applyStylesheets(pane.getStylesheets(), pane);
    }

    private static void applyStylesheets(java.util.List<String> stylesheets, Parent root) {
        stylesheets.clear();
        stylesheets.add(ThemeManager.class.getResource("/css/app.css").toExternalForm());
        if (isDark()) {
            stylesheets.add(ThemeManager.class.getResource("/css/dark.css").toExternalForm());
            if (root != null && !root.getStyleClass().contains("theme-dark")) {
                root.getStyleClass().add("theme-dark");
            }
        } else if (root != null) {
            root.getStyleClass().remove("theme-dark");
        }
    }

    private static Theme loadSaved() {
        try {
            return Theme.valueOf(PREFS.get(PREF_KEY, Theme.LIGHT.name()));
        } catch (Exception e) {
            return Theme.LIGHT;
        }
    }
}
