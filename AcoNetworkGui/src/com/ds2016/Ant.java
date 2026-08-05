package com.ds2016;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by damian on 16/5/16.
 */
class Ant extends Packet {

    final ArrayList<Double> timings = new ArrayList<>();
    private final ArrayList<Integer> path = new ArrayList<>();
    private final HashSet<Integer> tabuList = new HashSet<>();
    double totalTime;
    boolean isBackwards;

    /**
     * Initializes an ant
     *
     * @param source      Source node
     * @param destination Destination node
     * @param TTL         Time to live
     * @param creation    Time of creation
     */
    Ant(int source, int destination, int TTL, int creation) {
        super(source, destination, TTL, creation);
    }

    /**
     * Add a node to the path
     * Assumes that node is valid
     *
     * @param node Current node
     */
    void addNode(int node) {
        path.add(node);
        tabuList.add(node);
    }

    /**
     * Check if a node is
     * currently in the Tabu list
     *
     * @param node Neighbouring node
     * @return Whether node is valid
     */
    boolean canVisit(int node) {
        return !tabuList.contains(node);
    }

    /**
     * Find the previous node on the
     * path of the backwards ant
     *
     * @return The previous node
     */
    int previousNode() {
        return path.remove(path.size() - 1);
    }

    /**
     * Find the next node on the
     * path of the backwards ant
     *
     * @return The next node
     */
    int nextNode() {
        if (path.size() < 2) return -1;
        return path.get(path.size() - 2);
    }

    /**
     * Consume the timing pair for the hop just retraced.
     * <p>
     * Each forward hop records one queueing delay and one transmission cost
     * in arrival order, so the last two entries describe the hop from the
     * previous node back to the destination. Calling this at every backward
     * stop accumulates the forward trip time from the current node to the
     * destination, which is the sub-path used for reinforcement (as in
     * classic AntNet).
     */
    void updateTotalTime() {
        if (timings.size() < 2) return;
        totalTime += timings.remove(timings.size() - 1);
        totalTime += timings.remove(timings.size() - 1);
    }
}
