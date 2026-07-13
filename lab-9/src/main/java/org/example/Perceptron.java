package org.example;

import java.util.ArrayList;
import java.util.List;

public class Perceptron {
    public static class Layer {
        //this*prev
        double[][] weights;
        double[] thetas;
        public final int prevLayerSize;
        public final int thisLayerSize;

        public Layer(double[][] weights) {
            this.weights = weights;
            thisLayerSize = weights.length;
            prevLayerSize = weights[0].length;
            thetas = new  double[thisLayerSize];
            for (int i = 0; i < thisLayerSize; i++) {
//                for (int j = 0; j < prevLayerSize; j++) {
//                    weights[i][j] = (Math.random() - 0.5) * 0.1;
//                }
                thetas[i] = (Math.random() - 0.5) * 0.1;
            }
        }

        public Layer(int prevLayerSize, int thisLayerSize) {
            weights = new double[thisLayerSize][prevLayerSize];
            this.prevLayerSize = prevLayerSize;
            this.thisLayerSize = thisLayerSize;
            thetas = new  double[thisLayerSize];
            for (int i = 0; i < thisLayerSize; i++) {
                for (int j = 0; j < prevLayerSize; j++) {
                    weights[i][j] = (Math.random() - 0.5) * 0.1;
                }
                thetas[i] = (Math.random() - 0.5) * 0.1;
            }
        }

        public Neuron neuron(int i) {
            return new Neuron(weights[i], thetas[i]);
        }

        public Neuron neuron(int i, Neuron n) {
            if (n.weights.length != prevLayerSize)
                throw new IllegalArgumentException();

            Neuron old = neuron(i);
            weights[i] = n.weights.clone();
            thetas[i] = n.theta;
            return old;
        }

        private double F(double x, double theta){
            return x;
//            x = x - theta;
//            return x < 0 ? 0 : x > 1 ? 1 : x;
        }

        public double[] OUT(double[] in) {
            if (in.length != prevLayerSize)
                throw new IllegalArgumentException();
            double[] out = new double[thisLayerSize];
            for (int i = 0; i < thisLayerSize; i++) {
                double NET = 0;
                double[] weight1 = weights[i];
                for (int j = 0; j < prevLayerSize; j++) {
                    NET += in[j] * weights[i][j];
                }
                out[i] = F(NET, thetas[i]);
            }
            return out;
        }

        public record Neuron(double[] weights, double theta) {}
    }

    List<Layer> layers;
    private final int vectorSize;
    private final int resultSize;
    private final int layerCount;
    private final int layerSize;

    public Perceptron(List<Layer> layers) {
        this.layers = layers;
        this.layerCount = layers.size();
        this.layerSize = layerCount > 2 ? layers.get(1).prevLayerSize : -1;
        this.vectorSize = layers.getFirst().prevLayerSize;
        this.resultSize = layers.getLast().thisLayerSize;
    }

    public Perceptron(int layerCount, int layerSize, int vectorSize, int resultSize) {
        layers = new ArrayList<>(layerCount);
        for (int i = 0; i < layerCount; i++) {
            layers.add(new Layer(
                    i ==  0             ? vectorSize : layerSize,
                    i == layerCount - 1 ? resultSize : layerSize
            ));
        }
        this.layerCount = layerCount;
        this.layerSize = layerSize;
        this.vectorSize = vectorSize;
        this.resultSize = resultSize;
    }

    public double[] identify(double[] vec) {
        double[] res = vec;
        for (int i = 0; i < layerCount; i++) {
            res = layers.get(i).OUT(res);
        }
        return res;
    }
    
    public void train(double[] x, int trueClass) {
        double[] y = identify(x);

        int predictedClass = 0;
        for (int i = 1; i < y.length; i++) {
            if (y[i] > y[predictedClass])
                predictedClass = i;
        }

        if (predictedClass == trueClass)
            return;

        Layer last = layers.get(layerCount - 1);

        double deltaTrue = 1 - y[trueClass];
        double deltaPredicted = -y[predictedClass];

        for (int i = 0; i < last.prevLayerSize; i++) {
            last.weights[trueClass][i] += deltaTrue * x[i];
        }
        last.thetas[trueClass] += deltaTrue;

        for (int i = 0; i < last.prevLayerSize; i++) {
            last.weights[predictedClass][i] += deltaPredicted * x[i];
        }
        last.thetas[predictedClass] += deltaPredicted;
    }

    public void fit(List<double[]> X, List<Integer> Y, int epochs) {
        for (int e = 0; e < epochs; e++) {
            boolean changed = false;

            for (int i = 0; i < X.size(); i++) {
                double[] x = X.get(i);
                int y = Y.get(i);

                double[] before = identify(x);
                train(x, y);
                double[] after = identify(x);

                if (!java.util.Arrays.equals(before, after)) {
                    changed = true;
                }
            }

            if (!changed)
                break;
        }
    }
}