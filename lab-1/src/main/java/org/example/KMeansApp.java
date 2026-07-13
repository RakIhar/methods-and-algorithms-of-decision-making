package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class KMeansApp extends Application {
    private KMeansEngine engine;
    private ScatterChart<Number, Number> chart;
    private int kValue;
    private Label iterLabel;
    private int iterationCount = 0;

    @Override
    public void start(Stage stage) {

        TextField pointsInput = new TextField("2000");
        TextField clustersInput = new TextField("6");
        Button startBtn = new Button("Создать");
        Button stepBtn = new Button("Шаг алгоритма");
        iterLabel = new Label("Итерация: 0");


        NumberAxis xAxis = new NumberAxis(0, 100, 10);
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        chart = new ScatterChart<>(xAxis, yAxis);
        chart.setAnimated(false);

        startBtn.setOnAction(e -> {
            int n = Integer.parseInt(pointsInput.getText());
            kValue = Integer.parseInt(clustersInput.getText());
            engine = new KMeansEngine(n, kValue);
            iterationCount = 0;
            updateChart();
        });


        stepBtn.setOnAction(e -> {
            if (engine != null) {
                engine.singleStep();
                iterationCount++;
                updateChart();
            }
        });


        HBox controls = new HBox(10, new Label("Точек:"), pointsInput,
                new Label("Классов:"), clustersInput,
                startBtn, stepBtn, iterLabel);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-padding: 10; -fx-background-color: #eee;");

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(chart);

        Scene scene = new Scene(root, 1000, 700);


        scene.getStylesheets().add("data:text/css," +
                ".chart-symbol { -fx-background-radius: 5px; -fx-padding: 3px; }" +
                ".default-color0.chart-symbol { -fx-background-color: #f33; }" + // Красный
                ".default-color1.chart-symbol { -fx-background-color: #3f3; }" + // Зеленый
                ".default-color2.chart-symbol { -fx-background-color: #33f; }" + // Синий
                ".default-color3.chart-symbol { -fx-background-color: #f3f; }" + // Фиолетовый
                ".default-color4.chart-symbol { -fx-background-color: #ff3; }" + // Желтый
                ".default-color5.chart-symbol { -fx-background-color: #3ff; }" + // Голубой
                ".default-color6.chart-symbol { -fx-background-color: black; -fx-scale-x: 2; -fx-scale-y: 2; }" // Ядра
        );

        stage.setScene(scene);
        stage.show();
    }

    private void updateChart() {
        chart.getData().clear();
        iterLabel.setText("Итерация: " + iterationCount);

        for (int i = 0; i < kValue; i++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Кластер " + i);
            for (Point p : engine.getPoints()) {
                if (p.clusterId == i) series.getData().add(new XYChart.Data<>(p.x, p.y));
            }
            chart.getData().add(series);
        }

        XYChart.Series<Number, Number> centroids = new XYChart.Series<>();
        centroids.setName("Ядра");
        for (Point c : engine.getCentroids()) {
            centroids.getData().add(new XYChart.Data<>(c.x, c.y));
        }
        chart.getData().add(centroids);
    }
}
