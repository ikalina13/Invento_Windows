package com.ict.lending.view;

import com.ict.lending.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Animated pill switch for light / dark theme.
 */
public class ThemeSwitchControl extends StackPane {

    private static final double TRACK_WIDTH = 168;
    private static final double TRACK_HEIGHT = 36;
    private static final double KNOB_SIZE = 28;
    private static final double TRAVEL = 132;

    private final StackPane track = new StackPane();
    private final Circle knob = new Circle(KNOB_SIZE / 2.0);
    private final Label lightLabel = new Label("Light");
    private final Label darkLabel = new Label("Dark");
    private Runnable onToggled;
    private boolean animating;

    public ThemeSwitchControl() {
        getStyleClass().add("theme-switch");
        setMaxWidth(TRACK_WIDTH);
        setPrefWidth(TRACK_WIDTH);
        setPrefHeight(TRACK_HEIGHT);

        track.getStyleClass().add("theme-switch-track");
        track.setPrefSize(TRACK_WIDTH, TRACK_HEIGHT);
        track.setMinSize(TRACK_WIDTH, TRACK_HEIGHT);
        track.setMaxSize(TRACK_WIDTH, TRACK_HEIGHT);

        lightLabel.getStyleClass().addAll("theme-switch-label", "theme-switch-label-light");
        darkLabel.getStyleClass().addAll("theme-switch-label", "theme-switch-label-dark");
        HBox labels = new HBox(lightLabel, new Region(), darkLabel);
        labels.setAlignment(Pos.CENTER);
        labels.setPrefWidth(TRACK_WIDTH);
        HBox.setHgrow(labels.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        labels.setPadding(new javafx.geometry.Insets(0, 14, 0, 14));

        knob.getStyleClass().add("theme-switch-knob");
        StackPane knobLayer = new StackPane(knob);
        knobLayer.setAlignment(Pos.CENTER_LEFT);
        knobLayer.setPadding(new javafx.geometry.Insets(0, 0, 0, 4));
        knobLayer.setPrefSize(TRACK_WIDTH, TRACK_HEIGHT);
        knob.setTranslateX(ThemeManager.isDark() ? TRAVEL : 0);
        updateVisualState(false);

        track.getChildren().addAll(labels, knobLayer);
        getChildren().add(track);

        setOnMouseClicked(e -> toggle());
        setOnMouseEntered(e -> {
            if (!animating) {
                ScaleTransition pulse = new ScaleTransition(Duration.millis(120), knob);
                pulse.setToX(1.08);
                pulse.setToY(1.08);
                pulse.play();
            }
        });
        setOnMouseExited(e -> {
            if (!animating) {
                ScaleTransition pulse = new ScaleTransition(Duration.millis(120), knob);
                pulse.setToX(1.0);
                pulse.setToY(1.0);
                pulse.play();
            }
        });
    }

    public void setOnToggled(Runnable onToggled) {
        this.onToggled = onToggled;
    }

    public void syncFromTheme() {
        knob.setTranslateX(ThemeManager.isDark() ? TRAVEL : 0);
        updateVisualState(false);
    }

    private void toggle() {
        if (animating) {
            return;
        }
        animating = true;
        boolean toDark = !ThemeManager.isDark();
        ThemeManager.setTheme(toDark ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);

        TranslateTransition slide = new TranslateTransition(Duration.millis(280), knob);
        slide.setToX(toDark ? TRAVEL : 0);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        ScaleTransition bounce = new ScaleTransition(Duration.millis(140), knob);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.12);
        bounce.setToY(1.12);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(120), track);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.82);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(160), track);
        fadeIn.setFromValue(0.82);
        fadeIn.setToValue(1.0);
        fadeIn.setDelay(Duration.millis(120));

        ParallelTransition all = new ParallelTransition(slide, bounce, fadeOut, fadeIn);
        all.setOnFinished(e -> {
            updateVisualState(true);
            animating = false;
            if (onToggled != null) {
                onToggled.run();
            }
        });
        all.play();
    }

    private void updateVisualState(boolean animated) {
        getStyleClass().removeAll("theme-switch-dark", "theme-switch-light");
        getStyleClass().add(ThemeManager.isDark() ? "theme-switch-dark" : "theme-switch-light");
        if (animated) {
            FadeTransition fl = new FadeTransition(Duration.millis(180), lightLabel);
            FadeTransition fd = new FadeTransition(Duration.millis(180), darkLabel);
            if (ThemeManager.isDark()) {
                fl.setToValue(0.45);
                fd.setToValue(1.0);
            } else {
                fl.setToValue(1.0);
                fd.setToValue(0.45);
            }
            new ParallelTransition(fl, fd).play();
        } else {
            lightLabel.setOpacity(ThemeManager.isDark() ? 0.45 : 1.0);
            darkLabel.setOpacity(ThemeManager.isDark() ? 1.0 : 0.45);
        }
    }
}
