package com.ds2016;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by damian on 22/12/16.
 */
public class Graph {

    private final int numNodes, source;
    private final HashMap2D<Integer, Integer, Edge> adjMat;
    private final ArrayList<Integer> dfs_low = new ArrayList<>();
    private final ArrayList<Integer> dfs_num = new ArrayList<>();
    private final ArrayList<Integer> dfs_parent = new ArrayList<>();
    private final ArrayList<Boolean> articulation_point = new ArrayList<>();
    int numAP, endNode;
    private int dfsCounter, rootChildren;


    /**
     * Initialise graph data structure
     * Used primarily by the random graph generator
     *
     * @param _source   Source Node
     * @param _numNodes Number of nodes
     * @param _adjMat   Adjacency Matrix
     */
    Graph(int _source, int _numNodes, HashMap2D<Integer, Integer, Edge> _adjMat) {
        source = _source;
        numNodes = _numNodes;
        adjMat = _adjMat;
        endNode = calculateFurthest();
        numAP = calculateAP();
    }

    /**
     * Find a suitable end node
     *
     * @return Furthest node from source in terms of hops
     * @throws IllegalStateException if graph is not connected
     */
    private int calculateFurthest() throws IllegalStateException {
        ArrayList<Integer> Distance = new ArrayList<>();
        ArrayDeque<Integer> Queue = new ArrayDeque<>();
        for (int a = 0; a < numNodes; ++a) {
            Distance.add(-1);
        }
        Distance.set(source, 0);
        Queue.add(source);
        while (!Queue.isEmpty()) {
            int node = Queue.poll();
            int cost = Distance.get(node);
            for (Edge edge : adjMat.get(node).values()) {
                // Assuming edges are not offline
                // Assuming nodes are not offline
                if (Distance.get(edge.destination) == -1) {
                    Distance.set(edge.destination, cost + 1);
                    Queue.add(edge.destination);
                }
            }
        }
        int best = 0, maxDist = 0;
        for (int a = 0; a < numNodes; ++a) {
            if (Distance.get(a) == -1)
                throw new IllegalStateException("Graph is not connected");
            if (Distance.get(a) > maxDist) {
                maxDist = Distance.get(a);
                best = a;
            }
        }
        return best;
    }

    /**
     * AP DFS Subroutine
     *
     * @param node Current node
     */
    private void DFS(int node) {
        dfs_low.set(node, dfsCounter);
        dfs_num.set(node, dfsCounter);
        ++dfsCounter;
        for (Edge edge : adjMat.get(node).values()) {
            if (dfs_num.get(edge.destination) == -1) {
                dfs_parent.set(edge.destination, node);
                DFS(edge.destination);
                if (dfs_parent.get(node) == -1) ++rootChildren;
                if (dfs_low.get(edge.destination) >= dfs_num.get(node)) {
                    articulation_point.set(node, true);
                }
                dfs_low.set(node, Math.min(dfs_low.get(node), dfs_low.get(edge.destination)));
            } else if (edge.destination != dfs_parent.get(node)) {
                dfs_low.set(node, Math.min(dfs_low.get(node), dfs_num.get(edge.destination)));
            }
        }
    }

    /**
     * Find articulation points
     *
     * @return Number of articulation points in the graph
     */
    private int calculateAP() {
        for (int a = 0; a < numNodes; ++a) {
            dfs_low.add(0);
            dfs_num.add(-1);
            dfs_parent.add(0);
            articulation_point.add(false);
        }
        dfs_parent.set(source, -1);
        DFS(source);
        for (int a = 0; a < numNodes; ++a) {
            if (dfs_num.get(a) == -1) {
                dfs_parent.set(a, -1);
                DFS(a);
            }
        }
        articulation_point.set(source, rootChildren > 1);
        int cnt = 0;
        for (int a = 0; a < numNodes; ++a) {
            if (articulation_point.get(a)) {
                ++cnt;
            }
        }
        return cnt;
    }
}
