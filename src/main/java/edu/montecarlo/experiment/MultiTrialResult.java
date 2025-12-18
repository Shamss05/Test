package edu.montecarlo.experiment;

import edu.montecarlo.model.SimulationConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores results from multiple independent trials with averaged error statistics.
 * This class calculates the mean π estimate, mean error, standard deviation,
 * and other statistical measures across multiple trials.
 */
public class MultiTrialResult {
    private final SimulationConfig config;
    private final List<ExperimentResult> trialResults;
    private final double meanPiEstimate;
    private final double meanAbsoluteError;
    private final double standardDeviation;
    private final double minError;
    private final double maxError;
    private final long totalRuntimeMs;
    private final String estimatorType;

    /**
     * Creates a multi-trial result from a list of individual trial results.
     * 
     * @param config        Simulation configuration used
     * @param trialResults  List of results from each independent trial
     * @param estimatorType Type of estimator used (e.g., "Sequential", "Parallel")
     */
    public MultiTrialResult(SimulationConfig config, List<ExperimentResult> trialResults,
            String estimatorType) {
        this.config = config;
        this.trialResults = new ArrayList<>(trialResults);
        this.estimatorType = estimatorType;

        if (trialResults.isEmpty()) {
            throw new IllegalArgumentException("Cannot create MultiTrialResult with empty trial list");
        }

        // Calculate mean π estimate
        double sumPi = 0.0;
        double sumError = 0.0;
        double minErr = Double.MAX_VALUE;
        double maxErr = 0.0;
        long totalTime = 0;

        for (ExperimentResult result : trialResults) {
            double piEst = result.getPiEstimate();
            double error = result.getAbsoluteError();
            sumPi += piEst;
            sumError += error;
            minErr = Math.min(minErr, error);
            maxErr = Math.max(maxErr, error);
            totalTime += result.getRuntimeMs();
        }

        this.meanPiEstimate = sumPi / trialResults.size();
        this.meanAbsoluteError = sumError / trialResults.size();
        this.minError = minErr;
        this.maxError = maxErr;
        this.totalRuntimeMs = totalTime;

        // Calculate standard deviation of π estimates
        double variance = 0.0;
        for (ExperimentResult result : trialResults) {
            double diff = result.getPiEstimate() - meanPiEstimate;
            variance += diff * diff;
        }
        this.standardDeviation = Math.sqrt(variance / trialResults.size());
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public List<ExperimentResult> getTrialResults() {
        return new ArrayList<>(trialResults);
    }

    public double getMeanPiEstimate() {
        return meanPiEstimate;
    }

    public double getMeanAbsoluteError() {
        return meanAbsoluteError;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getMinError() {
        return minError;
    }

    public double getMaxError() {
        return maxError;
    }

    public long getTotalRuntimeMs() {
        return totalRuntimeMs;
    }

    public long getAverageRuntimeMs() {
        return totalRuntimeMs / trialResults.size();
    }

    public String getEstimatorType() {
        return estimatorType;
    }

    public int getNumTrials() {
        return trialResults.size();
    }

    /**
     * Returns a formatted string representation of the multi-trial results.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s | %s | ", estimatorType, config));
        sb.append(String.format("Mean π ≈ %.6f | Mean Error: %.6f", meanPiEstimate, meanAbsoluteError));
        sb.append(String.format(" | Std Dev: %.6f | Trials: %d", standardDeviation, trialResults.size()));
        sb.append(String.format(" | Avg Time: %,d ms", getAverageRuntimeMs()));
        return sb.toString();
    }

    /**
     * Returns a detailed summary including min/max error and individual trial results.
     */
    public String getDetailedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== Multi-Trial Results: %s ===\n", estimatorType));
        sb.append(String.format("Configuration: %s\n", config));
        sb.append(String.format("Number of Trials: %d\n", trialResults.size()));
        sb.append(String.format("Mean π Estimate: %.10f\n", meanPiEstimate));
        sb.append(String.format("Mean Absolute Error: %.10f\n", meanAbsoluteError));
        sb.append(String.format("Standard Deviation: %.10f\n", standardDeviation));
        sb.append(String.format("Min Error: %.10f\n", minError));
        sb.append(String.format("Max Error: %.10f\n", maxError));
        sb.append(String.format("Average Runtime: %,d ms\n", getAverageRuntimeMs()));
        sb.append(String.format("Total Runtime: %,d ms\n", totalRuntimeMs));
        sb.append("\nIndividual Trial Results:\n");
        for (int i = 0; i < trialResults.size(); i++) {
            sb.append(String.format("  Trial %d: %s\n", i + 1, trialResults.get(i)));
        }
        return sb.toString();
    }
}

