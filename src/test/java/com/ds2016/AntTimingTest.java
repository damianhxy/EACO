package com.ds2016;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the ant trip-time accounting.
 */
class AntTimingTest {

    @Test
    void totalTimeAccumulatesPerNodeSubPaths() {
        final Ant ant = new Ant(0, 2, 15000, 0);
        ant.timings.add(1.0); // dep(S->A)
        ant.timings.add(2.0); // cost(S,A)
        ant.timings.add(3.0); // dep(A->D)
        ant.timings.add(4.0); // cost(A,D)
        ant.updateTotalTime(); // at A: the A->D sub-path
        assertEquals(7.0, ant.totalTime);
        ant.updateTotalTime(); // at S: the full S->D trip
        assertEquals(10.0, ant.totalTime);
        assertTrue(ant.timings.isEmpty());
    }

    @Test
    void totalTimeIsNotDoubleCountedOnLaterCalls() {
        final Ant ant = new Ant(0, 6, 15000, 0);
        ant.timings.add(1.0);
        ant.timings.add(2.0);
        ant.updateTotalTime();
        ant.updateTotalTime();
        assertEquals(3.0, ant.totalTime);
        assertTrue(ant.timings.isEmpty());
    }

    @Test
    void forwardAntRecordsOnlyPerHopTimings() {
        // An ant travelling 0 -> 1 -> 2 records exactly one (queueing, cost)
        // pair per forward hop and nothing extra at the destination, so the
        // trip time is the forward (data-path) latency.
        final AntNet algo = new AntNet(0.4, 1, 15000, 100.0);
        final ArrayList<com.ds2016.Node_GUI> nodes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            nodes.add(new com.ds2016.Node_GUI());
        }
        final ArrayList<com.ds2016.SimpleEdge> edges = new ArrayList<>();
        edges.add(new com.ds2016.SimpleEdge(0, 1, 5, 1));
        edges.add(new com.ds2016.SimpleEdge(1, 2, 7, 1));
        algo.build(nodes, edges, 0, 2);
        for (int t = 0; t < 100; t++) {
            algo.tick();
        }

        final Ant ant = new Ant(0, 2, 15000, algo.getCurrentTime());
        algo.nodes.get(0).processAnt(ant);
        algo.nodes.get(1).processAnt(ant);
        algo.nodes.get(2).processAnt(ant);

        assertEquals(4, ant.timings.size());
    }
}
