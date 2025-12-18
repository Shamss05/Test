package edu.montecarlo.model;

/**
 * Configuration class for Monte Carlo simulation parameters.
 * Holds all settings needed for π estimation.
 */
public class SimulationConfig {
    private final long totalPoints; // Total number of random points to generate
    private final int numTasks; // Number of parallel tasks to divide work into
    private final int numThreads; // Size of thread pool

    /**
     * Creates a new simulation configuration.
     * 
     * @param totalPoints Total number of random points
     * @param numTasks    Number of tasks for parallel execution
     * @param numThreads  Number of threads in the pool
     */
    public SimulationConfig(long totalPoints, int numTasks, int numThreads) {
        this.totalPoints = totalPoints;
        this.numTasks = numTasks;
        this.numThreads = numThreads;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public int getNumTasks() {
        return numTasks;
    }

    public int getNumThreads() {
        return numThreads;
    }

    @Override
    public String toString() {
        return String.format("Config[points=%,d, tasks=%d, threads=%d]",
                totalPoints, numTasks, numThreads);
    }
}
