package com.ict.lending.view;

import com.ict.lending.model.Device;
import com.ict.lending.utils.AlertHelper;
import com.ict.lending.utils.DeviceImages;
import com.ict.lending.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceFormDialog extends Dialog<DeviceFormDialog.Result> {

    public record Result(String name, String category, String brand, String serial, int quantity,
                         Path imageSource, boolean clearImage) {
    }

    public DeviceFormDialog(Window owner, Device existing) {
        setTitle(existing == null ? "Add Device" : "Edit Device");
        initOwner(owner);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        categoryField.setPromptText("e.g. Keyboard, Laptop");
        TextField brandField = new TextField();
        TextField serialField = new TextField();
        serialField.setPromptText("Optional");
        TextField qtyField = new TextField();

        AtomicReference<Path> chosenImage = new AtomicReference<>(null);
        AtomicBoolean clearImage = new AtomicBoolean(false);
        AtomicReference<String> previewPath = new AtomicReference<>(
                existing != null ? existing.getImagePath() : null);

        StackPane imagePreview = new StackPane();
        imagePreview.setAlignment(Pos.CENTER);
        Runnable refreshPreview = () -> {
            imagePreview.getChildren().setAll(
                    DeviceImages.preview(previewPath.get(), 140, 100));
        };
        refreshPreview.run();

        Button chooseBtn = new Button("Choose Photo…");
        chooseBtn.getStyleClass().addAll("button", "btn-secondary");
        chooseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Device Photo");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg",
                            "*.gif", "*.webp", "*.bmp"));
            var file = chooser.showOpenDialog(owner);
            if (file != null) {
                Path path = file.toPath();
                if (!DeviceImages.looksLikeImage(path)) {
                    AlertHelper.error(owner, "Please choose a valid image file.");
                    return;
                }
                chosenImage.set(path);
                clearImage.set(false);
                previewPath.set(path.toAbsolutePath().toString());
                refreshPreview.run();
            }
        });

        Button removeBtn = new Button("Remove Photo");
        removeBtn.getStyleClass().addAll("button", "btn-danger");
        removeBtn.setOnAction(e -> {
            chosenImage.set(null);
            clearImage.set(true);
            previewPath.set(null);
            refreshPreview.run();
        });

        if (existing != null) {
            nameField.setText(existing.getDeviceName());
            categoryField.setText(existing.getCategory());
            brandField.setText(existing.getBrand());
            serialField.setText(existing.getSerialNumber());
            qtyField.setText(String.valueOf(existing.getQuantity()));
        } else {
            qtyField.setText("1");
        }

        HBox imageActions = new HBox(8, chooseBtn, removeBtn);
        imageActions.setAlignment(Pos.CENTER_LEFT);

        VBox imageBox = new VBox(8, new Label("Device Photo"), imagePreview, imageActions);
        imageBox.getStyleClass().add("device-form-image");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.getStyleClass().add("form-grid");
        grid.add(imageBox, 0, 0, 2, 1);
        grid.add(new Label("Device Name"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Category"), 0, 2);
        grid.add(categoryField, 1, 2);
        grid.add(new Label("Brand"), 0, 3);
        grid.add(brandField, 1, 3);
        grid.add(new Label("Serial Number"), 0, 4);
        grid.add(serialField, 1, 4);
        grid.add(new Label("Total Quantity"), 0, 5);
        grid.add(qtyField, 1, 5);
        nameField.setPrefWidth(260);

        getDialogPane().setContent(grid);
        ThemeManager.apply(getDialogPane());

        Button saveButton = (Button) getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText() == null || nameField.getText().isBlank()
                    || categoryField.getText() == null || categoryField.getText().isBlank()
                    || brandField.getText() == null || brandField.getText().isBlank()) {
                event.consume();
                AlertHelper.error(owner, "Device name, category, and brand are required.");
                return;
            }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) {
                    event.consume();
                    AlertHelper.error(owner, "Quantity must be greater than zero.");
                }
            } catch (NumberFormatException ex) {
                event.consume();
                AlertHelper.error(owner, "Quantity must be a valid number.");
            }
        });

        setResultConverter(btn -> {
            if (btn != saveType) {
                return null;
            }
            return new Result(
                    nameField.getText(),
                    categoryField.getText(),
                    brandField.getText(),
                    serialField.getText(),
                    Integer.parseInt(qtyField.getText().trim()),
                    chosenImage.get(),
                    clearImage.get()
            );
        });
    }
}
