package com.ict.lending.view;

import com.ict.lending.model.Transaction;
import com.ict.lending.service.ExportService;
import com.ict.lending.utils.IdGenerator;
import com.ict.lending.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Cashier-style thermal receipt print animation.
 * Paper feeds out of a printer slot with rounded corners.
 */
public final class ReceiptPrinterDialog {

    private static final double FEED_DELAY_MS = 320;
    private static final double FEED_DURATION_MS = 2100;
    private static final double SLOT_WIDTH = 328;
    private static final double SLOT_HEIGHT = 500;
    private static final double PAPER_RADIUS = 16;

    private ReceiptPrinterDialog() {
    }

    public static void show(Window owner, Transaction txn, String title, ExportService exportService) {
        show(owner, List.of(txn), title, exportService);
    }

    public static void show(Window owner, List<Transaction> txns, String title, ExportService exportService) {
        if (txns == null || txns.isEmpty()) {
            return;
        }
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(title);

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("receipt-print-overlay");
        overlay.setOpacity(0);

        VBox dialog = new VBox(16);
        dialog.getStyleClass().add("receipt-print-dialog");
        dialog.setAlignment(Pos.TOP_CENTER);
        dialog.setMaxWidth(400);
        dialog.setScaleX(0.94);
        dialog.setScaleY(0.94);

        Label heading = new Label(title);
        heading.getStyleClass().add("receipt-print-heading");

        Label status = new Label("Printing receipt…");
        status.getStyleClass().add("receipt-print-status");

        StackPane printerSlot = new StackPane();
        printerSlot.getStyleClass().add("receipt-printer-slot");
        printerSlot.setPrefSize(SLOT_WIDTH, SLOT_HEIGHT);
        printerSlot.setMinSize(SLOT_WIDTH, SLOT_HEIGHT);
        printerSlot.setMaxSize(SLOT_WIDTH, SLOT_HEIGHT);

        // Soft rounded clip for the whole printer bay
        Rectangle slotClip = new Rectangle(SLOT_WIDTH, SLOT_HEIGHT);
        slotClip.setArcWidth(28);
        slotClip.setArcHeight(28);
        printerSlot.setClip(slotClip);

        Region throat = new Region();
        throat.getStyleClass().add("receipt-printer-throat");
        throat.setPrefHeight(22);
        throat.setMaxHeight(22);
        throat.setMinHeight(22);
        StackPane.setAlignment(throat, Pos.TOP_CENTER);
        throat.setMaxWidth(SLOT_WIDTH - 20);

        StackPane feedWindow = new StackPane();
        feedWindow.getStyleClass().add("receipt-printer-feed-window");
        feedWindow.setPrefSize(SLOT_WIDTH - 20, SLOT_HEIGHT - 22);
        feedWindow.setMaxSize(SLOT_WIDTH - 20, SLOT_HEIGHT - 22);
        StackPane.setAlignment(feedWindow, Pos.TOP_CENTER);
        feedWindow.setTranslateY(22);

        Rectangle feedClip = new Rectangle(SLOT_WIDTH - 20, SLOT_HEIGHT - 22);
        feedClip.setArcWidth(18);
        feedClip.setArcHeight(18);
        feedWindow.setClip(feedClip);

        VBox paper = buildReceiptPaper(txns);
        paper.getStyleClass().add("thermal-receipt-paper");
        paper.setMaxWidth(SLOT_WIDTH - 36);
        paper.setPrefWidth(SLOT_WIDTH - 36);

        // Round the paper itself so corners stay soft while feeding
        Rectangle paperClip = new Rectangle();
        paperClip.widthProperty().bind(paper.widthProperty());
        paperClip.heightProperty().bind(paper.heightProperty());
        paperClip.setArcWidth(PAPER_RADIUS * 2);
        paperClip.setArcHeight(PAPER_RADIUS * 2);
        paper.setClip(paperClip);

        paper.setTranslateY(-(SLOT_HEIGHT));
        feedWindow.getChildren().add(paper);
        printerSlot.getChildren().addAll(feedWindow, throat);

        Button closeBtn = new Button("Done");
        closeBtn.getStyleClass().addAll("button", "btn-primary", "receipt-action-done");
        closeBtn.setDisable(true);
        closeBtn.setPrefWidth(150);
        closeBtn.setPrefHeight(40);
        closeBtn.setOnAction(e -> stage.close());

        Transaction primary = txns.get(0);
        Button savePdfBtn = new Button("Save PDF");
        savePdfBtn.getStyleClass().addAll("button", "btn-secondary", "receipt-action-save");
        savePdfBtn.setDisable(true);
        savePdfBtn.setPrefHeight(40);
        savePdfBtn.setOnAction(e -> {
            try {
                Path path = exportService.printReceipt(primary);
                status.setText("PDF saved: " + path.getFileName());
            } catch (Exception ex) {
                status.setText("Could not save PDF.");
            }
        });

        HBox actions = new HBox(12, savePdfBtn, closeBtn);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(4, 0, 0, 0));

        dialog.getChildren().addAll(heading, status, printerSlot, actions);
        overlay.getChildren().add(dialog);

        javafx.scene.Scene scene = new javafx.scene.Scene(overlay, 460, 680);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        ThemeManager.apply(scene);
        stage.setScene(scene);

        stage.setOnShown(e -> {
            playDialogIn(overlay, dialog);
            playFeed(paper, status, closeBtn, savePdfBtn, exportService, primary);
        });
        stage.show();
    }

    private static void playDialogIn(StackPane overlay, VBox dialog) {
        FadeTransition fade = new FadeTransition(Duration.millis(280), overlay);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition pop = new ScaleTransition(Duration.millis(360), dialog);
        pop.setFromX(0.94);
        pop.setFromY(0.94);
        pop.setToX(1);
        pop.setToY(1);
        pop.setInterpolator(Interpolator.SPLINE(0.2, 0.9, 0.2, 1.0));

        javafx.animation.ParallelTransition in = new javafx.animation.ParallelTransition(fade, pop);
        in.play();
    }

    private static void playFeed(VBox paper, Label status, Button closeBtn, Button savePdfBtn,
                                 ExportService exportService, Transaction txn) {
        paper.applyCss();
        paper.layout();
        double paperHeight = Math.max(paper.prefHeight(-1), SLOT_HEIGHT * 0.82);
        paper.setTranslateY(-paperHeight - 40);

        List<javafx.scene.Node> lines = new ArrayList<>();
        collectLines(paper, lines);
        for (javafx.scene.Node n : lines) {
            n.setOpacity(0);
        }

        PauseTransition delay = new PauseTransition(Duration.millis(FEED_DELAY_MS));

        // Main feed — ease out as paper exits the throat
        TranslateTransition feed = new TranslateTransition(Duration.millis(FEED_DURATION_MS), paper);
        feed.setToY(14);
        feed.setInterpolator(Interpolator.SPLINE(0.18, 0.7, 0.2, 1.0));

        // Soft settle bounce at the end
        TranslateTransition settle = new TranslateTransition(Duration.millis(220), paper);
        settle.setByY(-6);
        settle.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition settleBack = new TranslateTransition(Duration.millis(180), paper);
        settleBack.setByY(6);
        settleBack.setInterpolator(Interpolator.EASE_IN);

        SequentialTransition seq = new SequentialTransition(delay, feed, settle, settleBack);
        seq.setOnFinished(ev -> {
            status.setText("Receipt printed");
            closeBtn.setDisable(false);
            savePdfBtn.setDisable(false);
            for (javafx.scene.Node n : lines) {
                n.setOpacity(1);
            }
            try {
                exportService.printReceipt(txn);
            } catch (Exception ignored) {
            }
        });

        // Stagger line reveal while paper feeds
        PauseTransition lineStart = new PauseTransition(Duration.millis(FEED_DELAY_MS + 160));
        lineStart.setOnFinished(ev -> {
            SequentialTransition lineSeq = new SequentialTransition();
            for (javafx.scene.Node n : lines) {
                FadeTransition ft = new FadeTransition(Duration.millis(140), n);
                ft.setFromValue(0);
                ft.setToValue(1);
                lineSeq.getChildren().add(ft);
            }
            lineSeq.play();
        });

        seq.play();
        lineStart.play();
    }

    private static void collectLines(javafx.scene.Parent parent, List<javafx.scene.Node> out) {
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (child.getStyleClass().contains("thermal-receipt-line")
                    || child.getStyleClass().contains("thermal-receipt-rule")) {
                out.add(child);
            } else if (child instanceof javafx.scene.Parent p) {
                collectLines(p, out);
            }
        }
    }

    private static VBox buildReceiptPaper(List<Transaction> txns) {
        Transaction first = txns.get(0);
        VBox paper = new VBox(8);
        paper.setPadding(new Insets(20, 18, 22, 18));
        paper.setAlignment(Pos.TOP_CENTER);

        Label store = label("Invento", "thermal-receipt-title");
        Label tag = label("BORROW · TRACK · RETURN", "thermal-receipt-muted");
        boolean returned = txns.stream().allMatch(t -> "Returned".equalsIgnoreCase(t.getStatus()));
        Label type = label(returned ? "RETURN RECEIPT" : "BOOKING RECEIPT", "thermal-receipt-type");

        paper.getChildren().addAll(store, tag, type, rule());

        paper.getChildren().addAll(
                row("Txn ID", nullSafe(first.getTransactionId())
                        + (txns.size() > 1 ? " (+" + (txns.size() - 1) + ")" : "")),
                row("Status", nullSafe(first.getStatus())),
                rule(),
                row("Borrower", nullSafe(first.getBorrowerName())),
                row("Position", nullSafe(first.getPosition())),
                row("Grade/Dept", nullSafe(first.getGradeLevel())),
                row("Section", nullSafe(first.getSection())),
                row("Purpose", nullSafe(first.getPurpose())),
                rule()
        );

        for (Transaction txn : txns) {
            paper.getChildren().addAll(
                    row("Device", nullSafe(txn.getDeviceName())),
                    row("Quantity", String.valueOf(txn.getQuantity()))
            );
            if (txns.size() > 1) {
                paper.getChildren().add(row("Ref", nullSafe(txn.getTransactionId())));
            }
        }

        paper.getChildren().addAll(
                rule(),
                row("Borrowed", IdGenerator.formatDate(first.getBorrowDate())
                        + " " + IdGenerator.formatTime(first.getBorrowTime())),
                row("Returned", IdGenerator.formatDate(first.getReturnDate())
                        + " " + IdGenerator.formatTime(first.getReturnTime())),
                rule(),
                label("Keep this copy for laboratory records.", "thermal-receipt-footer"),
                label("*** Invento ***", "thermal-receipt-muted")
        );

        Region tear = new Region();
        tear.getStyleClass().add("thermal-receipt-tear");
        tear.setPrefHeight(12);
        tear.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(tear, Priority.NEVER);
        paper.getChildren().add(tear);

        return paper;
    }

    private static HBox row(String key, String value) {
        Label k = label(key, "thermal-receipt-key");
        Label v = label(value == null || value.isBlank() ? "—" : value, "thermal-receipt-value");
        v.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(6, k, spacer, v);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("thermal-receipt-line");
        return row;
    }

    private static Region rule() {
        Region r = new Region();
        r.getStyleClass().add("thermal-receipt-rule");
        r.setPrefHeight(1);
        r.setMaxWidth(Double.MAX_VALUE);
        return r;
    }

    private static Label label(String text, String style) {
        Label l = new Label(text);
        l.getStyleClass().add(style);
        l.getStyleClass().add("thermal-receipt-line");
        return l;
    }

    private static String nullSafe(String v) {
        return v == null ? "" : v;
    }
}
