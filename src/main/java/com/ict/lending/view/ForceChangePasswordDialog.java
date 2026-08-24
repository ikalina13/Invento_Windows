package com.ict.lending.view;

import com.ict.lending.service.AuthService;
import com.ict.lending.utils.AlertHelper;
import com.ict.lending.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Blocking dialog that forces the admin to replace the default password.
 */
public class ForceChangePasswordDialog extends Dialog<Boolean> {

    public ForceChangePasswordDialog(Window owner, AuthService authService) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Change Password Required");
        setHeaderText(null);

        // No Cancel — must set a new password
        ButtonType saveType = new ButtonType("Save New Password", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(saveType);

        Label title = new Label("Secure your account");
        title.getStyleClass().add("section-title");
        Label message = new Label(
                "You're still using the default password. Create a new password before continuing.");
        message.getStyleClass().add("page-subtitle");
        message.setWrapText(true);
        message.setMaxWidth(340);

        PasswordField current = new PasswordField();
        current.setPromptText("Current password (admin123)");
        PasswordField neu = new PasswordField();
        neu.setPromptText("New password (min 6 characters)");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Confirm new password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("form-grid");
        grid.add(new Label("Current"), 0, 0);
        grid.add(current, 1, 0);
        grid.add(new Label("New"), 0, 1);
        grid.add(neu, 1, 1);
        grid.add(new Label("Confirm"), 0, 2);
        grid.add(confirm, 1, 2);
        current.setPrefWidth(240);

        VBox box = new VBox(12, title, message, grid);
        box.setPadding(new Insets(8, 4, 4, 4));
        getDialogPane().setContent(box);
        ThemeManager.apply(getDialogPane());

        Button saveBtn = (Button) getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                authService.changePassword(current.getText(), neu.getText(), confirm.getText());
            } catch (Exception ex) {
                event.consume();
                AlertHelper.error(owner, ex.getMessage());
            }
        });

        setResultConverter(btn -> btn == saveType);
        setOnCloseRequest(e -> {
            // Block closing via window X while still on default password
            if (authService.mustChangePassword()) {
                e.consume();
                AlertHelper.warn(owner, "Please set a new password to continue.");
            }
        });
    }
}
