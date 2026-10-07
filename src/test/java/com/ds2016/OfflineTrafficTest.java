package com.ds2016;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests: once a node or link goes offline, the traffic that its
 * neighbours had already queued for it must not keep flowing into it.
 */
class OfflineTrafficTest {

    private static AlgorithmBase congested(final Supplier<AlgorithmBase> factory) {
        final AlgorithmBase algo = factory.get();
        algo.build(TestNetworks.nsfNodes(), TestNetworks.nsfEdges(), 0, 6);
        // Three packets per tick over unit-bandwidth links builds a backlog
        // at the source towards each of its neighbours.
        for (int t = 0; t < 2000; t++) {
            algo.tick();
        }
        return algo;
    }

    private static void assertOfflineNodeReceivesNothing(final Supplier<AlgorithmBase> factory) {
        final AlgorithmBase algo = congested(factory);
        algo.toggleNode(1);
        for (int t = 0; t < 200; t++) {
            algo.tick();
            assertEquals(0, (int) algo.getNodeStatus().get(1), "packets queued at offline node 1");
        }
    }

    private static void assertOfflineEdgeCarriesNothing(final Supplier<AlgorithmBase> factory) {
        final AlgorithmBase algo = congested(factory);
        algo.toggleEdge(0); // 0-1
        for (int t = 0; t < 200; t++) {
            algo.tick();
            assertEquals(0, algo.getEdgeStatus().get(0) + algo.getEdgeStatus().get(1),
                    "packets in flight on offline edge 0-1");
        }
    }

    @Test
    void antNetStopsSendingToOfflineNode() {
        assertOfflineNodeReceivesNothing(() -> new AntNet(0.4, 3, 15000, 0.1));
    }

    @Test
    void antNetStopsSendingOverOfflineEdge() {
        assertOfflineEdgeCarriesNothing(() -> new AntNet(0.4, 3, 15000, 0.1));
    }

    @Test
    void eacoStopsSendingToOfflineNode() {
        assertOfflineNodeReceivesNothing(() -> new EACO(0.4, 3, 15000, 0.1));
    }

    @Test
    void eacoStopsSendingOverOfflineEdge() {
        assertOfflineEdgeCarriesNothing(() -> new EACO(0.4, 3, 15000, 0.1));
    }

    @Test
    void ospfStopsSendingToOfflineNode() {
        assertOfflineNodeReceivesNothing(() -> new OSPF(15000, 3));
    }

    @Test
    void ospfStopsSendingOverOfflineEdge() {
        assertOfflineEdgeCarriesNothing(() -> new OSPF(15000, 3));
    }
}
