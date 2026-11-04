package com.ds2016;

/**
 * The routing algorithms under test, so a single test can run against each
 * of them. Use {@link ForEachAlgorithm} or {@link ForEachAcoAlgorithm}
 * rather than referring to the constants directly.
 */
public enum Algorithm {
    ANTNET("AntNet") {
        @Override
        public AlgorithmBase create(final double antInterval) {
            return new AntNet(ALPHA, TRAFFIC, TTL_MS, antInterval);
        }

        @Override
        int nodeCount(final AlgorithmBase algo) {
            return ((AntNet) algo).nodes.size();
        }

        @Override
        HashMap2D<Integer, Integer, Double> pheromone(final AlgorithmBase algo, final int node) {
            return ((AntNet) algo).nodes.get(node).pheromone;
        }

        @Override
        void processAnt(final AlgorithmBase algo, final int node, final Ant ant) {
            ((AntNet) algo).nodes.get(node).processAnt(ant);
        }

        @Override
        Object node(final AlgorithmBase algo, final int node) {
            return ((AntNet) algo).nodes.get(node);
        }
    },
    EACO("EACO") {
        @Override
        public AlgorithmBase create(final double antInterval) {
            return new com.ds2016.EACO(ALPHA, TRAFFIC, TTL_MS, antInterval);
        }

        @Override
        int nodeCount(final AlgorithmBase algo) {
            return ((com.ds2016.EACO) algo).nodes.size();
        }

        @Override
        HashMap2D<Integer, Integer, Double> pheromone(final AlgorithmBase algo, final int node) {
            return ((com.ds2016.EACO) algo).nodes.get(node).pheromone;
        }

        @Override
        void processAnt(final AlgorithmBase algo, final int node, final Ant ant) {
            ((com.ds2016.EACO) algo).nodes.get(node).processAnt(ant);
        }

        @Override
        Object node(final AlgorithmBase algo, final int node) {
            return ((com.ds2016.EACO) algo).nodes.get(node);
        }
    },
    OSPF("OSPF") {
        @Override
        public AlgorithmBase create(final double antInterval) {
            return new com.ds2016.OSPF(TTL_MS, TRAFFIC); // OSPF sends no ants
        }

        @Override
        int nodeCount(final AlgorithmBase algo) {
            return ((com.ds2016.OSPF) algo).nodes.size();
        }
    };

    /** Parameters shared by every test unless a test needs something else. */
    static final double ALPHA = 0.4;
    static final int TRAFFIC = 1;
    static final int TTL_MS = 15000;
    /** One ant per 100 ticks, so ants run regularly during short tests. */
    static final double DEFAULT_ANT_INTERVAL = 0.1;

    private final String displayName;

    Algorithm(final String displayName) {
        this.displayName = displayName;
    }

    /** A fresh, unbuilt instance with the default ant interval. */
    public AlgorithmBase create() {
        return create(DEFAULT_ANT_INTERVAL);
    }

    /** A fresh, unbuilt instance; {@code antInterval} is in seconds. */
    public abstract AlgorithmBase create(double antInterval);

    abstract int nodeCount(AlgorithmBase algo);

    /** The pheromone table of one node. ACO algorithms only. */
    HashMap2D<Integer, Integer, Double> pheromone(final AlgorithmBase algo, final int node) {
        throw new UnsupportedOperationException(displayName + " has no pheromone tables");
    }

    /** Hand an ant to one node. ACO algorithms only. */
    void processAnt(final AlgorithmBase algo, final int node, final Ant ant) {
        throw new UnsupportedOperationException(displayName + " has no ants");
    }

    /** The node object itself, for reflective access to private methods. ACO algorithms only. */
    Object node(final AlgorithmBase algo, final int node) {
        throw new UnsupportedOperationException(displayName + " nodes are not ACO nodes");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
