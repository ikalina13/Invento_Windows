package com.ict.lending.view;

import com.ict.lending.service.AuthService;
import com.ict.lending.utils.ThemeManager;
import com.ict.lending.utils.UiIcons;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainShellView {

    private final BorderPane root = new BorderPane();
    private final StackPane contentHost = new StackPane();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final AuthService authService;
    private final Stage stage;
    private final ThemeSwitchControl themeSwitch = new ThemeSwitchControl();

    private final DashboardView dashboardView = new DashboardView();
    private final InventoryView inventoryView;
    private final BorrowView borrowView;
    private final ReturnView returnView;
    private final HistoryView historyView = new HistoryView();
    private final SettingsView settingsView;

    public MainShellView(AuthService authService, Stage stage) {
        this.authService = authService;
        this.stage = stage;
        this.inventoryView = new InventoryView(this);
        this.borrowView = new BorrowView(this);
        this.returnView = new ReturnView(this);
        this.settingsView = new SettingsView(authService, stage, this);
        build();
        show("Dashboard");
    }

    public BorderPane getRoot() {
        return root;
    }

    public void refreshAll() {
        dashboardView.refresh();
        inventoryView.refresh();
        borrowView.refresh();
        returnView.refresh();
        historyView.refresh();
        settingsView.refresh();
    }

    public void applyThemeToStage() {
        ThemeManager.apply(stage.getScene());
        themeSwitch.syncFromTheme();
        settingsView.syncThemeControls();
        playContentThemeFlash();
    }

    private void playContentThemeFlash() {
        FadeTransition out = new FadeTransition(Duration.millis(90), contentHost);
        out.setFromValue(1.0);
        out.setToValue(0.72);
        out.setOnFinished(e -> {
            FadeTransition in = new FadeTransition(Duration.millis(140), contentHost);
            in.setFromValue(0.72);
            in.setToValue(1.0);
            in.play();
        });
        out.play();
    }

    private void build() {
        root.getStyleClass().add("shell-root");
        root.setLeft(buildSidebar());
        contentHost.getStyleClass().add("content-area");
        root.setCenter(contentHost);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        VBox brandBox = new VBox(8);
        brandBox.getStyleClass().add("sidebar-brand-box");
        ImageView logo = new ImageView(new Image(
                getClass().getResourceAsStream("/icons/invento-logo.png")));
        logo.setFitWidth(36);
        logo.setFitHeight(36);
        logo.setPreserveRatio(true);
        Label brand = new Label("Invento");
        brand.getStyleClass().add("sidebar-brand");
        Label sub = new Label("ICT Lab Lending");
        sub.getStyleClass().add("sidebar-brand-sub");
        VBox brandText = new VBox(1, brand, sub);
        HBox brandRow = new HBox(10, logo, brandText);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandBox.getChildren().add(brandRow);

        VBox nav = new VBox(4);
        nav.getStyleClass().add("sidebar-nav");
        String[] items = {"Dashboard", "Inventory", "Booking", "Return", "History", "Settings"};
        for (String item : items) {
            Button btn = new Button(item);
            btn.setGraphic(UiIcons.nav(item));
            btn.setGraphicTextGap(10);
            btn.getStyleClass().add("nav-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setOnAction(e -> show(item));
            navButtons.put(item, btn);
            nav.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label themeCaption = new Label("Theme");
        themeCaption.getStyleClass().add("sidebar-brand-sub");
        themeSwitch.setOnToggled(this::applyThemeToStage);
        VBox themeBox = new VBox(6, themeCaption, themeSwitch);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        themeBox.setPadding(new Insets(0, 14, 10, 14));

        Button logout = new Button("Sign out");
        logout.getStyleClass().addAll("nav-button", "nav-signout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setOnAction(e -> {
            authService.logout();
            LoginView loginView = new LoginView(authService, stage);
            SceneSwitcher.switchTo(stage, loginView.getRoot());
        });

        String user = authService.getCurrentAdmin() != null
                ? authService.getCurrentAdmin().getUsername()
                : "admin";
        Label userLabel = new Label(user);
        userLabel.getStyleClass().add("sidebar-user");
        userLabel.setPadding(new Insets(4, 16, 6, 16));

        sidebar.getChildren().addAll(brandBox, nav, spacer, themeBox, new Separator(), userLabel, logout);
        return sidebar;
    }

    private void show(String page) {
        for (Map.Entry<String, Button> e : navButtons.entrySet()) {
            e.getValue().getStyleClass().remove("active");
            if (e.getKey().equals(page)) {
                e.getValue().getStyleClass().add("active");
            }
        }
        contentHost.getChildren().clear();
        switch (page) {
            case "Dashboard" -> {
                dashboardView.refresh();
                contentHost.getChildren().add(dashboardView.getRoot());
            }
            case "Inventory" -> {
                inventoryView.refresh();
                contentHost.getChildren().add(inventoryView.getRoot());
            }
            case "Booking" -> {
                borrowView.refresh();
                contentHost.getChildren().add(borrowView.getRoot());
            }
            case "Return" -> {
                returnView.refresh();
                contentHost.getChildren().add(returnView.getRoot());
            }
            case "History" -> {
                historyView.refresh();
                contentHost.getChildren().add(historyView.getRoot());
            }
            case "Settings" -> {
                settingsView.refresh();
                contentHost.getChildren().add(settingsView.getRoot());
            }
            default -> {
            }
        }
        FadeTransition fade = new FadeTransition(Duration.millis(120), contentHost);
        fade.setFromValue(0.88);
        fade.setToValue(1.0);
        fade.play();
    }

    public static final class SceneSwitcher {
        private SceneSwitcher() {
        }

        public static void switchTo(Stage stage, javafx.scene.Parent root) {
            javafx.scene.Scene scene = new javafx.scene.Scene(
                    root, stage.getScene().getWidth(), stage.getScene().getHeight());
            ThemeManager.apply(scene);
            stage.setScene(scene);
        }
    }
}
