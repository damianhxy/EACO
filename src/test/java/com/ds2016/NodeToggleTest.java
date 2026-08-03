package com.ds2016;

import org.junit.jupiter.api.Test;

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

    private static void assertSurvivesToggle(final String name, final AlgorithmBase algo) {
        algo.build(NODES, EDGES, 0, 6);
        runTicks(algo, 1000);
        // Edge 0 is (0,1); node 1 is neither source (0) nor destination (6).
        assertDoesNotThrow(() -> {
            algo.toggleNode(1);
            algo.toggleEdge(0);
            runTicks(algo, 100);
        }, name + " crashed toggling node then adjacent edge offline");
        // Restore and try the opposite order.
        assertDoesNotThrow(() -> {
            algo.toggleEdge(0);
            algo.toggleNode(1);
            runTicks(algo, 100);
        }, name + " crashed toggling edge then adjacent node offline");
    }

    @Test
    void antNetSurvivesNodeAndEdgeToggles() {
        assertSurvivesToggle("AntNet", new AntNet(0.4, 1, 15000, 100.0));
    }

    @Test
    void eacoSurvivesNodeAndEdgeToggles() {
        assertSurvivesToggle("EACO", new EACO(0.4, 1, 15000, 100.0));
    }

    @Test
    void ospfSurvivesNodeAndEdgeToggles() {
        assertSurvivesToggle("OSPF", new OSPF(15000, 1));
    }
}
