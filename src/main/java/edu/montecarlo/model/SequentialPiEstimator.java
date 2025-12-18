package edu.montecarlo.model;

import java.util.Random;

/**
 * Sequential implementation of π estimation.
 * Uses single-threaded Monte Carlo method.
 */
public class SequentialPiEstimator implements PiEstimator {

    /**
     * Estimates π by generating random points and checking if they fall inside a
     * circle.
     * Formula: π ≈ 4 × (points inside circle) / (total points)
     */
    @Override
    public double estimatePi(SimulationConfig config) {
        Random random = new Random();
        long pointsInsideCircle = 0;

        // Generate random points and count those inside the unit circle
        for (long i = 0; i < config.getTotalPoints(); i++) {
            double x = random.nextDouble(); // Random x in [0, 1)
            double y = random.nextDouble(); // Random y in [0, 1)

            // Check if point is inside circle: x² + y² ≤ 1
            if (x * x + y * y <= 1.0) {
                pointsInsideCircle++;
            }
        }

        // π = 4 × (circle area / square area)
        return 4.0 * pointsInsideCircle / config.getTotalPoints();
    }
}
