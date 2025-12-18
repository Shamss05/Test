package edu.montecarlo.model;

/**
 * Interface for π estimation strategies.
 * Allows both sequential and parallel implementations.
 */
public interface PiEstimator {
    /**
     * Estimates the value of π using Monte Carlo simulation.
     * 
     * @param config Simulation configuration parameters
     * @return Estimated value of π
     */
    double estimatePi(SimulationConfig config);
}
