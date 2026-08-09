package com.ds2016;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Regression tests for the AntNet neighbour-viability accounting when
 * nodes and edges are toggled offline in overlapping combinations.
 */
class Node_AntNetTest {

    private static int numNeighbours(final Node_AntNet node) throws Exception {
        final Field f = Node_AntNet.class.getDeclaredField("numNeighbours");
        f.setAccessible(true);
        return f.getInt(node);
    }

    private static AntNet builtAntNet() {
        final AntNet algo = new AntNet(0.4, 1, 15000, 100.0);
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        for (int t = 0; t < 1000; t++) {
            algo.tick();
        }
        return algo;
    }

    @Test
    void neighbourCountIsStableWhenNodeThenEdgeTogglesOffline() throws Exception {
        final AntNet algo = builtAntNet();
        final Node_AntNet node0 = algo.nodes.get(0);

        assertEquals(3, numNeighbours(node0));
        algo.toggleNode(1);   // node 1 offline, edge (0,1) still up
        assertEquals(2, numNeighbours(node0));
        assertDoesNotThrow(algo::tick);
        algo.toggleEdge(0);   // edge (0,1) offline too: neighbour removed only once
        assertEquals(2, numNeighbours(node0));
        assertDoesNotThrow(algo::tick);
        algo.toggleEdge(0);   // edge back up, node still down: not re-added
        assertEquals(2, numNeighbours(node0));
        algo.toggleNode(1);   // node back up
        assertEquals(3, numNeighbours(node0));
        assertDoesNotThrow(algo::tick);
    }

    @Test
    void neighbourCountIsStableWhenEdgeThenNodeTogglesOffline() throws Exception {
        final AntNet algo = builtAntNet();
        final Node_AntNet node0 = algo.nodes.get(0);

        assertEquals(3, numNeighbours(node0));
        algo.toggleEdge(0);   // edge (0,1) offline
        assertEquals(2, numNeighbours(node0));
        assertDoesNotThrow(algo::tick);
        algo.toggleNode(1);   // node 1 offline too: neighbour removed only once
        assertEquals(2, numNeighbours(node0));
        assertDoesNotThrow(algo::tick);
        algo.toggleNode(1);   // node back up, edge still down: not re-added
        assertEquals(2, numNeighbours(node0));
        algo.toggleEdge(0);   // edge back up
        assertEquals(3, numNeighbours(node0));
        assertDoesNotThrow(algo::tick);
    }
}
