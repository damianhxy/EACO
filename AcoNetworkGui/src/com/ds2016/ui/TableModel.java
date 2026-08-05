package com.ds2016.ui;

import com.ds2016.Main;

import javax.swing.table.DefaultTableModel;

/**
 * Created by zwliew on 6/12/16.
 */
public class TableModel extends DefaultTableModel {

    private final int mNode;
    private String[][] mData = new String[Main.NUM_ARRAY_ROWS][Main.NUM_ARRAY_COLS];

    TableModel(final int node) {
        mNode = node;
        resetData();
    }

    @Override
    public int getRowCount() {
        return mData == null ? 0 : mData.length;
    }

    @Override
    public int getColumnCount() {
        return mData == null ? 0 : mData[0].length;
    }

    @Override
    public String getColumnName(int col) {
        return mData[0][col];
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return mData[row][col];
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (row >= mData.length || col >= mData[0].length) {
            grow(row, col);
        }
        mData[row][col] = String.valueOf(value);
        fireTableCellUpdated(row, col);
    }

    private void grow(final int row, final int col) {
        final int newRows = Math.max(mData.length, row + 1);
        final int newCols = Math.max(mData[0].length, col + 1);
        final String[][] copy = new String[newRows][newCols];
        for (int r = 0; r < mData.length; r++) {
            System.arraycopy(mData[r], 0, copy[r], 0, mData[r].length);
        }
        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if (copy[r][c] == null) {
                    copy[r][c] = r == 0 ? "FILLER" : "";
                }
            }
        }
        mData = copy;
    }

    void resetData() {
        for (int col = 0; col < mData[0].length; col++) {
            mData[0][col] = "FILLER";
        }
        for (int row = 1; row < mData.length; row++) {
            for (int col = 0; col < mData[0].length; col++) {
                mData[row][col] = "";
            }
        }
    }
}
