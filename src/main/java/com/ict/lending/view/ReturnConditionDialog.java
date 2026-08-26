package com.ict.lending.view;

import com.ict.lending.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/**
 * Admin-filled device condition report captured when a device is returned.
 */
public class ReturnConditionDialog extends Dialog<String> {

    public ReturnConditionDialog(Window owner, String deviceName) {
        setTitle("Device Condition Report");
        initOwner(owner);

        ButtonType submitType = new ButtonType("Confirm Return", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        Label hint = new Label("Note any damage, missing parts, or problems found with "
                + deviceName + " upon return. Leave blank if the device is in good condition.");
        hint.setWrapText(true);
        hint.setMaxWidth(360);

        TextArea reportField = new TextArea();
        reportField.setPromptText("e.g. Cracked screen, missing charger, not powering on…");
        reportField.setPrefRowCount(5);
        reportField.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.getStyleClass().add("form-grid");
        grid.add(hint, 0, 0);
        grid.add(new Label("Condition Report"), 0, 1);
        grid.add(reportField, 0, 2);
        reportField.setPrefWidth(360);

        getDialogPane().setContent(grid);
        ThemeManager.apply(getDialogPane());

        setResultConverter(btn -> btn == submitType ? reportField.getText() : null);
    }
}
