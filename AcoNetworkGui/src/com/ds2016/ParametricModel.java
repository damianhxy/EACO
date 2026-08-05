package com.ds2016;

/**
 * Created by damian on 30/11/16.
 */
public class ParametricModel {

    // Empirical Values
    // γ = Confidence Coefficient
    private final static double c1 = 0.63, c2 = 0.27, γ = 0.95, z = 1. / Math.sqrt(1 - γ);
    // η = Effective Samples
    // a has no empirical value
    private final static double η = 0.005, a = 10, c = 0.3;
    // w = Size of window (Dependant on value of η)
    private final static int w = (int) (5 * (c / η) + 0.5);
    private int N;
    // μ = Sample Mean
    // σ2 = Sample Variance
    // W = Sample Minimum
    private double μ, σ2, W = Double.MAX_VALUE;
    private int windowCount;

    /**
     * Squash Function
     * Place emphasis on good results
     *
     * @param X Original Value
     * @return Squashed value
     * @throws IllegalArgumentException if X is out of the range (0, 1]
     */
    private double squash(double X) throws IllegalArgumentException {
        if (X > 1) throw new IllegalArgumentException();
        if (N == 0) return 0;
        return (1 + Math.exp(a / N)) / (1 + Math.exp(a / (X * N)));
    }

    /**
     * Get reinforcement
     *
     * @param T  Time taken for trip
     * @param _N Number of neighbour nodes
     * @return Reinforcement Value
     */
    double getReinforcement(double T, int _N) {
        if (_N == 0) {
            return 0; // No neighbours: no reinforcement
        }
        // Update Variables
        N = _N;
        if (μ == 0) {
            μ = T;
        } else {
            double old_μ = μ;
            μ = μ + η * (T - μ);
            σ2 = σ2 + η * ((T - old_μ) * (T - μ) - σ2);
        }
        if (++windowCount > w) {
            windowCount = 1;
            W = μ;
        }
        if (T < W) {
            W = T;
        }
        // Calculate reinforcement
        double I_inf = W;
        double I_sup = μ + z * Math.sqrt(σ2 / w);
        double r;
        if ((I_sup - I_inf) + (T - I_inf) == 0) {
            r = (c1 + c2) * (W / T);
        } else {
            r = c1 * (W / T) + c2 * ((I_sup - I_inf) / ((I_sup - I_inf) + (T - I_inf)));
        }
        return squash(r) / squash(1);
    }
}
