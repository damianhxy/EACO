package com.ds2016;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for pheromone corruption on saturated entries.
 * A degree-1 node initialises its only pheromone entries to exactly 1.0;
 * reinforcing such an entry used to produce NaN. A row saturated at TMAX
 * with the remaining entries at TMIN (within rounding) used to produce NaN
 * when reinforced by a tiny amount.
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

    /**
     * NSF node 9 for destination 6, as reached by ordinary simulation: the
     * destination link saturated at TMAX and the other three entries a few
     * ulps-worth of rounding either side of TMIN = 0.05 / 3.
     */
    private static void saturateRow(final HashMap2D<Integer, Integer, Double> pheromone) {
        pheromone.put(6, 6, 0.95);
        pheromone.put(6, 8, 0.016666666066666665);
        pheromone.put(6, 11, 0.016666667766666667);
        pheromone.put(6, 13, 0.016666666166666667);
    }

    private static void reinforce(final Object node, final int neighbour, final int destination,
                                  final double change) throws Exception {
        final Method update = node.getClass().getDeclaredMethod(
                "updateHeuristic", int.class, int.class, double.class);
        update.setAccessible(true);
        update.invoke(node, neighbour, destination, change);
    }

    @Test
    void antNetTinyReinforcementOfSaturatedRowStaysFinite() throws Exception {
        final AntNet algo = new AntNet(0.4, 1, 15000, 0.1);
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        final Node_AntNet node = algo.nodes.get(9);
        saturateRow(node.pheromone);
        reinforce(node, 6, 6, 6.94e-8);
        assertAllFinite(node.pheromone);
    }

    @Test
    void eacoTinyReinforcementOfSaturatedRowStaysFinite() throws Exception {
        final EACO algo = new EACO(0.4, 1, 15000, 0.1);
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        final Node_EACO node = algo.nodes.get(9);
        saturateRow(node.pheromone);
        reinforce(node, 6, 6, 6.94e-8);
        assertAllFinite(node.pheromone);
    }
}
