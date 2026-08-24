package com.ict.lending.utils;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 * Small stroke icons drawn in code — utilitarian, not emoji / icon-pack fluff.
 */
public final class UiIcons {

    private UiIcons() {
    }

    public static Node nav(String key) {
        Node glyph = switch (key) {
            case "Dashboard" -> dashboard();
            case "Inventory" -> boxes();
            case "Booking" -> cart();
            case "Return" -> undo();
            case "History" -> list();
            case "Settings" -> gear();
            default -> dashboard();
        };
        StackPane wrap = new StackPane(glyph);
        wrap.setPrefSize(18, 18);
        wrap.setMinSize(18, 18);
        wrap.setMaxSize(18, 18);
        wrap.getStyleClass().add("nav-icon");
        return wrap;
    }

    private static Node dashboard() {
        Group g = new Group();
        g.getChildren().addAll(
                rect(2, 2, 6, 6),
                rect(10, 2, 6, 6),
                rect(2, 10, 6, 6),
                rect(10, 10, 6, 6)
        );
        return scaled(g);
    }

    private static Node boxes() {
        Group g = new Group();
        Rectangle a = rect(3, 5, 12, 10);
        Line mid = line(3, 10, 15, 10);
        g.getChildren().addAll(a, mid);
        return scaled(g);
    }

    private static Node cart() {
        Group g = new Group();
        SVGPath basket = new SVGPath();
        basket.setContent("M2 4 H4 L6 14 H14 L16 6 H5");
        styleStroke(basket);
        Circle w1 = new Circle(7, 16.5, 1.4);
        Circle w2 = new Circle(13, 16.5, 1.4);
        styleStroke(w1);
        styleStroke(w2);
        g.getChildren().addAll(basket, w1, w2);
        return scaled(g);
    }

    private static Node undo() {
        Group g = new Group();
        SVGPath arc = new SVGPath();
        arc.setContent("M14 8 A5 5 0 1 0 8 14");
        styleStroke(arc);
        Line a = line(14, 8, 14, 4);
        Line b = line(14, 8, 10, 8);
        g.getChildren().addAll(arc, a, b);
        return scaled(g);
    }

    private static Node list() {
        Group g = new Group();
        g.getChildren().addAll(
                line(5, 5, 15, 5),
                line(5, 9.5, 15, 9.5),
                line(5, 14, 15, 14),
                new Circle(3, 5, 1),
                new Circle(3, 9.5, 1),
                new Circle(3, 14, 1)
        );
        g.getChildren().forEach(n -> {
            if (n instanceof Circle c) {
                c.setFill(Color.WHITE);
                c.setStroke(null);
            } else {
                styleStroke(n);
            }
        });
        return scaled(g);
    }

    private static Node gear() {
        Group g = new Group();
        Circle ring = new Circle(9, 9, 4.5);
        styleStroke(ring);
        Circle hub = new Circle(9, 9, 1.6);
        hub.setFill(Color.WHITE);
        hub.setStroke(null);
        g.getChildren().addAll(
                line(9, 2, 9, 4.2),
                line(9, 13.8, 9, 16),
                line(2, 9, 4.2, 9),
                line(13.8, 9, 16, 9),
                ring,
                hub
        );
        g.getChildren().stream()
                .filter(n -> n instanceof Line)
                .forEach(UiIcons::styleStroke);
        return scaled(g);
    }

    private static Rectangle rect(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(x, y, w, h);
        styleStroke(r);
        return r;
    }

    private static Line line(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1, y1, x2, y2);
        styleStroke(l);
        return l;
    }

    private static void styleStroke(Node n) {
        if (n instanceof javafx.scene.shape.Shape s) {
            s.setFill(Color.TRANSPARENT);
            s.setStroke(Color.WHITE);
            s.setStrokeWidth(1.4);
        }
    }

    private static Node scaled(Group g) {
        g.setScaleX(0.95);
        g.setScaleY(0.95);
        return g;
    }
}
