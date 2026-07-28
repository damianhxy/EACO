package com.ds2016.ui;

import com.ds2016.Main;
import com.ds2016.Node_GUI;
import com.ds2016.SimpleEdge;
import com.ds2016.listeners.GraphEventListener;
import com.ds2016.listeners.GuiEventListener;
import com.ds2016.listeners.NetworkEventListener;
import com.ds2016.networks.Network;
import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.Sink;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by zwliew on 19/6/16.
 */
public class Gui implements GraphEventListener, NetworkEventListener {
    private static final String FRAME_TITLE = "EACO";
    private static final String GRAPH_TITLE = "Simulation";
    public ArrayList<Node_GUI> mNodeList = new ArrayList<>();
    public ArrayList<SimpleEdge> mEdgeList = new ArrayList<>();
    private JPanel mainPanel;
    private JTextField mAlphaField;
    private JTextField mDistanceField;
    private JTextField mFromField;
    private JTextField mToField;
    private JButton mAddNodeBtn;
    private JButton mAddEdgeBtn;
    private JButton mStartBtn;
    private JButton mStopBtn;
    private JButton mTickBtn;
    private JRadioButton mOspfBtn;
    private JRadioButton mAntNetBtn;
    private JRadioButton mEAcoBtn;
    private JTextField mSourceField;
    private JTextField mDestinationField;
    private JTextField mToggleEdgeField;
    private JButton mToggleEdgeBtn;
    private JTextField mBandwidthField;
    private JButton mUpdateBtn;
    private JButton mToggleNodeBtn;
    private JTextField mToggleNodeField;
    private JTextField mIntervalField;
    private JTextField mTrafficField;
    private JTextField mNumTicksField;
    private GuiEventListener mListener;
    private Graph mGraph;
    private DynamicAlgorithm mGraphAlgo;
    private DataChart mDataChart;
    private Network mNetwork;

    private int mSourceNode;
    private int mDestinationNode;

    public Gui(final GuiEventListener listener) {
        mListener = listener;
        mNetwork = Main.GUI_NETWORK;
    }

    public void init() {
        JFrame frame = new JFrame(FRAME_TITLE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth() / 2.2;
        double height = screenSize.getHeight() / 3;
        frame.setPreferredSize(new Dimension((int) width, (int) height));

        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        mNetwork.init(this);
        mNetwork.build();

        update();
    }

    private void initNetworkPanel() {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        mGraph = new SingleGraph(GRAPH_TITLE);
        mGraph.setStrict(false);
        mGraph.setAutoCreate(true);
        mGraph.addAttribute("ui.stylesheet", Main.STYLE_SHEET);

        mGraphAlgo = new GraphAlgo(this);
        mGraphAlgo.init(mGraph);

        if (Main.DISPLAY_NETWORK) {
            mGraph.display();
        }
    }

    private void initChartPanel() {
        mDataChart = new DataChart();
        mDataChart.display();
    }

    private void createUIComponents() {
        initNetworkPanel();
        initChartPanel();

        /*
          Set the current algorithm
         */
        mOspfBtn = new JRadioButton();
        mOspfBtn.addActionListener(actionEvent ->
                mListener.onAlgorithmChanged(Integer.parseInt(actionEvent.getActionCommand())));

        mAntNetBtn = new JRadioButton();
        mAntNetBtn.addActionListener(actionEvent ->
                mListener.onAlgorithmChanged(Integer.parseInt(actionEvent.getActionCommand())));

        mEAcoBtn = new JRadioButton();
        mEAcoBtn.addActionListener(actionEvent ->
                mListener.onAlgorithmChanged(Integer.parseInt(actionEvent.getActionCommand())));

        /*
          Add a new node
         */
        mAddNodeBtn = new JButton();
        mAddNodeBtn.addActionListener(actionEvent -> {
            addNode();
            mListener.onNodeAdded();
            mDataChart.addNode();
        });

        /*
          Toggle Node
         */
        mToggleNodeBtn = new JButton();
        mToggleNodeBtn.addActionListener(actionEvent -> {
            final int nodeId = Integer.parseInt(mToggleNodeField.getText());
            toggleNode(nodeId);
            mListener.onNodeToggled(nodeId);
            mDataChart.toggleNode(mNodeList.get(nodeId).isOffline);
        });

        /*
          Add a new edge
         */
        mAddEdgeBtn = new JButton();
        mAddEdgeBtn.addActionListener(actionEvent -> {
            int from = Integer.parseInt(mFromField.getText());
            int to = Integer.parseInt(mToField.getText());
            int distance = Integer.parseInt(mDistanceField.getText());
            int bandwidth = Integer.parseInt(mBandwidthField.getText());
            addEdge(from, to, distance, bandwidth);
            mListener.onEdgeAdded(from, to, distance, bandwidth);
        });

        /*
          Toggle edge
         */
        mToggleEdgeBtn = new JButton();
        mToggleEdgeBtn.addActionListener(actionEvent -> {
            final int id = Integer.parseInt(mToggleEdgeField.getText());
            toggleEdge(id);
            mListener.onEdgeToggled(Integer.parseInt(mToggleEdgeField.getText()));
        });

        /*
          Save parameters
         */
        mUpdateBtn = new JButton();
        mUpdateBtn.addActionListener(e -> update());

        /*
          Start the mGraph algorithm thread
         */
        mStartBtn = new JButton();
        mStartBtn.addActionListener(actionEvent -> mListener.onStart());

        /*
          Stop the mGraph algorithm thread
         */
        mStopBtn = new JButton();
        mStopBtn.addActionListener(actionEvent -> mListener.onStop());

        /*
          Undergo one tick of the entire program (mGraph + algorithm)
         */
        mTickBtn = new JButton();
        mTickBtn.addActionListener(actionEvent -> {
            tick();
            mListener.onTick();
        });
    }

    public void tick() {
        mGraphAlgo.compute();
        mDataChart.updateCharts();
    }

    private void update() {
        mSourceNode = Integer.parseInt(mSourceField.getText());
        mDestinationNode = Integer.parseInt(mDestinationField.getText());
        final ParameterStorage params = new ParameterStorage(
                mSourceNode,
                mDestinationNode,
                Double.parseDouble(mAlphaField.getText()),
                Double.parseDouble(mIntervalField.getText()),
                Integer.parseInt(mTrafficField.getText()),
                Integer.parseInt(mNumTicksField.getText()));
        colouriseNodes(mSourceNode, mDestinationNode);
        mListener.onUpdate(params);
        mDataChart.onUpdate(params);
        mDataChart.resetCharts();
    }

    /**
     * Colourise source and destination node
     */
    private void colouriseNodes(final int sourceId, final int destinationId) {
        Node source = mGraph.getNode(String.valueOf(sourceId));
        Node destination = mGraph.getNode(String.valueOf(destinationId));
        for (Node n : mGraph) {
            if (n == source) {
                n.setAttribute("ui.class", "source");
            } else if (n == destination) {
                n.setAttribute("ui.class", "destination");
            } else {
                n.removeAttribute("ui.class");
            }
        }
    }

    /**
     * Add a new node
     */
    private void addNode() {
        // Add GUI node
        int nodeCount = mGraph.getNodeCount();
        Node node = mGraph.addNode(String.valueOf(nodeCount));
        node.addAttribute("ui.label", nodeCount);

        // Add Node_GUI node for algorithm to read from
        Node_GUI listNode = new Node_GUI();
        mNodeList.add(listNode);
    }

    /**
     * Toggle state of a node
     *
     * @param id Node ID
     * @throws IllegalArgumentException if ID is out of bounds
     */
    private void toggleNode(final int id)
            throws IllegalArgumentException {
        if (id == mSourceNode || id == mDestinationNode) {
            throw new IllegalArgumentException();
        }
        Node node = mGraph.getNode(id);

        // Toggle mGraph state
        if (node.hasAttribute("ui.hide")) {
            node.removeAttribute("ui.hide");
        } else {
            node.addAttribute("ui.hide");
        }

        // Toggle Node_GUI state
        Node_GUI listNode = mNodeList.get(id);
        listNode.isOffline ^= true;
    }

    /**
     * Add a bidirectional edge
     *
     * @param node1     First node
     * @param node2     Second node
     * @param cost      Time taken
     * @param bandwidth Bandwidth
     * @throws IllegalArgumentException if ID is out of bounds
     */
    private void addEdge(int node1, int node2, int cost, int bandwidth) throws IllegalArgumentException {
        // We can't add edges between non-existent nodes
        int nodeCount = mGraph.getNodeCount();
        if (node1 >= nodeCount || node2 >= nodeCount) {
            throw new IllegalArgumentException();
        }

        // Add forward edge
        int edgeCount = mGraph.getEdgeCount() / 2;

        Edge forward = mGraph.addEdge(String.valueOf(edgeCount + "f"), node1, node2, true);
        forward.addAttribute("ui.label", String.valueOf(edgeCount));
        forward.addAttribute("edge.cluster", String.valueOf(edgeCount));
        forward.addAttribute("cost", cost);

        // Add backward edge
        Edge backward = mGraph.addEdge(String.valueOf(edgeCount + "b"), node2, node1, true);
        backward.addAttribute("edge.cluster", String.valueOf(edgeCount));
        backward.addAttribute("cost", cost);

        // Add edge to the SimpleEdge list
        mEdgeList.add(new SimpleEdge(node1, node2, cost, bandwidth));
    }

    /**
     * Toggle state of an edge
     *
     * @param ID Edge ID
     */
    private void toggleEdge(int ID) {
        String forwardId = ID + "f";
        String backwardId = ID + "b";
        Edge forward = mGraph.getEdge(forwardId);
        Edge backward = mGraph.getEdge(backwardId);

        // Toggle forward edge visibility
        if (forward.hasAttribute("ui.hide")) {
            forward.removeAttribute("ui.hide");
        } else {
            forward.addAttribute("ui.hide");
        }

        // Toggle backward edge visibility
        if (backward.hasAttribute("ui.hide")) {
            backward.removeAttribute("ui.hide");
        } else {
            backward.addAttribute("ui.hide");
        }
    }

    @Override
    public void onGraphTerminated(final Sink sink) {
        mGraph.removeSink(sink);
    }

    @Override
    public Graph onGraphUpdated() {
        return mGraph;
    }

    @Override
    public void onNodeAdded() {
        addNode();
        mListener.onNodeAdded();
        mDataChart.addNode();
    }

    @Override
    public void onEdgeAdded(final int source,
                            final int destination,
                            final int cost,
                            final int bandwidth) {
        addEdge(source, destination, cost, bandwidth);
        mListener.onEdgeAdded(source, destination, cost, bandwidth);
    }
}
