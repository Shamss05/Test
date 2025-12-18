/**
 * Monte Carlo π Estimation module.
 * Provides parallel and sequential estimation with JavaFX visualization.
 */
module edu.montecarlo {
    requires javafx.controls;
    requires javafx.fxml;

    // Export packages for JavaFX access
    opens edu.montecarlo.gui to javafx.fxml;

    exports edu.montecarlo;
    exports edu.montecarlo.model;
    exports edu.montecarlo.experiment;
    exports edu.montecarlo.gui;
}
