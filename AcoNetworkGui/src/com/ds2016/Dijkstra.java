package com.ds2016;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Created by damian on 28/5/16.
 */
public class Dijkstra {

    private final static int INF = 1000000000, K = 4;
    private final static Comparator<Pair<Integer, Integer>> pairCMP =
            Comparator.comparing(Pair::getKey);
    public final ArrayList<ArrayList<Integer>> P = new ArrayList<>(); // Best parents
    private final ArrayList<Integer> CNT = new ArrayList<>(); // Current count
    private final ArrayList<Integer> D = new ArrayList<>(); // Distance

    /**
     * Initialize Dijkstra data structure
     *
     * @param source Source node
     * @param nodes  ArrayList of Node_OSPF
     * @param adjMat Adjacency Matrix
     */
    Dijkstra(int source, ArrayList<Node_OSPF> nodes, HashMap2D<Integer, Integer, Edge> adjMat) {
        final PriorityQueue<Pair<Integer, Integer>> PQ = new PriorityQueue<>(nodes.size(), pairCMP);
        for (int a = 0; a < nodes.size(); ++a) {
            D.add(INF);
            P.add(new ArrayList<>());
            CNT.add(0);
        }
        D.set(source, 0);
        // Add neighbours
        HashMap<Integer, Edge> adj = adjMat.get(source);
        if (adj != null) for (Edge edge : adj.values()) {
            D.set(edge.destination, edge.cost);
            P.get(edge.destination).add(edge.destination);
            PQ.add(new Pair<>(edge.destination, edge.cost));
        }
        // Dijkstra
        while (!PQ.isEmpty()) {
            Pair<Integer, Integer> top = PQ.poll();
            if (!D.get(top.getKey()).equals(top.getValue())) continue;
            HashMap<Integer, Edge> adj2 = adjMat.get(top.getKey());
            if (adj2 == null) continue;
            for (Edge edge : adj2.values()) {
                if (edge.isOffline) continue;
                if (nodes.get(edge.destination).isOffline) continue;
                int nc = top.getValue() + edge.cost;
                if (nc < D.get(edge.destination)) {
                    D.set(edge.destination, nc);
                    // Add parents
                    P.get(edge.destination).clear();
                    for (Integer parent : P.get(top.getKey())) {
                        P.get(edge.destination).add(parent);
                    }
                    PQ.add(new Pair<>(edge.destination, nc));
                } else if (nc == D.get(edge.destination)) {
                    // Add parents
                    for (Integer parent : P.get(top.getKey())) {
                        if (P.get(edge.destination).size() == K) break;
                        P.get(edge.destination).add(parent);
                    }
                }
            }
        }
    }

    /**
     * Load balancing
     * Return the next hop for a given destination
     *
     * @param destination Destination node
     * @return Neighbour node
     */
    int next(int destination) {
        CNT.set(destination, (CNT.get(destination) + 1) % P.get(destination).size());
        return P.get(destination).get(CNT.get(destination));
    }
}
