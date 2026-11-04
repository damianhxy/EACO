package com.ds2016;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression tests for adding nodes mid-simulation.
 */
class AddNodeTest {

    private static void runTicks(final AlgorithmBase algo, final int ticks) {
        for (int t = 0; t < ticks; t++) {
            algo.tick();
        }
    }

    @ForEachAlgorithm
    void survivesMidRunNodeAddition(final Algorithm algorithm) {
        final AlgorithmBase algo = algorithm.create();
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        runTicks(algo, 1000);

        assertDoesNotThrow(() -> {
            algo.addNode();
            algo.addEdge(algorithm.nodeCount(algo) - 1, 0, 5, 1);
            runTicks(algo, 100);
        });
    }

    @Test
    void ospfInitialisesNewNodeRoutingTable() {
        final OSPF algo = new OSPF(15000, 1);
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        runTicks(algo, 1000);

        algo.addNode();
        final Node_OSPF newNode = algo.nodes.get(algo.nodes.size() - 1);
        assertNotNull(newNode.SSSP, "new OSPF node must have a routing table");
    }
}
