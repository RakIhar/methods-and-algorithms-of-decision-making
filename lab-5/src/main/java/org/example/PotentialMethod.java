package org.example;

import java.util.Arrays;
import java.util.List;

public class PotentialMethod {
    private double[] coefficients;
    //Sum pi  ;  Sum pi xi1  ;  Sum pi xi2  ;  Sum pi xi1 xi2

    public PotentialMethod() {
        this.coefficients = new double[4];
    }

    //корректно
    // K(test_vec, training_vec)
    public double potentialFunction(Data.Vector test_vec, Data.Vector training_vec) {
        double x1 = test_vec.x();
        double x2 = test_vec.y();
        double xi1 = training_vec.x();
        double xi2 = training_vec.y();

        return 1 + 4 * x1 * xi1 + 4 * x2 * xi2 + 16 * x1 * x2 * xi1 * xi2;
    }

    //корректно
    // K(x)
    public double separatingFunction(Data.Vector x) {
        double x1 = x.x();
        double x2 = x.y();

        return  (1 * coefficients[0]) +
                (2 * coefficients[1]) * (2 * x1) +
                (2 * coefficients[2]) * (2 * x2) +
                (4 * coefficients[3]) * (4 * x1 * x2);
    }


    public void trainFull(List<Data.Vector> trainingSet) {
        coefficients = new double[4];

        boolean isConverged;
        int maxIterations = 100;
        int iteration = 0;

        do {
            isConverged = true;

            for (Data.Vector vec : trainingSet) {
                if (vec.classLabel().equals(Data.ClassLabel.NotSet))
                    throw new IllegalArgumentException("Класс не установлен");

                Data.ClassLabel trueClass = vec.classLabel();
                double d = separatingFunction(vec);
                double rho = 0;
                if (trueClass == Data.ClassLabel.C1 && d <= 0) {
                    rho = 1;
                    isConverged = false;
                } else if (trueClass == Data.ClassLabel.C2 && d > 0) {
                    rho = -1;
                    isConverged = false;
                }

                updateCoefficients(vec, rho);
            }

            iteration++;
        } while (!isConverged && iteration < maxIterations);

        System.out.println("Обучение завершено за " + iteration + " итераций");
        System.out.println(getSeparatingFunctionEquation());
        System.out.println(Arrays.toString(coefficients));
    }

    public void train(List<Data.Vector> trainingSet) {
        for (Data.Vector vec : trainingSet) {
            if (vec.classLabel().equals(Data.ClassLabel.NotSet))
                throw new IllegalArgumentException("Класс не установлен");

            Data.ClassLabel trueClass = vec.classLabel();
            double d = separatingFunction(vec);
            double rho = 0;
            if (trueClass == Data.ClassLabel.C1 && d <= 0) {
                rho = 1;
            } else if (trueClass == Data.ClassLabel.C2 && d > 0) {
                rho = -1;
            }

            updateCoefficients(vec, rho);
        }
    }

    char sign(double d) {
        return d >= 0 ? '+' : '-';
    }

    public String getSeparatingFunctionEquation() {
        double[] cs = coefficients;
        return String.format("d(x) = %.2f %c %.2fx1 %c %.2fx2 %c %.2fx1x2", cs[0], sign(cs[1]), 4 * Math.abs(cs[1]), sign(cs[2]), 4 * Math.abs(cs[2]), sign(cs[3]), 16 * Math.abs(cs[3])) + "\n" +
                "(1*" + cs[0] + ", 2*" + cs[1] + ", 2*" + cs[2] + ", 4*" + cs[3] +
                ")*(1, 2x1, 2x2, 4x1x2)";
    }

    //корректно
    private void updateCoefficients(Data.Vector training_vec, double rho) {
        coefficients[0] += rho;
        coefficients[1] += rho * training_vec.x();
        coefficients[2] += rho * training_vec.y();
        coefficients[3] += rho * training_vec.x() * training_vec.y();
    }

    public Data.ClassLabel classify(Data.Vector vector) {
        return separatingFunction(vector) > 0 ? Data.ClassLabel.C1 : Data.ClassLabel.C2;
    }

    public double getBoundaryY(double x) {
        // d(vec) = cs[0] + 4*cs[1]*x + 4*cs[2]*y + 16*cs[3]*x*y = 0
        // y = -(cs[0] + 4*cs[1]*x) / (4*cs[2] + 16*cs[3]*x)

        double[] cs = coefficients;

        double denominator = 4 * cs[2] + 16 * cs[3] * x;
        if (Math.abs(denominator) < 1e-10)
            return Double.NaN;

        return -(cs[0] + 4 * cs[1] * x) / denominator;
    }

    public double[] coefficients() {
        return coefficients;
    }
}