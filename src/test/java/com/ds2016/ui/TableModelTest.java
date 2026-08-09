package com.ds2016.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableModelTest {

    @Test
    void growsBeyondInitialDimensions() {
        final TableModel model = new TableModel(0);
        model.setValueAt("hello", 40, 12);
        assertEquals(41, model.getRowCount());
        assertEquals(13, model.getColumnCount());
        assertEquals("hello", model.getValueAt(40, 12));
    }

    @Test
    void resetDataFillsHeadersAfterGrowth() {
        final TableModel model = new TableModel(0);
        model.setValueAt("value", 30, 15);
        model.resetData();
        assertEquals("FILLER", model.getValueAt(0, 15));
        assertEquals("", model.getValueAt(30, 15));
    }

    @Test
    void initialDimensions() {
        final TableModel model = new TableModel(0);
        assertEquals(14, model.getRowCount());
        assertEquals(10, model.getColumnCount());
    }
}
