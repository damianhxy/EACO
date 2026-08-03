package com.ds2016;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DijkstraTest {

    private static ArrayList<Node_OSPF> nodes(final int count) {
        final ArrayList<Node_OSPF> ns = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ns.add(new Node_OSPF(ns, new HashMap2D<>()));
        }
        return ns;
    }

    private static HashMap2D<Integer, Integer, Edge> adjMat(final int[][] undirectedEdges) {
        final HashMap2D<Integer, Integer, Edge> adj = new HashMap2D<>();
        for (int[] e : undirectedEdges) {
            adj.put(e[0], e[1], new Edge(e[0], e[1], e[2], e[3]));
            adj.put(e[1], e[0], new Edge(e[1], e[0], e[2], e[3]));
        }
        return adj;
    }

    @Test
    void findsCheapestParent() {
        // 0-1 (2), 0-2 (5), 1-2 (1), 2-3 (3). Cheapest 0->3 is 0-1-2-3 = 6.
        final Dijkstra d = new Dijkstra(0, nodes(4),
                adjMat(new int[][]{{0, 1, 2, 1}, {0, 2, 5, 1}, {1, 2, 1, 1}, {2, 3, 3, 1}}));
        assertEquals(List.of(1), d.P.get(3));
        assertEquals(1, d.next(3));
    }

    @Test
    void loadBalancesAcrossEqualPaths() {
        // 0-1 (2), 0-2 (2), 1-3 (2), 2-3 (2): two equal-cost 0->3 paths.
        final Dijkstra d = new Dijkstra(0, nodes(4),
                adjMat(new int[][]{{0, 1, 2, 1}, {0, 2, 2, 1}, {1, 3, 2, 1}, {2, 3, 2, 1}}));
        final List<Integer> parents = d.P.get(3);
        assertTrue(parents.contains(1) && parents.contains(2), "expected both parents, got " + parents);
        final List<Integer> hops = Arrays.asList(d.next(3), d.next(3), d.next(3), d.next(3));
        assertTrue(hops.contains(1) && hops.contains(2), "expected rotation through both parents, got " + hops);
    }

    @Test
    void returnsMinusOneForUnreachableDestination() {
        // Node 3 has no edges and is unreachable from 0.
        final Dijkstra d = new Dijkstra(0, nodes(4), adjMat(new int[][]{{0, 1, 5, 1}, {1, 2, 5, 1}}));
        assertEquals(-1, d.next(3));
    }
}
