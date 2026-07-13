package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class DendrogramGUI extends Application {
    private TextField nField;
    private TextField maxDistField;
    private DendrogramCanvas canvas;
    private TextArea dataTableArea;
    private Data currentData;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Dendrogram Visualizer");

        GridPane controls = new GridPane();
        controls.setPadding(new Insets(10));
        controls.setHgap(10);
        controls.setVgap(10);

        controls.add(new Label("n (objects):"), 0, 0);
        nField = new TextField("5");
        controls.add(nField, 1, 0);

        controls.add(new Label("Max distance:"), 0, 1);
        maxDistField = new TextField("10.0");
        controls.add(maxDistField, 1, 1);

        Button genBtn = new Button("Generate Data");
        Button minBtn = new Button("MIN (Single Linkage)");
        Button maxBtn = new Button("MAX (Complete Linkage)");

        minBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        maxBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        HBox buttons = new HBox(10, genBtn, minBtn, maxBtn);
        buttons.setPadding(new Insets(10, 0, 10, 0));

        dataTableArea = new TextArea();
        dataTableArea.setEditable(false);
        dataTableArea.setPrefRowCount(8);
        dataTableArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        canvas = new DendrogramCanvas();
        canvas.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        VBox root = new VBox(10, controls, buttons, canvas, dataTableArea);
        root.setPadding(new Insets(10));
        VBox.setVgrow(canvas, Priority.ALWAYS);

        Scene scene = new Scene(root, 1100, 800);
        stage.setScene(scene);
        stage.show();

        genBtn.setOnAction(e -> generateData());
        minBtn.setOnAction(e -> {
            if (currentData != null) {
//                double[][] objects = new double[4][4];
//                objects[0][1] = objects[1][0] = 5;
//                objects[0][2] = objects[2][0] = 0.5;
//                objects[0][3] = objects[3][0] = 2;
//                objects[1][2] = objects[2][1] = 1;
//                objects[1][3] = objects[3][1] = 0.6;
//                objects[2][3] = objects[3][2] = 2.5;
//                Data data = new Data(objects);
//                HierarchicalClustering.Cluster tree = HierarchicalClustering.hierarchyByMin(data);

                HierarchicalClustering.Cluster tree = HierarchicalClustering.hierarchyByMin(currentData);
                canvas.drawDendrogram(tree, currentData);
                showDataTable();
            }
        });
        maxBtn.setOnAction(e -> {
            if (currentData != null) {
//                double[][] objects = new double[4][4];
//                objects[0][1] = objects[1][0] = 5;
//                objects[0][2] = objects[2][0] = 0.5;
//                objects[0][3] = objects[3][0] = 2;
//                objects[1][2] = objects[2][1] = 1;
//                objects[1][3] = objects[3][1] = 0.6;
//                objects[2][3] = objects[3][2] = 2.5;
//                Data data = new Data(objects);
//                HierarchicalClustering.Cluster tree = HierarchicalClustering.hierarchyByMax(data);

                HierarchicalClustering.Cluster tree = HierarchicalClustering.hierarchyByMax(currentData);
                canvas.drawDendrogram(tree, currentData);
                showDataTable();
            }
        });
    }

    private void generateData() {
        try {
            int n = Integer.parseInt(nField.getText());
            double maxDist = Double.parseDouble(maxDistField.getText());
            currentData = new Data(n, maxDist);
            canvas.drawMessage("Data generated. Click MIN or MAX.");
            showDataTable();
        } catch (Exception e) {
            canvas.drawMessage("Invalid input!");
        }
    }

    private void showDataTable() {
        if (currentData == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Distance Matrix:\n");
        sb.append("      ");
        for (int i = 0; i < currentData.objects[0].length; i++) {
            sb.append(String.format("%8d", i));
        }
        sb.append("\n");
        for (int i = 0; i < currentData.objects[0].length; i++) {
            sb.append(String.format("%4d ", i));
            for (int j = 0; j < currentData.objects[0].length; j++) {
                sb.append(String.format("%8.4f", currentData.objects[i][j]));
            }
            sb.append("\n");
        }
        dataTableArea.setText(sb.toString());
    }
}

class DendrogramCanvas extends Canvas {
    private HierarchicalClustering.Cluster currentTree;
    private Data currentData;
    private double maxDistance;
    private Map<HierarchicalClustering.Cluster, Double> nodeX;
    private Map<HierarchicalClustering.Cluster, Double> nodeY;
    private List<Integer> leafOrder;

    public DendrogramCanvas() {
        super(900, 450);
        widthProperty().addListener(evt -> redraw());
        heightProperty().addListener(evt -> redraw());
    }

    public void drawDendrogram(HierarchicalClustering.Cluster tree, Data data) {
        this.currentTree = tree;
        this.currentData = data;
        redraw();
    }

    private void redraw() {
        if (currentTree == null || currentData == null) {
            drawMessage("No tree to display");
            return;
        }
        drawDendrogramInternal();
    }

    public void drawMessage(String msg) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.fillText(msg, 50, 50);
    }

    private void drawDendrogramInternal() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        nodeX = new HashMap<>();
        nodeY = new HashMap<>();

        leafOrder = new ArrayList<>();
        getLeafOrder(currentTree, leafOrder);

        int n = leafOrder.size();

        maxDistance = findMaxDistance(currentTree);
        if (maxDistance == 0) maxDistance = 1;

        double w = getWidth();
        double h = getHeight();
        double leftMargin = 100;  // РЕАЛЬНЫЙ отступ слева
        double rightMargin = 50;
        double topMargin = 50;
        double bottomMargin = 70;

        double drawingWidth = w - leftMargin - rightMargin;
        double drawingHeight = h - topMargin - bottomMargin;

        // Draw axes
        gc.strokeLine(leftMargin, topMargin, leftMargin, h - bottomMargin);
        gc.strokeLine(leftMargin, h - bottomMargin, w - rightMargin, h - bottomMargin);
        gc.fillText("Distance", 15, topMargin + drawingHeight / 2);

        // Y-axis labels
        for (int i = 0; i <= 4; i++) {
            double y = h - bottomMargin - (i * drawingHeight / 4);
            double dist = (i * maxDistance / 4);
            gc.fillText(String.format("%.3f", dist), 5, y + 4);
            gc.strokeLine(leftMargin - 5, y, leftMargin, y);
        }

        // X-axis - leaf positions
        double leafSpacing = drawingWidth / (n - 1);
        double[] leafX = new double[n];
        for (int i = 0; i < n; i++) {
            leafX[i] = leftMargin + i * leafSpacing;
            int leafIndex = leafOrder.get(i);
            gc.fillText(String.valueOf(leafIndex), leafX[i] - 5, h - bottomMargin + 20);
            gc.strokeLine(leafX[i], h - bottomMargin - 3, leafX[i], h - bottomMargin);
        }
        gc.fillText("Object Index (in tree order)", w / 2 - 80, h - 15);

        // Calculate positions
        calculateNodePositions(currentTree, leafX, leafOrder, h, bottomMargin, topMargin);

        // Draw
        drawCluster(gc, currentTree);
        drawDistanceLabels(gc, currentTree);
    }

    private void getLeafOrder(HierarchicalClustering.Cluster cluster, List<Integer> order) {
        if (cluster.isLeaf()) {
            order.add(cluster.signs().get(0));
            return;
        }
        getLeafOrder(cluster.left(), order);
        getLeafOrder(cluster.right(), order);
    }

    private double findMaxDistance(HierarchicalClustering.Cluster cluster) {
        if (cluster.isLeaf()) return cluster.distance();
        return Math.max(cluster.distance(),
                Math.max(findMaxDistance(cluster.left()), findMaxDistance(cluster.right())));
    }

    private void calculateNodePositions(HierarchicalClustering.Cluster cluster, double[] leafX,
                                        List<Integer> leafOrder, double canvasHeight,
                                        double bottomMargin, double topMargin) {
        if (cluster.isLeaf()) {
            int leafValue = cluster.signs().get(0);
            int idx = leafOrder.indexOf(leafValue);
            nodeX.put(cluster, leafX[idx]);
            nodeY.put(cluster, canvasHeight - bottomMargin);
            return;
        }

        calculateNodePositions(cluster.left(), leafX, leafOrder, canvasHeight, bottomMargin, topMargin);
        calculateNodePositions(cluster.right(), leafX, leafOrder, canvasHeight, bottomMargin, topMargin);

        double x = (nodeX.get(cluster.left()) + nodeX.get(cluster.right())) / 2;
        nodeX.put(cluster, x);

        double y = topMargin + (1 - cluster.distance() / maxDistance) * (canvasHeight - bottomMargin - topMargin);
        nodeY.put(cluster, y);
    }

    private void drawCluster(GraphicsContext gc, HierarchicalClustering.Cluster cluster) {
        if (cluster.isLeaf()) return;

        double x = nodeX.get(cluster);
        double y = nodeY.get(cluster);
        double xLeft = nodeX.get(cluster.left());
        double yLeft = nodeY.get(cluster.left());
        double xRight = nodeX.get(cluster.right());
        double yRight = nodeY.get(cluster.right());

        gc.strokeLine(xLeft, y, xRight, y);
        gc.strokeLine(xLeft, y, xLeft, yLeft);
        gc.strokeLine(xRight, y, xRight, yRight);

        drawCluster(gc, cluster.left());
        drawCluster(gc, cluster.right());
    }

    private void drawDistanceLabels(GraphicsContext gc, HierarchicalClustering.Cluster cluster) {
        if (cluster.isLeaf()) return;

        double x = nodeX.get(cluster);
        double y = nodeY.get(cluster);

        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillText(String.format("%.4f", cluster.distance()), x - 30, y - 5);
        gc.setFill(javafx.scene.paint.Color.BLACK);

        drawDistanceLabels(gc, cluster.left());
        drawDistanceLabels(gc, cluster.right());
    }
}