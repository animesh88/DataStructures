package com.vansh.ds;
import edu.princeton.cs.algs4.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class KruskalMSTStatistics {
    private static final double FLOATING_POINT_EPSILON = 1.0E-12;

    private double weight;                        // weight of MST
    private Queue<Edge> mst = new Queue<>();      // edges in MST

    public KruskalMSTStatistics(EdgeWeightedGraph G) {
        // create array of edges, sorted by weight
        Edge[] edges = new Edge[G.E()];
        int t = 0;
        for (Edge e : G.edges()) {
            edges[t++] = e;
        }
        Arrays.sort(edges);

        // run greedy algorithm
        UF uf = new UF(G.V());
        for (int i = 0; i < G.E() && mst.size() < G.V() - 1; i++) {
            Edge e = edges[i];
            int v = e.either();
            int w = e.other(v);

            // v-w does not create a cycle
            if (uf.find(v) != uf.find(w)) {
                uf.union(v, w);     // merge v and w components
                mst.enqueue(e);     // add edge e to mst
                weight += e.weight();
            }
        }

        // check optimality conditions
        assert check(G);
    }

    public Iterable<Edge> edges() {
        return mst;
    }

    public double weight() {
        return weight;
    }

    // check optimality conditions (takes time proportional to E V lg* V)
    private boolean check(EdgeWeightedGraph G) {
        // check total weight
        double total = 0.0;
        for (Edge e : edges()) {
            total += e.weight();
        }
        if (Math.abs(total - weight()) > FLOATING_POINT_EPSILON) {
            System.err.printf("Weight of edges does not equal weight(): %f vs. %f\n", total, weight());
            return false;
        }

        // check that it is acyclic
        UF uf = new UF(G.V());
        for (Edge e : edges()) {
            int v = e.either(), w = e.other(v);
            if (uf.find(v) == uf.find(w)) {
                System.err.println("Not a forest");
                return false;
            }
            uf.union(v, w);
        }

        // check that it is a spanning forest
        for (Edge e : G.edges()) {
            int v = e.either(), w = e.other(v);
            if (uf.find(v) != uf.find(w)) {
                System.err.println("Not a spanning forest");
                return false;
            }
        }

        // check that it is a minimal spanning forest (cut optimality conditions)
        for (Edge e : edges()) {
            // all edges in MST except e
            uf = new UF(G.V());
            for (Edge f : mst) {
                int x = f.either(), y = f.other(x);
                if (f != e) uf.union(x, y);
            }

            // check that e is min weight edge in crossing cut
            for (Edge f : G.edges()) {
                int x = f.either(), y = f.other(x);
                if (uf.find(x) != uf.find(y)) {
                    if (f.weight() < e.weight()) {
                        System.err.println("Edge " + f + " violates cut optimality conditions");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        List<String> datasetPaths = Arrays.asList(
                "C:/Users/VANSH TOLANI/Downloads/hu.txt",
                "C:/Users/VANSH TOLANI/Downloads/ro.txt",
                "C:/Users/VANSH TOLANI/Downloads/hr.txt"
        );

        List<EdgeWeightedGraph> graphs = new ArrayList<>();
        List<KruskalMSTStatistics> msts = new ArrayList<>();

        for (String datasetPath : datasetPaths) {
            In in = new In(datasetPath);
            EdgeWeightedGraph G = new EdgeWeightedGraph(in);
            KruskalMSTStatistics mst = new KruskalMSTStatistics(G);
            graphs.add(G);
            msts.add(mst);
        }

        double totalWeight = 0.0;
        double minWeight = Double.POSITIVE_INFINITY;
        double maxWeight = Double.NEGATIVE_INFINITY;
        List<Double> allWeights = new ArrayList<>();

        for (int i = 0; i < graphs.size(); i++) {
            EdgeWeightedGraph G = graphs.get(i);
            KruskalMSTStatistics mst = msts.get(i);

            double weight = mst.weight();
            totalWeight += weight;
            minWeight = Math.min(minWeight, weight);
            maxWeight = Math.max(maxWeight, weight);
            allWeights.add(weight);

            for (Edge e : mst.edges()) {
                StdOut.println(e);
            }
        }

        int totalTrees = datasetPaths.size();
        double combinedMean = totalWeight / totalTrees;

        double[] weightsArray = allWeights.stream().mapToDouble(Double::doubleValue).toArray();
        Arrays.sort(weightsArray);
        double combinedMedian;
        int totalVertices = weightsArray.length;
        if (totalVertices % 2 == 0) {
            combinedMedian = (weightsArray[totalVertices / 2 - 1] + weightsArray[totalVertices / 2]) / 2.0;
        } else {
            combinedMedian = weightsArray[totalVertices / 2];
        }

        StdOut.println("Combined Mean: " + combinedMean);
        StdOut.println("Combined Median: " + combinedMedian);
        StdOut.println("Combined Minimum: " + minWeight);
        StdOut.println("Combined Maximum: " + maxWeight);
    }
}