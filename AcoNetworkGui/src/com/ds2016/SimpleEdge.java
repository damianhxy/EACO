package com.ds2016;

/**
 * Created by damian on 17/5/16.
 */
public class SimpleEdge {

    final int source, destination, cost, bandwidth;
    boolean isOffline;

    /**
     * Initializes an edge
     *
     * @param _source      Start node
     * @param _destination End node
     * @param _cost        Time taken to traverse
     * @param _bandwidth   Packets per tick
     */
    public SimpleEdge(int _source, int _destination, int _cost, int _bandwidth) {
        source = _source;
        destination = _destination;
        cost = _cost;
        bandwidth = _bandwidth;
    }

    /**
     * Toggle isOffline
     */
    public void toggle() {
        isOffline ^= true;
    }
}
