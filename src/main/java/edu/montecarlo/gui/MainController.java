package edu.montecarlo.gui;

import java.util.ArrayList;
import java.util.List;

import edu.montecarlo.experiment.ExperimentResult;
import edu.montecarlo.experiment.MultiTrialResult;
import edu.montecarlo.experiment.PiExperimentRunner;
import edu.montecarlo.model.SimulationConfig;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;

/**
 * Controller for the Monte Carlo π Estimation GUI.
 * Handles user interactions and visualization updates.
 */
public class MainController {

    @FXML
    private Canvas visualizationCanvas;
    @FXML
    private Spinner<Integer> pointsSpinner;
    @FXML
    private Spinner<Integer> threadsSpinner;
    @FXML
    private Spinner<Integer> trialsSpinner;
    @FXML
    private RadioButton sequentialRadio;
    @FXML
    private RadioButton parallelRadio;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button clearButton;
    @FXML
    private Label piEstimateLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private Label meanErrorLabel;
    @FXML
    private Label stdDevLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea resultsTextArea;

    private VisualizationTask currentTask;
    private Thread simulationThread;
    private long totalPoints = 0;
    private long pointsInside = 0;
    private long startTime;

    /**
     * Initialize the controller and set up UI components.
     */
    @FXML
    public void initialize() {
        // Set up spinners
        pointsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 1000000, 10000, 1000));
        threadsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16, 4, 1));
        trialsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1, 1));

        // Group radio buttons
        ToggleGroup group = new ToggleGroup();
        sequentialRadio.setToggleGroup(group);
        parallelRadio.setToggleGroup(group);
        sequentialRadio.setSelected(true);

        // Disable threads spinner when sequential is selected
        threadsSpinner.setDisable(true);
        sequentialRadio.setOnAction(e -> threadsSpinner.setDisable(true));
        parallelRadio.setOnAction(e -> threadsSpinner.setDisable(false));

        // Draw initial canvas (circle and square)
        drawInitialCanvas();

        // Disable stop button initially
        stopButton.setDisable(true);

        // Add welcome message
        resultsTextArea.setText("Monte Carlo π Estimation\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Configure parameters and click Start to begin simulation.\n" +
                "The visualization shows random points inside a unit square.\n" +
                "Green points fall inside the circle, red points outside.\n\n" +
                "Actual π value: " + Math.PI + "\n");
    }

    /**
     * Draws the initial canvas with circle and square boundaries.
     */
    private void drawInitialCanvas() {
        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        double width = visualizationCanvas.getWidth();
        double height = visualizationCanvas.getHeight();

        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        // Draw square border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, width, height);

        // Draw full circle (inscribed in the square)
        gc.setStroke(Color.rgb(52, 152, 219, 0.7)); // Blue circle
        gc.setLineWidth(2);
        // Draw circle with center at (width/2, height/2) and radius = min(width,
        // height)/2
        double radius = Math.min(width, height) / 2;
        double centerX = width / 2;
        double centerY = height / 2;
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /**
     * Starts the Monte Carlo simulation.
     */
    @FXML
    private void handleStart() {
        // Reset counters
        totalPoints = 0;
        pointsInside = 0;
        startTime = System.currentTimeMillis();

        // Clear canvas
        drawInitialCanvas();

        // Get parameters
        int numPoints = pointsSpinner.getValue();
        boolean isParallel = parallelRadio.isSelected();
        int numThreads = threadsSpinner.getValue();
        int numTrials = trialsSpinner.getValue();

        // If multiple trials, run them and show averaged results
        if (numTrials > 1) {
            runMultipleTrials(numPoints, isParallel, numThreads, numTrials);
            return;
        }

        // Single trial - use visualization
        // Create visualization task
        currentTask = new VisualizationTask(numPoints, isParallel, numThreads, this::addPoint);

        // Bind UI to task
        progressBar.progressProperty().bind(currentTask.progressProperty());
        statusLabel.textProperty().bind(currentTask.messageProperty());

        // Handle task completion
        currentTask.setOnSucceeded(e -> {
            // Unbind before setting values
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            
            double piEstimate = currentTask.getValue();
            long elapsed = System.currentTimeMillis() - startTime;

            piEstimateLabel.setText(String.format("%.10f", piEstimate));
            errorLabel.setText(String.format("%.10f", Math.abs(piEstimate - Math.PI)));
            meanErrorLabel.setText("---");
            stdDevLabel.setText("---");
            timeLabel.setText(elapsed + " ms");

            // Log result
            String mode = isParallel ? "Parallel (" + numThreads + " threads)" : "Sequential";
            resultsTextArea.appendText(String.format("\n[%s] %,d points → π ≈ %.6f, Error: %.6f, Time: %,d ms",
                    mode, numPoints, piEstimate,
                    Math.abs(piEstimate - Math.PI), elapsed));

            startButton.setDisable(false);
            stopButton.setDisable(true);
        });

        currentTask.setOnCancelled(e -> {
            // Unbind before setting values
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            
            statusLabel.setText("Simulation cancelled");
            progressBar.setProgress(0);
            startButton.setDisable(false);
            stopButton.setDisable(true);
        });

        // Run task in background thread
        simulationThread = new Thread(currentTask);
        simulationThread.setDaemon(true);
        simulationThread.start();

        // Update button states
        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    /**
     * Stops the current simulation.
     */
    @FXML
    private void handleStop() {
        if (currentTask != null && simulationThread != null) {
            currentTask.cancel();
            simulationThread.interrupt();
        }
    }

    /**
     * Clears the visualization and results.
     */
    @FXML
    private void handleClear() {
        // Unbind progress bar and status label before clearing
        progressBar.progressProperty().unbind();
        statusLabel.textProperty().unbind();
        
        drawInitialCanvas();
        piEstimateLabel.setText("---");
        errorLabel.setText("---");
        meanErrorLabel.setText("---");
        stdDevLabel.setText("---");
        pointsLabel.setText("0");
        timeLabel.setText("---");
        progressBar.setProgress(0);
        statusLabel.setText("Ready");
        resultsTextArea.clear();
        totalPoints = 0;
        pointsInside = 0;
    }

    /**
     * Runs multiple independent trials and displays averaged results.
     */
    private void runMultipleTrials(int numPoints, boolean isParallel, int numThreads, int numTrials) {
        // Reset counters and clear canvas for visualization
        totalPoints = 0;
        pointsInside = 0;
        drawInitialCanvas();
        
        startButton.setDisable(true);
        stopButton.setDisable(true);
        statusLabel.setText(String.format("Running %d independent trials...", numTrials));
        progressBar.setProgress(0);

        // Run multiple trials in background
        Task<MultiTrialResult> task = new Task<>() {
            @Override
            protected MultiTrialResult call() throws Exception {
                try {
                    updateMessage("Initializing...");
                    updateProgress(0, numTrials);
                    
                    int tasks = isParallel ? numThreads * 2 : 1;
                    String type = isParallel 
                        ? "Parallel (" + numThreads + " threads)" 
                        : "Sequential";
                    
                    // Run trials with progress updates
                    List<ExperimentResult> trialResults = new ArrayList<>();
                    
                    for (int trial = 1; trial <= numTrials; trial++) {
                        if (isCancelled()) {
                            break;
                        }
                        
                        try {
                            updateMessage(String.format("Running trial %d of %d...", trial, numTrials));
                            updateProgress(trial - 1, numTrials);
                            
                            SimulationConfig singleTrialConfig = new SimulationConfig(
                                numPoints, tasks, numThreads);
                            
                            // Clear canvas and reset counters for each new trial (except first)
                            if (trial > 1) {
                                // Reset counters for new trial visualization
                                Platform.runLater(() -> {
                                    drawInitialCanvas();
                                    totalPoints = 0;
                                    pointsInside = 0;
                                });
                                // Small delay to allow UI to update
                                Thread.sleep(200);
                            }
                            
                            // Run ALL trials with visualization
                            long trialStartTime = System.currentTimeMillis();
                            VisualizationTask vizTask = new VisualizationTask(
                                numPoints, isParallel, numThreads, MainController.this::addPoint);
                            
                            // Run the visualization task
                            double piEstimate = vizTask.call();
                            
                            // Create result from visualization task
                            long runtime = System.currentTimeMillis() - trialStartTime;
                            ExperimentResult result = new ExperimentResult(
                                singleTrialConfig, piEstimate, runtime, type);
                            
                            trialResults.add(result);
                            updateMessage(String.format("Completed trial %d of %d (π ≈ %.6f)", 
                                trial, numTrials, result.getPiEstimate()));
                            
                        } catch (Exception ex) {
                            updateMessage(String.format("Error in trial %d: %s", trial, ex.getMessage()));
                            ex.printStackTrace();
                            // Continue with next trial even if one fails
                        }
                    }
                    
                    updateMessage("Calculating statistics...");
                    updateProgress(numTrials, numTrials);
                    
                    // Create config with numTrials for the result
                    SimulationConfig configForResult = new SimulationConfig(
                        numPoints, tasks, numThreads, numTrials);
                    
                    return new MultiTrialResult(configForResult, trialResults, type);
                } catch (Exception e) {
                    updateMessage("Error: " + e.getMessage());
                    throw e;
                }
            }
        };

        // Bind progress bar and status label
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        
        task.setOnSucceeded(e -> {
            try {
                // Unbind before setting values
                progressBar.progressProperty().unbind();
                statusLabel.textProperty().unbind();
                
                MultiTrialResult result = task.getValue();
                
                // Update UI with averaged results
                piEstimateLabel.setText(String.format("%.10f", result.getMeanPiEstimate()));
                errorLabel.setText(String.format("%.10f", Math.abs(result.getMeanPiEstimate() - Math.PI)));
                meanErrorLabel.setText(String.format("%.10f", result.getMeanAbsoluteError()));
                stdDevLabel.setText(String.format("%.10f", result.getStandardDeviation()));
                pointsLabel.setText(String.format("%,d", numPoints));
                timeLabel.setText(String.format("%,d ms (avg)", result.getAverageRuntimeMs()));
                
                // Display detailed results in text area
                resultsTextArea.appendText("\n=== Multiple Trials Results ===\n");
                resultsTextArea.appendText(result.getDetailedSummary());
                resultsTextArea.appendText("\n");
                
                statusLabel.setText("Completed");
                progressBar.setProgress(1.0);
            } catch (Exception ex) {
                progressBar.progressProperty().unbind();
                statusLabel.textProperty().unbind();
                statusLabel.setText("Error displaying results: " + ex.getMessage());
                progressBar.setProgress(0);
                ex.printStackTrace();
            } finally {
                startButton.setDisable(false);
            }
        });

        task.setOnFailed(e -> {
            // Unbind before setting values
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            
            Throwable exception = task.getException();
            String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
            statusLabel.setText("Error: " + errorMsg);
            progressBar.setProgress(0);
            if (exception != null) {
                exception.printStackTrace();
            }
            startButton.setDisable(false);
        });

        new Thread(task).start();
    }

    /**
     * Runs batch experiments comparing different configurations.
     */
    @FXML
    private void handleRunExperiments() {
        int numTrials = trialsSpinner.getValue();
        
        if (numTrials > 1) {
            resultsTextArea.appendText("\n\n=== Running Batch Experiments (Multiple Trials) ===\n");
            resultsTextArea.appendText(String.format("Running %d trials per configuration...\n\n", numTrials));
            
            // Run experiments with multiple trials in background
            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    PiExperimentRunner runner = new PiExperimentRunner();
                    long[] pointsList = { 100_000, 500_000, 1_000_000 };
                    int[] threadCounts = { 2, 4, 8 };

                    var results = runner.runComprehensiveExperimentsWithTrials(
                        pointsList, threadCounts, numTrials);

                    StringBuilder sb = new StringBuilder();
                    for (MultiTrialResult result : results) {
                        sb.append(result.toString()).append("\n");
                    }
                    sb.append("\n=== Detailed Summary ===\n");
                    for (MultiTrialResult result : results) {
                        sb.append(result.getDetailedSummary()).append("\n");
                    }
                    return sb.toString();
                }
            };

            task.setOnSucceeded(e -> {
                resultsTextArea.appendText(task.getValue());
                resultsTextArea.appendText("\nExperiments completed!\n");
            });

            new Thread(task).start();
        } else {
            // Original single-trial batch experiments
            resultsTextArea.appendText("\n\n=== Running Batch Experiments ===\n");

            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    PiExperimentRunner runner = new PiExperimentRunner();
                    long[] pointsList = { 100_000, 500_000, 1_000_000 };
                    int[] threadCounts = { 2, 4, 8 };

                    var results = runner.runComprehensiveExperiments(pointsList, threadCounts);

                    StringBuilder sb = new StringBuilder();
                    for (ExperimentResult result : results) {
                        sb.append(result.toString()).append("\n");
                    }
                    return sb.toString();
                }
            };

            task.setOnSucceeded(e -> {
                resultsTextArea.appendText(task.getValue());
                resultsTextArea.appendText("\nExperiments completed!\n");
            });

            new Thread(task).start();
        }
    }

    /**
     * Adds a point to the visualization.
     * Called from the background task for each generated point.
     */
    private void addPoint(VisualizationTask.PointData point) {
        GraphicsContext gc = visualizationCanvas.getGraphicsContext2D();
        double width = visualizationCanvas.getWidth();
        double height = visualizationCanvas.getHeight();

        // Map point from [-1,1] x [-1,1] to canvas centered at middle
        double radius = Math.min(width, height) / 2;
        double centerX = width / 2;
        double centerY = height / 2;

        // Scale from [-1,1] to canvas coordinates
        // point.x in [-1, 1] maps to [centerX - radius, centerX + radius]
        double canvasX = centerX + point.x * radius;
        double canvasY = centerY + point.y * radius;

        // Draw point
        gc.setFill(point.insideCircle ? Color.rgb(0, 200, 0, 0.7) : Color.rgb(200, 0, 0, 0.7));
        gc.fillOval(canvasX - 1.5, canvasY - 1.5, 3, 3);

        // Update counters
        totalPoints++;
        if (point.insideCircle) {
            pointsInside++;
        }

        // Update labels
        pointsLabel.setText(String.format("%,d", totalPoints));

        if (totalPoints > 0) {
            double currentEstimate = 4.0 * pointsInside / totalPoints;
            piEstimateLabel.setText(String.format("%.10f", currentEstimate));
            errorLabel.setText(String.format("%.10f", Math.abs(currentEstimate - Math.PI)));
            meanErrorLabel.setText("---");
            stdDevLabel.setText("---");
        }
    }
}
