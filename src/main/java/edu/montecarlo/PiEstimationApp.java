package edu.montecarlo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX application for Monte Carlo π Estimation.
 * Launches the GUI with real-time visualization.
 */
public class PiEstimationApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/edu/montecarlo/gui/main.fxml"));
        Scene scene = new Scene(loader.load());

        // Configure stage
        primaryStage.setTitle("Monte Carlo π Estimation");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(750);

        // Show window
        primaryStage.show();
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
