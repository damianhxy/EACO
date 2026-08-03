package com.ds2016;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the ant trip-time accounting.
 */
class AntTimingTest {

    @Test
    void totalTimeConsumesAllRecordedTimings() {
        final Ant ant = new Ant(0, 6, 15000, 0);
        ant.timings.add(1.0);
        ant.timings.add(2.0);
        ant.timings.add(3.0);
        ant.updateTotalTime();
        assertEquals(6.0, ant.totalTime);
        assertTrue(ant.timings.isEmpty());
    }

    @Test
    void totalTimeIsNotDoubleCountedOnLaterCalls() {
        final Ant ant = new Ant(0, 6, 15000, 0);
        ant.timings.add(1.0);
        ant.timings.add(2.0);
        ant.updateTotalTime();
        ant.updateTotalTime();
        assertEquals(3.0, ant.totalTime);
        assertTrue(ant.timings.isEmpty());
    }
}
