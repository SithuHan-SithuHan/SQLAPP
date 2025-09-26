package com.sqllearningapp;

import atlantafx.base.theme.PrimerLight;
import com.sqllearningapp.core.database.EmbeddedDatabase;
import com.sqllearningapp.core.services.LearningContentService;
import com.sqllearningapp.core.services.PracticeService;
import com.sqllearningapp.core.services.ProgressTrackingService;
import com.sqllearningapp.ui.MainWindow;
import com.sqllearningapp.utils.ConfigManager;
import com.sqllearningapp.utils.ProgressPersistenceManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLLearningApplication extends Application {

    private EmbeddedDatabase databaseManager;
    private ConfigManager configManager;
    private ProgressPersistenceManager progressManager;
    private LearningContentService learningService;
    private PracticeService practiceService;
    private ProgressTrackingService progressTrackingService;
    private MainWindow mainWindow;

    public static void main(String[] args) {
        // System properties for optimal JavaFX performance
        System.setProperty("javafx.preloader", "false");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        log.info("Starting SQL Learning Desktop Application 2.0");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        log.info("Initializing application components...");

        try {
            // Initialize configuration
            configManager = new ConfigManager();
            configManager.loadConfiguration();

            // Initialize progress persistence
            progressManager = new ProgressPersistenceManager();
            progressManager.loadProgress();

            // Initialize database
            databaseManager = new EmbeddedDatabase();
            databaseManager.initialize();

            // Initialize services
            learningService = new LearningContentService();
            practiceService = new PracticeService();
            progressTrackingService = new ProgressTrackingService(progressManager);

            log.info("Application initialization completed successfully");

        } catch (Exception e) {
            log.error("Failed to initialize application", e);
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting UI...");

        try {
            // Set modern theme
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            // Create main window with all services
            mainWindow = new MainWindow(
                    databaseManager,
                    learningService,
                    practiceService,
                    progressTrackingService,
                    configManager
            );

            // Show the main window
            mainWindow.show(primaryStage);

            // Handle application close
            primaryStage.setOnCloseRequest(event -> {
                event.consume(); // Prevent default close
                handleApplicationClose();
            });

            log.info("UI started successfully");

        } catch (Exception e) {
            log.error("Failed to start UI", e);
            showErrorAndExit("Startup Error", "Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        handleApplicationClose();
    }

    private void handleApplicationClose() {
        log.info("Shutting down application...");

        try {
            // Save progress
            if (progressTrackingService != null) {
                progressTrackingService.saveProgress();
            }

            // Save configuration
            if (configManager != null) {
                configManager.saveConfiguration();
            }

            // Close database
            if (databaseManager != null) {
                databaseManager.close();
            }

            // Shutdown main window
            if (mainWindow != null) {
                mainWindow.shutdown();
            }

            log.info("Application shutdown completed successfully");

        } catch (Exception e) {
            log.error("Error during application shutdown", e);
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    private void showErrorAndExit(String title, String message) {
        Platform.runLater(() -> {
            try {
                var alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText("Application Error");
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println(title + ": " + message);
            }
            Platform.exit();
            System.exit(1);
        });
    }
}