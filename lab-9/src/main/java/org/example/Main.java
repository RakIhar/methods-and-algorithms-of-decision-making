package org.example;

import org.example.Perceptron;

import java.util.Arrays;
import java.util.List;

public class Main {
    void main() {
        double[][] weights = {
                {-0.3, 0.1, 0.4, -0.5},
                {0.1, 0.3, -0.3, 0.2},
                {0.4, -0.2, 0.1, 0.2}
        };

        Perceptron.Layer layer = new Perceptron.Layer(weights); //случайные теты
        Perceptron perceptron = new Perceptron(List.of(layer));
        double[] ока = new double[]{0.25, 0.25, 0.25, 0};
        double[] газель = new double[]{0.75, 0.75, 0.75, 0.25};
        double[] камаз = new double[]{1, 1, 0.25, 1};
        perceptron.fit(List.of(камаз, ока, газель), List.of(2, 0, 1), 100);
        IO.println(Arrays.toString(perceptron.identify(ока)));
        IO.println(Arrays.toString(perceptron.identify(газель)));
        IO.println(Arrays.toString(perceptron.identify(камаз)));
    }
}