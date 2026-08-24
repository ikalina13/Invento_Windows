package com.ict.lending.view;

import com.ict.lending.controller.DashboardController;
import com.ict.lending.model.DashboardStats;
import com.ict.lending.model.Transaction;
import com.ict.lending.utils.IdGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardView {

    private final VBox root = new VBox(14);
    private final Label greetingLabel = new Label();
    private final Label dateLabel = new Label();
    private final Label totalValue = new Label("0");
    private final Label borrowedValue = new Label("0");
    private final Label returnedValue = new Label("0");
    private final TableView<Transaction> recentTable = new TableView<>();
    private final DashboardController controller = new DashboardController();

    public DashboardView() {
        build();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        DashboardStats stats = controller.loadStats();
        totalValue.setText(String.valueOf(stats.getTotalDevices()));
        borrowedValue.setText(String.valueOf(stats.getBorrowedDevices()));
        returnedValue.setText(String.valueOf(stats.getReturnedToday()));
        recentTable.getItems().setAll(controller.recent());
        greetingLabel.setText(timeGreeting());
        dateLabel.setText(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy  ·  h:mm a")));
    }

    private void build() {
        root.getChildren().addAll(buildHeader(), buildKpis(), buildRecentPanel());
        VBox.setVgrow(root.getChildren().get(2), Priority.ALWAYS);
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    private HBox buildHeader() {
        ImageView logo = new ImageView(new Image(
                getClass().getResourceAsStream("/icons/invento-logo.png")));
        logo.setFitWidth(40);
        logo.setFitHeight(40);
        logo.setPreserveRatio(true);

        greetingLabel.getStyleClass().add("page-title");
        dateLabel.getStyleClass().add("page-subtitle");
        Label place = new Label("ICT Laboratory desk");
        place.getStyleClass().add("page-kicker");

        VBox text = new VBox(2, place, greetingLabel, dateLabel);
        HBox header = new HBox(12, logo, text);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(2, 0, 4, 0));
        return header;
    }

    private HBox buildKpis() {
        HBox kpiRow = new HBox(12);
        kpiRow.getStyleClass().add("kpi-row");
        VBox a = kpi("Devices in stock list", totalValue);
        VBox b = kpi("Units currently borrowed", borrowedValue);
        VBox c = kpi("Returned today", returnedValue);
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        HBox.setHgrow(c, Priority.ALWAYS);
        kpiRow.getChildren().addAll(a, b, c);
        return kpiRow;
    }

    private VBox buildRecentPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        Label section = new Label("Recent activity");
        section.getStyleClass().add("section-title");
        setupTable();
        VBox.setVgrow(recentTable, Priority.ALWAYS);
        panel.getChildren().addAll(section, recentTable);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox kpi(String label, Label value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");
        Label l = new Label(label);
        l.getStyleClass().add("kpi-label");
        value.getStyleClass().add("kpi-value");
        card.getChildren().addAll(l, value);
        return card;
    }

    private void setupTable() {
        TableColumn<Transaction, String> idCol = new TableColumn<>("Txn ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionId()));
        idCol.setPrefWidth(140);

        TableColumn<Transaction, String> nameCol = new TableColumn<>("Borrower");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowerName()));
        nameCol.setPrefWidth(150);

        TableColumn<Transaction, String> deviceCol = new TableColumn<>("Device");
        deviceCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeviceName()));
        deviceCol.setPrefWidth(160);

        TableColumn<Transaction, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        qtyCol.setPrefWidth(50);

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Borrowed");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                IdGenerator.formatDate(c.getValue().getBorrowDate()) + " "
                        + IdGenerator.formatTime(c.getValue().getBorrowTime())));
        dateCol.setPrefWidth(180);

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        recentTable.getColumns().addAll(idCol, nameCol, deviceCol, qtyCol, dateCol, statusCol);
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        recentTable.setPlaceholder(new Label("No loans recorded yet."));
    }

    private static String timeGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 18) {
            return "Good afternoon";
        }
        return "Good evening";
    }
}
