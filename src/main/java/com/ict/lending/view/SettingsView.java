package com.ict.lending.view;

import com.ict.lending.controller.SettingsController;
import com.ict.lending.model.AuditLog;
import com.ict.lending.service.AuthService;
import com.ict.lending.utils.AlertHelper;
import com.ict.lending.utils.IdGenerator;
import com.ict.lending.utils.ThemeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;

public class SettingsView {

    private final VBox root = new VBox(16);
    private final PasswordField currentPass = new PasswordField();
    private final PasswordField newPass = new PasswordField();
    private final PasswordField confirmPass = new PasswordField();
    private final ComboBox<String> backupList = new ComboBox<>();
    private final TableView<AuditLog> auditTable = new TableView<>();
    private final SettingsController controller = new SettingsController();
    private final AuthService authService;
    private final Stage stage;
    private final MainShellView shell;
    private final ToggleButton lightBtn = new ToggleButton("Light Mode");
    private final ToggleButton darkBtn = new ToggleButton("Dark Mode");
    private final ToggleGroup themeGroup = new ToggleGroup();

    public SettingsView(AuthService authService, Stage stage, MainShellView shell) {
        this.authService = authService;
        this.stage = stage;
        this.shell = shell;
        build();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        List<Path> backups = controller.listBackups();
        backupList.setItems(FXCollections.observableArrayList(
                backups.stream().map(p -> p.getFileName().toString()).toList()));
        if (!backupList.getItems().isEmpty()) {
            backupList.getSelectionModel().selectFirst();
        }
        auditTable.getItems().setAll(controller.recentAudit());
        syncThemeControls();
    }

    public void syncThemeControls() {
        if (ThemeManager.isDark()) {
            darkBtn.setSelected(true);
        } else {
            lightBtn.setSelected(true);
        }
    }

    private void build() {
        Label kicker = new Label("Administration");
        kicker.getStyleClass().add("page-kicker");
        Label title = new Label("Settings");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Theme, password, backups, and activity log.");
        subtitle.getStyleClass().add("page-subtitle");

        VBox themePanel = buildThemePanel();
        VBox passwordPanel = buildPasswordPanel();
        VBox backupPanel = buildBackupPanel();
        VBox auditPanel = buildAuditPanel();

        HBox top = new HBox(14, themePanel, passwordPanel, backupPanel);
        HBox.setHgrow(themePanel, Priority.ALWAYS);
        HBox.setHgrow(passwordPanel, Priority.ALWAYS);
        HBox.setHgrow(backupPanel, Priority.ALWAYS);
        VBox.setVgrow(auditPanel, Priority.ALWAYS);

        root.getChildren().addAll(kicker, title, subtitle, top, auditPanel);
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    private VBox buildThemePanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        Label section = new Label("Appearance");
        section.getStyleClass().add("section-title");
        Label help = new Label("Switch between light and dark interface themes. Your choice is remembered.");
        help.getStyleClass().add("page-subtitle");
        help.setWrapText(true);

        lightBtn.setToggleGroup(themeGroup);
        darkBtn.setToggleGroup(themeGroup);
        lightBtn.getStyleClass().addAll("button", "btn-secondary");
        darkBtn.getStyleClass().addAll("button", "btn-secondary");
        lightBtn.setOnAction(e -> {
            ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
            shell.applyThemeToStage();
        });
        darkBtn.setOnAction(e -> {
            ThemeManager.setTheme(ThemeManager.Theme.DARK);
            shell.applyThemeToStage();
        });
        syncThemeControls();

        HBox row = new HBox(10, lightBtn, darkBtn);
        panel.getChildren().addAll(section, help, row);
        return panel;
    }

    private VBox buildPasswordPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        Label section = new Label("Change Password");
        section.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("form-grid");
        currentPass.setPromptText("Current password");
        newPass.setPromptText("New password (min 6 chars)");
        confirmPass.setPromptText("Confirm new password");
        grid.add(new Label("Current"), 0, 0);
        grid.add(currentPass, 1, 0);
        grid.add(new Label("New"), 0, 1);
        grid.add(newPass, 1, 1);
        grid.add(new Label("Confirm"), 0, 2);
        grid.add(confirmPass, 1, 2);
        currentPass.setPrefWidth(220);

        Button save = new Button("Update Password");
        save.getStyleClass().addAll("button", "btn-primary");
        save.setOnAction(e -> {
            try {
                if (!AlertHelper.confirm(window(), "Confirm", "Change admin password now?")) {
                    return;
                }
                authService.changePassword(currentPass.getText(), newPass.getText(), confirmPass.getText());
                currentPass.clear();
                newPass.clear();
                confirmPass.clear();
                AlertHelper.success(window(), "Password updated successfully.");
                refresh();
            } catch (Exception ex) {
                AlertHelper.error(window(), ex.getMessage());
            }
        });

        panel.getChildren().addAll(section, grid, save);
        return panel;
    }

    private VBox buildBackupPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        Label section = new Label("Database Backup & Restore");
        section.getStyleClass().add("section-title");
        Label help = new Label("Automatic daily backup runs on startup. Create a manual copy anytime.");
        help.getStyleClass().add("page-subtitle");
        help.setWrapText(true);

        Button backupBtn = new Button("Backup Now");
        backupBtn.getStyleClass().addAll("button", "btn-success");
        backupBtn.setOnAction(e -> {
            try {
                Path path = controller.backupNow();
                AlertHelper.success(window(), "Backup saved:\n" + path);
                refresh();
            } catch (Exception ex) {
                AlertHelper.error(window(), ex.getMessage());
            }
        });

        backupList.setPrefWidth(280);
        backupList.setPromptText("Select backup file");

        Button restoreBtn = new Button("Restore Selected");
        restoreBtn.getStyleClass().addAll("button", "btn-danger");
        restoreBtn.setOnAction(e -> restoreSelected());

        Button browseBtn = new Button("Restore from File…");
        browseBtn.getStyleClass().addAll("button", "btn-secondary");
        browseBtn.setOnAction(e -> restoreFromFile());

        HBox row = new HBox(10, backupList, restoreBtn);
        panel.getChildren().addAll(section, help, backupBtn, row, browseBtn);
        return panel;
    }

    private VBox buildAuditPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        Label section = new Label("Audit Log");
        section.getStyleClass().add("section-title");

        TableColumn<AuditLog, String> timeCol = new TableColumn<>("When");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                IdGenerator.formatDateTime(c.getValue().getCreatedAt())));
        timeCol.setPrefWidth(160);
        TableColumn<AuditLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));
        actionCol.setPrefWidth(120);
        TableColumn<AuditLog, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetails()));
        detailsCol.setPrefWidth(400);
        auditTable.getColumns().addAll(timeCol, actionCol, detailsCol);
        auditTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        auditTable.setPlaceholder(new Label("No audit entries"));
        VBox.setVgrow(auditTable, Priority.ALWAYS);

        panel.getChildren().addAll(section, auditTable);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private void restoreSelected() {
        String name = backupList.getValue();
        if (name == null || name.isBlank()) {
            AlertHelper.warn(window(), "Select a backup to restore.");
            return;
        }
        Path path = controller.listBackups().stream()
                .filter(p -> p.getFileName().toString().equals(name))
                .findFirst()
                .orElse(null);
        if (path == null) {
            AlertHelper.error(window(), "Backup file not found.");
            return;
        }
        doRestore(path);
    }

    private void restoreFromFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select SQLite Backup");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        var file = chooser.showOpenDialog(stage);
        if (file != null) {
            doRestore(file.toPath());
        }
    }

    private void doRestore(Path path) {
        if (!AlertHelper.confirm(window(), "Restore Database",
                "Restoring will replace the current database.\nA safety copy will be created first.\n\nContinue?")) {
            return;
        }
        try {
            controller.restore(path);
            AlertHelper.success(window(),
                    "Database restored. Please sign out and sign back in if data looks stale.");
            refresh();
        } catch (Exception ex) {
            AlertHelper.error(window(), ex.getMessage());
        }
    }

    private javafx.stage.Window window() {
        return root.getScene() != null ? root.getScene().getWindow() : null;
    }
}
