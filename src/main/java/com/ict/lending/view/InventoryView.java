package com.ict.lending.view;

import com.ict.lending.controller.InventoryController;
import com.ict.lending.model.Device;
import com.ict.lending.utils.AlertHelper;
import com.ict.lending.utils.DeviceImages;
import com.ict.lending.utils.IdGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class InventoryView {

    private final VBox root = new VBox(12);
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final TableView<Device> table = new TableView<>();
    private final FlowPane grid = new FlowPane(10, 10);
    private final StackPane bodyHost = new StackPane();
    private final ScrollPane gridScroll = new ScrollPane();
    private final InventoryController controller = new InventoryController();
    private final MainShellView shell;
    private final ToggleButton listBtn = new ToggleButton("List");
    private final ToggleButton gridBtn = new ToggleButton("Photos");
    private boolean photoMode = true;

    public InventoryView(MainShellView shell) {
        this.shell = shell;
        build();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        String selected = categoryFilter.getValue();
        categoryFilter.setItems(FXCollections.observableArrayList(controller.categoriesWithAll()));
        if (selected != null && categoryFilter.getItems().contains(selected)) {
            categoryFilter.setValue(selected);
        } else {
            categoryFilter.setValue("All");
        }
        reload();
    }

    public void applyExternalSearch(String query) {
        searchField.setText(query == null ? "" : query);
        reload();
    }

    private void reload() {
        List<Device> devices = controller.list(searchField.getText(), categoryFilter.getValue());
        table.getItems().setAll(devices);
        grid.getChildren().clear();
        if (devices.isEmpty()) {
            VBox empty = new VBox(6);
            empty.getStyleClass().add("empty-hint");
            Label t = new Label("No devices yet");
            t.getStyleClass().add("section-title");
            Label s = new Label("Use Add device to start the inventory.");
            s.getStyleClass().add("page-subtitle");
            Button add = new Button("Add device");
            add.getStyleClass().addAll("button", "btn-primary");
            add.setOnAction(e -> openForm(null));
            empty.getChildren().addAll(t, s, add);
            grid.getChildren().add(empty);
        } else {
            for (Device d : devices) {
                grid.getChildren().add(buildGridCard(d));
            }
        }
        showMode();
    }

    private void build() {
        Label kicker = new Label("Stockroom");
        kicker.getStyleClass().add("page-kicker");
        Label title = new Label("Inventory");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Register devices, attach photos, and track how many are still here.");
        subtitle.getStyleClass().add("page-subtitle");

        searchField.setPromptText("Search name, brand, or serial…");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((o, a, b) -> reload());

        categoryFilter.setPrefWidth(140);
        categoryFilter.setOnAction(e -> reload());

        ToggleGroup mode = new ToggleGroup();
        listBtn.setToggleGroup(mode);
        gridBtn.setToggleGroup(mode);
        gridBtn.setSelected(true);
        listBtn.getStyleClass().addAll("button", "btn-toggle");
        gridBtn.getStyleClass().addAll("button", "btn-toggle");
        listBtn.setOnAction(e -> {
            photoMode = false;
            showMode();
        });
        gridBtn.setOnAction(e -> {
            photoMode = true;
            showMode();
        });

        Button addBtn = new Button("Add device");
        addBtn.getStyleClass().addAll("button", "btn-primary");
        addBtn.setOnAction(e -> openForm(null));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("button", "btn-secondary");
        editBtn.setOnAction(e -> {
            Device selected = table.getSelectionModel().getSelectedItem();
            if (selected == null && photoMode) {
                AlertHelper.warn(window(), "Open a device card, or switch to List and select a row.");
                return;
            }
            if (selected == null) {
                AlertHelper.warn(window(), "Select a device to edit.");
                return;
            }
            openForm(selected);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("button", "btn-danger");
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox modeBox = new HBox(4, gridBtn, listBtn);
        modeBox.getStyleClass().add("toggle-group");

        HBox toolbar = new HBox(10, searchField, categoryFilter, modeBox, addBtn, editBtn, deleteBtn);
        toolbar.getStyleClass().add("toolbar");

        setupTable();
        grid.getStyleClass().add("pos-catalog");
        gridScroll.setContent(grid);
        gridScroll.setFitToWidth(true);
        gridScroll.getStyleClass().add("pos-catalog-scroll");
        gridScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        bodyHost.getChildren().addAll(table, gridScroll);
        VBox.setVgrow(bodyHost, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(gridScroll, Priority.ALWAYS);

        VBox panel = new VBox(12, toolbar, bodyHost);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        root.getChildren().addAll(kicker, title, subtitle, panel);
        VBox.setVgrow(root, Priority.ALWAYS);
        showMode();
    }

    private void showMode() {
        table.setVisible(!photoMode);
        table.setManaged(!photoMode);
        gridScroll.setVisible(photoMode);
        gridScroll.setManaged(photoMode);
        gridBtn.setSelected(photoMode);
        listBtn.setSelected(!photoMode);
    }

    private VBox buildGridCard(Device device) {
        VBox card = new VBox(6);
        card.getStyleClass().add("inv-card");
        card.setPrefWidth(168);
        card.setMinWidth(168);
        card.setPadding(new Insets(8));

        card.getChildren().add(DeviceImages.preview(device.getImagePath(), 152, 100));

        Label name = new Label(device.getDeviceName());
        name.getStyleClass().add("pos-card-name");
        name.setWrapText(true);

        Label meta = new Label(device.getCategory() + "  ·  " + device.getBrand());
        meta.getStyleClass().add("pos-card-meta");

        Label qty = new Label(device.getAvailableQuantity() + " / " + device.getQuantity() + " available");
        qty.getStyleClass().add("pos-card-meta");

        Label status = new Label(device.getStatus());
        if ("Available".equals(device.getStatus())) {
            status.getStyleClass().add("status-available");
        } else if ("Low Stock".equals(device.getStatus())) {
            status.getStyleClass().add("status-low");
        } else {
            status.getStyleClass().add("status-out");
        }

        Button edit = new Button("Edit");
        edit.getStyleClass().addAll("button", "btn-secondary");
        edit.setMaxWidth(Double.MAX_VALUE);
        edit.setOnAction(e -> openForm(device));

        card.getChildren().addAll(name, meta, qty, status, edit);
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openForm(device);
            }
        });
        return card;
    }

    private void setupTable() {
        TableColumn<Device, Device> photoCol = new TableColumn<>("Photo");
        photoCol.setPrefWidth(72);
        photoCol.setMaxWidth(80);
        photoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        photoCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Device device, boolean empty) {
                super.updateItem(device, empty);
                if (empty || device == null) {
                    setGraphic(null);
                } else {
                    setGraphic(DeviceImages.preview(device.getImagePath(), 52, 40));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Device, String> idCol = col("ID", d -> String.valueOf(d.getDeviceId()), 50);
        TableColumn<Device, String> nameCol = col("Device", Device::getDeviceName, 160);
        TableColumn<Device, String> catCol = col("Category", Device::getCategory, 100);
        TableColumn<Device, String> brandCol = col("Brand", Device::getBrand, 100);
        TableColumn<Device, String> serialCol = col("Serial",
                d -> d.getSerialNumber() == null ? "—" : d.getSerialNumber(), 100);
        TableColumn<Device, String> qtyCol = col("Total", d -> String.valueOf(d.getQuantity()), 60);
        TableColumn<Device, String> availCol = col("Available",
                d -> String.valueOf(d.getAvailableQuantity()), 70);
        TableColumn<Device, String> statusCol = col("Status", Device::getStatus, 100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-available", "status-low", "status-out");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("Available".equals(item)) {
                        getStyleClass().add("status-available");
                    } else if ("Low Stock".equals(item)) {
                        getStyleClass().add("status-low");
                    } else {
                        getStyleClass().add("status-out");
                    }
                }
            }
        });
        TableColumn<Device, String> dateCol = col("Added",
                d -> IdGenerator.formatDate(d.getDateAdded()), 110);

        table.getColumns().addAll(photoCol, idCol, nameCol, catCol, brandCol, serialCol,
                qtyCol, availCol, statusCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No devices found."));
        table.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<Device>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openForm(row.getItem());
                }
            });
            return row;
        });
    }

    private TableColumn<Device, String> col(String title, java.util.function.Function<Device, String> mapper, double width) {
        TableColumn<Device, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(mapper.apply(d.getValue())));
        c.setPrefWidth(width);
        return c;
    }

    private void openForm(Device existing) {
        DeviceFormDialog dialog = new DeviceFormDialog(window(), existing);
        dialog.showAndWait().ifPresent(result -> {
            try {
                if (existing == null) {
                    controller.add(result.name(), result.category(), result.brand(),
                            result.serial(), result.quantity(), result.imageSource());
                    AlertHelper.success(window(), "Device saved.");
                } else {
                    controller.update(existing.getDeviceId(), result.name(), result.category(),
                            result.brand(), result.serial(), result.quantity(),
                            result.imageSource(), result.clearImage());
                    AlertHelper.success(window(), "Device updated.");
                }
                shell.refreshAll();
            } catch (Exception ex) {
                AlertHelper.error(window(), ex.getMessage());
            }
        });
    }

    private void deleteSelected() {
        Device selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.warn(window(), photoMode
                    ? "Switch to List, select a row, then Delete."
                    : "Select a device to delete.");
            return;
        }
        if (!AlertHelper.confirm(window(), "Delete device",
                "Delete \"" + selected.getDeviceName() + "\"? This cannot be undone.")) {
            return;
        }
        try {
            controller.delete(selected.getDeviceId());
            AlertHelper.success(window(), "Device deleted.");
            shell.refreshAll();
        } catch (Exception ex) {
            AlertHelper.error(window(), ex.getMessage());
        }
    }

    private javafx.stage.Window window() {
        return root.getScene() != null ? root.getScene().getWindow() : null;
    }
}
