package edu.montecarlo.experiment;

import edu.montecarlo.model.SimulationConfig;

/**
 * Stores results from a single π estimation experiment.
 */
public class ExperimentResult {
    private final SimulationConfig config;
    private final double piEstimate;
    private final long runtimeMs;
    private final double absoluteError;
    private final String estimatorType;

    public ExperimentResult(SimulationConfig config, double piEstimate,
            long runtimeMs, String estimatorType) {
        this.config = config;
        this.piEstimate = piEstimate;
        this.runtimeMs = runtimeMs;
        this.estimatorType = estimatorType;
        this.absoluteError = Math.abs(piEstimate - Math.PI);
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public double getPiEstimate() {
        return piEstimate;
    }

    public long getRuntimeMs() {
        return runtimeMs;
    }

    public double getAbsoluteError() {
        return absoluteError;
    }

    public String getEstimatorType() {
        return estimatorType;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | π ≈ %.6f | Error: %.6f | Time: %,d ms",
                estimatorType, config, piEstimate, absoluteError, runtimeMs);
    }
}
