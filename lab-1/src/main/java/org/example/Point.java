package org.example;

import java.util.*;

//1'000-100'000 образов - тут точки
//образ описывается признаками (вектором)
//признак - координата вектора
//2-20 классов
//класс - группа объектов, похожая друг на друга - номер группы
//ядро класса - точка-центр группы
class Point {
    double x, y;
    int clusterId = -1;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
// 1)
// r-этап - поиск ядра, тут считается расстояние
// |X_p - N_i(r)| < |X_p - N_j(r)|  для всех i!=j

// 2)
// Среднее арифметическое всех точек группы
// J_i = SUM |X_p - N_i(r+1)|^2 -> min

// 3)
// Выход, если на (r+1) ядра не меняются

class KMeansEngine {
    private List<Point> points = new ArrayList<>();
    private List<Point> centroids = new ArrayList<>();
    private int k;

    public KMeansEngine(int numPoints, int k) {
        this.k = k;
        Random rand = new Random();

        //образы
        for (int i = 0; i < numPoints; i++) {
            points.add(new Point(rand.nextDouble() * 100, rand.nextDouble() * 100));
        }

        // ядра
        for (int i = 0; i < k; i++) {
            Point p = points.get(rand.nextInt(numPoints));
            centroids.add(new Point(p.x, p.y));
        }
    }

//    public void calculate() {
//        boolean changed = true;
//        while (changed) {
//            changed = false;
//            // Минимальное расстояние
//            for (Point p : points) {
//                int nearest = 0;
//                double minDist = Double.MAX_VALUE;
//                for (int i = 0; i < k; i++) {
//                    double d = Math.sqrt(Math.pow(p.x - centroids.get(i).x, 2) + Math.pow(p.y - centroids.get(i).y, 2));
//                    if (d < minDist) {
//                        minDist = d;
//                        nearest = i;
//                    }
//                }
//
//                if (p.clusterId != nearest) {
//                    p.clusterId = nearest;
//                    changed = true;
//                }
//            }
//            // Пересчет ядер
//            for (int i = 0; i < k; i++) {
//                double sumX = 0, sumY = 0;
//                int count = 0;
//                for (Point p : points) {
//                    if (p.clusterId == i) {
//                        sumX += p.x; sumY += p.y; count++;
//                    }
//                }
//                if (count > 0) {
//                    centroids.get(i).x = sumX / count;
//                    centroids.get(i).y = sumY / count;
//                }
//            }
//        }
//    }


    public void singleStep() {
        // Привязка точек к ближайшим ядрам
        for (Point p : points) {
            int nearest = 0;
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < k; i++) {
                double d = Math.sqrt(Math.pow(p.x - centroids.get(i).x, 2) + Math.pow(p.y - centroids.get(i).y, 2));
                if (d < minDist) {
                    minDist = d;
                    nearest = i;
                }
            }
            p.clusterId = nearest;
        }

        // Пересчет координат ядер
        for (int i = 0; i < k; i++) {
            double sumX = 0, sumY = 0;
            int count = 0;
            for (Point p : points) {
                if (p.clusterId == i) {
                    sumX += p.x; sumY += p.y; count++;
                }
            }
            if (count > 0) {
                centroids.get(i).x = sumX / count;
                centroids.get(i).y = sumY / count;
            }
        }
    }

    public List<Point> getPoints() { return points; }
    public List<Point> getCentroids() { return centroids; }
}
