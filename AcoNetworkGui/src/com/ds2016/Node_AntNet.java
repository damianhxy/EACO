package com.ds2016;

import javafx.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by damian on 27/5/16.
 */
public class Node_AntNet {

    private final static double EPS = 1e-9, EXP = 1.4, TMAX = .95;
    public final HashMap2D<Integer, Integer, Double> pheromone = new HashMap2D<>(); // Destination, Node
    final int ID;
    final HashMap<Integer, ArrayDeque<Ant>> fastQ = new HashMap<>();
    final HashMap<Integer, ArrayDeque<Packet>> slowQ = new HashMap<>();
    private final double alpha;
    private final ArrayList<Node_AntNet> nodes;
    private final HashMap2D<Integer, Integer, Edge_ACO> adjMat;
    private final HashMap2D<Integer, Integer, Double> routing = new HashMap2D<>();
    private final ParametricModel PM = new ParametricModel();
    boolean isOffline;
    int tripTime, numPackets;
    private int numNeighbours;

    /**
     * Initialize a node
     *
     * @param _nodes  ArrayList of Node_AntNet
     * @param _adjMat Adjacency Matrix
     * @param _alpha  Weightage of pheromone
     */
    Node_AntNet(ArrayList<Node_AntNet> _nodes,
                HashMap2D<Integer, Integer, Edge_ACO> _adjMat, double _alpha) {
        ID = _nodes.size();
        alpha = _alpha;
        nodes = _nodes;
        adjMat = _adjMat;
    }

    /**
     * Build pheromone table
     */
    void init() {
        HashMap<Integer, Edge_ACO> adj = adjMat.get(ID);
        if (adj == null) return;
        for (Edge_ACO edge : adj.values()) {
            if (!edge.isOffline && !nodes.get(edge.destination).isOffline)
                ++numNeighbours;
        }
        for (int a = 0; a < nodes.size(); ++a) { // For each destination
            if (a == ID) continue;
            for (Edge_ACO edge : adj.values()) { // For each neighbour
                if (edge.isOffline || nodes.get(edge.destination).isOffline) continue;
                addHeuristic(edge.destination, a);
            }
            updateRouting(a);
        }
    }

    /**
     * React to toggling of node
     *
     * @param ID Node ID
     */
    void toggleNode(int ID) {
        if (nodes.get(ID).isOffline) {
            --numNeighbours;
            for (int a = 0; a < nodes.size(); ++a) {
                if (a == ID) continue;
                removeHeuristic(ID, a);
            }
        } else {
            ++numNeighbours;
            for (int a = 0; a < nodes.size(); ++a) {
                if (a == ID) continue;
                updateHeuristic(ID, a, 1. / numNeighbours);
            }
        }
    }

    /**
     * React to addition of an edge
     *
     * @param node Other Node
     */
    void addEdge(int node) {
        ++numNeighbours;
        for (int a = 0; a < nodes.size(); ++a) {
            if (a == ID) continue;
            updateHeuristic(node, a, 1. / numNeighbours);
        }
    }

    /**
     * React to toggling of an edge
     *
     * @param node Other Node
     */
    void toggleEdge(int node) {
        if (adjMat.get(ID, node).isOffline) {
            --numNeighbours;
            for (int a = 0; a < nodes.size(); ++a) {
                if (a == ID) continue;
                removeHeuristic(node, a);
            }
        } else {
            ++numNeighbours;
            for (int a = 0; a < nodes.size(); ++a) {
                if (a == ID) continue;
                updateHeuristic(node, a, 1. / numNeighbours);
            }
        }
    }

    /**
     * Use heuristics to calculate the
     * next best hop, for a given destination
     *
     * @param packet Packet being processed
     * @return Neighbour for next hop, or null if no candidates
     */
    private Integer nextHop(Packet packet) {
        double RNG = Math.random(), totVal = 0;
        double beta = 1 - alpha;
        ArrayList<Pair<Integer, Double>> neighbours = new ArrayList<>(); // Neighbour, Heuristic
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            if (edge.isOffline) continue; // Link is offline
            if (nodes.get(edge.destination).isOffline) continue; // Node is offline
            if (packet instanceof Ant && !((Ant) packet).canVisit(edge.destination)) continue; // Cycle detection
            Double tau = routing.get(packet.destination, edge.destination); // Pheromone
            if (packet instanceof Ant) tau = pheromone.get(packet.destination, edge.destination);
            if (tau == null) continue; // Not viable
            double eta = 1. / edge.cost; // 1 / Distance
            neighbours.add(new Pair<>(edge.destination, Math.pow(tau, alpha) * Math.pow(eta, beta)));
            totVal += neighbours.get(neighbours.size() - 1).getValue();
        }
        if (neighbours.isEmpty()) {
            return null;
        }
        for (Pair<Integer, Double> neighbour : neighbours) {
            RNG -= neighbour.getValue() / totVal;
            if (RNG <= EPS) return neighbour.getKey();
        }
        return neighbours.get(neighbours.size() - 1).getKey();
    }

    /**
     * Change the value by a certain amount
     * Modifies other values proportionally to
     * achieve a sum of one
     *
     * @param neighbour   ID of neighbour
     * @param destination ID of destination
     * @param change      Pheromone change
     * @throws IllegalStateException if pheromone does not add up to 1.
     */
    private void updateHeuristic(int neighbour, int destination, double change) throws IllegalArgumentException {
        // First we fix any rounding errors
        int cnt = 0;
        double tot = .0;
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            Double val = pheromone.get(destination, edge.destination);
            if (val != null) {
                tot += val;
                ++cnt;
            }
        }
        if (cnt > 0) {
            for (Edge_ACO edge : adjMat.get(ID).values()) {
                Double val = pheromone.get(destination, edge.destination);
                if (val != null) {
                    double new_val = val + (1. - tot) * (val / tot);
                    pheromone.put(destination, edge.destination, new_val);
                }
            }
        }
        // Main update code
        double TMIN = 0.05 / (numNeighbours - 1);
        Double cur = pheromone.get(destination, neighbour);
        if (cur == null) cur = 0.;
        double maxChange = (TMAX - cur) / (1. - cur);
        change = Math.min(change, maxChange);
        cur = cur * (1 - change) + change;
        pheromone.put(destination, neighbour, cur);
        // Decrease the rest
        double owed = .0;
        double excess = .0;
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            if (edge.destination == neighbour) continue;
            Double val = pheromone.get(destination, edge.destination);
            if (val != null) {
                double new_val = val * (1 - change);
                if (TMIN - new_val > EPS) {
                    owed += TMIN - new_val;
                    new_val = TMIN;
                } else excess += new_val - TMIN;
                if (TMIN - new_val > EPS) throw new IllegalStateException();
                pheromone.put(destination, edge.destination, new_val);
            }
        }
        // Pay off the debt
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            if (edge.destination == neighbour) continue;
            Double val = pheromone.get(destination, edge.destination);
            if (val != null && val - TMIN > EPS) {
                double new_val = val - owed * ((val - TMIN) / excess);
                if (TMIN - new_val > EPS) throw new IllegalStateException();
                pheromone.put(destination, edge.destination, new_val);
            }
        }
        updateRouting(destination);
    }

    /**
     * Process an ant
     *
     * @param ant Ant
     */
    void processAnt(Ant ant) {
        Integer nxt;
        if (ant.isBackwards) { // Backward ant
            ant.updateTotalTime();
            int prev = ant.previousNode();
            Double P = pheromone.get(ant.destination, prev);
            if (P == null) {
                return; // Previous Node is gone
            }
            double R = PM.getReinforcement(ant.totalTime / 1000, adjMat.get(ID).values().size());
            updateHeuristic(prev, ant.destination, R);
            if (ant.source == ID) {
                return; // Reached source
            }
            nxt = ant.nextNode();
            if (pheromone.get(nxt, nxt) == null) {
                return; // Next node is gone
            }
            fastQ.putIfAbsent(nxt, new ArrayDeque<>());
            fastQ.get(nxt).add(ant);
        } else { // Forward ant
            ant.addNode(ID);
            if (ant.destination == ID) {
                ant.isBackwards = true;
                nxt = ant.nextNode();
                if (pheromone.get(nxt, nxt) == null) {
                    return; // Next node is gone
                }
                fastQ.putIfAbsent(nxt, new ArrayDeque<>());
                ant.timings.add(getDepletionTime(nxt));
                fastQ.get(nxt).add(ant);
            } else {
                nxt = nextHop(ant);
                if (nxt == null) {
                    return; // No valid next node
                }
                fastQ.putIfAbsent(nxt, new ArrayDeque<>());
                ant.timings.add(getDepletionTime(nxt));
                ant.timings.add((double) adjMat.get(ID, nxt).cost);
                fastQ.get(nxt).add(ant);
            }
        }
    }

    /**
     * Process a packet
     *
     * @param packet Packet
     * @return 1, if packet has reached destination
     */
    int processPacket(Packet packet) {
        if (packet.destination == ID) {
            if (Main.DEBUG_LATENCIES) {
                tripTime += (packet.timestamp - packet.getCreationTime());
                ++numPackets;
            }
            return 1;
        }
        Integer nxt = nextHop(packet);
        if (nxt != null) {
            slowQ.putIfAbsent(nxt, new ArrayDeque<>());
            slowQ.get(nxt).add(packet);
        }
        return 0;
    }

    /**
     * Calculate size of backlog
     *
     * @return Number of packets in link-queues
     */
    int numberOfPackets() {
        int total = 0;
        for (Integer neighbour : slowQ.keySet()) {
            total += slowQ.get(neighbour).size();
        }
        return total;
    }

    /**
     * Clear the ant queue
     */
    void clearFastQ() {
        fastQ.clear();
    }

    /**
     * Clear the packet queue
     */
    void clearSlowQ() {
        slowQ.clear();
    }

    /**
     * Toggle isOffline
     */
    void toggle() {
        isOffline ^= true;
    }

    /**
     * Updating routing tables
     *
     * @param destination ID of destination
     */
    private void updateRouting(int destination) {
        double tot = 0;
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            Double val = pheromone.get(destination, edge.destination);
            if (val != null) {
                tot += Math.pow(val, EXP);
                routing.put(destination, edge.destination, Math.pow(val, EXP));
            }
        }
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            if (pheromone.get(destination, edge.destination) != null) {
                routing.put(destination, edge.destination, routing.get(destination, edge.destination) / tot);
            }
        }
    }

    /**
     * Give pheromone to a new neighbour
     *
     * @param neighbour   Node ID
     * @param destination Destination ID
     */
    private void addHeuristic(int neighbour, int destination) {
        boolean destinationIsNeighbour = false;
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            if (edge.isOffline || nodes.get(edge.destination).isOffline) continue;
            if (edge.destination == destination) destinationIsNeighbour = true;
        }
        double NN = numNeighbours;
        if (destinationIsNeighbour) {
            // Intelligent Initialization
            if (neighbour == destination) {
                double amt = 1. / NN + 3. / 2. * (NN - 1) / (NN * NN);
                pheromone.put(destination, neighbour, amt);
            } else {
                double amt = 1. / NN - 3. / 2. * 1. / (NN * NN);
                pheromone.put(destination, neighbour, amt);
            }
        } else {
            pheromone.put(destination, neighbour, 1. / NN);
        }
    }

    /**
     * Remove pheromone from a node that is
     * no longer a neighbour
     *
     * @param neighbour   Node ID
     * @param destination Destination ID
     */
    private void removeHeuristic(int neighbour, int destination) {
        Double amount = pheromone.get(destination, neighbour);
        if (amount == null) return;
        pheromone.put(destination, neighbour, null);
        /* Decrease proportionally */
        for (Edge_ACO edge : adjMat.get(ID).values()) {
            Double val = pheromone.get(destination, edge.destination);
            if (val != null) {
                Double new_val = val + amount * (val / (1 - amount));
                pheromone.put(destination, edge.destination, new_val);
            }
        }
        updateRouting(destination);
    }

    /**
     * Time needed to deplete queue
     *
     * @param neighbour Neighbour ID
     */
    private double getDepletionTime(int neighbour) {
        return (double) fastQ.get(neighbour).size() / adjMat.get(ID, neighbour).bandwidth;
    }
}
