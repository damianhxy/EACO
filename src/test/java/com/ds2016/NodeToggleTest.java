package com.ds2016;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Regression tests for crashes when toggling nodes and edges offline
 * mid-simulation.
 */
class NodeToggleTest {

    private static final ArrayList<Node_GUI> NODES = TestNetworks.nsfNodes();
    private static final ArrayList<SimpleEdge> EDGES = TestNetworks.nsfEdges();

    private static void runTicks(final AlgorithmBase algo, final int ticks) {
        for (int t = 0; t < ticks; t++) {
            algo.tick();
        }
    }

    @ForEachAlgorithm
    void survivesNodeAndEdgeToggles(final Algorithm algorithm) {
        final AlgorithmBase algo = algorithm.create();
        algo.build(NODES, EDGES, 0, 6);
        runTicks(algo, 1000);
        // Edge 0 is (0,1); node 1 is neither source (0) nor destination (6).
        assertDoesNotThrow(() -> {
            algo.toggleNode(1);
            algo.toggleEdge(0);
            runTicks(algo, 100);
        }, "crashed toggling node then adjacent edge offline");
        // Restore and try the opposite order.
        assertDoesNotThrow(() -> {
            algo.toggleEdge(0);
            algo.toggleNode(1);
            runTicks(algo, 100);
        }, "crashed toggling edge then adjacent node offline");
    }
}
