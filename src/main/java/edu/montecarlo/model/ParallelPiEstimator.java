package edu.montecarlo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Parallel implementation of π estimation using ExecutorService and Futures.
 * Divides work into multiple tasks for concurrent execution.
 */
public class ParallelPiEstimator implements PiEstimator {

    /**
     * Estimates π using parallel tasks.
     * Each task generates its portion of random points independently.
     */
    @Override
    public double estimatePi(SimulationConfig config) {
        // Create fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(config.getNumThreads());

        // Calculate points per task
        long pointsPerTask = config.getTotalPoints() / config.getNumTasks();
        long remainder = config.getTotalPoints() % config.getNumTasks();

        // Create and submit all tasks
        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < config.getNumTasks(); i++) {
            // Last task gets any remaining points
            long points = (i == config.getNumTasks() - 1) ? pointsPerTask + remainder : pointsPerTask;
            futures.add(executor.submit(new MonteCarloTask(points)));
        }

        // Collect results from all tasks
        long totalPointsInsideCircle = 0;
        try {
            for (Future<Long> future : futures) {
                totalPointsInsideCircle += future.get(); // Wait for task completion
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during parallel execution", e);
        } finally {
            executor.shutdown(); // Clean up thread pool
        }

        // Calculate π estimate
        return 4.0 * totalPointsInsideCircle / config.getTotalPoints();
    }

    /**
     * Callable task that generates random points and counts hits inside circle.
     * Uses ThreadLocalRandom for thread-safe random number generation.
     */
    private static class MonteCarloTask implements Callable<Long> {
        private final long numPoints;

        public MonteCarloTask(long numPoints) {
            this.numPoints = numPoints;
        }

        @Override
        public Long call() {
            long hitsInsideCircle = 0;

            // Use ThreadLocalRandom for better performance in concurrent scenarios
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (long i = 0; i < numPoints; i++) {
                double x = random.nextDouble();
                double y = random.nextDouble();

                // Check if point falls inside unit circle
                if (x * x + y * y <= 1.0) {
                    hitsInsideCircle++;
                }
            }

            return hitsInsideCircle;
        }
    }
}
