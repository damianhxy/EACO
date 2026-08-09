package com.ds2016;

import java.util.ArrayList;

/**
 * Test topologies shared across the test suite.
 */
final class TestNetworks {

    private TestNetworks() {
    }

    /**
     * The 14-node NSF topology used as the default GUI network.
     */
    static ArrayList<Node_GUI> nsfNodes() {
        ArrayList<Node_GUI> nodes = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            nodes.add(new Node_GUI());
        }
        return nodes;
    }

    static ArrayList<SimpleEdge> nsfEdges() {
        ArrayList<SimpleEdge> edges = new ArrayList<>();
        int[][] e = {
                {0, 1, 9, 1}, {0, 2, 9, 1}, {0, 3, 7, 1}, {1, 3, 13, 1}, {1, 6, 20, 1},
                {2, 4, 70, 1}, {2, 7, 16, 1}, {3, 10, 15, 1}, {4, 5, 7, 1}, {4, 10, 11, 1},
                {5, 6, 7, 1}, {6, 9, 7, 1}, {7, 8, 5, 1}, {7, 13, 8, 1}, {8, 9, 5, 1},
                {8, 12, 7, 1}, {9, 11, 8, 1}, {9, 13, 8, 1}, {10, 11, 9, 1}, {10, 12, 14, 1},
                {12, 13, 4, 1}
        };
        for (int[] x : e) {
            edges.add(new SimpleEdge(x[0], x[1], x[2], x[3]));
        }
        return edges;
    }
}
