package com.ds2016;

import com.ds2016.networks.Network;
import com.ds2016.networks.NsfNetwork;

import javax.swing.*;

/**
 * We run on the assumption that 1 tick == 1 ms
 */
public class Main {
    // Debug tunables
    public static final boolean DEBUG_THROUGHPUT = false;
    public static final boolean DEBUG_LATENCIES = false;
    public static final int DEBUG_NUM_TICKS_PER_UPDATE = 1000;
    // Algorithm tunables
    public static final int TTL_MS = 15000;
    // Debug table tunables
    public static final int NUM_ARRAY_ROWS = 14;
    public static final int NUM_ARRAY_COLS = 10;
    // GUI network tunables
    public static final String STYLE_SHEET =
            "edge.highLoad { fill-color: #F44336; }" +
                    "edge.midLoad { fill-color: #FF9800; }" +
                    "edge.lowLoad { fill-color: #8BC34A; }" +
                    "edge.minimalLoad { fill-color: #607D8B; }" +
                    "edge.noLoad { fill-color: #000000; }" +
                    "node.highLoad { fill-color: #F44336; }" +
                    "node.midLoad { fill-color: #FF9800; }" +
                    "node.lowLoad { fill-color: #8BC34A; }" +
                    "node.minimalLoad { fill-color: #607D8B; }" +
                    "node.noLoad { fill-color: #9E9E9E; }" +
                    "node.source { fill-color: #AAE66E; }" +
                    "node.destination { fill-color: #E66E6E; }" +
                    "node { fill-color: #9E9E9E; }";
    // GUI network color algorithm tunables
    public static final double HIGH_LOAD_FACTOR = 3;
    public static final double MED_LOAD_FACTOR = 1.5;
    public static final double LOW_LOAD_FACTOR = 0.5;
    // GUI network shape tunables
    public static final Network GUI_NETWORK = new NsfNetwork();
    // Display GUI elements
    public static final boolean DISPLAY_NETWORK = true;
    public static final boolean DISPLAY_PHEROMONE = true;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final Link link = new Link();
            link.init();
        });
    }
}
