package com.ds2016;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression tests for adding nodes mid-simulation.
 */
class AddNodeTest {

    @Test
    void ospfInitialisesNewNodeRoutingTable() {
        final OSPF algo = new OSPF(15000, 1);
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        for (int t = 0; t < 1000; t++) {
            algo.tick();
        }

        algo.addNode();
        final Node_OSPF newNode = algo.nodes.get(algo.nodes.size() - 1);
        assertNotNull(newNode.SSSP, "new OSPF node must have a routing table");

        assertDoesNotThrow(() -> {
            algo.addEdge(newNode.ID, 0, 5, 1);
            for (int t = 0; t < 100; t++) {
                algo.tick();
            }
        });
    }

    @Test
    void antNetSurvivesMidRunNodeAddition() {
        final AntNet algo = new AntNet(0.4, 1, 15000, 100.0);
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        for (int t = 0; t < 1000; t++) {
            algo.tick();
        }

        assertDoesNotThrow(() -> {
            algo.addNode();
            algo.addEdge(algo.nodes.size() - 1, 0, 5, 1);
            for (int t = 0; t < 100; t++) {
                algo.tick();
            }
        });
    }

    @Test
    void eacoSurvivesMidRunNodeAddition() {
        final EACO algo = new EACO(0.4, 1, 15000, 100.0);
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        for (int t = 0; t < 1000; t++) {
            algo.tick();
        }

        assertDoesNotThrow(() -> {
            algo.addNode();
            algo.addEdge(algo.nodes.size() - 1, 0, 5, 1);
            for (int t = 0; t < 100; t++) {
                algo.tick();
            }
        });
    }
}
