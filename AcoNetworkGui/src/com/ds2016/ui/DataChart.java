package com.ds2016.ui;

import com.ds2016.*;
import com.ds2016.listeners.GuiEventListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class DataChart implements GuiEventListener {
    private static final String FRAME_TITLE = "Data charts";
    private static final String THROUGHPUT_TITLE = "Throughput";
    private static final String CHART_X = "Elapsed time";
    private static final String CHART_THROUGHPUT_Y = "Packets received";

    private JTabbedPane mTabbedPane;
    private XYSeries mThroughputSeries;

    private java.util.List<TableModel> mModelList = new ArrayList<>();

    private int mConsistentCount;
    private boolean mLoggedMax;
    private int mNumTicks;
    private int mCurThroughput;
    private ArrayDeque<Integer> mThroughputs = new ArrayDeque<>();
    private int mPacketsPerTick;

    void display() {
        JFrame frame = new JFrame(FRAME_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mTabbedPane = new JTabbedPane();
        mTabbedPane.add(THROUGHPUT_TITLE, throughputPane());
        frame.add(mTabbedPane, BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        frame.add(panel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private ChartPanel throughputPane() {
        mThroughputSeries = new XYSeries("Throughput data");
        final XYSeriesCollection dataset = new XYSeriesCollection(mThroughputSeries);
        final JFreeChart chart = ChartFactory.createXYLineChart(THROUGHPUT_TITLE, CHART_X, CHART_THROUGHPUT_Y,
                dataset, PlotOrientation.VERTICAL, false, false, false);
        final ChartPanel panel = new ChartPanel(chart);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final double width = screenSize.getWidth() / 3.3;
        final double height = screenSize.getHeight() / 3.75;
        panel.setPreferredSize(new Dimension((int) width, (int) height));
        return panel;
    }

    private void updatePheromoneTables() {
        for (int i = 0; i < mModelList.size(); i++) {
            int row = 1;
            final TableModel model = mModelList.get(i);
            if (Link.sAlgorithm instanceof EACO) {
                final Node_EACO node = ((EACO) Link.sAlgorithm).nodes.get(i);
                for (Map.Entry<Integer, HashMap<Integer, Double>> entry : node.pheromone.M.entrySet()) {
                    final HashMap<Integer, Double> value = entry.getValue();
                    int col = 1;
                    for (Map.Entry<Integer, Double> valueEntry : value.entrySet()) {
                        model.setValueAt(valueEntry.getKey(), 0, col);
                        model.setValueAt(String.valueOf(valueEntry.getValue()), row, col);
                        col++;
                    }
                    final int key = entry.getKey();
                    model.setValueAt(key, row, 0);
                    row++;
                }
            } else if (Link.sAlgorithm instanceof AntNet) {
                final Node_AntNet node = ((AntNet) Link.sAlgorithm).nodes.get(i);
                for (Map.Entry<Integer, HashMap<Integer, Double>> entry : node.pheromone.M.entrySet()) {
                    final HashMap<Integer, Double> value = entry.getValue();
                    int col = 1;
                    for (Map.Entry<Integer, Double> valueEntry : value.entrySet()) {
                        model.setValueAt(valueEntry.getKey(), 0, col);
                        model.setValueAt(String.valueOf(valueEntry.getValue()), row, col);
                        col++;
                    }
                    final int key = entry.getKey();
                    model.setValueAt(key, row, 0);
                    row++;
                }
            } else if (Link.sAlgorithm instanceof OSPF) {
                final Node_OSPF node = ((OSPF) Link.sAlgorithm).nodes.get(i);
                for (ArrayList<Integer> value : node.SSSP.P) {
                    int col = 1;
                    for (Integer valueEntry : value) {
                        model.setValueAt(col, 0, col);
                        model.setValueAt(String.valueOf(valueEntry), row, col);
                        col++;
                    }
                    model.setValueAt(row - 1, row, 0);
                    row++;
                }
            }
        }
    }

    void updateCharts() {
        updatePheromoneTables();

        mThroughputs.addLast(Link.sThroughput);
        mCurThroughput += Link.sThroughput;
        if (mThroughputs.size() > 1000) {
            mCurThroughput -= mThroughputs.getFirst();
            mThroughputs.pollFirst();
        }
        ++mNumTicks;

        if (mNumTicks % Main.DEBUG_NUM_TICKS_PER_UPDATE == 0) {
            mThroughputSeries.add(mThroughputSeries.getItemCount(), mCurThroughput);
        }

        if (Main.DEBUG_THROUGHPUT) {
            if (!mLoggedMax) {
                if (mCurThroughput >= mPacketsPerTick * Main.DEBUG_NUM_TICKS_PER_UPDATE) {
                    System.out.println("100% at " + mNumTicks + " ticks");
                    mLoggedMax = true;
                }
            }

            if (mCurThroughput >= 0.95 * mPacketsPerTick * Main.DEBUG_NUM_TICKS_PER_UPDATE) {
                ++mConsistentCount;
                if (mConsistentCount == 5 * Main.DEBUG_NUM_TICKS_PER_UPDATE) {
                    System.out.println("95% at "
                            + (mNumTicks - 5 * Main.DEBUG_NUM_TICKS_PER_UPDATE + 1) + " ticks");
                }
            } else {
                mConsistentCount = 0;
            }
        }
    }

    void resetCharts() {
        mThroughputs.clear();
        mNumTicks = 0;
        mCurThroughput = 0;
        mConsistentCount = 0;
        mLoggedMax = false;
        mThroughputSeries.clear();
        mThroughputSeries.add(0, 0);
        for (int node = 0; node < mModelList.size(); node++) {
            final TableModel model = mModelList.get(node);
            model.resetData();
        }
        updatePheromoneTables();
    }

    void addNode() {
        if (!Main.DISPLAY_PHEROMONE) {
            return;
        }
        final TableModel model = new TableModel(mModelList.size());
        mModelList.add(model);
        final JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        mTabbedPane.add("Node " + mModelList.size(), new JScrollPane(table));
    }

    @Override
    public void onStart() {
        // Do nothing
    }

    @Override
    public void onStop() {
        // Do nothing
    }

    @Override
    public void onTick() {
        // Do nothing
    }

    @Override
    public void onUpdate(ParameterStorage params) {
        mPacketsPerTick = params.getTraffic();
    }

    @Override
    public void onAlgorithmChanged(int algorithmId) {
        // Do nothing
    }

    @Override
    public void onNodeAdded() {
        // Do nothing
    }

    @Override
    public void onNodeToggled(int nodeId) {
        // Do nothing
    }

    @Override
    public void onEdgeAdded(int source, int destination, int cost, int bandwidth) {
        // Do nothing
    }

    @Override
    public void onEdgeToggled(int edgeId) {
        // Do nothing
    }
}