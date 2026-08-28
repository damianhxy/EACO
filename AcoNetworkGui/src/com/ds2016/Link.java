package com.ds2016;

import com.ds2016.listeners.GuiEventListener;
import com.ds2016.ui.Gui;
import com.ds2016.ui.ParameterStorage;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zwliew on 4/7/16.
 * <p>
 * Common methods which act on both the graph and the GUI
 */
public class Link implements GuiEventListener {

    private static final int ALGO_OSPF = 0;
    private static final int ALGO_ANTNET = 1;
    private static final int ALGO_EACO = 2;
    public static volatile AlgorithmBase sAlgorithm;
    public static volatile int sThroughput;
    private final ReentrantLock mMutex = new ReentrantLock();
    private Gui mGui;
    private ParameterStorage mParams;
    private Thread mThread;
    private Runnable mRunnable;
    private volatile boolean mStarted;

    Link() {
        mGui = new Gui(this);
        mParams = new ParameterStorage(0, 6,
                0.4, 0.1, 1, 30000, ALGO_EACO);
        sAlgorithm = new EACO(mParams.getAlpha(), mParams.getTraffic(), Main.TTL_MS, mParams.getInterval());
    }

    void init() {
        mGui.init();

        mRunnable = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    boolean shouldStop;
                    mMutex.lock();
                    try {
                        tick();
                        shouldStop = sAlgorithm.getCurrentTime() >= mParams.getNumTicks()
                                && mParams.getNumTicks() > 0;
                    } finally {
                        mMutex.unlock();
                    }
                    updateGuiAndWait(mGui::tick);
                    if (shouldStop) {
                        stop();
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                // Do not let an old worker clear the state of a replacement.
                if (Thread.currentThread() == mThread) {
                    mStarted = false;
                }
            }
        };
    }

    private void tick() {
        sThroughput = sAlgorithm.tick();
    }

    /**
     * Run a GUI mutation on Swing's Event Dispatch Thread and wait for it to
     * finish. Waiting prevents the simulation loop from flooding the event
     * queue with chart updates when ticks are faster than rendering.
     */
    static void runOnEdtAndWait(final Runnable update) {
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(update);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("GUI update failed", cause);
        }
    }

    /**
     * Serialize GUI reads of the simulation with algorithm mutations while
     * keeping all Swing and chart writes on the Event Dispatch Thread.
     */
    private void updateGuiAndWait(final Runnable update) {
        runOnEdtAndWait(() -> {
            mMutex.lock();
            try {
                update.run();
            } finally {
                mMutex.unlock();
            }
        });
    }

    private void addNode() {
        mMutex.lock();
        try {
            sAlgorithm.addNode();
        } finally {
            mMutex.unlock();
        }
    }

    private void toggleNode(final int id) {
        mMutex.lock();
        try {
            sAlgorithm.toggleNode(id);
        } finally {
            mMutex.unlock();
        }
    }

    private void addEdge(final int source,
                         final int destination,
                         final int cost,
                         final int bandwidth) {
        mMutex.lock();
        try {
            sAlgorithm.addEdge(source, destination, cost, bandwidth);
        } finally {
            mMutex.unlock();
        }
    }

    private void toggleEdge(final int id) {
        mMutex.lock();
        try {
            sAlgorithm.toggleEdge(id);
        } finally {
            mMutex.unlock();
        }
    }

    private void start() {
        if (mStarted) return;
        mThread = new Thread(mRunnable, "ALGO_THREAD");
        mStarted = true;
        mThread.start();
    }

    private void stop() {
        if (!mStarted) return;
        if (mThread != null) {
            mThread.interrupt();
        }
        mStarted = false;
    }

    private void update(final ParameterStorage params) {
        mParams.setSource(params.getSource());
        mParams.setDestination(params.getDestination());
        mParams.setAlpha(params.getAlpha());
        mParams.setInterval(params.getInterval());
        mParams.setTraffic(params.getTraffic());
        mParams.setNumTicks(params.getNumTicks());

        buildNewAlgorithm(params);

        // The chart reset reads the algorithm's state and mutates Swing data.
        updateGuiAndWait(mGui::resetCharts);
    }

    private void buildNewAlgorithm(final ParameterStorage params) {
        final int source = params.getSource();
        final int destination = params.getDestination();
        final double alpha = params.getAlpha();
        final double interval = params.getInterval();
        final int traffic = params.getTraffic();
        final int algorithm = mParams.getAlgorithm();

        mMutex.lock();
        try {
            switch (algorithm) {
                case ALGO_OSPF:
                    sAlgorithm = new OSPF(Main.TTL_MS, traffic);
                    break;
                case ALGO_ANTNET:
                    sAlgorithm = new AntNet(alpha, traffic, Main.TTL_MS, interval);
                    break;
                case ALGO_EACO:
                    sAlgorithm = new EACO(alpha, traffic, Main.TTL_MS, interval);
                    break;
            }
            sAlgorithm.build(mGui.mNodeList, mGui.mEdgeList, source, destination);
        } finally {
            mMutex.unlock();
        }
    }

    @Override
    public void onStart() {
        start();
    }

    @Override
    public void onStop() {
        stop();
    }

    @Override
    public void onTick() {
        // Manual ticks only make sense while the simulation is paused;
        // otherwise the algo thread is already ticking.
        if (mStarted) return;
        mMutex.lock();
        try {
            tick();
        } finally {
            mMutex.unlock();
        }
        updateGuiAndWait(mGui::tick);
    }

    @Override
    public void onUpdate(final ParameterStorage params) {
        update(params);
    }

    @Override
    public void onAlgorithmChanged(final int algorithmId) {
        mParams.setAlgorithm(algorithmId);
    }

    @Override
    public void onNodeAdded() {
        addNode();
    }

    @Override
    public void onNodeToggled(final int nodeId) {
        toggleNode(nodeId);
    }

    @Override
    public void onEdgeAdded(
            final int source, final int destination, final int cost, final int bandwidth) {
        addEdge(source, destination, cost, bandwidth);
    }

    @Override
    public void onEdgeToggled(final int edgeId) {
        toggleEdge(edgeId);
    }
}
