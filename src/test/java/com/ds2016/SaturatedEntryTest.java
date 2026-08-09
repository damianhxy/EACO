package com.ds2016;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for pheromone corruption on saturated entries.
 * A degree-1 node initialises its only pheromone entries to exactly 1.0;
 * reinforcing such an entry used to produce NaN.
 */
class SaturatedEntryTest {

    private static void runTwoNodeNetwork(final AlgorithmBase algo) {
        final ArrayList<Node_GUI> nodes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            nodes.add(new Node_GUI());
        }
        final ArrayList<SimpleEdge> edges = new ArrayList<>();
        edges.add(new SimpleEdge(0, 1, 5, 1));
        algo.build(nodes, edges, 0, 1);
        assertDoesNotThrow(() -> {
            // Interval 0.1s => one ant per 100 ticks, so ants run regularly.
            for (int t = 0; t < 5000; t++) {
                algo.tick();
            }
        });
    }

    private static void assertAllFinite(final HashMap2D<Integer, Integer, Double> pheromone) {
        for (Map.Entry<Integer, HashMap<Integer, Double>> row : pheromone.M.entrySet()) {
            for (Map.Entry<Integer, Double> e : row.getValue().entrySet()) {
                assertTrue(Double.isFinite(e.getValue()),
                        "non-finite pheromone for destination " + row.getKey()
                                + " via " + e.getKey() + ": " + e.getValue());
            }
        }
    }

    @Test
    void antNetKeepsDegreeOnePheromoneFinite() {
        final AntNet algo = new AntNet(0.4, 1, 15000, 0.1);
        runTwoNodeNetwork(algo);
        assertAllFinite(algo.nodes.get(0).pheromone);
    }

    @Test
    void eacoKeepsDegreeOnePheromoneFinite() {
        final EACO algo = new EACO(0.4, 1, 15000, 0.1);
        runTwoNodeNetwork(algo);
        assertAllFinite(algo.nodes.get(0).pheromone);
    }
}
