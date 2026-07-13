package org.example;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Data {

    public enum ClassLabel {
        C1,
        C2,
        NotSet
    }

    public static class Vector {
        final double x;
        final double y;
        ClassLabel classLabel;

        public Vector(double x, double y) {
            this(x, y, ClassLabel.NotSet);
        }

        public Vector(double x, double y, ClassLabel classLabel) {
            this.x = x;
            this.y = y;
            this.classLabel = classLabel;
        }

        public static Vector randomVector(double min, double max) {
            return new Vector(ThreadLocalRandom.current().nextDouble(min, max), ThreadLocalRandom.current().nextDouble(min, max));
        }

        public double x() { return x; }
        public double y() { return y; }
        public ClassLabel classLabel() { return classLabel; }
        public void classLabel(ClassLabel classLabel) { this.classLabel = classLabel; }
    }

    List<Vector> trainingSet;
    List<Vector> testSet;

    public Data(List<Vector> trainingSet, List<Vector> testSet) {
        this.trainingSet = trainingSet;
        this.testSet = testSet;
    }
}