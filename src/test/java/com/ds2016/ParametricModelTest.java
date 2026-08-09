package com.ds2016;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParametricModelTest {

    @Test
    void reinforcementIsZeroWithoutNeighbours() {
        // N == 0 previously produced 0/0 == NaN.
        final ParametricModel pm = new ParametricModel();
        assertEquals(0.0, pm.getReinforcement(0.1, 0));
    }

    @Test
    void reinforcementStaysFiniteOverManySamples() {
        final ParametricModel pm = new ParametricModel();
        final Random rng = new Random(42);
        for (int i = 0; i < 20000; i++) {
            final double r = pm.getReinforcement(0.1 + 0.01 * rng.nextGaussian(), 4);
            assertTrue(Double.isFinite(r), "non-finite reinforcement at sample " + i + ": " + r);
        }
    }

    @Test
    void reinforcementIsInUnitInterval() {
        final ParametricModel pm = new ParametricModel();
        final Random rng = new Random(7);
        for (int i = 0; i < 5000; i++) {
            final double r = pm.getReinforcement(0.1 + 0.01 * rng.nextGaussian(), 4);
            assertTrue(r >= 0 && r <= 1, "reinforcement out of [0,1] at sample " + i + ": " + r);
        }
    }
}
