package com.sqllearningapp.ui.components;

import com.sqllearningapp.core.models.QueryResult;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Enhanced Results Panel - Displays query results with modern styling
 * Preserves your original results display functionality with improvements
 */
@Slf4j
public class ResultsPanel extends VBox {

    private TabPane tabPane;
    private TableView<Map<String, Object>> resultsTable;
    private TextArea messagesArea;
    private Label summaryLabel;
    private ProgressIndicator loadingIndicator;

    // Current result state
    private QueryResult currentResult;

    public ResultsPanel() {
        setupUI();
        getStyleClass().add("results-panel");
    }

    private void setupUI() {
        setPadding(new Insets(10));
        setSpacing(8);

        // Create summary bar
        HBox summaryBar = createSummaryBar();

        // Create tabbed content area
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("results-tabs");

        // Results tab
        Tab resultsTab = new Tab("📊 Results");
        resultsTab.setContent(createResultsContent());

        // Messages tab
        Tab messagesTab = new Tab("💬 Messages");
        messagesTab.setContent(createMessagesContent());

        tabPane.getTabs().addAll(resultsTab, messagesTab);

        // Set growth priorities
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        getChildren().addAll(summaryBar, tabPane);
    }

    private HBox createSummaryBar() {
        HBox summaryBar = new HBox(15);
        summaryBar.getStyleClass().add("results-summary-bar");
        summaryBar.setPadding(new Insets(5, 10, 5, 10));

        summaryLabel = new Label("No results");
        summaryLabel.getStyleClass().add("results-summary");

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(20, 20);
        loadingIndicator.setVisible(false);

        // Export button
        Button exportBtn = new Button("📤 Export");
        exportBtn.getStyleClass().add("export-btn");
        exportBtn.setOnAction(e -> exportResults());
        exportBtn.setDisable(true);

        // Clear button
        Button clearBtn = new Button("🗑 Clear");
        clearBtn.getStyleClass().add("clear-btn");
        clearBtn.setOnAction(e -> clear());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        summaryBar.getChildren().addAll(
                summaryLabel, loadingIndicator, spacer, exportBtn, clearBtn
        );

        return summaryBar;
    }

    private ScrollPane createResultsContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create results table
        resultsTable = new TableView<>();
        resultsTable.getStyleClass().add("results-table");
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resultsTable.setPlaceholder(new Label("No data to display\nExecute a SELECT query to see results here"));

        // Enable row selection and copying
        resultsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resultsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();

            // Context menu for copying data
            ContextMenu contextMenu = new ContextMenu();
            MenuItem copyItem = new MenuItem("Copy Selected Rows");
            copyItem.setOnAction(e -> copySelectedRows());
            contextMenu.getItems().add(copyItem);

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    // Double-click to view row details
                    showRowDetails(row.getItem());
                }
            });

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });

        content.getChildren().add(resultsTable);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("results-scroll");

        return scrollPane;
    }

    private ScrollPane createMessagesContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create messages area
        messagesArea = new TextArea();
        messagesArea.setEditable(false);
        messagesArea.setWrapText(true);
        messagesArea.getStyleClass().add("messages-area");
        messagesArea.setPromptText("Query messages and execution details will appear here");

        content.getChildren().add(messagesArea);
        VBox.setVgrow(messagesArea, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    // ===== PUBLIC METHODS =====

    /**
     * Display query results (preserving your original displayExecutionResult logic)
     */
    public void displayResult(QueryResult result) {
        this.currentResult = result;

        if (result == null) {
            clear();
            return;
        }

        // Update summary
        updateSummary(result);

        // Update messages
        updateMessages(result);

        // Display results if it's a SELECT query
        if (result.getQueryType().equals("SELECT") && result.getRows() != null) {
            displayTableResults(result);

            // Select results tab
            tabPane.getSelectionModel().select(0);
        } else {
            // Clear table and show messages tab for non-SELECT queries
            clearTable();
            tabPane.getSelectionModel().select(1);
        }

        // Enable export if we have data
        updateExportButton();

        log.debug("Displayed result: {} rows, {} ms",
                result.getRowCount(), result.getExecutionTimeMs());
    }

    /**
     * Display error message
     */
    public void displayError(String errorMessage) {
        clearTable();

        summaryLabel.setText("❌ Error");
        summaryLabel.getStyleClass().removeAll("success", "warning", "info");
        summaryLabel.getStyleClass().add("error");

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        String message = String.format("[%s] ERROR: %s\n", timestamp, errorMessage);
        messagesArea.appendText(message);

        // Select messages tab
        tabPane.getSelectionModel().select(1);

        log.debug("Displayed error: {}", errorMessage);
    }

    /**
     * Display success message
     */
    public void displayMessage(String message, String type) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        String icon = switch (type.toLowerCase()) {
            case "success" -> "✅";
            case "warning" -> "⚠️";
            case "error" -> "❌";
            case "info" -> "ℹ️";
            default -> "📝";
        };

        String formattedMessage = String.format("[%s] %s %s\n", timestamp, icon, message);
        messagesArea.appendText(formattedMessage);

        // Update summary
        summaryLabel.setText(icon + " " + type.toUpperCase());
        summaryLabel.getStyleClass().removeAll("success", "warning", "error", "info");
        summaryLabel.getStyleClass().add(type.toLowerCase());

        // Select messages tab
        tabPane.getSelectionModel().select(1);
    }

    /**
     * Clear all results
     */
    public void clear() {
        currentResult = null;
        clearTable();
        messagesArea.clear();
        summaryLabel.setText("No results");
        summaryLabel.getStyleClass().removeAll("success", "warning", "error", "info");
        updateExportButton();
    }

    /**
     * Show loading state
     */
    public void showLoading(String message) {
        loadingIndicator.setVisible(true);
        summaryLabel.setText("⏳ " + message);
    }

    /**
     * Hide loading state
     */
    public void hideLoading() {
        loadingIndicator.setVisible(false);
    }

    // ===== PRIVATE METHODS =====

    private void displayTableResults(QueryResult result) {
        if (result.getColumnNames() == null || result.getRows() == null) {
            clearTable();
            return;
        }

        // Clear existing columns
        resultsTable.getColumns().clear();

        // Create columns dynamically
        for (String columnName : result.getColumnNames()) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(data -> {
                Object value = data.getValue().get(columnName);
                return new SimpleStringProperty(value != null ? value.toString() : "NULL");
            });

            // Add custom cell factory for better formatting
            column.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("null-cell", "number-cell", "text-cell");
                    } else {
                        setText(item);

                        // Apply styling based on content
                        getStyleClass().removeAll("null-cell", "number-cell", "text-cell");
                        if ("NULL".equals(item)) {
                            getStyleClass().add("null-cell");
                        } else if (item.matches("-?\\d+(\\.\\d+)?")) {
                            getStyleClass().add("number-cell");
                        } else {
                            getStyleClass().add("text-cell");
                        }
                    }
                }
            });

            resultsTable.getColumns().add(column);
        }

        // Set data
        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(result.getRows());
        resultsTable.setItems(data);

        // Auto-resize columns
        Platform.runLater(() -> {
            if (!resultsTable.getColumns().isEmpty()) {
                // Set preferred widths
                double tableWidth = resultsTable.getWidth();
                double columnWidth = Math.max(100, tableWidth / resultsTable.getColumns().size());

                for (TableColumn<?, ?> column : resultsTable.getColumns()) {
                    column.setPrefWidth(columnWidth);
                }
            }
        });
    }

    private void clearTable() {
        resultsTable.getColumns().clear();
        resultsTable.setItems(null);
    }

    private void updateSummary(QueryResult result) {
        String summaryText;
        String styleClass;

        if (result.isSuccess()) {
            if (result.getQueryType().equals("SELECT")) {
                summaryText = String.format("✅ %d row(s) returned in %dms",
                        result.getRowCount(), result.getExecutionTimeMs());
            } else {
                summaryText = String.format("✅ %s completed (%d row(s) affected) in %dms",
                        result.getQueryType(), result.getRowCount(), result.getExecutionTimeMs());
            }
            styleClass = "success";
        } else {
            summaryText = "❌ Query failed";
            styleClass = "error";
        }

        summaryLabel.setText(summaryText);
        summaryLabel.getStyleClass().removeAll("success", "warning", "error", "info");
        summaryLabel.getStyleClass().add(styleClass);
    }

    private void updateMessages(QueryResult result) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        String message = String.format("[%s] %s: %s\n",
                timestamp, result.getQueryType(), result.getMessage());

        messagesArea.appendText(message);

        // Scroll to bottom
        messagesArea.setScrollTop(Double.MAX_VALUE);
    }

    private void updateExportButton() {
        Button exportBtn = (Button) ((HBox) getChildren().get(0)).getChildren().get(3);
        exportBtn.setDisable(currentResult == null ||
                currentResult.getRows() == null ||
                currentResult.getRows().isEmpty());
    }

    private void copySelectedRows() {
        ObservableList<Map<String, Object>> selectedItems =
                resultsTable.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        // Add header
        for (int i = 0; i < resultsTable.getColumns().size(); i++) {
            if (i > 0) sb.append("\t");
            sb.append(resultsTable.getColumns().get(i).getText());
        }
        sb.append("\n");

        // Add selected rows
        for (Map<String, Object> row : selectedItems) {
            for (int i = 0; i < resultsTable.getColumns().size(); i++) {
                if (i > 0) sb.append("\t");

                String columnName = resultsTable.getColumns().get(i).getText();
                Object value = row.get(columnName);
                sb.append(value != null ? value.toString() : "");
            }
            sb.append("\n");
        }

        // Copy to clipboard
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(sb.toString());
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

        // Show feedback
        displayMessage(String.format("Copied %d row(s) to clipboard", selectedItems.size()), "info");
    }

    private void showRowDetails(Map<String, Object> row) {
        if (row == null) return;

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Row Details");
        dialog.setHeaderText("Detailed view of selected row");

        // Create formatted content
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            content.append(String.format("%-20s: %s\n",
                    entry.getKey(),
                    entry.getValue() != null ? entry.getValue().toString() : "NULL"));
        }

        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(Math.min(20, row.size() + 2));
        textArea.setPrefColumnCount(60);

        dialog.getDialogPane().setContent(textArea);
        dialog.showAndWait();
    }

    private void exportResults() {
        if (currentResult == null || currentResult.getRows() == null) {
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Results");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new javafx.stage.FileChooser.ExtensionFilter("Tab Separated", "*.tsv"),
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
        );

        javafx.stage.Stage stage = (javafx.stage.Stage) getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                exportToFile(file);
                displayMessage("Results exported to: " + file.getName(), "success");
            } catch (Exception e) {
                displayMessage("Export failed: " + e.getMessage(), "error");
                log.error("Failed to export results", e);
            }
        }
    }

    private void exportToFile(java.io.File file) throws java.io.IOException {
        String separator = file.getName().toLowerCase().endsWith(".tsv") ? "\t" : ",";

        try (java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {

            // Write header
            writer.println(String.join(separator, currentResult.getColumnNames()));

            // Write data
            for (Map<String, Object> row : currentResult.getRows()) {
                List<String> values = currentResult.getColumnNames().stream()
                        .map(col -> {
                            Object value = row.get(col);
                            String str = value != null ? value.toString() : "";
                            // Escape CSV values if needed
                            if (separator.equals(",") && (str.contains(",") || str.contains("\"") || str.contains("\n"))) {
                                str = "\"" + str.replace("\"", "\"\"") + "\"";
                            }
                            return str;
                        })
                        .toList();

                writer.println(String.join(separator, values));
            }
        }
    }

    // ===== GETTER METHODS =====

    public QueryResult getCurrentResult() {
        return currentResult;
    }

    public boolean hasResults() {
        return currentResult != null && currentResult.getRows() != null && !currentResult.getRows().isEmpty();
    }

    public int getResultCount() {
        return currentResult != null ? currentResult.getRowCount() : 0;
    }
}