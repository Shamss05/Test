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
