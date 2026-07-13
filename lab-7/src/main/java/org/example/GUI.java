package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;


public class GUI extends Application {

    private TextField inputField;
    private Canvas canvas;
    private ChromosomeRecognizer recognizer;
    private Label resultLabel;

    @Override
    public void start(Stage primaryStage) {
        recognizer = new ChromosomeRecognizer();

        inputField = new TextField();

        Button recognizeButton = new Button("Определить");
        recognizeButton.setOnAction(e -> recognizeAndDraw());

        resultLabel = new Label();
        resultLabel.setFont(new Font(14));
        resultLabel.setTextAlignment(TextAlignment.CENTER);

        Pane canvasContainer = new Pane();
        canvas = new Canvas();
        canvasContainer.getChildren().add(canvas);

        canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
                drawTree();
            }
        });

        canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
                drawTree();
            }
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(inputField, recognizeButton, resultLabel, canvasContainer);

        VBox.setVgrow(canvasContainer, Priority.ALWAYS);

        Scene scene = new Scene(root, 850, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void recognizeAndDraw() {
        String input = inputField.getText().trim();

        if (input.isEmpty()) {
            resultLabel.setText("Ошибка: введите строку!");
        } else {
            ChromosomeRecognizer.Chromosome result = recognizer.recognize(input);
            ChromosomeRecognizer.Token root = recognizer.getRootToken();

            String resultText = switch (result) {
                case Telocentric -> "Результат: ТЕЛОЦЕНТРИЧЕСКАЯ хромосома (S)";
                case Metacentric -> "Результат: V-ОБРАЗНАЯ хромосома (T)";
                default -> "Результат: НЕ РАСПОЗНАНО";
            };
            resultLabel.setText(resultText);

            buildVisualTree(root);

            drawTree();
        }
    }

    //n - количество листьев
    //корень в (0.5, 0), листья в (k*1/n 1)
    private record VisualToken(String value, VisualToken left, VisualToken right, double absX, double absY) {
        boolean isLeaf() {
            return left == null && right == null;
        }
    }

    private static VisualToken visualRoot = null;

    void buildVisualTree(ChromosomeRecognizer.Token root) {
        if (root == null) {
            visualRoot = null;
            return;
        }

        int depth = getDepth(root);

        visualRoot = buildVisualNode(root, 0, 0.5, 0, depth);

    }

    private VisualToken buildVisualNode(ChromosomeRecognizer.Token token, double parentX, double shift, int currentDepth, int maxDepth) {
        if (token == null)
            return null;
        double currX = parentX + shift;
        double currY = currentDepth / (double) maxDepth;
        shift = Math.abs(shift);
        VisualToken left = buildVisualNode(token.left(), currX, - shift / 2, currentDepth + 1, maxDepth);
        VisualToken right = buildVisualNode(token.right(), currX, shift / 2, currentDepth + 1, maxDepth);
        return new VisualToken(token.value(), left, right, currX, currY);
    }

    private int getDepth(ChromosomeRecognizer.Token node) {
        if (node == null)
            return 0;
        return 1 + Math.max(getDepth(node.left()), getDepth(node.right()));
    }

    private String getDisplayValue(String value) {
        return switch (value) {
            case ChromosomeRecognizer.SHOULDER -> "П";
            case ChromosomeRecognizer.SIDE -> "С";
            case ChromosomeRecognizer.BOTTOM -> "О";
            case ChromosomeRecognizer.LEFT_PART -> "ЛЧ";
            case ChromosomeRecognizer.RIGHT_PART -> "ПЧ";
            case ChromosomeRecognizer.SHOULDER_PAIR -> "ПП";
            default -> value;
        };
    }

    private void drawTree() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (visualRoot == null) {
            gc.setFont(new Font(16));
            gc.setFill(Color.RED);
            gc.fillText("Дерево разбора не построено", canvas.getWidth() / 2 - 150, canvas.getHeight() / 2);
            return;
        }

        drawNode(gc, visualRoot, canvas.getWidth(), canvas.getHeight());
    }

    private void drawNode(GraphicsContext gc, VisualToken node, double width, double height) {
        if (node == null)
            return;

        double x = node.absX * width;
        double y = node.absY * height;

        if (node.left != null) {
            double leftX = node.left.absX * width;
            double leftY = node.left.absY * height;
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, leftX, leftY);
            drawNode(gc, node.left, width, height);
        }

        if (node.right != null) {
            double rightX = node.right.absX * width;
            double rightY = node.right.absY * height;
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, rightX, rightY);
            drawNode(gc, node.right, width, height);
        }

        double radius = 25;
        gc.setFill(Color.LIGHTBLUE);
        gc.fillOval(x - radius/2, y - radius/2, radius, radius);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeOval(x - radius/2, y - radius/2, radius, radius);

        gc.setFill(Color.BLACK);
        gc.setFont(new Font(12));
        String displayText = getDisplayValue(node.value);

        gc.fillText(displayText, x - 8, y + 5);
    }
}