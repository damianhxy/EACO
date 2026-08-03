package com.ds2016;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by damian on 28/5/16.
 */
public class OSPF implements AlgorithmBase {

    public final ArrayList<Node_OSPF> nodes = new ArrayList<>();
    private final int TTL, traffic;
    private final ArrayList<Edge> edgeList = new ArrayList<>();
    private final HashMap2D<Integer, Integer, Edge> adjMat = new HashMap2D<>();
    private int source, destination;
    private int success, currentTime;
    private boolean didInit;

    /**
     * Initialize OSPF
     *
     * @param _TTL     Time To Live of packets
     * @param _traffic Packets per tick
     */
    public OSPF(int _TTL, int _traffic) {
        TTL = _TTL;
        traffic = _traffic;
    }

    /**
     * Initialize OSPF
     *
     * @param _source      Source node
     * @param _destination Destination node
     */
    public void init(int _source, int _destination) {
        currentTime = 0;
        source = _source;
        destination = _destination;
        for (Node_OSPF node : nodes) {
            node.update();
        }
        didInit = true;
    }

    @Override
    public int getCurrentTime() {
        return currentTime;
    }

    /**
     * Retrieve the current load of the network's nodes
     *
     * @return Number of packets on each node
     */
    public ArrayList<Integer> getNodeStatus() {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Node_OSPF node : nodes) {
            ret.add(node.numberOfPackets());
        }
        return ret;
    }

    /**
     * Retrieve the current load of the network's edges
     *
     * @return Number of packets on each edge
     */
    public ArrayList<Integer> getEdgeStatus() {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Edge edge : edgeList) {
            ret.add(edge.packets.size());
        }
        return ret;
    }

    /**
     * Add a new node
     */
    public void addNode() {
        nodes.add(new Node_OSPF(nodes, adjMat));
        nodes.get(nodes.size() - 1).update();
    }

    /**
     * Toggle state of anode
     *
     * @param ID Node ID
     * @throws IllegalArgumentException if ID is out of bounds
     */
    public void toggleNode(int ID) throws IllegalArgumentException {
        if (ID == source || ID == destination || ID >= nodes.size()) {
            throw new IllegalArgumentException();
        }
        Node_OSPF node = nodes.get(ID);
        node.toggle();
        if (node.isOffline) {
            node.clearQ();
            for (Edge edge : edgeList) {
                if (edge.source != ID && edge.destination != ID) continue;
                edge.clearPacketQ();
            }
        }
        if (didInit)
            for (Node_OSPF _node : nodes) {
                _node.update();
            }
    }

    /**
     * Add a bidirectional edge
     *
     * @param node1     First node
     * @param node2     Second node
     * @param cost      Time taken
     * @param bandwidth Packets per tick
     * @throws IllegalArgumentException if ID is out of bounds
     */
    public void addEdge(int node1, int node2, int cost, int bandwidth) throws IllegalArgumentException {
        if (node1 >= nodes.size() || node2 >= nodes.size()) {
            throw new IllegalArgumentException();
        }
        Edge forward = new Edge(node1, node2, cost, bandwidth);
        Edge backward = new Edge(node2, node1, cost, bandwidth);
        edgeList.add(forward);
        edgeList.add(backward);
        adjMat.put(node1, node2, forward);
        adjMat.put(node2, node1, backward);
        if (didInit)
            for (Node_OSPF node : nodes) {
                node.update();
            }
    }

    /**
     * Toggle state of an edge
     *
     * @param ID Edge ID
     */
    public void toggleEdge(int ID) {
        Edge forward = edgeList.get(ID * 2);
        Edge backward = edgeList.get(ID * 2 + 1);
        forward.toggle();
        backward.toggle();
        if (forward.isOffline) {
            forward.clearPacketQ();
            backward.clearPacketQ();
        }
        if (didInit)
            for (Node_OSPF node : nodes) {
                node.update();
            }
    }

    /**
     * Simulate node
     *
     * @param node Node being processed
     */
    private void processNode(Node_OSPF node) {
        // Process Packets
        for (Integer neighbour : node.Q.keySet()) {
            ArrayDeque<Packet> Q = node.Q.get(neighbour);
            Edge edge = adjMat.get(node.ID, neighbour);
            int bandwidth = edge.bandwidth;
            while (!Q.isEmpty() && bandwidth-- > 0) {
                edge.addPacket(Q.poll(), currentTime);
            }
        }
    }

    /**
     * Simulate edge
     *
     * @param edge Edge being processed
     */
    private void processEdge(Edge edge) {
        while (!edge.packets.isEmpty()) {
            if (edge.packets.peek().timestamp > currentTime) break;
            if (!edge.packets.peek().isValid(currentTime)) {
                edge.packets.poll();
                continue;
            }
            success += nodes.get(edge.destination).process(edge.packets.poll());
        }
    }

    /**
     * Generate packets from source
     */
    private void generatePackets() {
        Node_OSPF src = nodes.get(source);
        for (int cnt = 0; cnt < traffic; ++cnt) {
            src.process(new Packet(source, destination, TTL, currentTime));
        }
    }

    /**
     * One tick of the simulation
     *
     * @return Number of packets that reached their destination
     */
    public int tick() {
        success = 0;
        ++currentTime;
        for (Edge edge : edgeList) {
            if (edge.isOffline) continue;
            processEdge(edge);
        }
        generatePackets();
        for (Node_OSPF node : nodes) {
            if (node.isOffline) continue;
            processNode(node);
        }
        if (Main.DEBUG_LATENCIES) {
            if (currentTime % Main.DEBUG_NUM_TICKS_PER_UPDATE == 0) {
                double avgTripTime = (double) nodes.get(destination).tripTime / nodes.get(destination).numPackets;
                System.out.println("avgTripTime: " + avgTripTime);
                nodes.get(destination).tripTime = 0;
                nodes.get(destination).numPackets = 0;
            }
        }
        return success;
    }

    /**
     * Build sGraph from supplied information
     *
     * @param _nodes       Node_GUI nodes
     * @param _edgeList    SimpleEdge edges
     * @param _source      Source node
     * @param _destination Destination node
     */
    public void build(ArrayList<Node_GUI> _nodes, ArrayList<SimpleEdge> _edgeList, int _source, int _destination) {
        nodes.clear();
        edgeList.clear();
        adjMat.clear();
        // Node
        for (Node_GUI node : _nodes) {
            nodes.add(new Node_OSPF(nodes, adjMat));
            if (node.isOffline) {
                nodes.get(nodes.size() - 1).toggle();
            }
        }
        // Edge
        for (SimpleEdge edge : _edgeList) {
            Edge forward = new Edge(edge.source, edge.destination, edge.cost, edge.bandwidth);
            Edge backward = new Edge(edge.destination, edge.source, edge.cost, edge.bandwidth);
            if (edge.isOffline) {
                forward.toggle();
                backward.toggle();
            }
            edgeList.add(forward);
            edgeList.add(backward);
            adjMat.put(edge.source, edge.destination, forward);
            adjMat.put(edge.destination, edge.source, backward);
        }
        init(_source, _destination);
    }
}
