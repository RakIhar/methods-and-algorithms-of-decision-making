package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HierarchicalClustering {

    public record Cluster(List<Integer> signs, double distance, Cluster left, Cluster right) {
        public boolean isLeaf() {
            return left == null && right == null;
        }

        @Override
        public String toString() {
            return "Cluster{signs=" + signs + ", dist=" + String.format("%.4f", distance) + "}";
        }
    }

    public static Cluster hierarchyByMin(Data data) {
        List<Cluster> current = new ArrayList<>();
        for (int i = 0; i < data.objects[0].length; i++) {
            current.add(new Cluster(Collections.singletonList(i), 0.0, null, null));
        }

        while (current.size() > 1) {
            int bestI = 0, bestJ = 0;
            double minDist = Double.MAX_VALUE;

            for (int i = 0; i < current.size(); i++) {
                for (int j = i + 1; j < current.size(); j++) { //перебор всех кластеров со всеми на текущем этапе
                    for (int a : current.get(i).signs) {
                        for (int b : current.get(j).signs) {
                            double d = data.objects[a][b];
                            if (d < minDist) {
                                minDist = d;
                                bestI = i;
                                bestJ = j;
                            }
                        }
                    }
                }
            }

            Cluster merged = new Cluster(
                    mergeSigns(current.get(bestI).signs, current.get(bestJ).signs),
                    minDist,
                    current.get(bestI),
                    current.get(bestJ)
            );

            List<Cluster> next = new ArrayList<>();
            for (int k = 0; k < current.size(); k++) {
                if (k != bestI && k != bestJ) {
                    next.add(current.get(k));
                }
            }
            next.add(merged);
            current = next;
        }

        return current.getFirst();
    }

    public static Cluster hierarchyByMax(Data data) {
        List<Cluster> current = new ArrayList<>();
        for (int i = 0; i < data.objects[0].length; i++) {
            current.add(new Cluster(Collections.singletonList(i), 0.0, null, null));
        }

        while (current.size() > 1) {
            int bestI = 0, bestJ = 0;
            double minDist = Double.MAX_VALUE;

            for (int i = 0; i < current.size(); i++) {
                for (int j = i + 1; j < current.size(); j++) { //перебор всех кластеров со всеми на текущем этапе
                    for (int a : current.get(i).signs) {
                        for (int b : current.get(j).signs) {
                            double d = data.reversed[a][b];
                            if (d < minDist) {
                                minDist = d;
                                bestI = i;
                                bestJ = j;
                            }
                        }
                    }
                }
            }

            Cluster merged = new Cluster(
                    mergeSigns(current.get(bestI).signs, current.get(bestJ).signs),
                    minDist,
                    current.get(bestI),
                    current.get(bestJ)
            );

            List<Cluster> next = new ArrayList<>();
            for (int k = 0; k < current.size(); k++) {
                if (k != bestI && k != bestJ) {
                    next.add(current.get(k));
                }
            }
            next.add(merged);
            current = next;
        }

        return current.getFirst();
    }

    private static List<Integer> mergeSigns(List<Integer> a, List<Integer> b) {
        List<Integer> result = new ArrayList<>(a);
        result.addAll(b);
        Collections.sort(result);
        return result;
    }
}