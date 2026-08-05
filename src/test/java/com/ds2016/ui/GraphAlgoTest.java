package com.ds2016.ui;

import com.ds2016.EACO;
import com.ds2016.Link;
import com.ds2016.Node_GUI;
import com.ds2016.SimpleEdge;
import com.ds2016.listeners.GraphEventListener;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.Sink;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression tests for the load-colouring used by the graph view.
 * These exercise the compute logic without requiring a display.
 */
class GraphAlgoTest {

    private static Graph buildGraph(final int edgeCount) {
        final Graph graph = new SingleGraph("test");
        graph.setStrict(false);
        graph.setAutoCreate(true);
        // A line of edgeCount+1 nodes: every edge endpoint always exists.
        for (int i = 0; i <= edgeCount; i++) {
            graph.addNode(String.valueOf(i));
        }
        for (int e = 0; e < edgeCount; e++) {
            // Mirrors Gui.addEdge: one directed pair per undirected edge.
            graph.addEdge(String.valueOf(e + "f"), e, e + 1, true);
            graph.addEdge(String.valueOf(e + "b"), e + 1, e, true);
        }
        return graph;
    }

    private static GraphEventListener listener(final Graph graph) {
        return new GraphEventListener() {
            @Override
            public Graph onGraphUpdated() {
                return graph;
            }

            @Override
            public void onGraphTerminated(final Sink sink) {
            }
        };
    }

    @Test
    void computeDoesNotCrashWithDoubledEdgeStatus() {
        // getEdgeStatus() returns two entries per undirected edge; this used
        // to index graph edges past the last one and throw an NPE.
        final int edgeCount = 3;
        final Graph graph = buildGraph(edgeCount);
        final ArrayList<Node_GUI> nodes = new ArrayList<>();
        for (int i = 0; i <= edgeCount; i++) {
            nodes.add(new Node_GUI());
        }
        final ArrayList<SimpleEdge> edges = new ArrayList<>();
        for (int e = 0; e < edgeCount; e++) {
            edges.add(new SimpleEdge(e, e + 1, 5, 1));
        }
        final EACO algo = new EACO(0.4, 1, 15000, 100.0);
        algo.build(nodes, edges, 0, edgeCount);
        Link.sAlgorithm = algo;

        final GraphAlgo graphAlgo = new GraphAlgo(listener(graph));
        graphAlgo.init(graph);
        assertDoesNotThrow(graphAlgo::compute);

        for (Edge edge : graph.getEdgeSet()) {
            assertNotNull(edge.getAttribute("ui.class"));
        }
    }
}
