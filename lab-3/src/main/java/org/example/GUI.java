package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application {
    private LineChart<Number, Number> chart;
    private Label resultLabel;

    @Override
    public void start(Stage stage) {
        // Поля ввода с начальными значениями для примера
        TextField ev1Input = new TextField("10"); // mu1
        TextField ev2Input = new TextField("15"); // mu2
        TextField d1Input = new TextField("4");   // sigma^2
        TextField d2Input = new TextField("9");   // sigma^2
        TextField p1Input = new TextField("0.5"); // P(C1)

        Button calculateBtn = new Button("Рассчитать");
        resultLabel = new Label("Результаты: ");

        // Настройка осей для плавных кривых
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Признак X");
        yAxis.setLabel("P(Ck)*p(X|Ck)");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setCreateSymbols(false); // Убираем точки, оставляем только линии
        chart.setAnimated(false);

        calculateBtn.setOnAction(e -> {
            try {
                double mu1 = Double.parseDouble(ev1Input.getText());
                double mu2 = Double.parseDouble(ev2Input.getText());
                double disp1 = Double.parseDouble(d1Input.getText());
                double disp2 = Double.parseDouble(d2Input.getText());
                double prior1 = Double.parseDouble(p1Input.getText());

                InputValues input = new InputValues(mu1, mu2, disp1, disp2, prior1);
                Probabilities res = Gauss.getProbabilities(input);

                if (res != null) {
                    updateChart(input);
                    resultLabel.setText(String.format(
                            "Ложная тревога (Pлт): %.4f | Пропуск (Pпо): %.4f | Общая ошибка: %.4f",
                            res.probabilityOfFalseAlarm(),
                            res.probabilityOfMissingError(),
                            res.probabilityOfTotalClassificationError()
                    ));
                }
            } catch (NumberFormatException ex) {
                resultLabel.setText("Ошибка: введите числовые значения");
            }
        });

        HBox inputs = new HBox(10,
                new Label("μ1:"), ev1Input,
                new Label("μ2:"), ev2Input,
                new Label("σ²1:"), d1Input,
                new Label("σ²2:"), d2Input,
                new Label("P{1}:"), p1Input);
        inputs.setAlignment(Pos.CENTER);

        VBox controls = new VBox(10, inputs, calculateBtn, resultLabel);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-padding: 10; -fx-background-color: #eee;");

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(chart);

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("Классификация объектов: Вероятностный подход");
        stage.show();
    }

    private void updateChart(InputValues vals) {
        chart.getData().clear();

        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName("Класс C1 (взвеш.)");
        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        series2.setName("Класс C2 (взвеш.)");

        double s1 = Math.sqrt(vals.sigma1());
        double s2 = Math.sqrt(vals.sigma2());
        double p1 = vals.priorProb1();
        double p2 = 1.0 - p1;

        // Определяем диапазон отрисовки (правило 3-4 сигм)
        double minX = Math.min(vals.mu1() - 4 * s1, vals.mu2() - 4 * s2);
        double maxX = Math.max(vals.mu1() + 4 * s1, vals.mu2() + 4 * s2);
        double step = (maxX - minX) / 200;

        for (double x = minX; x <= maxX; x += step) {
            series1.getData().add(new XYChart.Data<>(x, p1 * gaussianPdf(x, vals.mu1(), s1)));
            series2.getData().add(new XYChart.Data<>(x, p2 * gaussianPdf(x, vals.mu2(), s2)));
        }

        chart.getData().addAll(series1, series2);
    }

    // Формула (1) из методички
    private double gaussianPdf(double x, double mu, double sigma) {
        return (1.0 / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp(-0.5 * Math.pow((x - mu) / sigma, 2));
    }
}