package com.ict.lending.controller;

import com.ict.lending.service.AuthService;
import com.ict.lending.utils.ThemeManager;
import com.ict.lending.view.ForceChangePasswordDialog;
import com.ict.lending.view.MainShellView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {

    private final AuthService authService;
    private final Stage stage;

    public LoginController(AuthService authService, Stage stage) {
        this.authService = authService;
        this.stage = stage;
    }

    public boolean login(String username, String password) {
        if (!authService.login(username, password)) {
            return false;
        }

        if (authService.mustChangePassword()) {
            ForceChangePasswordDialog dialog = new ForceChangePasswordDialog(stage, authService);
            Boolean changed = dialog.showAndWait().orElse(false);
            if (!Boolean.TRUE.equals(changed) || authService.mustChangePassword()) {
                authService.logout();
                throw new IllegalStateException(
                        "You must set a new password before accessing the system.");
            }
        }

        MainShellView shell = new MainShellView(authService, stage);
        Scene scene = new Scene(shell.getRoot(), stage.getScene().getWidth(), stage.getScene().getHeight());
        ThemeManager.apply(scene);
        stage.setScene(scene);
        return true;
    }
}
