package edu.montecarlo.experiment;

import java.util.ArrayList;
import java.util.List;

import edu.montecarlo.model.ParallelPiEstimator;
import edu.montecarlo.model.PiEstimator;
import edu.montecarlo.model.SequentialPiEstimator;
import edu.montecarlo.model.SimulationConfig;

/**
 * Runs experiments to compare sequential and parallel π estimators.
 * Measures runtime, accuracy, and speedup for different configurations.
 */
public class PiExperimentRunner {

    /**
     * Runs a single experiment with the given configuration and estimator.
     * 
     * @param estimator The π estimation strategy to use
     * @param config    Simulation parameters
     * @param type      Descriptive name for this estimator
     * @return Experiment result with timing and accuracy data
     */
    public ExperimentResult runExperiment(PiEstimator estimator,
            SimulationConfig config,
            String type) {
        // Warm up JVM (optional but recommended for accurate timing)
        if (config.getTotalPoints() > 100000) {
            estimator.estimatePi(new SimulationConfig(10000, config.getNumTasks(), config.getNumThreads()));
        }

        // Measure execution time
        long startTime = System.currentTimeMillis();
        double piEstimate = estimator.estimatePi(config);
        long endTime = System.currentTimeMillis();

        long runtime = endTime - startTime;

        return new ExperimentResult(config, piEstimate, runtime, type);
    }

    /**
     * Runs multiple independent trials and returns averaged results.
     * This is useful for getting more reliable error estimates by running
     * the same experiment multiple times and averaging the results.
     * 
     * @param estimator The π estimation strategy to use
     * @param config    Simulation parameters (must have numTrials > 1)
     * @param type      Descriptive name for this estimator
     * @return MultiTrialResult with averaged error statistics
     */
    public MultiTrialResult runMultipleTrials(PiEstimator estimator,
            SimulationConfig config,
            String type) {
        if (config.getNumTrials() < 1) {
            throw new IllegalArgumentException("Number of trials must be at least 1");
        }

        List<ExperimentResult> trialResults = new ArrayList<>();
        
        System.out.println(String.format("Running %d independent trials...", config.getNumTrials()));

        // Run each trial independently
        for (int trial = 1; trial <= config.getNumTrials(); trial++) {
            // Create a single-trial config for each run
            SimulationConfig singleTrialConfig = new SimulationConfig(
                    config.getTotalPoints(),
                    config.getNumTasks(),
                    config.getNumThreads());
            
            ExperimentResult result = runExperiment(estimator, singleTrialConfig, type);
            trialResults.add(result);
            
            System.out.println(String.format("  Trial %d/%d: π ≈ %.6f, Error: %.6f, Time: %,d ms",
                    trial, config.getNumTrials(),
                    result.getPiEstimate(),
                    result.getAbsoluteError(),
                    result.getRuntimeMs()));
        }

        return new MultiTrialResult(config, trialResults, type);
    }

    /**
     * Runs a comprehensive set of experiments comparing sequential and parallel
     * estimators.
     * 
     * @param pointsList   Different sample sizes to test
     * @param threadCounts Different thread pool sizes to test
     * @return List of all experiment results
     */
    public List<ExperimentResult> runComprehensiveExperiments(long[] pointsList, int[] threadCounts) {
        List<ExperimentResult> results = new ArrayList<>();

        PiEstimator sequentialEstimator = new SequentialPiEstimator();
        PiEstimator parallelEstimator = new ParallelPiEstimator();

        System.out.println("=== Monte Carlo π Estimation Experiments ===\n");

        // Test each sample size
        for (long points : pointsList) {
            System.out.println("Testing with " + String.format("%,d", points) + " points:");

            // Run sequential version
            SimulationConfig seqConfig = new SimulationConfig(points, 1, 1);
            ExperimentResult seqResult = runExperiment(sequentialEstimator, seqConfig, "Sequential");
            results.add(seqResult);
            System.out.println("  " + seqResult);

            // Run parallel versions with different thread counts
            for (int threads : threadCounts) {
                int tasks = threads * 2; // Use 2x tasks as threads for better load balancing
                SimulationConfig parConfig = new SimulationConfig(points, tasks, threads);
                ExperimentResult parResult = runExperiment(parallelEstimator, parConfig,
                        "Parallel(" + threads + " threads)");
                results.add(parResult);

                // Calculate speedup
                double speedup = (double) seqResult.getRuntimeMs() / parResult.getRuntimeMs();
                System.out.println("  " + parResult +
                        String.format(" | Speedup: %.2fx", speedup));
            }

            System.out.println();
        }

        return results;
    }

    /**
     * Runs comprehensive experiments with multiple trials and averaged error.
     * 
     * @param pointsList   Different sample sizes to test
     * @param threadCounts Different thread pool sizes to test
     * @param numTrials    Number of independent trials to run for each configuration
     * @return List of multi-trial results
     */
    public List<MultiTrialResult> runComprehensiveExperimentsWithTrials(
            long[] pointsList, int[] threadCounts, int numTrials) {
        List<MultiTrialResult> results = new ArrayList<>();

        PiEstimator sequentialEstimator = new SequentialPiEstimator();
        PiEstimator parallelEstimator = new ParallelPiEstimator();

        System.out.println("=== Monte Carlo π Estimation Experiments (Multiple Trials) ===\n");
        System.out.println(String.format("Running %d independent trials per configuration\n", numTrials));

        // Test each sample size
        for (long points : pointsList) {
            System.out.println("Testing with " + String.format("%,d", points) + " points:");

            // Run sequential version with multiple trials
            SimulationConfig seqConfig = new SimulationConfig(points, 1, 1, numTrials);
            MultiTrialResult seqResult = runMultipleTrials(sequentialEstimator, seqConfig, "Sequential");
            results.add(seqResult);
            System.out.println("  " + seqResult);
            System.out.println();

            // Run parallel versions with different thread counts
            for (int threads : threadCounts) {
                int tasks = threads * 2; // Use 2x tasks as threads for better load balancing
                SimulationConfig parConfig = new SimulationConfig(points, tasks, threads, numTrials);
                MultiTrialResult parResult = runMultipleTrials(parallelEstimator, parConfig,
                        "Parallel(" + threads + " threads)");
                results.add(parResult);

                // Calculate speedup based on average runtime
                double speedup = (double) seqResult.getAverageRuntimeMs() / parResult.getAverageRuntimeMs();
                System.out.println("  " + parResult +
                        String.format(" | Avg Speedup: %.2fx", speedup));
                System.out.println();
            }
        }

        return results;
    }

    /**
     * Prints a summary table of all experiment results.
     */
    public void printResultsSummary(List<ExperimentResult> results) {
        System.out.println("\n=== Experiment Summary ===");
        System.out.println(String.format("%-20s | %-15s | %-12s | %-12s | %-10s",
                "Estimator", "Points", "π Estimate", "Error", "Time (ms)"));
        System.out.println("-".repeat(85));

        for (ExperimentResult result : results) {
            System.out.println(String.format("%-20s | %,15d | %.10f | %.10f | %,10d",
                    result.getEstimatorType(),
                    result.getConfig().getTotalPoints(),
                    result.getPiEstimate(),
                    result.getAbsoluteError(),
                    result.getRuntimeMs()));
        }
    }

    /**
     * Prints a summary table of multi-trial experiment results.
     */
    public void printMultiTrialResultsSummary(List<MultiTrialResult> results) {
        System.out.println("\n=== Multi-Trial Experiment Summary ===");
        System.out.println(String.format("%-20s | %-15s | %-6s | %-12s | %-12s | %-12s | %-10s",
                "Estimator", "Points", "Trials", "Mean π", "Mean Error", "Std Dev", "Avg Time (ms)"));
        System.out.println("-".repeat(110));

        for (MultiTrialResult result : results) {
            System.out.println(String.format("%-20s | %,15d | %6d | %.10f | %.10f | %.10f | %,10d",
                    result.getEstimatorType(),
                    result.getConfig().getTotalPoints(),
                    result.getNumTrials(),
                    result.getMeanPiEstimate(),
                    result.getMeanAbsoluteError(),
                    result.getStandardDeviation(),
                    result.getAverageRuntimeMs()));
        }
    }

    /**
     * Main method to run default experiments.
     */
    public static void main(String[] args) {
        PiExperimentRunner runner = new PiExperimentRunner();

        // Define test configurations
        long[] pointsList = { 100_000, 1_000_000, 10_000_000 };
        int[] threadCounts = { 2, 4, 8 };

        // Run experiments
        List<ExperimentResult> results = runner.runComprehensiveExperiments(pointsList, threadCounts);

        // Print summary
        runner.printResultsSummary(results);

        System.out.println("\nActual π value: " + Math.PI);
    }
}
