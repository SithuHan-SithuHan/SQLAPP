package com.sqllearningapp.ui.components;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced Learning Panel - Displays learning content with modern web view
 * Preserves your original content display functionality with improvements
 */
@Slf4j
public class LearningPanel extends VBox {

    private WebView webView;
    private WebEngine webEngine;
    private ProgressBar loadingProgress;
    private Label statusLabel;
    private ToolBar toolbar;

    // Current content state
    private String currentContent;
    private String currentTopic;

    public LearningPanel() {
        setupUI();
        getStyleClass().add("learning-panel");
    }

    private void setupUI() {
        setPadding(new Insets(10));
        setSpacing(8);

        // Create web view for content FIRST
        webView = new WebView();
        webEngine = webView.getEngine();

        setupWebEngine();

        // Now create toolbar (after webEngine is available)
        toolbar = createToolbar();

        // Create status bar
        HBox statusBar = createStatusBar();

        // Set growth priorities
        VBox.setVgrow(webView, Priority.ALWAYS);

        getChildren().addAll(toolbar, webView, statusBar);

        // Load welcome content
        loadWelcomeContent();
    }
    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();
        toolbar.getStyleClass().add("learning-toolbar");

        // Navigation buttons
        Button backBtn = new Button("←");
        backBtn.getStyleClass().addAll("nav-btn", "back-btn");
        backBtn.setTooltip(new Tooltip("Go Back"));
        backBtn.setOnAction(e -> webEngine.getHistory().go(-1));
        backBtn.disableProperty().bind(
                webEngine.getHistory().currentIndexProperty().isEqualTo(0));

        Button forwardBtn = new Button("→");
        forwardBtn.getStyleClass().addAll("nav-btn", "forward-btn");
        forwardBtn.setTooltip(new Tooltip("Go Forward"));
        forwardBtn.setOnAction(e -> webEngine.getHistory().go(1));
        forwardBtn.disableProperty().bind(
                webEngine.getHistory().currentIndexProperty().greaterThanOrEqualTo(
                        webEngine.getHistory().getEntries().size() - 1));

        Button refreshBtn = new Button("⟳");
        refreshBtn.getStyleClass().addAll("nav-btn", "refresh-btn");
        refreshBtn.setTooltip(new Tooltip("Refresh Content"));
        refreshBtn.setOnAction(e -> refreshContent());

        Separator separator1 = new Separator();

        // Font size controls
        Button zoomInBtn = new Button("A+");
        zoomInBtn.getStyleClass().add("zoom-btn");
        zoomInBtn.setTooltip(new Tooltip("Increase Text Size"));
        zoomInBtn.setOnAction(e -> adjustZoom(0.1));

        Button zoomOutBtn = new Button("A-");
        zoomOutBtn.getStyleClass().add("zoom-btn");
        zoomOutBtn.setTooltip(new Tooltip("Decrease Text Size"));
        zoomOutBtn.setOnAction(e -> adjustZoom(-0.1));

        Button zoomResetBtn = new Button("A");
        zoomResetBtn.getStyleClass().add("zoom-btn");
        zoomResetBtn.setTooltip(new Tooltip("Reset Text Size"));
        zoomResetBtn.setOnAction(e -> webView.setZoom(1.0));

        Separator separator2 = new Separator();

        // Print button
        Button printBtn = new Button("🖨");
        printBtn.getStyleClass().add("print-btn");
        printBtn.setTooltip(new Tooltip("Print Content"));
        printBtn.setOnAction(e -> printContent());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Progress indicator
        loadingProgress = new ProgressBar();
        loadingProgress.setPrefWidth(120);
        loadingProgress.setVisible(false);

        toolbar.getItems().addAll(
                backBtn, forwardBtn, refreshBtn, separator1,
                zoomInBtn, zoomOutBtn, zoomResetBtn, separator2,
                printBtn, spacer, loadingProgress
        );

        return toolbar;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("learning-status");
        statusBar.setPadding(new Insets(5, 10, 5, 10));

        statusLabel = new Label("Ready to learn");
        statusLabel.getStyleClass().add("status-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Current user and date info
        Label userInfo = new Label(String.format("User: %s | Date: %s",
                "SithuHan-SithuHan",
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        userInfo.getStyleClass().add("user-info");

        statusBar.getChildren().addAll(statusLabel, spacer, userInfo);

        return statusBar;
    }

    private void setupWebEngine() {
        // Set user agent
        webEngine.setUserAgent("SQLLearning/2.0 (SithuHan-SithuHan)");

        // Handle loading states
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case RUNNING:
                    loadingProgress.setVisible(true);
                    statusLabel.setText("Loading content...");
                    break;
                case SUCCEEDED:
                    loadingProgress.setVisible(false);
                    statusLabel.setText("Content loaded successfully");
                    break;
                case FAILED:
                    loadingProgress.setVisible(false);
                    statusLabel.setText("Failed to load content");
                    log.error("Failed to load web content");
                    break;
                case CANCELLED:
                    loadingProgress.setVisible(false);
                    statusLabel.setText("Loading cancelled");
                    break;
            }
        });

        // Handle progress
        webEngine.getLoadWorker().progressProperty().addListener((obs, oldProgress, newProgress) -> {
            loadingProgress.setProgress(newProgress.doubleValue());
        });

        // Handle JavaScript alerts and errors
        webEngine.setOnAlert(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Content Message");
            alert.setHeaderText(null);
            alert.setContentText(event.getData());
            alert.showAndWait();
        });

        // Disable context menu for cleaner experience
        webView.setContextMenuEnabled(false);

        // Set initial zoom
        webView.setZoom(1.0);
    }

    private void loadWelcomeContent() {
        String welcomeContent = createWelcomeHTML();
        webEngine.loadContent(welcomeContent, "text/html");
        currentTopic = "Welcome";
        statusLabel.setText("Welcome to SQL Learning");
    }

    private String createWelcomeHTML() {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>SQL Learning - Welcome</title>
                <style>
                    %s
                </style>
            </head>
            <body>
                <div class="welcome-container">
                    <div class="header">
                        <h1>🎓 Welcome to SQL Learning Professional Edition</h1>
                        <p class="subtitle">Master SQL with comprehensive lessons and hands-on practice</p>
                    </div>
                    
                    <div class="user-info">
                        <h2>👋 Hello, SithuHan-SithuHan!</h2>
                        <p>Session started: %s</p>
                        <p>Repository: <a href="https://github.com/SithuHan-SithuHan/SQL_Learning_APP" target="_blank">SQL_Learning_APP</a></p>
                    </div>
                    
                    <div class="features">
                        <h2>🌟 What You'll Learn</h2>
                        <div class="feature-grid">
                            <div class="feature-card">
                                <h3>🏗️ DDL - Data Definition</h3>
                                <p>CREATE, ALTER, DROP tables and constraints</p>
                            </div>
                            <div class="feature-card">
                                <h3>🔄 DML - Data Manipulation</h3>
                                <p>SELECT, INSERT, UPDATE, DELETE operations</p>
                            </div>
                            <div class="feature-card">
                                <h3>🔐 DCL - Data Control</h3>
                                <p>GRANT and REVOKE permissions</p>
                            </div>
                            <div class="feature-card">
                                <h3>💾 TCL - Transaction Control</h3>
                                <p>COMMIT, ROLLBACK, SAVEPOINT</p>
                            </div>
                            <div class="feature-card">
                                <h3>📐 Database Normalization</h3>
                                <p>1NF, 2NF, 3NF, and BCNF principles</p>
                            </div>
                            <div class="feature-card">
                                <h3>🎯 Hands-on Practice</h3>
                                <p>20+ questions across all difficulty levels</p>
                            </div>
                        </div>
                    </div>
                    
                    <div class="getting-started">
                        <h2>🚀 Getting Started</h2>
                        <ol>
                            <li>Select a topic from the tree on the left</li>
                            <li>Read through the comprehensive lessons</li>
                            <li>Practice with the interactive SQL editor</li>
                            <li>Track your progress as you learn</li>
                        </ol>
                    </div>
                    
                    <div class="tips">
                        <h2>💡 Learning Tips</h2>
                        <ul>
                            <li>Start with DDL basics if you're new to SQL</li>
                            <li>Practice each concept before moving to the next</li>
                            <li>Use the database browser to understand table structures</li>
                            <li>Don't hesitate to reset the practice database</li>
                        </ul>
                    </div>
                </div>
            </body>
            </html>
            """,
                getModernCSS(),
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"))
        );
    }

    private String getModernCSS() {
        return """
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                line-height: 1.6;
                color: #2d3748;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                margin: 0;
                padding: 20px;
                min-height: 100vh;
            }
            
            .welcome-container {
                max-width: 800px;
                margin: 0 auto;
                background: white;
                border-radius: 12px;
                box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
                overflow: hidden;
            }
            
            .header {
                background: linear-gradient(135deg, #2563eb, #1d4ed8);
                color: white;
                padding: 40px;
                text-align: center;
            }
            
            .header h1 {
                margin: 0 0 10px 0;
                font-size: 2.5em;
                font-weight: 700;
            }
            
            .subtitle {
                font-size: 1.2em;
                opacity: 0.9;
                margin: 0;
            }
            
            .user-info {
                background: #f8fafc;
                padding: 30px 40px;
                border-bottom: 1px solid #e2e8f0;
            }
            
            .user-info h2 {
                margin: 0 0 15px 0;
                color: #1a202c;
                font-size: 1.5em;
            }
            
            .user-info p {
                margin: 5px 0;
                color: #4a5568;
            }
            
            .user-info a {
                color: #2563eb;
                text-decoration: none;
                font-weight: 600;
            }
            
            .user-info a:hover {
                text-decoration: underline;
            }
            
            .features {
                padding: 40px;
            }
            
            .features h2 {
                margin: 0 0 25px 0;
                color: #1a202c;
                font-size: 1.8em;
            }
            
            .feature-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                gap: 20px;
            }
            
            .feature-card {
                background: #f7fafc;
                padding: 20px;
                border-radius: 8px;
                border-left: 4px solid #2563eb;
                transition: transform 0.2s;
            }
            
            .feature-card:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }
            
            .feature-card h3 {
                margin: 0 0 10px 0;
                color: #2d3748;
                font-size: 1.1em;
            }
            
            .feature-card p {
                margin: 0;
                color: #4a5568;
                font-size: 0.95em;
            }
            
            .getting-started, .tips {
                padding: 40px;
                border-top: 1px solid #e2e8f0;
            }
            
            .getting-started h2, .tips h2 {
                margin: 0 0 20px 0;
                color: #1a202c;
                font-size: 1.5em;
            }
            
            .getting-started ol, .tips ul {
                padding-left: 20px;
            }
            
            .getting-started li, .tips li {
                margin-bottom: 10px;
                color: #4a5568;
            }
            
            /* Responsive design */
            @media (max-width: 768px) {
                body {
                    padding: 10px;
                }
                
                .header {
                    padding: 30px 20px;
                }
                
                .header h1 {
                    font-size: 2em;
                }
                
                .feature-grid {
                    grid-template-columns: 1fr;
                }
                
                .features, .getting-started, .tips, .user-info {
                    padding: 30px 20px;
                }
            }
            """;
    }

    // ===== PUBLIC METHODS =====

    /**
     * Set content to display (preserving your original loadTopicContent logic)
     */
    public void setContent(String htmlContent) {
        if (htmlContent != null && !htmlContent.trim().isEmpty()) {
            currentContent = htmlContent;

            // Enhance the HTML with modern styling
            String enhancedContent = enhanceContentHTML(htmlContent);
            webEngine.loadContent(enhancedContent, "text/html");

            log.debug("Loading content: {} characters", htmlContent.length());
        }
    }

    /**
     * Set content with topic information
     */
    public void setContent(String htmlContent, String topicName) {
        currentTopic = topicName;
        statusLabel.setText("Viewing: " + topicName);
        setContent(htmlContent);
    }

    /**
     * Clear content
     */
    public void clearContent() {
        loadWelcomeContent();
    }

    /**
     * Refresh current content
     */
    public void refreshContent() {
        if (currentContent != null) {
            setContent(currentContent);
        } else {
            loadWelcomeContent();
        }
    }

    /**
     * Print content
     */
    public void printContent() {
        webEngine.print(javafx.print.PrinterJob.createPrinterJob());
    }

    /**
     * Adjust zoom level
     */
    public void adjustZoom(double delta) {
        double currentZoom = webView.getZoom();
        double newZoom = Math.max(0.5, Math.min(3.0, currentZoom + delta));
        webView.setZoom(newZoom);
    }

    /**
     * Export content to file
     */
    public void exportContent() {
        if (currentContent == null) return;

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Learning Content");
        fileChooser.setInitialFileName(
                (currentTopic != null ? currentTopic.replaceAll("[^a-zA-Z0-9]", "_") : "content") + ".html");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("HTML Files", "*.html"));

        javafx.stage.Stage stage = (javafx.stage.Stage) getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                java.nio.file.Files.writeString(file.toPath(),
                        enhanceContentHTML(currentContent),
                        java.nio.charset.StandardCharsets.UTF_8);
                statusLabel.setText("Content exported to: " + file.getName());
            } catch (Exception e) {
                statusLabel.setText("Export failed: " + e.getMessage());
                log.error("Failed to export content", e);
            }
        }
    }

    // ===== PRIVATE METHODS =====

    private String enhanceContentHTML(String originalContent) {
        // Check if it's already a complete HTML document
        if (originalContent.trim().toLowerCase().startsWith("<!doctype") ||
                originalContent.trim().toLowerCase().startsWith("<html")) {
            return originalContent;
        }

        // Wrap content in proper HTML structure with enhanced styling
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - SQL Learning</title>
                <style>
                    %s
                </style>
            </head>
            <body>
                <div class="content-header">
                    <h1>%s</h1>
                    <div class="meta-info">
                        <span class="user">👤 SithuHan-SithuHan</span>
                        <span class="date">📅 %s</span>
                    </div>
                </div>
                <div class="content-body">
                    %s
                </div>
                <div class="content-footer">
                    <p>SQL Learning Professional Edition | <a href="https://github.com/SithuHan-SithuHan/SQL_Learning_APP" target="_blank">GitHub Repository</a></p>
                </div>
            </body>
            </html>
            """,
                currentTopic != null ? currentTopic : "SQL Learning",
                getLearningContentCSS(),
                currentTopic != null ? currentTopic : "SQL Learning Content",
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                originalContent
        );
    }

    private String getLearningContentCSS() {
        return """
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                line-height: 1.6;
                color: #2d3748;
                background-color: #ffffff;
                margin: 0;
                padding: 0;
                max-width: 100%;
                overflow-x: hidden;
            }
            
            .content-header {
                background: linear-gradient(135deg, #2563eb, #1d4ed8);
                color: white;
                padding: 30px 40px;
                margin-bottom: 0;
            }
            
            .content-header h1 {
                margin: 0 0 10px 0;
                font-size: 2.2em;
                font-weight: 700;
            }
            
            .meta-info {
                display: flex;
                gap: 20px;
                font-size: 0.9em;
                opacity: 0.9;
            }
            
            .meta-info span {
                display: flex;
                align-items: center;
                gap: 5px;
            }
            
            .content-body {
                padding: 40px;
                max-width: 800px;
                margin: 0 auto;
            }
            
            .content-body h1 {
                color: #1a202c;
                border-bottom: 3px solid #2563eb;
                padding-bottom: 10px;
                margin-top: 0;
            }
            
            .content-body h2 {
                color: #2d3748;
                margin-top: 30px;
                margin-bottom: 15px;
                font-size: 1.4em;
            }
            
            .content-body h3 {
                color: #4a5568;
                margin-top: 25px;
                margin-bottom: 10px;
            }
            
            .content-body p {
                margin-bottom: 16px;
                color: #4a5568;
            }
            
            .content-body .code {
                background: #1a202c;
                color: #e2e8f0;
                padding: 20px;
                border-radius: 8px;
                font-family: 'JetBrains Mono', 'Consolas', monospace;
                font-size: 14px;
                line-height: 1.5;
                border-left: 4px solid #2563eb;
                margin: 20px 0;
                overflow-x: auto;
            }
            
            .content-body .syntax {
                background: #f0fff4;
                border: 2px solid #68d391;
                padding: 15px;
                border-radius: 8px;
                font-family: 'JetBrains Mono', 'Consolas', monospace;
                margin: 15px 0;
            }
            
            .content-body .highlight, .content-body .note {
                background: #fffbeb;
                border-left: 4px solid #f59e0b;
                padding: 15px;
                margin: 20px 0;
                border-radius: 0 8px 8px 0;
            }
            
            .content-body .warning {
                background: #fef2f2;
                border-left: 4px solid #ef4444;
                padding: 15px;
                margin: 20px 0;
                border-radius: 0 8px 8px 0;
            }
            
            .content-body table {
                width: 100%;
                border-collapse: collapse;
                margin: 20px 0;
                background: white;
                border-radius: 8px;
                overflow: hidden;
                box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            }
            
            .content-body th {
                background: #f7fafc;
                padding: 15px;
                text-align: left;
                font-weight: 600;
                color: #2d3748;
                border-bottom: 2px solid #e2e8f0;
            }
            
            .content-body td {
                padding: 12px 15px;
                border-bottom: 1px solid #e2e8f0;
            }
            
            .content-body tr:last-child td {
                border-bottom: none;
            }
            
            .content-body tr:hover {
                background-color: #f7fafc;
            }
            
            .content-body ul, .content-body ol {
                padding-left: 25px;
            }
            
            .content-body li {
                margin-bottom: 8px;
                color: #4a5568;
            }
            
            .content-body strong {
                color: #2d3748;
                font-weight: 600;
            }
            
            .content-body code {
                background: #edf2f7;
                color: #2d3748;
                padding: 2px 6px;
                border-radius: 4px;
                font-family: 'JetBrains Mono', 'Consolas', monospace;
                font-size: 0.9em;
            }
            
            .content-footer {
                background: #f7fafc;
                padding: 20px;
                text-align: center;
                border-top: 1px solid #e2e8f0;
                color: #718096;
                font-size: 0.9em;
            }
            
            .content-footer a {
                color: #2563eb;
                text-decoration: none;
            }
            
            .content-footer a:hover {
                text-decoration: underline;
            }
            
            /* Responsive design */
            @media (max-width: 768px) {
                .content-header {
                    padding: 20px;
                }
                
                .content-header h1 {
                    font-size: 1.8em;
                }
                
                .content-body {
                    padding: 20px;
                }
                
                .meta-info {
                    flex-direction: column;
                    gap: 5px;
                }
                
                .content-body .code {
                    padding: 15px;
                    font-size: 13px;
                }
            }
            
            /* Print styles */
            @media print {
                .content-header {
                    background: #2563eb !important;
                    -webkit-print-color-adjust: exact;
                    color-adjust: exact;
                }
                
                .content-body .code {
                    background: #f7fafc !important;
                    color: #2d3748 !important;
                    border: 1px solid #e2e8f0;
                }
            }
            """;
    }

    // ===== GETTER METHODS =====

    public String getCurrentContent() {
        return currentContent;
    }

    public String getCurrentTopic() {
        return currentTopic;
    }

    public WebView getWebView() {
        return webView;
    }

    public boolean isLoading() {
        return webEngine.getLoadWorker().getState() == Worker.State.RUNNING;
    }
}