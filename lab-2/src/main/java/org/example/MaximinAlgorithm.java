package org.example;

import javafx.util.Pair;
import java.util.List;

public class MaximinAlgorithm {
    private final Data data;
    public MaximinAlgorithm(Data data) {
        this.data = data;
    }

    private void selectFirstCentroid() {
        if (data.centroids.isEmpty() && !data.points.isEmpty()) {
            data.centroids.add(data.points.removeFirst());
        }
    }

    private void selectSecondCentroid() {
        Data.Vector firstCentroid = data.centroids.getFirst();
        double maxDist = -1;
        Data.Vector farthestVec = null;

        for (Data.Vector v : data.points) {
            double d = distance(firstCentroid, v);
            if (d > maxDist) {
                maxDist = d;
                farthestVec = v;
            }
        }

        if (farthestVec != null) {
            data.centroids.add(farthestVec);
            data.points.remove(farthestVec);
        }
    }

    private void assignPointsToClusters() {
        for (Data.Vector p : data.points) {
            int nearest = 0;
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < data.centroids.size(); i++) {
                double d = distance(p, data.centroids.get(i));
                if (d < minDist) {
                    minDist = d;
                    nearest = i;
                }
            }
            p.centroid = nearest;
        }
    }

    private Pair<Data.Vector, Double> findFarthestPointFromItsCentroid() {
        Data.Vector farthest = data.points.getFirst();
        double maxDistance = 0;
        for (Data.Vector p : data.points) {
            Data.Vector centroid = data.centroids.get(p.centroid);
            double d = distance(p, centroid);
            if (d > maxDistance) {
                maxDistance = d;
                farthest = p;
            }
        }
        return new Pair<>(farthest, maxDistance);
    }

    private boolean evaluateNewKernelCandidate(Pair<Data.Vector, Double> candidate) {
        double threshold = getMeanInterKernelDistance() / 2.0;
        return candidate.getValue() > threshold;
    }

    private double getMeanInterKernelDistance() {
        List<Data.Vector> kernels = data.centroids;
        int m = kernels.size();

        if (m < 2) return 0.0;

        double sumDist = 0;
        int pairsCount = 0;

        for (int i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++) {
                sumDist += distance(kernels.get(i), kernels.get(j));
                pairsCount++;
            }
        }

        return sumDist / pairsCount;
    }

    private static double distance(Data.Vector a, Data.Vector b){
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public void run() {
        if (currStep == 0) {
            selectFirstCentroid();
            selectSecondCentroid();

            boolean isActive = true;
            while (isActive) {
                assignPointsToClusters();

                Pair<Data.Vector, Double> candidate = findFarthestPointFromItsCentroid();

                if (evaluateNewKernelCandidate(candidate)) {
                    data.centroids.add(candidate.getKey());
                    data.points.remove(candidate.getKey());
                } else {
                    isActive = false;
                }
            }
        }
    }

    private int currStep = 0;
    private Pair<Data.Vector, Double> currentCandidate = null;

    public boolean singleStep() {
        switch (currStep) {
            case 0 -> {
                selectFirstCentroid();
                currStep = 1;
                return true;
            }
            case 1 -> {
                selectSecondCentroid();
                currStep = 2;
                return true;
            }
            case 2 -> {
                if (data.points.isEmpty()) return false;
                assignPointsToClusters();
                currStep = 3;
                return true;
            }
            case 3 -> {
                currentCandidate = findFarthestPointFromItsCentroid();
                currStep = 4;
                return true;
            }
            case 4 -> {
                if (currentCandidate != null && evaluateNewKernelCandidate(currentCandidate)) {
                    data.centroids.add(currentCandidate.getKey());
                    data.points.remove(currentCandidate.getKey());
                    currStep = 2;
                    return true;
                } else {
                    refineCentroids();
                    return false;
                }
            }
            default -> {
                return false;
            }
        }
    }
    private void refineCentroids() {
        double[] sumX = new double[data.centroids.size()];
        double[] sumY = new double[data.centroids.size()];
        int[] counts = new int[data.centroids.size()];

        for (Data.Vector p : data.points) {
            sumX[p.centroid] += p.x;
            sumY[p.centroid] += p.y;
            counts[p.centroid]++;
        }

        for (int i = 0; i < data.centroids.size(); i++) {
            if (counts[i] > 0) {
                data.centroids.get(i).x = sumX[i] / counts[i];
                data.centroids.get(i).y = sumY[i] / counts[i];
            }
        }
    }
}
