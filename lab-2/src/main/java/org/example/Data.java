package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Data {
    public static class Vector {
        public double x;
        public double y;
        public Integer centroid = null;
        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    List<Vector> points;
    List<Vector> centroids = new ArrayList<>();

    public Data(int numPoints/*1'000-100'000*/){
        points = new ArrayList<>(numPoints);

        for (int i = 0; i < numPoints; i++) {
            points.add(new Vector(
                    ThreadLocalRandom.current().nextDouble(-100.0, 100.0),
                    ThreadLocalRandom.current().nextDouble(-100.0, 100.0))
            );
        }
    };
}
