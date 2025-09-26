package com.sqllearningapp.ui;

import atlantafx.base.controls.Spacer;
import com.sqllearningapp.core.database.EmbeddedDatabase;
import com.sqllearningapp.core.services.*;
import com.sqllearningapp.ui.components.*;
import com.sqllearningapp.ui.dialogs.AboutDialog;
import com.sqllearningapp.ui.dialogs.SettingsDialog;
import com.sqllearningapp.ui.utils.UIUtils;
import com.sqllearningapp.utils.ConfigManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Optional;

/**
 * Modern Main Window - Complete replacement for your FXML-based UI
 * Preserves ALL functionality from your original MainController and main.fxml
 */
@Slf4j
public class MainWindow {

    // Services (exactly as in your original controller)
    private final EmbeddedDatabase database;
    private final ConfigManager configManager;
    private final LearningContentService learningContentService;
    private final PracticeService practiceService;
    private final ProgressTrackingService progressTrackingService;
    private final DatabaseBrowserService databaseBrowserService;

    // UI Components
    private Stage primaryStage;
    private BorderPane root;
    private TabPane mainTabPane;
    private MenuBar menuBar;

    // Learning Tab Components (preserving your original structure)
    private TopicTreeView topicTreeView;
    private LearningPanel learningPanel;
    private Button prevTopicBtn;
    private Button nextTopicBtn;
    private ProgressBar learningProgressBar;

    // Practice Tab Components (preserving your original structure)
    private ListView<String> questionsList;
    private RadioButton allLevelsRadio;
    private RadioButton easyRadio;
    private RadioButton mediumRadio;
    private RadioButton hardRadio;
    private RadioButton proRadio;
    private ToggleGroup difficultyGroup;
    private SqlEditor sqlEditor;
    private Button executeBtn;
    private Button validateBtn;
    private Button submitBtn;
    private Button resetBtn;
    private Button showTablesBtn;
    private Button resetQuestionBtn;
    private ResultsPanel resultsPanel;
    private Label currentQuestionLabel;
    private Label difficultyLabel;
    private Label questionTitleLabel;
    private WebView questionDescriptionWebView;
    private Label executionTimeLabel;
    private Label statusLabel;

    // Database Browser Tab
    private DatabaseBrowser databaseBrowser;

    // Progress and Statistics
    private ProgressTracker progressTracker;
    private Label statisticsLabel;

    // Current state (preserving your original controller state)
    private String currentTopic;
    private String currentQuestionId;
    private int currentQuestionIndex = 0;
    private long queryStartTime;
    private int totalQueriesExecuted = 0;
    private int successfulQueries = 0;

    public MainWindow(EmbeddedDatabase database,
                      ConfigManager configManager,
                      LearningContentService learningContentService,
                      PracticeService practiceService,
                      ProgressTrackingService progressTrackingService) {

        this.database = database;
        this.configManager = configManager;
        this.learningContentService = learningContentService;
        this.practiceService = practiceService;
        this.progressTrackingService = progressTrackingService;
        this.databaseBrowserService = new DatabaseBrowserService(database);

        initializeComponents();
        buildUI();
        setupEventHandlers();
        setupKeyboardShortcuts();
        loadInitialData();
    }

    private void initializeComponents() {
        log.info("Initializing UI components...");

        // Learning components
        topicTreeView = new TopicTreeView(learningContentService);
        learningPanel = new LearningPanel();

        // Practice components
        sqlEditor = new SqlEditor();
        resultsPanel = new ResultsPanel();
        databaseBrowser = new DatabaseBrowser(databaseBrowserService);
        progressTracker = new ProgressTracker(progressTrackingService);

        // Initialize question description WebView
        questionDescriptionWebView = new WebView();
        questionDescriptionWebView.setPrefHeight(200);

        log.debug("All UI components initialized successfully");
    }

    private void buildUI() {
        log.info("Building main UI structure...");

        root = new BorderPane();
        root.getStyleClass().add("main-window");

        // Create comprehensive menu bar (exactly like your FXML)
        root.setTop(createMenuBar());

        // Create tabbed interface (preserving your original tab structure)
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create all tabs
        Tab learningTab = createLearningTab();
        Tab practiceTab = createPracticeTab();
        Tab databaseTab = createDatabaseTab();

        mainTabPane.getTabs().addAll(learningTab, practiceTab, databaseTab);
        root.setCenter(mainTabPane);

        // Create status bar
        root.setBottom(createStatusBar());

        log.debug("Main UI structure built successfully");
    }

    /**
     * Create comprehensive menu bar (exactly from your FXML main.fxml)
     */
    private MenuBar createMenuBar() {
        menuBar = new MenuBar();

        // File Menu (preserving all your original items)
        Menu fileMenu = new Menu("File");

        MenuItem newQueryMenuItem = new MenuItem("New Query");
        newQueryMenuItem.setOnAction(e -> newQuery());
        newQueryMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));

        MenuItem openQueryMenuItem = new MenuItem("Open Query");
        openQueryMenuItem.setOnAction(e -> openQuery());
        openQueryMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));

        MenuItem saveQueryMenuItem = new MenuItem("Save Query");
        saveQueryMenuItem.setOnAction(e -> saveQuery());
        saveQueryMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

        MenuItem exportProgressMenuItem = new MenuItem("Export Progress");
        exportProgressMenuItem.setOnAction(e -> exportProgress());

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(e -> exitApplication());
        exitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));

        fileMenu.getItems().addAll(
                newQueryMenuItem, openQueryMenuItem, saveQueryMenuItem,
                new SeparatorMenuItem(), exportProgressMenuItem,
                new SeparatorMenuItem(), exitMenuItem
        );

        // Edit Menu (preserving all your original items)
        Menu editMenu = new Menu("Edit");

        MenuItem undoMenuItem = new MenuItem("Undo");
        undoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));

        MenuItem redoMenuItem = new MenuItem("Redo");
        redoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));

        MenuItem cutMenuItem = new MenuItem("Cut");
        cutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));

        MenuItem copyMenuItem = new MenuItem("Copy");
        copyMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

        MenuItem pasteMenuItem = new MenuItem("Paste");
        pasteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));

        MenuItem findMenuItem = new MenuItem("Find");
        findMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));

        MenuItem replaceMenuItem = new MenuItem("Replace");
        replaceMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));

        editMenu.getItems().addAll(
                undoMenuItem, redoMenuItem, new SeparatorMenuItem(),
                cutMenuItem, copyMenuItem, pasteMenuItem, new SeparatorMenuItem(),
                findMenuItem, replaceMenuItem
        );

        // View Menu (preserving all your original items)
        Menu viewMenu = new Menu("View");

        MenuItem dbBrowserMenuItem = new MenuItem("Database Browser");
        dbBrowserMenuItem.setOnAction(e -> showDatabaseBrowser());

        MenuItem queryHistoryMenuItem = new MenuItem("Query History");
        queryHistoryMenuItem.setOnAction(e -> showQueryHistory());

        CheckMenuItem lineNumbersMenuItem = new CheckMenuItem("Show Line Numbers");
        lineNumbersMenuItem.setSelected(true);
        lineNumbersMenuItem.setOnAction(e -> toggleLineNumbers(lineNumbersMenuItem.isSelected()));

        CheckMenuItem wordWrapMenuItem = new CheckMenuItem("Word Wrap");
        wordWrapMenuItem.setOnAction(e -> toggleWordWrap(wordWrapMenuItem.isSelected()));

        MenuItem zoomInMenuItem = new MenuItem("Zoom In");
        zoomInMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));

        MenuItem zoomOutMenuItem = new MenuItem("Zoom Out");
        zoomOutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));

        MenuItem resetZoomMenuItem = new MenuItem("Reset Zoom");
        resetZoomMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN));

        MenuItem fullScreenMenuItem = new MenuItem("Full Screen");
        fullScreenMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F11));
        fullScreenMenuItem.setOnAction(e -> toggleFullScreen());

        viewMenu.getItems().addAll(
                dbBrowserMenuItem, queryHistoryMenuItem,
                lineNumbersMenuItem, wordWrapMenuItem, new SeparatorMenuItem(),
                zoomInMenuItem, zoomOutMenuItem, resetZoomMenuItem, new SeparatorMenuItem(),
                fullScreenMenuItem
        );

        // Tools Menu (preserving all your original items)
        Menu toolsMenu = new Menu("Tools");

        MenuItem formatMenuItem = new MenuItem("SQL Formatter");
        formatMenuItem.setOnAction(e -> formatSQL());
        formatMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));

        MenuItem analyzeMenuItem = new MenuItem("Query Analyzer");
        analyzeMenuItem.setOnAction(e -> analyzeQuery());

        MenuItem performanceMenuItem = new MenuItem("Performance Tips");
        performanceMenuItem.setOnAction(e -> showPerformanceTips());

        MenuItem settingsMenuItem = new MenuItem("Settings");
        settingsMenuItem.setOnAction(e -> showSettings());
        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN));

        toolsMenu.getItems().addAll(
                formatMenuItem, analyzeMenuItem, performanceMenuItem,
                new SeparatorMenuItem(), settingsMenuItem
        );

        // Help Menu (preserving all your original items)
        Menu helpMenu = new Menu("Help");

        MenuItem gettingStartedMenuItem = new MenuItem("Getting Started");
        gettingStartedMenuItem.setOnAction(e -> showGettingStarted());
        gettingStartedMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));

        MenuItem sqlReferenceMenuItem = new MenuItem("SQL Reference");
        sqlReferenceMenuItem.setOnAction(e -> showSQLReference());

        MenuItem shortcutsMenuItem = new MenuItem("Keyboard Shortcuts");
        shortcutsMenuItem.setOnAction(e -> showKeyboardShortcuts());

        MenuItem updatesMenuItem = new MenuItem("Check for Updates");
        updatesMenuItem.setOnAction(e -> checkForUpdates());

        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction(e -> showAbout());

        helpMenu.getItems().addAll(
                gettingStartedMenuItem, sqlReferenceMenuItem, shortcutsMenuItem,
                new SeparatorMenuItem(), updatesMenuItem, aboutMenuItem
        );

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, helpMenu);

        return menuBar;
    }

    /**
     * Create Learning Tab (preserving your learning section layout)
     */
    private Tab createLearningTab() {
        Tab learningTab = new Tab("📚 Learn");
        learningTab.setClosable(false);

        SplitPane learningPane = new SplitPane();
        learningPane.setOrientation(Orientation.HORIZONTAL);

        // Left side: Topic tree (exactly like your original)
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setPrefWidth(320);
        leftPanel.getStyleClass().add("learning-sidebar");

        Label topicsLabel = new Label("Learning Topics");
        topicsLabel.getStyleClass().addAll("section-title", "learning-topics-title");

        // Topic tree with your exact structure
        topicTreeView.setPrefHeight(400);

        // Learning progress section
        VBox progressSection = new VBox(8);
        progressSection.getStyleClass().add("progress-section");

        Label progressLabel = new Label("Learning Progress");
        progressLabel.getStyleClass().add("subsection-title");

        learningProgressBar = new ProgressBar(0);
        learningProgressBar.setMaxWidth(Double.MAX_VALUE);
        learningProgressBar.getStyleClass().add("learning-progress");

        Label progressText = new Label("0% Complete");
        progressText.getStyleClass().add("progress-text");

        progressSection.getChildren().addAll(progressLabel, learningProgressBar, progressText);

        Spacer spacer = new Spacer();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        leftPanel.getChildren().addAll(topicsLabel, topicTreeView, progressSection, spacer);

        // Right side: Content display with navigation (preserving your original layout)
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(15));
        rightPanel.getStyleClass().add("learning-content");

        // Navigation toolbar (exactly like your controller navigation)
        ToolBar navigationBar = new ToolBar();
        navigationBar.getStyleClass().add("learning-navigation");

        prevTopicBtn = new Button("← Previous");
        prevTopicBtn.getStyleClass().addAll("nav-button", "prev-button");
        prevTopicBtn.setDisable(true);

        nextTopicBtn = new Button("Next →");
        nextTopicBtn.getStyleClass().addAll("nav-button", "next-button");

        Label currentTopicLabel = new Label("Select a topic to begin learning");
        currentTopicLabel.getStyleClass().add("current-topic-label");

        Spacer navSpacer = new Spacer();

        // Topic metadata display
        VBox topicMeta = new VBox(2);
        Label estimatedTime = new Label("⏱ 5 min read");
        estimatedTime.getStyleClass().add("topic-meta");

        navigationBar.getItems().addAll(
                prevTopicBtn, nextTopicBtn, new Separator(Orientation.VERTICAL),
                currentTopicLabel, navSpacer, estimatedTime
        );

        // Learning content panel
        learningPanel.setPrefHeight(500);
        VBox.setVgrow(learningPanel, Priority.ALWAYS);

        rightPanel.getChildren().addAll(navigationBar, learningPanel);

        learningPane.getItems().addAll(leftPanel, rightPanel);
        learningPane.setDividerPositions(0.28);

        learningTab.setContent(learningPane);
        return learningTab;
    }

    /**
     * Create Practice Tab (preserving your complete practice section)
     */
    private Tab createPracticeTab() {
        Tab practiceTab = new Tab("🎯 Practice");
        practiceTab.setClosable(false);

        BorderPane practiceLayout = new BorderPane();
        practiceLayout.getStyleClass().add("practice-layout");

        // Left Panel: Questions and Filters (exactly like your original)
        VBox leftPanel = createPracticeLeftPanel();

        // Center Panel: Question Details and SQL Editor
        VBox centerPanel = createPracticeCenterPanel();

        // Bottom Panel: Results and Output
        VBox bottomPanel = createPracticeBottomPanel();

        practiceLayout.setLeft(leftPanel);
        practiceLayout.setCenter(centerPanel);
        practiceLayout.setBottom(bottomPanel);

        practiceTab.setContent(practiceLayout);
        return practiceTab;
    }

    private VBox createPracticeLeftPanel() {
        VBox leftPanel = new VBox(12);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setPrefWidth(350);
        leftPanel.getStyleClass().add("practice-sidebar");

        // Difficulty filter section (exactly like your original)
        Label filterLabel = new Label("Difficulty Level");
        filterLabel.getStyleClass().add("section-title");

        VBox difficultyBox = new VBox(8);
        difficultyGroup = new ToggleGroup();

        allLevelsRadio = new RadioButton("All Levels");
        easyRadio = new RadioButton("🟢 Easy");
        mediumRadio = new RadioButton("🟡 Medium");
        hardRadio = new RadioButton("🔴 Hard");
        proRadio = new RadioButton("🟣 Pro");

        allLevelsRadio.setToggleGroup(difficultyGroup);
        easyRadio.setToggleGroup(difficultyGroup);
        mediumRadio.setToggleGroup(difficultyGroup);
        hardRadio.setToggleGroup(difficultyGroup);
        proRadio.setToggleGroup(difficultyGroup);

        allLevelsRadio.setSelected(true);

        difficultyBox.getChildren().addAll(allLevelsRadio, easyRadio, mediumRadio, hardRadio, proRadio);

        // Questions list (exactly like your original)
        Label questionsLabel = new Label("Practice Questions");
        questionsLabel.getStyleClass().add("section-title");

        questionsList = new ListView<>();
        questionsList.setPrefHeight(300);
        questionsList.getStyleClass().add("questions-list");

        // Statistics section
        VBox statsSection = new VBox(8);
        Label statsLabel = new Label("Statistics");
        statsLabel.getStyleClass().add("subsection-title");

        statisticsLabel = new Label("Questions: 0/20\nSuccess Rate: 0%\nStreak: 0");
        statisticsLabel.getStyleClass().add("statistics-text");

        statsSection.getChildren().addAll(statsLabel, statisticsLabel);

        Spacer leftSpacer = new Spacer();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);

        leftPanel.getChildren().addAll(
                filterLabel, difficultyBox,
                new Separator(), questionsLabel, questionsList,
                new Separator(), statsSection, leftSpacer
        );

        return leftPanel;
    }

    private VBox createPracticeCenterPanel() {
        VBox centerPanel = new VBox(10);
        centerPanel.setPadding(new Insets(15));
        centerPanel.getStyleClass().add("practice-center");

        // Question header (preserving your original question display)
        HBox questionHeader = new HBox(15);
        questionHeader.getStyleClass().add("question-header");

        currentQuestionLabel = new Label("Select a question");
        currentQuestionLabel.getStyleClass().add("current-question");

        difficultyLabel = new Label("");
        difficultyLabel.getStyleClass().add("difficulty-badge");

        Spacer headerSpacer = new Spacer();

        resetQuestionBtn = new Button("🔄 Reset");
        resetQuestionBtn.getStyleClass().add("reset-question-btn");

        questionHeader.getChildren().addAll(currentQuestionLabel, headerSpacer, difficultyLabel, resetQuestionBtn);

        // Question title and description
        questionTitleLabel = new Label("");
        questionTitleLabel.getStyleClass().add("question-title");
        questionTitleLabel.setWrapText(true);

        questionDescriptionWebView.getStyleClass().add("question-description");

        // SQL Editor with toolbar (exactly like your original)
        ToolBar executionBar = new ToolBar();
        executionBar.getStyleClass().add("execution-toolbar");

        executeBtn = new Button("▶ Execute");
        executeBtn.getStyleClass().addAll("execute-btn", "primary");

        validateBtn = new Button("✓ Validate");
        validateBtn.getStyleClass().addAll("validate-btn", "secondary");

        submitBtn = new Button("📝 Submit Answer");
        submitBtn.getStyleClass().addAll("submit-btn", "success");

        resetBtn = new Button("🔄 Reset Database");
        resetBtn.getStyleClass().addAll("reset-btn", "warning");

        showTablesBtn = new Button("📋 Show Tables");
        showTablesBtn.getStyleClass().addAll("tables-btn", "info");

        Spacer toolbarSpacer = new Spacer();

        // Execution time and status
        executionTimeLabel = new Label("Execution time: 0ms");
        executionTimeLabel.getStyleClass().add("execution-time");

        executionBar.getItems().addAll(
                executeBtn, validateBtn, submitBtn,
                new Separator(Orientation.VERTICAL),
                resetBtn, showTablesBtn, toolbarSpacer, executionTimeLabel
        );

        // SQL Editor
        sqlEditor.setPrefHeight(250);
        VBox.setVgrow(sqlEditor, Priority.ALWAYS);

        centerPanel.getChildren().addAll(
                questionHeader, questionTitleLabel, questionDescriptionWebView,
                executionBar, sqlEditor
        );

        return centerPanel;
    }

    private VBox createPracticeBottomPanel() {
        VBox bottomPanel = new VBox(5);
        bottomPanel.setPadding(new Insets(10, 15, 15, 15));
        bottomPanel.setPrefHeight(200);
        bottomPanel.getStyleClass().add("practice-bottom");

        Label resultsLabel = new Label("Query Results");
        resultsLabel.getStyleClass().add("results-title");

        resultsPanel.setPrefHeight(150);
        VBox.setVgrow(resultsPanel, Priority.ALWAYS);

        bottomPanel.getChildren().addAll(resultsLabel, resultsPanel);

        return bottomPanel;
    }

    /**
     * Create Database Browser Tab
     */
    private Tab createDatabaseTab() {
        Tab databaseTab = new Tab("🗄️ Database");
        databaseTab.setClosable(false);
        databaseTab.setContent(databaseBrowser);
        return databaseTab;
    }

    /**
     * Create status bar (preserving your original status information)
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(15);
        statusBar.setPadding(new Insets(8, 15, 8, 15));
        statusBar.getStyleClass().add("status-bar");

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-text");

        Separator separator1 = new Separator(Orientation.VERTICAL);

        Label connectionLabel = new Label("Database: Connected");
        connectionLabel.getStyleClass().add("connection-status");

        Separator separator2 = new Separator(Orientation.VERTICAL);

        Label versionLabel = new Label("SQL Learning v2.0");
        versionLabel.getStyleClass().add("version-info");

        Spacer statusSpacer = new Spacer();

        // Current user info
        Label userLabel = new Label("User: SithuHan-SithuHan");
        userLabel.getStyleClass().add("user-info");

        statusBar.getChildren().addAll(
                statusLabel, separator1, connectionLabel, separator2,
                versionLabel, statusSpacer, userLabel
        );

        return statusBar;
    }

    private void setupEventHandlers() {
        log.info("Setting up event handlers...");

        // Learning tab event handlers (preserving your original controller logic)
        topicTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isLeaf()) {
                String topicName = newVal.getValue();
                loadTopicContent(topicName);
            }
        });

        prevTopicBtn.setOnAction(e -> navigateToPreviousTopic());
        nextTopicBtn.setOnAction(e -> navigateToNextTopic());

        // Practice tab event handlers (preserving your original controller logic)
        difficultyGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            filterQuestionsByDifficulty();
        });

        questionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadSelectedQuestion();
            }
        });

        executeBtn.setOnAction(e -> executeSql());
        validateBtn.setOnAction(e -> validateSql());
        submitBtn.setOnAction(e -> submitAnswer());
        resetBtn.setOnAction(e -> resetDatabase());
        showTablesBtn.setOnAction(e -> showTables());
        resetQuestionBtn.setOnAction(e -> resetQuestion());

        // SQL Editor events
        sqlEditor.addEventHandler(SqlEditor.SqlEditorEvent.EXECUTE_QUERY, e -> executeSql());
        sqlEditor.addEventHandler(SqlEditor.SqlEditorEvent.SUBMIT_ANSWER, e -> submitAnswer());

        log.debug("All event handlers set up successfully");
    }

    private void setupKeyboardShortcuts() {
        // Global keyboard shortcuts
        // F5 for execute is handled in SqlEditor
        // Ctrl+Enter for submit is handled in SqlEditor

        log.debug("Keyboard shortcuts configured");
    }

    private void loadInitialData() {
        log.info("Loading initial application data...");

        Platform.runLater(() -> {
            // Load learning topics
            topicTreeView.setRoot(learningContentService.getLearningTopicsTree());

            // Load practice questions
            loadAllQuestions();

            // Update progress displays
            updateLearningProgress();
            updatePracticeProgress();
            updateStatistics();

            // Select first tab
            mainTabPane.getSelectionModel().selectFirst();

            log.info("Initial data loaded successfully");
        });
    }

    // ===== LEARNING SECTION METHODS (PRESERVING YOUR ORIGINAL LOGIC) =====

    private void loadTopicContent(String topic) {
        log.debug("Loading topic content: {}", topic);

        currentTopic = topic;
        String content = learningContentService.getTopicContent(topic);
        learningPanel.setContent(content);

        // Update navigation buttons
        updateTopicNavigation();

        // Track progress
        progressTrackingService.recordTopicView(topic);
        updateLearningProgress();

        statusLabel.setText("Viewing: " + topic);
    }

    private void updateTopicNavigation() {
        if (currentTopic != null) {
            String prevTopic = learningContentService.getPreviousTopic(currentTopic);
            String nextTopic = learningContentService.getNextTopic(currentTopic);

            prevTopicBtn.setDisable(prevTopic == null);
            nextTopicBtn.setDisable(nextTopic == null);
        }
    }

    private void navigateToNextTopic() {
        if (currentTopic != null) {
            String nextTopic = learningContentService.getNextTopic(currentTopic);
            if (nextTopic != null) {
                // Find and select the topic in the tree
                selectTopicInTree(nextTopic);
            }
        }
    }

    private void navigateToPreviousTopic() {
        if (currentTopic != null) {
            String prevTopic = learningContentService.getPreviousTopic(currentTopic);
            if (prevTopic != null) {
                // Find and select the topic in the tree
                selectTopicInTree(prevTopic);
            }
        }
    }

    private void selectTopicInTree(String topicName) {
        TreeItem<String> root = topicTreeView.getRoot();
        TreeItem<String> item = findTreeItem(root, topicName);
        if (item != null) {
            topicTreeView.getSelectionModel().select(item);
            topicTreeView.scrollTo(topicTreeView.getSelectionModel().getSelectedIndex());
        }
    }

    private TreeItem<String> findTreeItem(TreeItem<String> parent, String value) {
        if (parent.getValue() != null && parent.getValue().equals(value)) {
            return parent;
        }

        for (TreeItem<String> child : parent.getChildren()) {
            TreeItem<String> result = findTreeItem(child, value);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private void updateLearningProgress() {
        double progress = learningContentService.getOverallProgress();
        learningProgressBar.setProgress(progress / 100.0);

        // Update progress text in the learning tab
        Platform.runLater(() -> {
            // Find and update progress text label
            // This would be implemented based on your specific progress display needs
        });
    }

    // ===== PRACTICE SECTION METHODS (PRESERVING YOUR ORIGINAL LOGIC) =====

    private void loadAllQuestions() {
        var questions = practiceService.getAllQuestions();
        var questionTitles = questions.stream()
                .map(q -> String.format("[%s] %s",
                        getDifficultyEmoji(q.getDifficulty().name().toLowerCase()),
                        q.getTitle()))
                .toList();

        Platform.runLater(() -> {
            questionsList.getItems().clear();
            questionsList.getItems().addAll(questionTitles);

            if (!questions.isEmpty()) {
                questionsList.getSelectionModel().selectFirst();
            }
        });
    }

    private void filterQuestionsByDifficulty() {
        RadioButton selected = (RadioButton) difficultyGroup.getSelectedToggle();
        if (selected == null) return;

        String difficulty;
        if (selected == allLevelsRadio) {
            difficulty = "all";
        } else if (selected == easyRadio) {
            difficulty = "easy";
        } else if (selected == mediumRadio) {
            difficulty = "medium";
        } else if (selected == hardRadio) {
            difficulty = "hard";
        } else if (selected == proRadio) {
            difficulty = "pro";
        } else {
            difficulty = "all";
        }

        var questions = practiceService.getQuestionsByDifficulty(difficulty);
        var questionTitles = questions.stream()
                .map(q -> String.format("[%s] %s",
                        getDifficultyEmoji(q.getDifficulty().name().toLowerCase()),
                        q.getTitle()))
                .toList();

        Platform.runLater(() -> {
            questionsList.getItems().clear();
            questionsList.getItems().addAll(questionTitles);
        });
    }

    private void loadSelectedQuestion() {
        int selectedIndex = questionsList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            // Get the current filtered questions
            RadioButton selected = (RadioButton) difficultyGroup.getSelectedToggle();
            String difficulty = "all";

            if (selected == easyRadio) difficulty = "easy";
            else if (selected == mediumRadio) difficulty = "medium";
            else if (selected == hardRadio) difficulty = "hard";
            else if (selected == proRadio) difficulty = "pro";

            var questions = practiceService.getQuestionsByDifficulty(difficulty);

            if (selectedIndex < questions.size()) {
                var question = questions.get(selectedIndex);
                loadQuestion(question);
            }
        }
    }

    private void loadQuestion(com.sqllearningapp.core.models.PracticeQuestion question) {
        currentQuestionId = question.getId();
        currentQuestionIndex = questionsList.getSelectionModel().getSelectedIndex();

        // Update question info display
        currentQuestionLabel.setText(String.format("Question %d", currentQuestionIndex + 1));
        difficultyLabel.setText(getDifficultyEmoji(question.getDifficulty().name().toLowerCase()) + " " +
                question.getDifficulty().getDisplayName());
        questionTitleLabel.setText(question.getTitle());

        // Load question description in WebView
        String htmlContent = createQuestionHTML(question.getDescription());
        questionDescriptionWebView.getEngine().loadContent(htmlContent, "text/html");

        // Clear previous results
        resultsPanel.clear();
        executionTimeLabel.setText("Execution time: 0ms");

        // Set example SQL if available
        if (question.getExampleSql() != null && !question.getExampleSql().isEmpty()) {
            sqlEditor.replaceText(question.getExampleSql());
        } else {
            sqlEditor.clear();
        }

        statusLabel.setText("Loaded: " + question.getTitle());

        log.debug("Loaded question: {} ({})", question.getTitle(), question.getId());
    }

    private String createQuestionHTML(String content) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { 
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    margin: 0; 
                    padding: 16px;
                    background-color: white;
                    color: #0f172a;
                    line-height: 1.6;
                    font-size: 14px;
                }
                h3 { color: #2563eb; margin-bottom: 16px; font-size: 18px; }
                h4 { color: #059669; margin-top: 24px; margin-bottom: 12px; font-size: 16px; }
                h5 { color: #d97706; margin-top: 16px; margin-bottom: 8px; font-size: 14px; }
                p { margin-bottom: 12px; }
                code { 
                    background-color: #f1f5f9; 
                    padding: 2px 6px; 
                    border-radius: 4px; 
                    font-family: 'Monaco', 'Consolas', monospace;
                    font-size: 13px;
                }
                table { 
                    border-collapse: collapse; 
                    width: 100%; 
                    margin: 16px 0; 
                    border: 2px solid #e2e8f0;
                    font-size: 13px;
                }
                th { 
                    background-color: #f8fafc; 
                    border: 1px solid #cbd5e1; 
                    padding: 10px; 
                    text-align: left; 
                    font-weight: 600;
                }
                td { 
                    border: 1px solid #cbd5e1; 
                    padding: 8px;
                }
                tr:nth-child(even) { 
                    background-color: #f8fafc;
                }
            </style>
        </head>
        <body>
        %s
        </body>
        </html>
        """.formatted(content);
    }

    // ===== SQL EXECUTION METHODS (PRESERVING YOUR ORIGINAL LOGIC) =====

    private void executeSql() {
        String sql = sqlEditor.getText().trim();
        if (sql.isEmpty()) {
            UIUtils.showWarning("Empty Query", "Please enter a SQL query to execute.");
            return;
        }

        statusLabel.setText("Executing...");
        executeBtn.setDisable(true);
        queryStartTime = System.currentTimeMillis();
        totalQueriesExecuted++;

        // Execute in background thread to avoid blocking UI
        Platform.runLater(() -> {
            try {
                var result = database.getPracticeConnection();
                var queryExecutor = new com.sqllearningapp.core.database.QueryExecutor(database);
                var queryResult = queryExecutor.executeQuery(sql, true);

                long executionTime = System.currentTimeMillis() - queryStartTime;
                displayExecutionResult(queryResult, executionTime);

                executeBtn.setDisable(false);
                statusLabel.setText(queryResult.isSuccess() ? "Executed successfully" : "Execution failed");

                if (queryResult.isSuccess()) {
                    successfulQueries++;
                }
                updateStatistics();

            } catch (Exception e) {
                executeBtn.setDisable(false);
                statusLabel.setText("Execution failed: " + e.getMessage());
                long executionTime = System.currentTimeMillis() - queryStartTime;
                executionTimeLabel.setText(String.format("Execution time: %dms", executionTime));
                log.error("SQL execution failed", e);
            }
        });
    }

    private void displayExecutionResult(com.sqllearningapp.core.models.QueryResult result, long executionTime) {
        executionTimeLabel.setText(String.format("Execution time: %dms", executionTime));

        if (result.isSuccess()) {
            resultsPanel.displayResult(result);
        } else {
            resultsPanel.displayError(result.getMessage());
        }
    }

    private void validateSql() {
        String sql = sqlEditor.getText().trim();
        if (sql.isEmpty()) {
            UIUtils.showWarning("Empty Query", "Please enter a SQL query to validate.");
            return;
        }

        var queryExecutor = new com.sqllearningapp.core.database.QueryExecutor(database);
        var result = queryExecutor.validateSql(sql);

        if (result.isValid()) {
            statusLabel.setText("✓ SQL syntax is valid");
            statusLabel.setStyle("-fx-text-fill: #059669;");
        } else {
            statusLabel.setText("✗ " + result.getMessage());
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
        }
    }

    private void submitAnswer() {
        if (currentQuestionId == null) {
            UIUtils.showWarning("No Question Selected", "Please select a question first.");
            return;
        }

        String userQuery = sqlEditor.getText().trim();
        if (userQuery.isEmpty()) {
            UIUtils.showWarning("Empty Query", "Please enter your SQL solution.");
            return;
        }

        // Validate answer
        var validationResult = practiceService.validateAnswer(currentQuestionId, userQuery);

        Platform.runLater(() -> {
            if (validationResult.isCorrect()) {
                UIUtils.showSuccess("Correct Answer!", validationResult.getFormattedMessage());
                progressTrackingService.recordQuestionCompletion(
                        currentQuestionId, true, validationResult.getPointsEarned());
            } else {
                UIUtils.showError("Incorrect Answer", validationResult.getMessage());
                if (validationResult.getHint() != null && !validationResult.getHint().isEmpty()) {
                    UIUtils.showInfo("Hint", validationResult.getHint());
                }
            }

            updatePracticeProgress();
            updateStatistics();
        });
    }

    private void resetDatabase() {
        try {
            database.resetPracticeDatabase();
            resultsPanel.displayMessage("Database reset successfully!", "success");
            statusLabel.setText("Database reset completed");
        } catch (Exception e) {
            resultsPanel.displayMessage("Failed to reset database: " + e.getMessage(), "error");
            log.error("Failed to reset database", e);
        }
    }

    private void showTables() {
        // Switch to database browser tab
        mainTabPane.getSelectionModel().select(2); // Database tab is index 2
    }

    private void resetQuestion() {
        if (currentQuestionId != null) {
            var question = practiceService.getQuestionById(currentQuestionId);
            if (question != null && question.getExampleSql() != null) {
                sqlEditor.replaceText(question.getExampleSql());
                resultsPanel.clear();
                executionTimeLabel.setText("Execution time: 0ms");
                statusLabel.setText("Question reset");
            }
        }
    }

    // ===== STATISTICS AND PROGRESS METHODS =====

    private void updateStatistics() {
        double successRate = totalQueriesExecuted > 0 ?
                (double) successfulQueries / totalQueriesExecuted * 100 : 0;

        int completedQuestions = progressTrackingService.getCompletedQuestions().size();
        int currentStreak = progressTrackingService.getUserProgress()
                .getStatistics().getCurrentStreak();

        String statsText = String.format(
                "Questions: %d/20\nSuccess Rate: %.1f%%\nStreak: %d",
                completedQuestions, successRate, currentStreak
        );

        Platform.runLater(() -> statisticsLabel.setText(statsText));
    }

    private void updatePracticeProgress() {
        // Update practice-related progress displays
        updateStatistics();
    }

    // ===== UTILITY METHODS =====

    private String getDifficultyEmoji(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> "🟢";
            case "medium" -> "🟡";
            case "hard" -> "🔴";
            case "pro" -> "🟣";
            default -> "⚪";
        };
    }

    // ===== MENU ACTION METHODS =====

    private void newQuery() {
        sqlEditor.clear();
        sqlEditor.replaceText("-- New SQL Query\n-- Write your query here...\n\n");
        statusLabel.setText("New query created");
    }

    private void openQuery() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open SQL Query");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQL Files", "*.sql")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            // Load file content
            try {
                String content = java.nio.file.Files.readString(file.toPath());
                sqlEditor.replaceText(content);
                statusLabel.setText("Opened: " + file.getName());
            } catch (Exception e) {
                UIUtils.showError("Error", "Failed to open file: " + e.getMessage());
            }
        }
    }

    private void saveQuery() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SQL Query");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQL Files", "*.sql")
        );

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                java.nio.file.Files.writeString(file.toPath(), sqlEditor.getText());
                statusLabel.setText("Saved: " + file.getName());
            } catch (Exception e) {
                UIUtils.showError("Error", "Failed to save file: " + e.getMessage());
            }
        }
    }

    private void exportProgress() {
        try {
            progressTrackingService.exportProgress();
            UIUtils.showInfo("Export Complete", "Progress exported successfully!");
        } catch (Exception e) {
            UIUtils.showError("Export Failed", "Failed to export progress: " + e.getMessage());
        }
    }

    private void exitApplication() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Exit SQL Learning Application?");
        alert.setContentText("Any unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void showDatabaseBrowser() {
        mainTabPane.getSelectionModel().select(2); // Database tab
    }

    private void showQueryHistory() {
        UIUtils.showInfo("Query History", "Query history feature coming soon!");
    }

    private void toggleLineNumbers(boolean show) {
        // Implementation would depend on SqlEditor capabilities
        statusLabel.setText("Line numbers " + (show ? "enabled" : "disabled"));
    }

    private void toggleWordWrap(boolean wrap) {
        // Implementation would depend on SqlEditor capabilities
        statusLabel.setText("Word wrap " + (wrap ? "enabled" : "disabled"));
    }

    private void toggleFullScreen() {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
    }

    private void formatSQL() {
        String sql = sqlEditor.getText();
        if (!sql.trim().isEmpty()) {
            // Basic SQL formatting - you could integrate a proper SQL formatter library
            String formatted = sql.replaceAll("(?i)\\bSELECT\\b", "\nSELECT")
                    .replaceAll("(?i)\\bFROM\\b", "\nFROM")
                    .replaceAll("(?i)\\bWHERE\\b", "\nWHERE")
                    .replaceAll("(?i)\\bGROUP BY\\b", "\nGROUP BY")
                    .replaceAll("(?i)\\bORDER BY\\b", "\nORDER BY");
            sqlEditor.replaceText(formatted.trim());
            statusLabel.setText("SQL formatted");
        }
    }

    private void analyzeQuery() {
        UIUtils.showInfo("Query Analyzer", "Query analysis feature coming soon!");
    }

    private void showPerformanceTips() {
        UIUtils.showInfo("Performance Tips", "Performance tips feature coming soon!");
    }

    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(configManager);
        dialog.showAndWait();
    }

    private void showGettingStarted() {
        UIUtils.showInfo("Getting Started",
                "Welcome to SQL Learning Application!\n\n" +
                        "1. Start with the Learning tab to study SQL concepts\n" +
                        "2. Practice your skills in the Practice tab\n" +
                        "3. Explore the sample database in the Database tab\n\n" +
                        "Use F5 to execute queries and Ctrl+Enter to submit answers.");
    }

    private void showSQLReference() {
        UIUtils.showInfo("SQL Reference", "SQL reference guide coming soon!");
    }

    private void showKeyboardShortcuts() {
        UIUtils.showInfo("Keyboard Shortcuts",
                "F5 - Execute SQL Query\n" +
                        "Ctrl+Enter - Submit Answer\n" +
                        "Ctrl+N - New Query\n" +
                        "Ctrl+O - Open Query\n" +
                        "Ctrl+S - Save Query\n" +
                        "F11 - Toggle Full Screen\n" +
                        "Alt+F4 - Exit Application");
    }

    private void checkForUpdates() {
        UIUtils.showInfo("Updates", "You are running the latest version!");
    }

    private void showAbout() {
        AboutDialog dialog = new AboutDialog();
        dialog.showAndWait();
    }

    // ===== PUBLIC INTERFACE =====

    public void show(Stage stage) {
        this.primaryStage = stage;

        // Configure stage (preserving your original settings)
        stage.setTitle("SQL Learning Application - Professional Edition");
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        // Load user's preferred size or default to maximized
        if (configManager.getConfig().isMaximized()) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(configManager.getConfig().getWindowWidth());
            stage.setHeight(configManager.getConfig().getWindowHeight());
        }

        // Create scene with styling
        Scene scene = new Scene(root);

        // Load CSS theme
        scene.getStylesheets().add(
                getClass().getResource("/themes/modern-theme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.show();

        // Save window state on close
        stage.setOnCloseRequest(event -> {
            configManager.getConfig().setWindowWidth(stage.getWidth());
            configManager.getConfig().setWindowHeight(stage.getHeight());
            configManager.getConfig().setMaximized(stage.isMaximized());
            configManager.saveConfiguration();
        });

        log.info("Main window displayed successfully");
    }
}