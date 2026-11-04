package com.ds2016;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Long-running smoke tests that toggle nodes and edges mid-simulation.
 */
class SimulationStressTest {

    private static final ArrayList<Node_GUI> NODES = TestNetworks.nsfNodes();
    private static final ArrayList<SimpleEdge> EDGES = TestNetworks.nsfEdges();

    @ForEachAlgorithm
    void survivesThirtyThousandTicks(final Algorithm algorithm) {
        final AlgorithmBase algo = algorithm.create();
        algo.build(NODES, EDGES, 0, 6);
        assertDoesNotThrow(() -> {
            for (int t = 0; t < 30000; t++) {
                algo.tick();
                if (t % 5000 == 4999) {
                    // Toggle a non source/destination node (7..11) and an edge.
                    final int node = (t / 5000) % 5 + 7;
                    final int edge = (t / 5000) % 21;
                    algo.toggleNode(node);
                    algo.toggleEdge(edge);
                    algo.toggleEdge(edge);
                    algo.toggleNode(node);
                }
            }
        });
    }
}
