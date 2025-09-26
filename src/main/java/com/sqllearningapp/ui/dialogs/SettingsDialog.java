package com.sqllearningapp.ui.dialogs;

import com.sqllearningapp.utils.ConfigManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import lombok.extern.slf4j.Slf4j;

/**
 * Settings Dialog for SQL Learning Application
 * Provides comprehensive configuration options for all application settings
 */
@Slf4j
public class SettingsDialog extends Dialog<ButtonType> {

    private final ConfigManager configManager;
    private final ConfigManager.AppConfig config;

    // UI Preferences
    private ComboBox<String> themeComboBox;
    private Spinner<Integer> fontSizeSpinner;
    private CheckBox showLineNumbersCheckBox;
    private CheckBox wordWrapCheckBox;
    private CheckBox autoSaveCheckBox;
    private CheckBox autoIndentCheckBox;

    // Learning Preferences
    private CheckBox autoAdvanceTopicsCheckBox;
    private CheckBox showHintsCheckBox;
    private CheckBox trackProgressCheckBox;
    private CheckBox playSoundCheckBox;
    private ComboBox<String> learningModeComboBox;

    // Database Settings
    private Spinner<Integer> queryTimeoutSpinner;
    private Spinner<Integer> maxResultRowsSpinner;
    private CheckBox autoResetDatabaseCheckBox;
    private CheckBox enableQueryLoggingCheckBox;

    // Developer Settings
    private CheckBox debugModeCheckBox;
    private CheckBox showExecutionTimeCheckBox;
    private CheckBox logQueriesCheckBox;
    private CheckBox enableMetricsCheckBox;
    private CheckBox verboseLoggingCheckBox;

    public SettingsDialog(ConfigManager configManager) {
        this.configManager = configManager;
        this.config = configManager.getConfig();

        initializeDialog();
        createContent();
        loadCurrentSettings();
        setupEventHandlers();
    }

    private void initializeDialog() {
        setTitle("Application Settings");
        setHeaderText("Configure SQL Learning Application");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);

        // Add button types
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        // Style the dialog
        getDialogPane().getStyleClass().add("settings-dialog");
        getDialogPane().setPrefSize(600, 500);
    }

    private void createContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create tabs
        Tab uiTab = createUIPreferencesTab();
        Tab learningTab = createLearningPreferencesTab();
        Tab databaseTab = createDatabaseSettingsTab();
        Tab developerTab = createDeveloperSettingsTab();

        tabPane.getTabs().addAll(uiTab, learningTab, databaseTab, developerTab);

        getDialogPane().setContent(tabPane);
    }

    private Tab createUIPreferencesTab() {
        Tab tab = new Tab("🎨 UI Preferences");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Theme selection
        VBox themeSection = createSection("Theme & Appearance");

        HBox themeRow = new HBox(10);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        Label themeLabel = new Label("Theme:");
        themeLabel.setPrefWidth(120);

        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(
                "modern-light", "modern-dark", "classic", "high-contrast"
        );
        themeComboBox.setPrefWidth(150);

        themeRow.getChildren().addAll(themeLabel, themeComboBox);
        themeSection.getChildren().add(themeRow);

        // Font size
        HBox fontRow = new HBox(10);
        fontRow.setAlignment(Pos.CENTER_LEFT);
        Label fontLabel = new Label("Font Size:");
        fontLabel.setPrefWidth(120);

        fontSizeSpinner = new Spinner<>(8, 72, 14, 1);
        fontSizeSpinner.setPrefWidth(80);
        fontSizeSpinner.setEditable(true);

        fontRow.getChildren().addAll(fontLabel, fontSizeSpinner);
        themeSection.getChildren().add(fontRow);

        // Editor preferences
        VBox editorSection = createSection("Editor Options");

        showLineNumbersCheckBox = new CheckBox("Show line numbers");
        wordWrapCheckBox = new CheckBox("Word wrap");
        autoSaveCheckBox = new CheckBox("Auto-save changes");
        autoIndentCheckBox = new CheckBox("Auto-indent code");

        editorSection.getChildren().addAll(
                showLineNumbersCheckBox, wordWrapCheckBox,
                autoSaveCheckBox, autoIndentCheckBox
        );

        content.getChildren().addAll(themeSection, editorSection);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private Tab createLearningPreferencesTab() {
        Tab tab = new Tab("📚 Learning");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Learning behavior
        VBox behaviorSection = createSection("Learning Behavior");

        autoAdvanceTopicsCheckBox = new CheckBox("Automatically advance to next topic");
        showHintsCheckBox = new CheckBox("Show hints for questions");
        trackProgressCheckBox = new CheckBox("Track learning progress");
        playSoundCheckBox = new CheckBox("Play notification sounds");

        behaviorSection.getChildren().addAll(
                autoAdvanceTopicsCheckBox, showHintsCheckBox,
                trackProgressCheckBox, playSoundCheckBox
        );

        // Learning mode
        VBox modeSection = createSection("Learning Mode");

        HBox modeRow = new HBox(10);
        modeRow.setAlignment(Pos.CENTER_LEFT);
        Label modeLabel = new Label("Mode:");
        modeLabel.setPrefWidth(120);

        learningModeComboBox = new ComboBox<>();
        learningModeComboBox.getItems().addAll("guided", "free-form");
        learningModeComboBox.setPrefWidth(150);

        modeRow.getChildren().addAll(modeLabel, learningModeComboBox);
        modeSection.getChildren().add(modeRow);

        content.getChildren().addAll(behaviorSection, modeSection);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private Tab createDatabaseSettingsTab() {
        Tab tab = new Tab("🗄️ Database");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Query execution
        VBox executionSection = createSection("Query Execution");

        HBox timeoutRow = new HBox(10);
        timeoutRow.setAlignment(Pos.CENTER_LEFT);
        Label timeoutLabel = new Label("Query Timeout (seconds):");
        timeoutLabel.setPrefWidth(180);

        queryTimeoutSpinner = new Spinner<>(5, 300, 30, 5);
        queryTimeoutSpinner.setPrefWidth(80);
        queryTimeoutSpinner.setEditable(true);

        timeoutRow.getChildren().addAll(timeoutLabel, queryTimeoutSpinner);
        executionSection.getChildren().add(timeoutRow);

        HBox rowsRow = new HBox(10);
        rowsRow.setAlignment(Pos.CENTER_LEFT);
        Label rowsLabel = new Label("Max Result Rows:");
        rowsLabel.setPrefWidth(180);

        maxResultRowsSpinner = new Spinner<>(100, 10000, 1000, 100);
        maxResultRowsSpinner.setPrefWidth(80);
        maxResultRowsSpinner.setEditable(true);

        rowsRow.getChildren().addAll(rowsLabel, maxResultRowsSpinner);
        executionSection.getChildren().add(rowsRow);

        // Database options
        VBox optionsSection = createSection("Database Options");

        autoResetDatabaseCheckBox = new CheckBox("Auto-reset database between questions");
        enableQueryLoggingCheckBox = new CheckBox("Enable query logging");

        optionsSection.getChildren().addAll(
                autoResetDatabaseCheckBox, enableQueryLoggingCheckBox
        );

        content.getChildren().addAll(executionSection, optionsSection);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private Tab createDeveloperSettingsTab() {
        Tab tab = new Tab("⚙️ Developer");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Debug options
        VBox debugSection = createSection("Debug & Logging");

        debugModeCheckBox = new CheckBox("Enable debug mode");
        showExecutionTimeCheckBox = new CheckBox("Show query execution time");
        logQueriesCheckBox = new CheckBox("Log all SQL queries");
        enableMetricsCheckBox = new CheckBox("Enable performance metrics");
        verboseLoggingCheckBox = new CheckBox("Verbose logging");

        debugSection.getChildren().addAll(
                debugModeCheckBox, showExecutionTimeCheckBox,
                logQueriesCheckBox, enableMetricsCheckBox, verboseLoggingCheckBox
        );

        // Info section
        VBox infoSection = createSection("Application Information");

        Label versionLabel = new Label("Version: 2.0");
        Label userLabel = new Label("User: " + configManager.getUserLogin());
        Label repoLabel = new Label("Repository: " + configManager.getGitHubRepository());

        versionLabel.getStyleClass().add("info-label");
        userLabel.getStyleClass().add("info-label");
        repoLabel.getStyleClass().add("info-label");

        infoSection.getChildren().addAll(versionLabel, userLabel, repoLabel);

        content.getChildren().addAll(debugSection, infoSection);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private VBox createSection(String title) {
        VBox section = new VBox(10);
        section.getStyleClass().add("settings-section");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        section.getChildren().add(titleLabel);
        return section;
    }

    private void loadCurrentSettings() {
        // UI Preferences
        themeComboBox.setValue(config.getTheme());
        fontSizeSpinner.getValueFactory().setValue(config.getFontSize());
        showLineNumbersCheckBox.setSelected(config.isShowLineNumbers());
        wordWrapCheckBox.setSelected(config.isWordWrap());
        autoSaveCheckBox.setSelected(config.isAutoSave());
        autoIndentCheckBox.setSelected(config.isAutoIndent());

        // Learning Preferences
        autoAdvanceTopicsCheckBox.setSelected(config.isAutoAdvanceTopics());
        showHintsCheckBox.setSelected(config.isShowHints());
        trackProgressCheckBox.setSelected(config.isTrackProgress());
        playSoundCheckBox.setSelected(config.isPlaySound());
        learningModeComboBox.setValue(config.getLearningMode());

        // Database Settings
        queryTimeoutSpinner.getValueFactory().setValue(config.getQueryTimeout());
        maxResultRowsSpinner.getValueFactory().setValue(config.getMaxResultRows());
        autoResetDatabaseCheckBox.setSelected(config.isAutoResetDatabase());
        enableQueryLoggingCheckBox.setSelected(config.isEnableQueryLogging());

        // Developer Settings
        debugModeCheckBox.setSelected(config.isDebugMode());
        showExecutionTimeCheckBox.setSelected(config.isShowExecutionTime());
        logQueriesCheckBox.setSelected(config.isLogQueries());
        enableMetricsCheckBox.setSelected(config.isEnableMetrics());
        verboseLoggingCheckBox.setSelected(config.isVerboseLogging());
    }

    private void setupEventHandlers() {
        // Handle OK button
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setOnAction(e -> {
            if (validateAndSaveSettings()) {
                log.info("Settings saved successfully");
            }
        });

        // Handle Apply button
        Button applyButton = (Button) getDialogPane().lookupButton(ButtonType.APPLY);
        applyButton.setOnAction(e -> {
            validateAndSaveSettings();
            e.consume(); // Prevent dialog from closing
        });

        // Handle Cancel button (default behavior - just close)

        // Add validation for numeric spinners
        fontSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal < 8) fontSizeSpinner.getValueFactory().setValue(8);
            if (newVal > 72) fontSizeSpinner.getValueFactory().setValue(72);
        });

        queryTimeoutSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal < 5) queryTimeoutSpinner.getValueFactory().setValue(5);
            if (newVal > 300) queryTimeoutSpinner.getValueFactory().setValue(300);
        });
    }

    private boolean validateAndSaveSettings() {
        try {
            // Save UI Preferences
            config.setTheme(themeComboBox.getValue());
            config.setFontSize(fontSizeSpinner.getValue());
            config.setShowLineNumbers(showLineNumbersCheckBox.isSelected());
            config.setWordWrap(wordWrapCheckBox.isSelected());
            config.setAutoSave(autoSaveCheckBox.isSelected());
            config.setAutoIndent(autoIndentCheckBox.isSelected());

            // Save Learning Preferences
            config.setAutoAdvanceTopics(autoAdvanceTopicsCheckBox.isSelected());
            config.setShowHints(showHintsCheckBox.isSelected());
            config.setTrackProgress(trackProgressCheckBox.isSelected());
            config.setPlaySound(playSoundCheckBox.isSelected());
            config.setLearningMode(learningModeComboBox.getValue());

            // Save Database Settings
            config.setQueryTimeout(queryTimeoutSpinner.getValue());
            config.setMaxResultRows(maxResultRowsSpinner.getValue());
            config.setAutoResetDatabase(autoResetDatabaseCheckBox.isSelected());
            config.setEnableQueryLogging(enableQueryLoggingCheckBox.isSelected());

            // Save Developer Settings
            config.setDebugMode(debugModeCheckBox.isSelected());
            config.setShowExecutionTime(showExecutionTimeCheckBox.isSelected());
            config.setLogQueries(logQueriesCheckBox.isSelected());
            config.setEnableMetrics(enableMetricsCheckBox.isSelected());
            config.setVerboseLogging(verboseLoggingCheckBox.isSelected());

            // Save configuration
            configManager.saveConfiguration();

            return true;

        } catch (Exception e) {
            log.error("Failed to save settings", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Settings Error");
            alert.setHeaderText("Failed to save settings");
            alert.setContentText("An error occurred while saving settings: " + e.getMessage());
            alert.showAndWait();

            return false;
        }
    }
}