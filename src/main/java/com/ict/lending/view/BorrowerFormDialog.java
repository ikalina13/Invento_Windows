package com.ict.lending.view;

import com.ict.lending.model.Borrower;
import com.ict.lending.utils.ThemeManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class BorrowerFormDialog extends Dialog<Borrower> {

    public BorrowerFormDialog(Window owner, String title, boolean includePurpose) {
        setTitle(title);
        initOwner(owner);

        ButtonType submitType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        ComboBox<String> positionBox = new ComboBox<>(FXCollections.observableArrayList(
                "Student", "Teacher", "Staff"));
        positionBox.setValue("Student");
        TextField gradeField = new TextField();
        gradeField.setPromptText("e.g. Grade 12 / ICT Department");
        TextField sectionField = new TextField();
        TextArea purposeField = new TextArea();
        purposeField.setPrefRowCount(3);
        purposeField.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.getStyleClass().add("form-grid");
        int row = 0;
        grid.add(new Label("Full Name"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Position"), 0, row);
        grid.add(positionBox, 1, row++);
        grid.add(new Label("Grade / Department"), 0, row);
        grid.add(gradeField, 1, row++);
        grid.add(new Label("Section"), 0, row);
        grid.add(sectionField, 1, row++);
        if (includePurpose) {
            grid.add(new Label("Purpose"), 0, row);
            grid.add(purposeField, 1, row);
        }
        nameField.setPrefWidth(280);

        getDialogPane().setContent(grid);
        ThemeManager.apply(getDialogPane());

        setResultConverter(btn -> {
            if (btn != submitType) {
                return null;
            }
            Borrower b = new Borrower();
            b.setFullName(nameField.getText());
            b.setPosition(positionBox.getValue());
            b.setGradeLevel(gradeField.getText());
            b.setSection(sectionField.getText());
            b.setPurpose(includePurpose ? purposeField.getText() : "Return verification");
            return b;
        });
    }
}
