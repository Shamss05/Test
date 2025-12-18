package edu.montecarlo.model;

/**
 * Configuration class for Monte Carlo simulation parameters.
 * Holds all settings needed for π estimation.
 */
public class SimulationConfig {
    private final long totalPoints; // Total number of random points to generate
    private final int numTasks; // Number of parallel tasks to divide work into
    private final int numThreads; // Size of thread pool
    private final int numTrials; // Number of independent trials to run

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
        this.numTrials = 1; // Default to single trial
    }

    /**
     * Creates a new simulation configuration with multiple trials.
     * 
     * @param totalPoints Total number of random points
     * @param numTasks    Number of tasks for parallel execution
     * @param numThreads  Number of threads in the pool
     * @param numTrials   Number of independent trials to run
     */
    public SimulationConfig(long totalPoints, int numTasks, int numThreads, int numTrials) {
        this.totalPoints = totalPoints;
        this.numTasks = numTasks;
        this.numThreads = numThreads;
        this.numTrials = numTrials;
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

    public int getNumTrials() {
        return numTrials;
    }

    @Override
    public String toString() {
        if (numTrials > 1) {
            return String.format("Config[points=%,d, tasks=%d, threads=%d, trials=%d]",
                    totalPoints, numTasks, numThreads, numTrials);
        }
        return String.format("Config[points=%,d, tasks=%d, threads=%d]",
                totalPoints, numTasks, numThreads);
    }
}
