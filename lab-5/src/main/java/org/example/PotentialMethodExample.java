package org.example;

import java.util.ArrayList;
import java.util.List;

public class PotentialMethodExample {
    public static void main(String[] args) {
        List<Data.Vector> points = List.of (
                new Data.Vector(-1, 0, Data.ClassLabel.C1),
                new Data.Vector(1, 1,  Data.ClassLabel.C1),
                new Data.Vector(2, 0,  Data.ClassLabel.C2),
                new Data.Vector(1, -2, Data.ClassLabel.C2)
        );

        PotentialMethod pm = new PotentialMethod();

        pm.trainFull(points);

        System.out.println("\nИтоговая разделяющая функция:");
        System.out.println(pm.getSeparatingFunctionEquation());
        System.out.println("d(x) = 1 - 8x1 + 4x2 + 16x1x2 (методичка)");
    }
}