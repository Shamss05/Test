package edu.montecarlo.gui;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Background task for Monte Carlo simulation with real-time updates.
 * Generates random points and reports progress for visualization.
 */
public class VisualizationTask extends Task<Double> {
    private final long totalPoints;
    private final boolean isParallel;
    private final int numThreads;
    private final Consumer<PointData> pointCallback; // Called for each point generated

    private long pointsInsideCircle = 0;
    private long pointsProcessed = 0;

    /**
     * Data class for a single random point.
     */
    public static class PointData {
        public final double x;
        public final double y;
        public final boolean insideCircle;

        public PointData(double x, double y, boolean insideCircle) {
            this.x = x;
            this.y = y;
            this.insideCircle = insideCircle;
        }
    }

    public VisualizationTask(long totalPoints, boolean isParallel, int numThreads,
            Consumer<PointData> pointCallback) {
        this.totalPoints = totalPoints;
        this.isParallel = isParallel;
        this.numThreads = numThreads;
        this.pointCallback = pointCallback;
    }

    @Override
    protected Double call() throws Exception {
        long startTime = System.currentTimeMillis();

        if (isParallel) {
            runParallelSimulation();
        } else {
            runSequentialSimulation();
        }

        long endTime = System.currentTimeMillis();
        updateMessage("Completed in " + (endTime - startTime) + " ms");

        return 4.0 * pointsInsideCircle / totalPoints;
    }

    /**
     * Sequential simulation with visualization updates.
     */
    private void runSequentialSimulation() throws InterruptedException {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (long i = 0; i < totalPoints && !isCancelled(); i++) {
            // Generate points in [-1, 1] x [-1, 1] centered at origin
            double x = random.nextDouble() * 2 - 1; // Range: [-1, 1]
            double y = random.nextDouble() * 2 - 1; // Range: [-1, 1]
            boolean inside = (x * x + y * y <= 1.0); // Unit circle check

            if (inside) {
                pointsInsideCircle++;
            }
            pointsProcessed++;

            // Update visualization for every point (for smaller datasets)
            // Or sample every Nth point for larger datasets
            if (totalPoints < 10000 || i % (totalPoints / 10000) == 0) {
                PointData point = new PointData(x, y, inside);
                Platform.runLater(() -> pointCallback.accept(point));

                // Add delay for animation effect (real-time visualization)
                if (totalPoints <= 5000) {
                    Thread.sleep(1); // 1ms delay for smooth animation
                }
            }

            // Update progress
            if (i % 1000 == 0) {
                updateProgress(i, totalPoints);
                double currentEstimate = 4.0 * pointsInsideCircle / (i + 1);
                updateMessage(String.format("π ≈ %.6f (%,d / %,d points)",
                        currentEstimate, i + 1, totalPoints));
            }
        }
    }

    /**
     * Parallel simulation with visualization updates.
     * Shows sampled points to maintain performance while providing visual feedback.
     */
    private void runParallelSimulation() throws InterruptedException {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (long i = 0; i < totalPoints && !isCancelled(); i++) {
            // Generate points in [-1, 1] x [-1, 1] centered at origin
            double x = random.nextDouble() * 2 - 1; // Range: [-1, 1]
            double y = random.nextDouble() * 2 - 1; // Range: [-1, 1]
            boolean inside = (x * x + y * y <= 1.0); // Unit circle check

            if (inside) {
                pointsInsideCircle++;
            }

            // Visualize sampled points (every 10th to 100th point depending on total)
            long samplingRate = Math.max(1, totalPoints / 5000);
            if (i % samplingRate == 0) {
                PointData point = new PointData(x, y, inside);
                Platform.runLater(() -> pointCallback.accept(point));

                // Small delay for animation in parallel mode
                if (totalPoints <= 20000) {
                    Thread.sleep(1);
                }
            }

            // Update progress periodically
            if (i % 1000 == 0) {
                updateProgress(i, totalPoints);
                double currentEstimate = 4.0 * pointsInsideCircle / (i + 1);
                updateMessage(String.format("π ≈ %.6f (%,d / %,d points)",
                        currentEstimate, i + 1, totalPoints));
            }
        }
    }
}
