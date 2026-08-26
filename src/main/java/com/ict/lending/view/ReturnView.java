package com.ict.lending.view;

import com.ict.lending.controller.ReturnController;
import com.ict.lending.model.Borrower;
import com.ict.lending.model.Transaction;
import com.ict.lending.service.ExportService;
import com.ict.lending.utils.AlertHelper;
import com.ict.lending.utils.IdGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ReturnView {

    private final VBox root = new VBox(16);
    private final TextField searchField = new TextField();
    private final TableView<Transaction> table = new TableView<>();
    private final ReturnController controller = new ReturnController();
    private final ExportService exportService = new ExportService();
    private final MainShellView shell;

    public ReturnView(MainShellView shell) {
        this.shell = shell;
        build();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        table.getItems().setAll(controller.listActive(searchField.getText()));
    }

    public void applyExternalSearch(String query) {
        searchField.setText(query == null ? "" : query);
        refresh();
    }

    private void build() {
        Label kicker = new Label("Check-in");
        kicker.getStyleClass().add("page-kicker");
        Label title = new Label("Return");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Pick an open borrow, confirm the borrower, then mark it returned.");
        subtitle.getStyleClass().add("page-subtitle");
        root.getChildren().add(0, kicker);

        searchField.setPromptText("Search by transaction ID, borrower, or device…");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((o, a, b) -> refresh());

        Button returnBtn = new Button("Process Return");
        returnBtn.getStyleClass().addAll("button", "btn-primary");
        returnBtn.setOnAction(e -> processReturn());

        HBox toolbar = new HBox(10, searchField, returnBtn);
        toolbar.getStyleClass().add("toolbar");

        TableColumn<Transaction, String> idCol = col("Transaction ID", Transaction::getTransactionId, 140);
        TableColumn<Transaction, String> nameCol = col("Borrower", Transaction::getBorrowerName, 140);
        TableColumn<Transaction, String> deviceCol = col("Device", Transaction::getDeviceName, 150);
        TableColumn<Transaction, String> qtyCol = col("Qty", t -> String.valueOf(t.getQuantity()), 50);
        TableColumn<Transaction, String> dateCol = col("Borrowed",
                t -> IdGenerator.formatDate(t.getBorrowDate()) + " " + IdGenerator.formatTime(t.getBorrowTime()), 170);
        TableColumn<Transaction, String> purposeCol = col("Purpose", Transaction::getPurpose, 160);
        table.getColumns().addAll(idCol, nameCol, deviceCol, qtyCol, dateCol, purposeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No active borrows"));

        VBox panel = new VBox(12, toolbar, table);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(panel, Priority.ALWAYS);
        root.getChildren().addAll(title, subtitle, panel);
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    private TableColumn<Transaction, String> col(String title,
                                                  java.util.function.Function<Transaction, String> mapper,
                                                  double width) {
        TableColumn<Transaction, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(mapper.apply(d.getValue())));
        c.setPrefWidth(width);
        return c;
    }

    private void processReturn() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.warn(window(), "Select an active transaction to return.");
            return;
        }
        BorrowerFormDialog dialog = new BorrowerFormDialog(window(), "Verify Borrower Information", false);
        dialog.showAndWait().ifPresent(borrower -> {
            ReturnConditionDialog conditionDialog =
                    new ReturnConditionDialog(window(), selected.getDeviceName());
            conditionDialog.showAndWait().ifPresent(report -> completeReturn(selected, borrower, report));
        });
    }

    private void completeReturn(Transaction selected, Borrower borrower, String conditionReport) {
        try {
            Transaction updated = controller.returnDevice(
                    selected.getTransactionId(),
                    borrower.getFullName(),
                    borrower.getIdNumber(),
                    borrower.getPosition(),
                    borrower.getGradeLevel(),
                    borrower.getSection(),
                    conditionReport);
            shell.refreshAll();
            boolean print = AlertHelper.confirm(window(), "Return Successful",
                    "Transaction " + updated.getTransactionId() + " marked as returned.\n\nPrint return receipt?");
            if (print) {
                ReceiptPrinterDialog.show(window(), updated, "Return Receipt", exportService);
            } else {
                AlertHelper.success(window(), "Return recorded successfully.");
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
