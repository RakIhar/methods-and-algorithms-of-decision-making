package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

public class PotentialMethodGUI extends Application {
    private PotentialMethod potentialMethod;
    private List<Data.Vector> trainingSet;
    private List<Data.Vector> testSet;

    private final Canvas canvasLeft = new Canvas(600, 600);
    private final Canvas canvasRight = new Canvas(600, 600);

    private static final double X_MIN = -5;
    private static final double X_MAX = 5;
    private static final double Y_MIN = -5;
    private static final double Y_MAX = 5;

    @Override
    public void start(Stage stage) {
        initializeTrainingSet();
        initializeTestSet();

        potentialMethod = new PotentialMethod();
        potentialMethod.trainFull(trainingSet);

        VBox root = createGUI();

        Scene scene = new Scene(root, 1300, 750);
        stage.setTitle("Метод потенциалов");
        stage.setScene(scene);
        stage.show();

        draw();
    }

    private void initializeTrainingSet() {
        trainingSet = List.of(
                new Data.Vector(-1, 0, Data.ClassLabel.C1),
                new Data.Vector(1, 1,  Data.ClassLabel.C1),
                new Data.Vector(2, 0,  Data.ClassLabel.C2),
                new Data.Vector(1, -2, Data.ClassLabel.C2));
    }

    private void initializeTestSet() {
        testSet = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            testSet.add(Data.Vector.randomVector(X_MIN, X_MAX));
        }
    }

    private VBox createGUI() {

        Label infoLabel = new Label(String.format(
                "Тестовая выборка: %d случайных векторов",
                testSet.size()
        ));

        HBox canvasesBox = new HBox(20, canvasLeft, canvasRight);
        canvasesBox.setAlignment(Pos.CENTER);
        canvasesBox.setStyle("-fx-padding: 10;");

        VBox legendBox = createLegend();

        HBox buttonsBox = createButtons();

        Label equationLabel = new Label(potentialMethod.getSeparatingFunctionEquation());


        VBox root = new VBox(10, infoLabel, canvasesBox,
                legendBox, buttonsBox, equationLabel);

        root.setAlignment(Pos.CENTER);

        return root;
    }

    private VBox createLegend() {
        VBox legend = new VBox(5);
        legend.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 5;");
        legend.setAlignment(Pos.CENTER_LEFT);

        Label legendTitle = new Label("Легенда:");
        legendTitle.setStyle("-fx-font-weight: bold;");

        HBox c1Box = new HBox(10);
        Rectangle c1Rect = new Rectangle(15, 15, Color.GREEN);
        Label c1Label = new Label("Класс C1 (положительные)");
        c1Label.setTextFill(Color.GREEN);
        c1Box.getChildren().addAll(c1Rect, c1Label);

        HBox c2Box = new HBox(10);
        Rectangle c2Rect = new Rectangle(15, 15, Color.RED);
        Label c2Label = new Label("Класс C2 (отрицательные)");
        c2Label.setTextFill(Color.RED);
        c2Box.getChildren().addAll(c2Rect, c2Label);

        HBox boundaryBox = new HBox(10);
        Rectangle boundaryLine = new Rectangle(40, 2, Color.BLUE);
        Label boundaryLabel = new Label("Разделяющая граница");
        boundaryLabel.setTextFill(Color.BLUE);
        boundaryBox.getChildren().addAll(boundaryLine, boundaryLabel);

        legend.getChildren().addAll(legendTitle, c1Box, c2Box, boundaryBox);

        return legend;
    }

    private HBox createButtons() {
        Button newTestBtn = new Button("Новая тестовая выборка (1000 точек)");
        newTestBtn.setOnAction(e -> {
            initializeTestSet();
            draw();
        });

        Button classifyBtn = new Button("Классифицировать тестовые точки");
        classifyBtn.setOnAction(e -> {
            for (Data.Vector vec : testSet) {
                vec.classLabel(potentialMethod.classify(vec));
            }
            draw();
        });

        HBox buttonsBox = new HBox(15, newTestBtn, classifyBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        return buttonsBox;
    }

    private void draw() {
        drawLeftCanvas();
        drawRightCanvas();
    }

    private void drawLeftCanvas() {
        GraphicsContext gc = canvasLeft.getGraphicsContext2D();
        double w = canvasLeft.getWidth();
        double h = canvasLeft.getHeight();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        drawGrid(gc, w, h);

        for (Data.Vector v : trainingSet) {
            double[] screen = toScreen(v.x(), v.y(), w, h);

            if (v.classLabel() == Data.ClassLabel.C1) {
                gc.setFill(Color.GREEN);
                gc.fillRect(screen[0] - 5, screen[1] - 5, 10, 10);
                gc.setFill(Color.DARKGREEN);
                gc.fillRect(screen[0] - 2, screen[1] - 2, 4, 4);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(screen[0] - 5, screen[1] - 5, 10, 10);
                gc.setFill(Color.DARKRED);
                gc.fillOval(screen[0] - 2, screen[1] - 2, 4, 4);
            }

            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.fillText(String.format("(%.1f,%.1f)", v.x(), v.y()), screen[0] + 8, screen[1] - 5);
        }

        drawAxes(gc, w, h);
    }

    private void drawRightCanvas() {
        GraphicsContext gc = canvasRight.getGraphicsContext2D();
        double w = canvasRight.getWidth();
        double h = canvasRight.getHeight();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        drawGrid(gc, w, h);

        drawClassificationRegions(gc, w, h);

        drawBoundary(gc, w, h);

        for (Data.Vector v : testSet) {
            double[] screen = toScreen(v.x(), v.y(), w, h);
            Data.ClassLabel classification = v.classLabel;

            if (classification == Data.ClassLabel.C1) {
                gc.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.5));
                gc.fillRect(screen[0] - 2, screen[1] - 2, 4, 4);
            } else if (classification == Data.ClassLabel.C2) {
                gc.setFill(Color.RED.deriveColor(0, 1, 1, 0.5));
                gc.fillOval(screen[0] - 2, screen[1] - 2, 4, 4);
            } else {
                gc.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.5));
                gc.fillRect(screen[0] - 2, screen[1] - 2, 4, 4);
            }
        }

        drawAxes(gc, w, h);
    }

    private void drawGrid(GraphicsContext gc, double w, double h) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        for (double x = -5; x <= 5; x += 1) {
            double[] screen = toScreen(x, 0, w, h);
            gc.strokeLine(screen[0], 0, screen[0], h);
        }

        for (double y = -5; y <= 5; y += 1) {
            double[] screen = toScreen(0, y, w, h);
            gc.strokeLine(0, screen[1], w, screen[1]);
        }
    }

    private void drawAxes(GraphicsContext gc, double w, double h) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        double[] origin = toScreen(0, 0, w, h);
        gc.strokeLine(0, origin[1], w, origin[1]);

        gc.strokeLine(origin[0], 0, origin[0], h);

        gc.setFill(Color.BLACK);

        gc.fillPolygon(new double[]{w, w-10, w-10}, new double[]{origin[1], origin[1]-5, origin[1]+5}, 3);

        gc.fillPolygon(new double[]{origin[0], origin[0]-5, origin[0]+5}, new double[]{0, 10, 10}, 3);

        gc.fillText("X", w-15, origin[1]-5);
        gc.fillText("Y", origin[0]+5, 15);

        gc.setFont(javafx.scene.text.Font.font(10));
        for (int i = -5; i <= 5; i++) {
            if (i != 0) {
                double[] xPoint = toScreen(i, 0, w, h);
                double[] yPoint = toScreen(0, i, w, h);
                gc.fillText(String.valueOf(i), xPoint[0]-3, origin[1]+15);
                gc.fillText(String.valueOf(i), origin[0]+5, yPoint[1]+3);
            }
        }

        gc.fillText("0", origin[0]+5, origin[1]+15);
    }

    private void drawClassificationRegions(GraphicsContext gc, double w, double h) {
        for (int px = 0; px < w; px += 10) {
            for (int py = 0; py < h; py += 10) {
                double x = fromScreenX(px, w);
                double y = fromScreenY(py, h);

                if (x >= X_MIN && x <= X_MAX && y >= Y_MIN && y <= Y_MAX) {
                    Data.Vector point = new Data.Vector(x, y);
                    Data.ClassLabel classification = potentialMethod.classify(point);

                    if (classification == Data.ClassLabel.C1) {
                        gc.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.1));
                    } else if (classification == Data.ClassLabel.C2) {
                        gc.setFill(Color.RED.deriveColor(0, 1, 1, 0.1));
                    } else {
                        gc.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.1));
                    }
                    gc.fillRect(px, py, 10, 10);
                }
            }
        }
    }

    private void drawBoundary(GraphicsContext gc, double w, double h) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);

        double prevY_screen = Double.NaN;

        for (int px = 0; px <= w; px++) {
            double x = fromScreenX(px, w);
            double y = potentialMethod.getBoundaryY(x);

            if (!Double.isNaN(y) && y >= Y_MIN && y <= Y_MAX) {
                double py = toScreenY(y, h);

                if (!Double.isNaN(prevY_screen) && Math.abs(prevY_screen - py) < h/2) {
                    gc.strokeLine(px - 1, prevY_screen, px, py);
                }
                prevY_screen = py;
            } else {
                prevY_screen = Double.NaN;
            }
        }
    }

    private double[] toScreen(double x, double y, double w, double h) {
        double scaleX = w / (X_MAX - X_MIN);
        double scaleY = h / (Y_MAX - Y_MIN);

        double sx = (x - X_MIN) * scaleX;
        double sy = h - (y - Y_MIN) * scaleY;

        return new double[]{sx, sy};
    }

    private double fromScreenX(double sx, double w) {
        return X_MIN + (sx / w) * (X_MAX - X_MIN);
    }

    private double fromScreenY(double sy, double h) {
        return Y_MAX - (sy / h) * (Y_MAX - Y_MIN);
    }

    private double toScreenY(double y, double h) {
        return h - (y - Y_MIN) * (h / (Y_MAX - Y_MIN));
    }

    private static class Rectangle extends javafx.scene.shape.Rectangle {
        public Rectangle(double w, double h, Color color) {
            super(w, h);
            setFill(color);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}