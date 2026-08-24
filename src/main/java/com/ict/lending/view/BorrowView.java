package com.ict.lending.view;

import com.ict.lending.controller.BorrowController;
import com.ict.lending.model.Borrower;
import com.ict.lending.model.Device;
import com.ict.lending.model.Transaction;
import com.ict.lending.service.BorrowService;
import com.ict.lending.service.ExportService;
import com.ict.lending.utils.AlertHelper;
import com.ict.lending.utils.DeviceImages;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Counter-style booking: photo tiles + slip (no payment).
 */
public class BorrowView {

    private final VBox root = new VBox(12);
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final FlowPane catalog = new FlowPane(10, 10);
    private final VBox basketList = new VBox(6);
    private final Label basketCountLabel = new Label("Empty");
    private final BorrowController controller = new BorrowController();
    private final ExportService exportService = new ExportService();
    private final MainShellView shell;

    private final Map<Integer, Integer> basket = new LinkedHashMap<>();
    private final Map<Integer, Device> basketDevices = new LinkedHashMap<>();
    private List<Device> currentCatalog = List.of();

    public BorrowView(MainShellView shell) {
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
        reloadCatalog();
        List<Integer> remove = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : basket.entrySet()) {
            Device live = currentCatalog.stream()
                    .filter(d -> d.getDeviceId() == e.getKey())
                    .findFirst()
                    .orElse(null);
            if (live == null || live.getAvailableQuantity() <= 0) {
                remove.add(e.getKey());
            } else if (e.getValue() > live.getAvailableQuantity()) {
                basket.put(e.getKey(), live.getAvailableQuantity());
                basketDevices.put(e.getKey(), live);
            } else {
                basketDevices.put(e.getKey(), live);
            }
        }
        remove.forEach(id -> {
            basket.remove(id);
            basketDevices.remove(id);
        });
        renderBasket();
    }

    public void applyExternalSearch(String query) {
        searchField.setText(query == null ? "" : query);
        reloadCatalog();
    }

    private void reloadCatalog() {
        currentCatalog = controller.catalogDevices(searchField.getText(), categoryFilter.getValue());
        catalog.getChildren().clear();
        if (currentCatalog.isEmpty()) {
            VBox empty = new VBox(6);
            empty.getStyleClass().add("empty-hint");
            Label t = new Label("Nothing to show");
            t.getStyleClass().add("section-title");
            Label s = new Label("Try another search, or add devices under Inventory.");
            s.getStyleClass().add("page-subtitle");
            empty.getChildren().addAll(t, s);
            catalog.getChildren().add(empty);
            return;
        }
        for (Device device : currentCatalog) {
            catalog.getChildren().add(buildCard(device));
        }
    }

    private void build() {
        Label kicker = new Label("Front desk");
        kicker.getStyleClass().add("page-kicker");
        Label title = new Label("Booking");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Pick devices, fill the borrow slip, then print a receipt.");
        subtitle.getStyleClass().add("page-subtitle");

        searchField.setPromptText("Find a device…");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((o, a, b) -> reloadCatalog());
        categoryFilter.setPrefWidth(150);
        categoryFilter.setOnAction(e -> reloadCatalog());

        HBox toolbar = new HBox(10, searchField, categoryFilter);
        toolbar.getStyleClass().add("toolbar");

        catalog.getStyleClass().add("pos-catalog");
        catalog.setPadding(new Insets(2));

        ScrollPane scroll = new ScrollPane(catalog);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("pos-catalog-scroll");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Label catalogTitle = new Label("Available devices");
        catalogTitle.getStyleClass().add("section-title");

        VBox left = new VBox(10, catalogTitle, toolbar, scroll);
        left.getStyleClass().add("panel");
        HBox.setHgrow(left, Priority.ALWAYS);
        VBox.setVgrow(left, Priority.ALWAYS);

        VBox right = buildBasketPanel();
        right.setPrefWidth(292);
        right.setMinWidth(270);
        right.setMaxWidth(320);

        HBox body = new HBox(12, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        VBox.setVgrow(body, Priority.ALWAYS);

        root.getChildren().addAll(kicker, title, subtitle, body);
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    private VBox buildBasketPanel() {
        Label heading = new Label("Borrow slip");
        heading.getStyleClass().add("section-title");
        basketCountLabel.getStyleClass().add("page-subtitle");

        basketList.getStyleClass().add("pos-basket-list");
        ScrollPane basketScroll = new ScrollPane(basketList);
        basketScroll.setFitToWidth(true);
        basketScroll.getStyleClass().add("pos-basket-scroll");
        VBox.setVgrow(basketScroll, Priority.ALWAYS);

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().addAll("button", "btn-secondary");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            basket.clear();
            basketDevices.clear();
            renderBasket();
        });

        Button checkoutBtn = new Button("Record borrow");
        checkoutBtn.getStyleClass().addAll("button", "btn-primary");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> checkout());

        VBox panel = new VBox(10, heading, basketCountLabel, basketScroll, clearBtn, checkoutBtn);
        panel.getStyleClass().addAll("panel", "pos-basket-panel");
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildCard(Device device) {
        VBox card = new VBox(6);
        card.getStyleClass().add("pos-device-card");
        boolean out = device.getAvailableQuantity() <= 0;
        if (out) {
            card.getStyleClass().add("pos-device-card-oos");
        }
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(158);
        card.setMinWidth(158);
        card.setMaxWidth(168);

        card.getChildren().add(DeviceImages.preview(device.getImagePath(), 146, 96));

        Label name = new Label(device.getDeviceName());
        name.getStyleClass().add("pos-card-name");
        name.setWrapText(true);
        name.setMaxWidth(146);

        Label meta = new Label(device.getCategory() + "  ·  " + device.getBrand());
        meta.getStyleClass().add("pos-card-meta");
        meta.setWrapText(true);

        String stockText;
        String stockClass;
        if (out) {
            stockText = "None left";
            stockClass = "pos-stock-out";
        } else if (device.getAvailableQuantity() <= Math.max(1, device.getQuantity() / 4)) {
            stockText = device.getAvailableQuantity() + " left";
            stockClass = "pos-stock-low";
        } else {
            stockText = device.getAvailableQuantity() + " left";
            stockClass = "pos-stock-ok";
        }
        Label stock = new Label(stockText);
        stock.getStyleClass().add(stockClass);

        Button addBtn = new Button(out ? "Unavailable" : "Add to slip");
        addBtn.getStyleClass().addAll("button", out ? "btn-secondary" : "btn-primary");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setDisable(out);
        addBtn.setOnAction(e -> addToBasket(device));

        card.getChildren().addAll(name, meta, stock, addBtn);
        if (!out) {
            card.setOnMouseClicked(e -> {
                if (!(e.getTarget() instanceof Button)) {
                    addToBasket(device);
                }
            });
        }
        return card;
    }

    private void addToBasket(Device device) {
        if (device.getAvailableQuantity() <= 0) {
            return;
        }
        int current = basket.getOrDefault(device.getDeviceId(), 0);
        if (current >= device.getAvailableQuantity()) {
            AlertHelper.warn(window(), "No more units of " + device.getDeviceName() + ".");
            return;
        }
        basket.put(device.getDeviceId(), current + 1);
        basketDevices.put(device.getDeviceId(), device);
        renderBasket();
    }

    private void renderBasket() {
        basketList.getChildren().clear();
        int totalUnits = basket.values().stream().mapToInt(Integer::intValue).sum();
        basketCountLabel.setText(totalUnits == 0
                ? "Empty"
                : totalUnits + (totalUnits == 1 ? " item" : " items"));

        if (basket.isEmpty()) {
            Label empty = new Label("Select a device on the left to start a borrow slip.");
            empty.getStyleClass().add("pos-basket-empty");
            empty.setWrapText(true);
            basketList.getChildren().add(empty);
            return;
        }

        for (Map.Entry<Integer, Integer> entry : basket.entrySet()) {
            Device device = basketDevices.get(entry.getKey());
            if (device == null) {
                continue;
            }
            basketList.getChildren().add(buildBasketRow(device, entry.getValue()));
        }
    }

    private HBox buildBasketRow(Device device, int qty) {
        var thumb = DeviceImages.preview(device.getImagePath(), 40, 32);
        Label name = new Label(device.getDeviceName());
        name.getStyleClass().add("pos-basket-name");
        name.setWrapText(true);

        Spinner<Integer> spinner = new Spinner<>();
        int max = Math.max(1, device.getAvailableQuantity());
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, qty));
        spinner.setEditable(true);
        spinner.setPrefWidth(70);
        spinner.valueProperty().addListener((o, a, b) -> {
            if (b != null) {
                basket.put(device.getDeviceId(), b);
                int total = basket.values().stream().mapToInt(Integer::intValue).sum();
                basketCountLabel.setText(total + (total == 1 ? " item" : " items"));
            }
        });

        Button remove = new Button("Remove");
        remove.getStyleClass().addAll("button", "btn-ghost");
        remove.setOnAction(e -> {
            basket.remove(device.getDeviceId());
            basketDevices.remove(device.getDeviceId());
            renderBasket();
        });

        VBox text = new VBox(4, name, spinner, remove);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(8, thumb, text);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("pos-basket-row");
        row.setPadding(new Insets(8));
        return row;
    }

    private void checkout() {
        if (basket.isEmpty()) {
            AlertHelper.warn(window(), "Add at least one device to the borrow slip.");
            return;
        }
        BorrowerFormDialog dialog = new BorrowerFormDialog(window(), "Borrower details", true);
        dialog.showAndWait().ifPresent(this::completeBooking);
    }

    private void completeBooking(Borrower borrower) {
        try {
            List<BorrowService.BasketLine> lines = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : basket.entrySet()) {
                lines.add(new BorrowService.BasketLine(e.getKey(), e.getValue()));
            }
            List<Transaction> txns = controller.borrowBasket(lines, borrower);
            basket.clear();
            basketDevices.clear();
            shell.refreshAll();

            String summary = txns.size() == 1
                    ? "Saved as " + txns.get(0).getTransactionId() + "."
                    : txns.size() + " items booked for " + borrower.getFullName() + ".";
            boolean print = AlertHelper.confirm(window(), "Borrow recorded",
                    summary + "\n\nPrint receipt?");
            if (print) {
                ReceiptPrinterDialog.show(window(), txns, "Borrow receipt", exportService);
            } else {
                AlertHelper.success(window(), summary);
            }
            refresh();
        } catch (Exception ex) {
            AlertHelper.error(window(), ex.getMessage());
        }
    }

    private javafx.stage.Window window() {
        return root.getScene() != null ? root.getScene().getWindow() : null;
    }
}
