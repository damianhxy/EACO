package com.ds2016;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by damian on 28/5/16.
 */
public class Node_OSPF {

    final int ID;
    final HashMap<Integer, ArrayDeque<Packet>> Q = new HashMap<>();
    private final ArrayList<Node_OSPF> nodes;
    private final HashMap2D<Integer, Integer, Edge> adjMat;
    public Dijkstra SSSP;
    boolean isOffline;

    int tripTime, numPackets;

    /**
     * Initialize a node
     *
     * @param _nodes  ArrayList of Node_OSPF
     * @param _adjMat Adjacency Matrix
     */
    Node_OSPF(ArrayList<Node_OSPF> _nodes, HashMap2D<Integer, Integer, Edge> _adjMat) {
        ID = _nodes.size();
        nodes = _nodes;
        adjMat = _adjMat;
    }

    /**
     * React to updates
     */
    void update() {
        SSSP = new Dijkstra(ID, nodes, adjMat);
    }

    /**
     * Calculate next best hop, for a
     * given destination
     *
     * @param packet Packet being processed
     * @return Neighbour for next hop
     */
    private int nextHop(Packet packet) {
        return SSSP.next(packet.destination);
    }

    /**
     * Process a packet
     *
     * @param packet Packet
     * @return 1, if packet has reached destination
     */
    int process(Packet packet) {
        if (packet.destination == ID) {
            if (Main.DEBUG_LATENCIES) {
                tripTime += (packet.timestamp - packet.getCreationTime());
                ++numPackets;
            }
            return 1;
        }
        int nxt = nextHop(packet);
        if (nxt < 0) {
            return 0; // No route; drop packet
        }
        Q.putIfAbsent(nxt, new ArrayDeque<>());
        Q.get(nxt).add(packet);
        return 0;
    }

    /**
     * Calculate size of backlog
     *
     * @return Number of packets in link-queues
     */
    int numberOfPackets() {
        int total = 0;
        for (Integer neighbour : Q.keySet()) {
            total += Q.get(neighbour).size();
        }
        return total;
    }

    /**
     * Clear the packet queue
     */
    void clearQ() {
        Q.clear();
    }

    /**
     * Toggle isOffline
     */
    void toggle() {
        isOffline ^= true;
    }
}
