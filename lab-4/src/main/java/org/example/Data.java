package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class Data {
    public static class Vector {
        public final List<Double> coordinates;
        public int centroid = -1;

        public Vector(int dimension) {
            coordinates = new ArrayList<>(Collections.nCopies(dimension, 0d));
        }
        public void setRandomCoordinates(double min, double max) {
            coordinates.replaceAll(ignored -> ThreadLocalRandom.current().nextDouble(min, max));
        }
    }
    List<Vector> points;
    List<Vector> centroids = new ArrayList<>();

    public Data(int numPoints/*1'000-100'000*/){
        points = new ArrayList<>(numPoints);

        for (int i = 0; i < numPoints; i++) {
            Vector v = new Vector(2);
            v.setRandomCoordinates(-100, 100);
            points.add(v);
        }
    };
}