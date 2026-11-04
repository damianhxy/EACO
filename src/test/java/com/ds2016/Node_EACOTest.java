package com.ds2016;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class Node_EACOTest {

    private static ArrayList<Node_GUI> nodes() {
        ArrayList<Node_GUI> nodes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            nodes.add(new Node_GUI());
        }
        return nodes;
    }

    private static ArrayList<SimpleEdge> triangle() {
        ArrayList<SimpleEdge> edges = new ArrayList<>();
        edges.add(new SimpleEdge(0, 1, 1, 1));
        edges.add(new SimpleEdge(1, 2, 1, 1));
        edges.add(new SimpleEdge(0, 2, 2, 1));
        return edges;
    }

    private static double pheromoneTotal(final Node_EACO node, final int destination) {
        HashMap<Integer, Double> entries = node.pheromone.M.get(destination);
        return entries.values().stream()
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Test
    void incidentEdgeToggleUpdatesEndpointPheromoneTables() {
        EACO algorithm = new EACO(0.4, 1, 15000, 0.1);
        algorithm.build(nodes(), triangle(), 0, 2);
        Node_EACO node0 = algorithm.nodes.get(0);
        Node_EACO node1 = algorithm.nodes.get(1);

        assertNotNull(node0.pheromone.get(2, 1));
        assertNotNull(node1.pheromone.get(2, 0));

        algorithm.toggleEdge(0);

        assertNull(node0.pheromone.get(2, 1));
        assertNull(node1.pheromone.get(2, 0));
        assertEquals(1.0, pheromoneTotal(node0, 2), 1e-9);
        assertEquals(1.0, pheromoneTotal(node1, 2), 1e-9);

        algorithm.toggleEdge(0);

        assertNotNull(node0.pheromone.get(2, 1));
        assertNotNull(node1.pheromone.get(2, 0));
        assertEquals(1.0, pheromoneTotal(node0, 2), 1e-9);
        assertEquals(1.0, pheromoneTotal(node1, 2), 1e-9);
    }
}
