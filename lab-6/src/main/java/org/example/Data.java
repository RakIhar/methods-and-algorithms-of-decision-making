package org.example;

import java.util.concurrent.ThreadLocalRandom;

public class Data {
    double[][] objects;
    double[][] reversed;

    public Data(double[][] objects) {
        this.objects = objects;
        int n = objects[0].length;
        reversed = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                double reverse = 1 / objects[i][j];
                reversed[i][j] = reverse;
                reversed[j][i] = reverse;
            }
        }
    }

    public Data(int n, double maxDistance) {
        if (n < 5)
            n = 5;
        objects = new double[n][n];
        reversed = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                double d = ThreadLocalRandom.current().nextDouble(0.01, maxDistance);
                objects[i][j] = d;
                objects[j][i] = d;
                double reverse = 1 / d;
                reversed[i][j] = reverse;
                reversed[j][i] = reverse;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(String.format("%f ", (double)0));
        for (int i = 0; i < objects[0].length; i++) {
            res.append(String.format("%f ", (double)i));
        }
        res.append("\n");
        for (int i = 0; i < objects[0].length; i++) {
            res.append(String.format("%f ", (double)i));
            for (int j = 0; j < objects[0].length; j++) {
                res.append(String.format("%f ", objects[i][j]));
            }
            res.append("\n");
        }
        res.append("\n");
        res.append(String.format("%f ", (double)0));
        for (int i = 0; i < objects[0].length; i++) {
            res.append(String.format("%f ", (double)i));
        }
        res.append("\n");
        for (int i = 0; i < objects[0].length; i++) {
            res.append(String.format("%f ", (double)i));
            for (int j = 0; j < objects[0].length; j++) {
                res.append(String.format("%f ", reversed[i][j]));
            }
            res.append("\n");
        }
        res.append("\n");
        return res.toString();
    }
}
