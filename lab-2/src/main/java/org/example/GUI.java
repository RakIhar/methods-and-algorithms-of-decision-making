package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GUI extends Application {
    private MaximinAlgorithm algorithm;
    private Data data;
    private ScatterChart<Number, Number> chart;
    private Label iterLabel;
    private int iterationCount = 0;

    @Override
    public void start(Stage stage) {
        TextField pointsInput = new TextField("200");
        Button startBtn = new Button("Создать");
        Button stepBtn = new Button("Шаг");
        iterLabel = new Label("Итерация: 0");

        NumberAxis xAxis = new NumberAxis(-100, 100, 10);
        NumberAxis yAxis = new NumberAxis(-100, 100, 10);
        chart = new ScatterChart<>(xAxis, yAxis);
        chart.setAnimated(false);

        startBtn.setOnAction(e -> {
            int n = Integer.parseInt(pointsInput.getText());
            this.data = new Data(n);
            this.algorithm = new MaximinAlgorithm(data);
            iterationCount = 0;
            updateChart();
        });

        stepBtn.setOnAction(e -> {
            if (algorithm != null) {
                boolean continued = algorithm.singleStep();
                iterationCount++;
                updateChart();
                if (!continued)
                    iterLabel.setText("Завершено на итерации: " + iterationCount);
            }
        });

        HBox controls = new HBox(10, new Label("Точек:"), pointsInput, startBtn, stepBtn, iterLabel);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-padding: 10; -fx-background-color: #eee;");

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(chart);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Maximin Clustering Visualizer");
        stage.show();
    }

    private void updateChart() {
        chart.getData().clear();
        if (data == null)
            return;

        iterLabel.setText("Итерация: " + iterationCount);

        XYChart.Series<Number, Number> unassignedSeries = new XYChart.Series<>();
        unassignedSeries.setName("Не распределены");

        List<XYChart.Series<Number, Number>> clusterSeriesList = new ArrayList<>();
        for (int i = 0; i < data.centroids.size(); i++) {
            XYChart.Series<Number, Number> s = new XYChart.Series<>();
            s.setName("Кластер " + i);
            clusterSeriesList.add(s);
        }

        for (Data.Vector v : data.points) {
            XYChart.Data<Number, Number> d = new XYChart.Data<>(v.x, v.y);

            if (v.centroid == null || v.centroid < 0) {
                unassignedSeries.getData().add(d);
                applyStyle(d, "#bbb", false);
            } else {
                int clusterIdx = v.centroid;
                clusterSeriesList.get(clusterIdx).getData().add(d);
                applyStyle(d, getColor(clusterIdx), false);
            }
        }

        XYChart.Series<Number, Number> kernelSeries = new XYChart.Series<>();
        kernelSeries.setName("Ядра");
        for (Data.Vector k : data.centroids) {
            XYChart.Data<Number, Number> d = new XYChart.Data<>(k.x, k.y);
            kernelSeries.getData().add(d);
            applyStyle(d, "black", true);
        }

        if (!unassignedSeries.getData().isEmpty()) {
            chart.getData().add(unassignedSeries);
        }
        chart.getData().addAll(clusterSeriesList);
        chart.getData().add(kernelSeries);
    }

    private void applyStyle(XYChart.Data<Number, Number> data, String color, boolean isKernel) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                double size = isKernel ? 12 : 6;
                // У ядер делаем круг (-fx-background-radius), у обычных точек можно сделать квадрат или круг
                String shape = isKernel ? "10px" : "0px"; // 0px сделает квадраты, 5px - круги

                newNode.setStyle(
                        "-fx-background-color: " + color + ";" +
                                "-fx-background-radius: " + (isKernel ? "10px" : "2px") + ";" +
                                "-fx-padding: " + (size / 2) + "px;"
                );
            }
        });
    }

    private String getColor(int index) {
        // Желтый удален, добавлены глубокие и яркие контрастные цвета
        String[] colors = {
                "#E6194B", // Красный
                "#3CB44B", // Зеленый
                "#4363D8", // Синий
                "#F58231", // Оранжевый (вместо желтого)
                "#911EB4", // Пурпурный
                "#42D4F4", // Голубой
                "#F032E6", // Маджента
                "#BFEF45", // Лайм (хорошо виден, в отличие от желтого)
                "#FABEBE", // Розовый
                "#469990", // Морская волна
                "#9A6324", // Коричневый
                "#000075"  // Темно-синий
        };
        return colors[index % colors.length];
    }
}