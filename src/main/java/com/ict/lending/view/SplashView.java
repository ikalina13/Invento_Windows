package com.ict.lending.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Monarch-style startup splash: white + logo tile → blue expands → brand hold.
 */
public class SplashView {

    private static final double LOGO_SIZE = 148;
    private static final String BLUE = "#0162ef";

    private final StackPane root = new StackPane();
    private final Region blueExpand = new Region();
    private final ImageView logoView = new ImageView();
    private final VBox brandLockup = new VBox(8);

    public SplashView() {
        build();
    }

    public StackPane getRoot() {
        return root;
    }

    public void play(Runnable onFinished) {
        root.applyCss();
        root.layout();

        double cover = coverScale();
        blueExpand.setScaleX(1);
        blueExpand.setScaleY(1);

        PauseTransition hold = new PauseTransition(Duration.millis(420));

        ScaleTransition expand = new ScaleTransition(Duration.millis(720), blueExpand);
        expand.setToX(cover);
        expand.setToY(cover);
        expand.setInterpolator(javafx.animation.Interpolator.EASE_IN);

        FadeTransition logoOut = new FadeTransition(Duration.millis(280), logoView);
        logoOut.setFromValue(1);
        logoOut.setToValue(0);
        logoOut.setDelay(Duration.millis(380));

        FadeTransition brandIn = new FadeTransition(Duration.millis(360), brandLockup);
        brandIn.setFromValue(0);
        brandIn.setToValue(1);
        brandIn.setDelay(Duration.millis(480));

        ParallelTransition expandPhase = new ParallelTransition(expand, logoOut, brandIn);
        PauseTransition brandHold = new PauseTransition(Duration.millis(520));

        SequentialTransition sequence = new SequentialTransition(hold, expandPhase, brandHold);
        sequence.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        sequence.play();
    }

    private void build() {
        root.getStyleClass().add("splash-root");
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        blueExpand.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        blueExpand.setMinSize(LOGO_SIZE, LOGO_SIZE);
        blueExpand.setMaxSize(LOGO_SIZE, LOGO_SIZE);
        blueExpand.setStyle(
                "-fx-background-color: " + BLUE + ";"
                        + "-fx-background-radius: 28;");
        StackPane.setAlignment(blueExpand, Pos.CENTER);

        Image logo = loadImage("/icons/invento-logo.png");
        logoView.setImage(logo);
        logoView.setFitWidth(LOGO_SIZE);
        logoView.setFitHeight(LOGO_SIZE);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        Rectangle clip = new Rectangle(LOGO_SIZE, LOGO_SIZE);
        clip.setArcWidth(56);
        clip.setArcHeight(56);
        logoView.setClip(clip);
        StackPane.setAlignment(logoView, Pos.CENTER);

        Label brand = new Label("Invento");
        brand.getStyleClass().add("splash-brand");
        brand.setStyle(
                "-fx-font-size: 36px; -fx-font-weight: 700; -fx-text-fill: white;"
                        + "-fx-font-family: 'Segoe UI', Candara, sans-serif;");

        Label tag = new Label("BORROW. TRACK. RETURN.");
        tag.getStyleClass().add("splash-tagline");
        tag.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: rgba(255,255,255,0.88);");

        brandLockup.setAlignment(Pos.CENTER);
        brandLockup.setOpacity(0);
        brandLockup.setMouseTransparent(true);
        brandLockup.getChildren().addAll(brand, tag);
        StackPane.setAlignment(brandLockup, Pos.CENTER);

        root.getChildren().addAll(blueExpand, logoView, brandLockup);
    }

    private double coverScale() {
        double w = root.getWidth() > 1 ? root.getWidth() : 1100;
        double h = root.getHeight() > 1 ? root.getHeight() : 720;
        double diagonal = Math.hypot(w, h);
        return (diagonal / LOGO_SIZE) * 1.15;
    }

    private static Image loadImage(String path) {
        var stream = SplashView.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing resource: " + path);
        }
        return new Image(stream);
    }
}
