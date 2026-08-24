package com.ict.lending.view;

import com.ict.lending.controller.LoginController;
import com.ict.lending.service.AuthService;
import com.ict.lending.utils.AlertHelper;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView {

    private static final double PANEL_WIDTH = 420;

    private final StackPane root = new StackPane();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final LoginController controller;
    private final StackPane cardShell = new StackPane();
    private final VBox loginCard = new VBox(0);
    private final VBox brandPane = new VBox(12);
    private final StackPane decorLayer = new StackPane();

    public LoginView(AuthService authService, Stage stage) {
        this.controller = new LoginController(authService, stage);
        build();
    }

    public StackPane getRoot() {
        return root;
    }

    /**
     * Full-height white panel slides in from far off the right edge.
     */
    public void playEntrance(Runnable onFinished) {
        root.setVisible(true);
        root.setManaged(true);
        root.setTranslateY(0);
        root.setOpacity(1);

        root.applyCss();
        root.layout();

        double panelW = cardShell.getWidth() > 1 ? cardShell.getWidth() : PANEL_WIDTH;
        double slideFrom = Math.max(panelW + 80, root.getWidth() > 1 ? root.getWidth() * 0.45 : panelW + 200);

        cardShell.setTranslateX(slideFrom);
        brandPane.setOpacity(0);

        FadeTransition brandIn = new FadeTransition(Duration.millis(520), brandPane);
        brandIn.setFromValue(0);
        brandIn.setToValue(1);
        brandIn.setDelay(Duration.millis(180));

        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(780), cardShell);
        cardSlide.setFromX(slideFrom);
        cardSlide.setToX(0);
        cardSlide.setInterpolator(Interpolator.SPLINE(0.16, 0.84, 0.18, 1.0));

        ParallelTransition entrance = new ParallelTransition(cardSlide, brandIn);
        entrance.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
            usernameField.requestFocus();
        });
        entrance.play();
        startAmbientMotion();
    }

    private void build() {
        root.getStyleClass().add("login-root");
        root.setVisible(false);
        root.setManaged(false);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ImageView bg = new ImageView(loadImage("/images/login-bg.png"));
        bg.setPreserveRatio(false);
        bg.fitWidthProperty().bind(root.widthProperty());
        bg.fitHeightProperty().bind(root.heightProperty());
        bg.setMouseTransparent(true);

        decorLayer.setMouseTransparent(true);
        decorLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        buildDecor();

        brandPane.getStyleClass().add("login-brand-pane");
        brandPane.setAlignment(Pos.CENTER_LEFT);
        brandPane.setFillWidth(true);
        brandPane.setMaxWidth(480);
        StackPane.setAlignment(brandPane, Pos.CENTER_LEFT);
        StackPane.setMargin(brandPane, new Insets(40, PANEL_WIDTH + 48, 40, 64));

        Label brand = new Label("Invento");
        brand.getStyleClass().add("login-brand-hero");
        brand.setWrapText(true);

        Label tag = new Label("ICT laboratory lending");
        tag.getStyleClass().add("login-brand-tag");

        Label blurb = new Label("Record borrows and returns for lab equipment.\nWorks offline on this computer.");
        blurb.getStyleClass().add("login-brand-blurb");
        blurb.setWrapText(true);
        brandPane.getChildren().addAll(brand, tag, blurb);

        buildLoginPanel();

        root.getChildren().addAll(bg, decorLayer, brandPane, cardShell);
        usernameField.setText("admin");
    }

    private void buildLoginPanel() {
        loginCard.getStyleClass().add("login-column");
        loginCard.setAlignment(Pos.CENTER_LEFT);
        loginCard.setPrefWidth(PANEL_WIDTH);
        loginCard.setMinWidth(PANEL_WIDTH);
        loginCard.setMaxWidth(PANEL_WIDTH);
        loginCard.setMaxHeight(Double.MAX_VALUE);
        loginCard.prefHeightProperty().bind(root.heightProperty());

        VBox body = new VBox(14);
        body.setPadding(new Insets(48, 40, 48, 40));
        body.setAlignment(Pos.CENTER_LEFT);
        body.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(body, Priority.ALWAYS);

        HBox logoRow = new HBox(12);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        ImageView logo = new ImageView(loadImage("/icons/invento-logo.png"));
        logo.setFitWidth(44);
        logo.setFitHeight(44);
        logo.setPreserveRatio(true);
        VBox logoText = new VBox(2);
        Label welcome = new Label("Welcome back");
        welcome.getStyleClass().add("login-welcome-chip");
        Label logoSub = new Label("Staff access");
        logoSub.getStyleClass().add("login-logo-sub");
        logoText.getChildren().addAll(welcome, logoSub);
        logoRow.getChildren().addAll(logo, logoText);

        Separator rule = new Separator();
        rule.getStyleClass().add("login-rule");

        Label title = new Label("Sign in");
        title.getStyleClass().add("login-form-title");

        Label subtitle = new Label("Enter your lab admin credentials to continue.");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);

        VBox fields = new VBox(10);
        fields.setAlignment(Pos.CENTER_LEFT);
        fields.setMaxWidth(Double.MAX_VALUE);

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("login-label");
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(44);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.getStyleClass().add("login-input");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("login-label");
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(44);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.getStyleClass().add("login-input");

        fields.getChildren().addAll(userLabel, usernameField, passLabel, passwordField);

        Button loginBtn = new Button("Sign in");
        loginBtn.getStyleClass().addAll("button", "btn-primary", "login-submit");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(48);
        loginBtn.setOnAction(e -> attemptLogin());

        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                attemptLogin();
            }
        });
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        Label hint = new Label("First sign-in requires a new password.");
        hint.getStyleClass().add("login-hint");
        hint.setWrapText(true);

        body.getChildren().addAll(
                logoRow, rule, title, subtitle, spacer(4), fields, spacer(10), loginBtn, hint);
        loginCard.getChildren().setAll(body);

        cardShell.getChildren().setAll(loginCard);
        cardShell.getStyleClass().add("login-card-shell");
        cardShell.setPrefWidth(PANEL_WIDTH);
        cardShell.setMinWidth(PANEL_WIDTH);
        cardShell.setMaxWidth(PANEL_WIDTH);
        cardShell.setMaxHeight(Double.MAX_VALUE);
        cardShell.prefHeightProperty().bind(root.heightProperty());
        StackPane.setAlignment(cardShell, Pos.CENTER_RIGHT);
        StackPane.setMargin(cardShell, Insets.EMPTY);
        // Park fully off the right edge until entrance plays
        cardShell.setTranslateX(PANEL_WIDTH + 200);
    }

    private void buildDecor() {
        addBubble(95, 0.14, Pos.BOTTOM_LEFT, 60, -50);
        addBubble(140, 0.10, Pos.TOP_LEFT, 120, 80);
        addBubble(70, 0.16, Pos.CENTER_LEFT, 40, -20);
        addBubble(110, 0.11, Pos.TOP_RIGHT, -90, 70);
        addBubble(160, 0.08, Pos.BOTTOM_RIGHT, -70, -60);
        addBubble(55, 0.18, Pos.CENTER_RIGHT, -40, 40);

        StackPane ringsLeft = ringCluster(100, 155, 210);
        StackPane.setAlignment(ringsLeft, Pos.BOTTOM_LEFT);
        ringsLeft.setTranslateX(24);
        ringsLeft.setTranslateY(-28);
        decorLayer.getChildren().add(ringsLeft);

        StackPane ringsRight = ringCluster(70, 115, 165);
        StackPane.setAlignment(ringsRight, Pos.TOP_RIGHT);
        ringsRight.setTranslateX(-40);
        ringsRight.setTranslateY(48);
        decorLayer.getChildren().add(ringsRight);

        StackPane ringsMid = ringCluster(55, 95, 140);
        StackPane.setAlignment(ringsMid, Pos.CENTER_LEFT);
        ringsMid.setTranslateX(180);
        ringsMid.setTranslateY(-80);
        decorLayer.getChildren().add(ringsMid);
    }

    private void addBubble(double radius, double opacity, Pos anchor, double tx, double ty) {
        Circle bubble = new Circle(radius);
        bubble.setFill(Color.rgb(255, 255, 255, opacity));
        bubble.setMouseTransparent(true);
        StackPane.setAlignment(bubble, anchor);
        bubble.setTranslateX(tx);
        bubble.setTranslateY(ty);
        bubble.getProperties().put("baseX", tx);
        bubble.getProperties().put("baseY", ty);
        bubble.getStyleClass().add("login-bubble");
        decorLayer.getChildren().add(bubble);
    }

    private static StackPane ringCluster(double... radii) {
        StackPane cluster = new StackPane();
        cluster.setMouseTransparent(true);
        for (int i = 0; i < radii.length; i++) {
            Circle ring = new Circle(radii[i]);
            ring.setFill(Color.TRANSPARENT);
            ring.getStyleClass().add(i % 2 == 0 ? "login-decor-ring" : "login-decor-ring-soft");
            cluster.getChildren().add(ring);
        }
        return cluster;
    }

    private void startAmbientMotion() {
        for (Node node : decorLayer.getChildren()) {
            if (node instanceof Circle bubble && bubble.getStyleClass().contains("login-bubble")) {
                floatBubble(bubble);
            } else if (node instanceof StackPane cluster) {
                driftAndSpin(cluster);
            }
        }
    }

    private void floatBubble(Circle bubble) {
        double baseX = ((Number) bubble.getProperties().getOrDefault("baseX", 0)).doubleValue();
        double baseY = ((Number) bubble.getProperties().getOrDefault("baseY", 0)).doubleValue();
        double ampX = 18 + Math.random() * 28;
        double ampY = 22 + Math.random() * 34;
        double duration = 4200 + Math.random() * 3200;

        TranslateTransition drift = new TranslateTransition(Duration.millis(duration), bubble);
        drift.setFromX(baseX);
        drift.setFromY(baseY);
        drift.setToX(baseX + (Math.random() > 0.5 ? ampX : -ampX));
        drift.setToY(baseY + (Math.random() > 0.5 ? ampY : -ampY));
        drift.setAutoReverse(true);
        drift.setCycleCount(Animation.INDEFINITE);
        drift.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(duration * 0.85), bubble);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.06 + Math.random() * 0.08);
        pulse.setToY(1.06 + Math.random() * 0.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition breath = new FadeTransition(Duration.millis(duration * 1.1), bubble);
        breath.setFromValue(0.55);
        breath.setToValue(1.0);
        breath.setAutoReverse(true);
        breath.setCycleCount(Animation.INDEFINITE);

        new ParallelTransition(drift, pulse, breath).play();
    }

    private void driftAndSpin(StackPane cluster) {
        double baseX = cluster.getTranslateX();
        double baseY = cluster.getTranslateY();

        RotateTransition spin = new RotateTransition(Duration.millis(18000 + Math.random() * 8000), cluster);
        spin.setByAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);

        TranslateTransition drift = new TranslateTransition(Duration.millis(7000 + Math.random() * 4000), cluster);
        drift.setFromX(baseX);
        drift.setFromY(baseY);
        drift.setToX(baseX + 24 - Math.random() * 48);
        drift.setToY(baseY + 18 - Math.random() * 36);
        drift.setAutoReverse(true);
        drift.setCycleCount(Animation.INDEFINITE);
        drift.setInterpolator(Interpolator.EASE_BOTH);

        Timeline delay = new Timeline();
        delay.getKeyFrames().add(new javafx.animation.KeyFrame(
                Duration.millis(Math.random() * 900),
                e -> new ParallelTransition(spin, drift).play()));
        delay.play();
    }

    private void attemptLogin() {
        try {
            boolean ok = controller.login(usernameField.getText(), passwordField.getText());
            if (!ok) {
                AlertHelper.error(root.getScene().getWindow(), "Invalid username or password.");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (Exception ex) {
            AlertHelper.error(root.getScene().getWindow(), ex.getMessage());
        }
    }

    private static Image loadImage(String path) {
        var stream = LoginView.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing resource: " + path);
        }
        return new Image(stream);
    }

    private static Region spacer(double h) {
        Region r = new Region();
        r.setMinHeight(h);
        r.setPrefHeight(h);
        return r;
    }
}
