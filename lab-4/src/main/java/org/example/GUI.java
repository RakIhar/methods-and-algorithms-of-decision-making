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

public class GUI extends Application {
    private Data data;
    private LinearClassifier classifier;
    private List<Data.Vector> seenPoints = new ArrayList<>();

    private Canvas canvasLeft = new Canvas(500, 500);
    private Canvas canvasRight = new Canvas(500, 500);

    private String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#F333FF", "#FF33A1", "#33FFF5", "#FFC300"};

    @Override
    public void start(Stage stage) {
        TextField maxPointsField = new TextField("1000");
        TextField batchSizeField = new TextField("50");
        TextField cField = new TextField("0.25");

        Button genBtn = new Button("1. Максимин");
        Button stepBtn = new Button("2. Обучить шаг");
        Button autoTrainBtn = new Button("3. Обучить до сходимости");
        autoTrainBtn.setDisable(true);
        stepBtn.setDisable(true);

        genBtn.setOnAction(e -> {
            int n = Integer.parseInt(maxPointsField.getText());
            data = new Data(n);
            new MaximinAlgorithm(data).run();

            classifier = new LinearClassifier(data.centroids.size());
            seenPoints.clear();
            stepBtn.setDisable(false);
            autoTrainBtn.setDisable(false);
            draw();
        });

        stepBtn.setOnAction(e -> {
            if (data == null) return;
            double c = Double.parseDouble(cField.getText());
            int batchSize = Integer.parseInt(batchSizeField.getText());

            List<Data.Vector> all = new ArrayList<>(data.points);
            Collections.shuffle(all);
            List<Data.Vector> batch = all.subList(0, Math.min(batchSize, all.size()));

            for (Data.Vector v : batch) {
                if (!seenPoints.contains(v)) seenPoints.add(v);
            }

            classifier.train(batch, c);
            draw();
        });

        autoTrainBtn.setOnAction(e -> {
            if (data == null) return;
            double c = Double.parseDouble(cField.getText());
            int maxEpochs = 5000;
            int epoch = 0;

            while (classifier.train(data.points, c) && epoch < maxEpochs) {
                epoch++;
            }

            seenPoints.clear();
            seenPoints.addAll(data.points);
            draw();
            System.out.println("Обучение завершено за " + epoch + " эпох.");
        });

        HBox controls = new HBox(15,
                new Label("Точек:"), maxPointsField, genBtn,
                new Separator(),
                new Label("C:"), cField,
                new Label("Batch:"), batchSizeField,
                stepBtn, autoTrainBtn
        );
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-padding: 10; -fx-background-color: #ddd;");

        // Подписи к графикам
        HBox labels = new HBox(400, new Label("ИСХОДНЫЕ ДАННЫЕ (MAXIMIN)"), new Label("ПРОЦЕСС ОБУЧЕНИЯ (PERCEPTRON)"));
        labels.setAlignment(Pos.CENTER);

        HBox canvases = new HBox(20, canvasLeft, canvasRight);
        canvases.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, controls, labels, canvases);
        stage.setScene(new Scene(root, 1100, 650));
        stage.setTitle("Сравнение: Максимин vs Перцептрон");
        stage.show();
    }

    private void draw() {
        drawLeft();
        drawRight();
    }

    // Левый график: Исходные данные и кластеры Максимина
    private void drawLeft() {
        GraphicsContext gc = canvasLeft.getGraphicsContext2D();
        double w = canvasLeft.getWidth();
        double h = canvasLeft.getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0, 0, w, h);

        if (data == null) return;

        for (Data.Vector v : data.points) {
            double[] scr = toScreen(v.coordinates.get(0), v.coordinates.get(1), w, h);
            gc.setFill(v.centroid == -1 ? Color.GRAY : Color.web(colors[v.centroid % colors.length]));
            gc.fillOval(scr[0] - 2, scr[1] - 2, 4, 4);
        }

        for (Data.Vector v : data.centroids) {
            double[] scr = toScreen(v.coordinates.get(0), v.coordinates.get(1), w, h);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(scr[0] - 5, scr[1] - 5, 10, 10);
        }
    }

    // Правый график: Обученные области и только использованные точки
    private void drawRight() {
        GraphicsContext gc = canvasRight.getGraphicsContext2D();
        double w = canvasRight.getWidth();
        double h = canvasRight.getHeight();
        gc.clearRect(0, 0, w, h);

        if (classifier == null) return;

        // 1. Рисуем области классификации (фон)
        for (int i = 0; i < w; i += 5) {
            for (int j = 0; j < h; j += 5) {
                double mapX = ((i - w/2) / (w/2)) * 100;
                double mapY = ((h/2 - j) / (h/2)) * 100;

                int cls = classifier.classify(mapX, mapY);
                gc.setFill(Color.web(colors[cls % colors.length], 0.3));
                gc.fillRect(i, j, 5, 5);
            }
        }

        // 2. Рисуем только те точки, на которых учились
        for (Data.Vector v : seenPoints) {
            double[] scr = toScreen(v.coordinates.get(0), v.coordinates.get(1), w, h);
            // Цвет точки берем из Максимина (как "правильный ответ")
            gc.setFill(Color.web(colors[v.centroid % colors.length]));
            gc.fillOval(scr[0] - 3, scr[1] - 3, 6, 6);

            // Если классификатор ошибается на этой точке, обведем её красным
            if (classifier.classify(v.coordinates.get(0), v.coordinates.get(1)) != v.centroid) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(1);
                gc.strokeOval(scr[0] - 4, scr[1] - 4, 8, 8);
            }
        }
    }

    private double[] toScreen(double x, double y, double w, double h) {
        double sx = (x / 100.0 * (w / 2.0)) + w / 2.0;
        double sy = h / 2.0 - (y / 100.0 * (h / 2.0));
        return new double[]{sx, sy};
    }
}