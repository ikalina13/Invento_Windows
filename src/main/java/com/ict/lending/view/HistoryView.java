package com.ict.lending.view;

import com.ict.lending.controller.HistoryController;
import com.ict.lending.model.Transaction;
import com.ict.lending.service.ExportService;
import com.ict.lending.utils.AlertHelper;
import com.ict.lending.utils.IdGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.nio.file.Path;
import java.util.List;

public class HistoryView {

    private final VBox root = new VBox(16);
    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final TableView<Transaction> table = new TableView<>();
    private final HistoryController controller = new HistoryController();
    private final ExportService exportService = new ExportService();

    public HistoryView() {
        build();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        reload();
    }

    public void applyExternalSearch(String query) {
        searchField.setText(query == null ? "" : query);
        reload();
    }

    private void reload() {
        table.getItems().setAll(controller.list(searchField.getText(), statusFilter.getValue()));
    }

    private void build() {
        Label kicker = new Label("Records");
        kicker.getStyleClass().add("page-kicker");
        Label title = new Label("History");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Search loans, reprint receipts, or export to Excel / PDF.");
        subtitle.getStyleClass().add("page-subtitle");

        searchField.setPromptText("Search ID, borrower, or device…");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((o, a, b) -> reload());

        statusFilter.setItems(FXCollections.observableArrayList("All", "Borrowed", "Returned"));
        statusFilter.setValue("All");
        statusFilter.setPrefWidth(130);
        statusFilter.setOnAction(e -> reload());

        Button printBtn = new Button("Print Receipt");
        printBtn.getStyleClass().addAll("button", "btn-secondary");
        printBtn.setOnAction(e -> printReceipt());

        Button pdfBtn = new Button("Export PDF");
        pdfBtn.getStyleClass().addAll("button", "btn-secondary");
        pdfBtn.setOnAction(e -> exportPdf());

        Button excelBtn = new Button("Export Excel");
        excelBtn.getStyleClass().addAll("button", "btn-primary");
        excelBtn.setOnAction(e -> exportExcel());

        HBox toolbar = new HBox(10, searchField, statusFilter, printBtn, pdfBtn, excelBtn);
        toolbar.getStyleClass().add("toolbar");

        setupTable();

        VBox panel = new VBox(12, toolbar, table);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(panel, Priority.ALWAYS);
        root.getChildren().addAll(kicker, title, subtitle, panel);
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    private void setupTable() {
        table.getColumns().addAll(
                col("Transaction ID", Transaction::getTransactionId, 130),
                col("Borrower", Transaction::getBorrowerName, 130),
                col("Device", Transaction::getDeviceName, 140),
                col("Qty", t -> String.valueOf(t.getQuantity()), 45),
                col("Date Borrowed", t -> IdGenerator.formatDate(t.getBorrowDate()), 110),
                col("Time Borrowed", t -> IdGenerator.formatTime(t.getBorrowTime()), 100),
                col("Date Returned", t -> IdGenerator.formatDate(t.getReturnDate()), 110),
                col("Time Returned", t -> IdGenerator.formatTime(t.getReturnTime()), 100)
        );
        TableColumn<Transaction, String> statusCol = col("Status", Transaction::getStatus, 90);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-borrowed", "status-returned");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("Borrowed".equals(item) ? "status-borrowed" : "status-returned");
                }
            }
        });
        table.getColumns().add(statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No transactions found"));
    }

    private TableColumn<Transaction, String> col(String title,
                                                  java.util.function.Function<Transaction, String> mapper,
                                                  double width) {
        TableColumn<Transaction, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(mapper.apply(d.getValue())));
        c.setPrefWidth(width);
        return c;
    }

    private void printReceipt() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.warn(window(), "Select a transaction to print.");
            return;
        }
        String title = "Returned".equalsIgnoreCase(selected.getStatus())
                ? "Return Receipt"
                : "Borrow Receipt";
        ReceiptPrinterDialog.show(window(), selected, title, exportService);
    }

    private void exportPdf() {
        try {
            List<Transaction> data = table.getItems();
            Path file = exportService.exportPdfReport(data);
            openFile(file);
            AlertHelper.success(window(), "PDF exported to:\n" + file);
        } catch (Exception ex) {
            AlertHelper.error(window(), ex.getMessage());
        }
    }

    private void exportExcel() {
        try {
            List<Transaction> data = table.getItems();
            Path file = exportService.exportExcel(data);
            openFile(file);
            AlertHelper.success(window(), "Excel exported to:\n" + file);
        } catch (Exception ex) {
            AlertHelper.error(window(), ex.getMessage());
        }
    }

    private void openFile(Path file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file.toFile());
            }
        } catch (Exception ignored) {
        }
    }

    private javafx.stage.Window window() {
        return root.getScene() != null ? root.getScene().getWindow() : null;
    }
}
