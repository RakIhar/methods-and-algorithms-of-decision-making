package org.example;

import java.util.List;

public class LinearClassifier {
    public double[][] weights;
    private final int numClasses;

    public LinearClassifier(int numClasses) {
        this.numClasses = numClasses;
        this.weights = new double[numClasses][3]; // x, y, bias
    }

    public boolean train(List<Data.Vector> batch, double c) {
        boolean changed = false;
        for (Data.Vector v : batch) {
            if (v.centroid == -1) continue;
            double[] x = {v.coordinates.get(0), v.coordinates.get(1), 1.0};

            double[] d = new double[numClasses];
            for (int j = 0; j < numClasses; j++) {
                for (int k = 0; k < 3; k++)
                    d[j] += weights[j][k] * x[k];
            }

            int correct = v.centroid;
            for (int l = 0; l < numClasses; l++) {
                if (l != correct && d[correct] <= d[l]) {
                    changed = true;
                    for (int k = 0; k < 3; k++) {
                        weights[correct][k] += c * x[k];
                        weights[l][k] -= c * x[k];
                    }
                }
            }
        }
        return changed;
    }
    
    public int classify(double x, double y) {
        int best = 0;
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < numClasses; i++) {
            double val = weights[i][0] * x + weights[i][1] * y + weights[i][2];
            if (val > max) {
                max = val;
                best = i;
            }
        }
        return best;
    }
}
