package com.sqllearningapp.ui.components;

import com.sqllearningapp.core.services.DatabaseBrowserService;
import com.sqllearningapp.core.services.DatabaseBrowserService.TableInfo;
import com.sqllearningapp.core.services.DatabaseBrowserService.ColumnInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Enhanced Database Browser - Explore database structure and data
 * Preserves your original database browser functionality with modern UI
 */
@Slf4j
public class DatabaseBrowser extends BorderPane {

    private final DatabaseBrowserService databaseBrowserService;

    // UI Components
    private ListView<TableInfo> tablesListView;
    private TableView<ColumnInfo> columnsTableView;
    private TableView<Map<String, Object>> dataTableView;
    private TextArea tableInfoArea;
    private Label statusLabel;

    // Current state
    private TableInfo currentTable;

    public DatabaseBrowser(DatabaseBrowserService databaseBrowserService) {
        this.databaseBrowserService = databaseBrowserService;
        setupUI();
        loadTables();
        getStyleClass().add("database-browser");
    }

    private void setupUI() {
        setPadding(new Insets(10));

        // Left panel: Tables list
        VBox leftPanel = createTablesPanel();

        // Center panel: Table details
        TabPane centerPanel = createDetailsPanel();

        // Bottom panel: Status bar
        HBox statusPanel = createStatusPanel();

        // Layout
        setLeft(leftPanel);
        setCenter(centerPanel);
        setBottom(statusPanel);
    }

    private VBox createTablesPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(250);
        panel.getStyleClass().add("tables-panel");

        // Header
        HBox header = new HBox(10);
        Label titleLabel = new Label("📋 Database Tables");
        titleLabel.getStyleClass().add("panel-title");

        Button refreshBtn = new Button("🔄");
        refreshBtn.getStyleClass().add("refresh-btn");
        refreshBtn.setTooltip(new Tooltip("Refresh Tables"));
        refreshBtn.setOnAction(e -> refreshTables());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, refreshBtn);

        // Tables list
        tablesListView = new ListView<>();
        tablesListView.getStyleClass().add("tables-list");
        tablesListView.setCellFactory(lv -> new TableListCell());

        tablesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldTable, newTable) -> {
            if (newTable != null) {
                loadTableDetails(newTable);
            }
        });

        VBox.setVgrow(tablesListView, Priority.ALWAYS);

        // Quick stats
        Label statsLabel = new Label("Database Statistics");
        statsLabel.getStyleClass().add("subsection-title");

        TextArea statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setPrefRowCount(4);
        statsArea.getStyleClass().add("stats-area");
        statsArea.setText(generateDatabaseStats());

        panel.getChildren().addAll(header, tablesListView, statsLabel, statsArea);

        return panel;
    }

    private TabPane createDetailsPanel() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("details-tabs");

        // Structure tab
        Tab structureTab = new Tab("🏗️ Structure");
        structureTab.setContent(createStructureTab());

        // Data tab
        Tab dataTab = new Tab("📊 Data");
        dataTab.setContent(createDataTab());

        // Info tab
        Tab infoTab = new Tab("ℹ️ Info");
        infoTab.setContent(createInfoTab());

        tabPane.getTabs().addAll(structureTab, dataTab, infoTab);

        return tabPane;
    }

    private ScrollPane createStructureTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Table Structure");
        titleLabel.getStyleClass().add("section-title");

        // Columns table
        columnsTableView = new TableView<>();
        columnsTableView.getStyleClass().add("columns-table");
        columnsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns for the columns table
        TableColumn<ColumnInfo, String> nameCol = new TableColumn<>("Column Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.getStyleClass().add("column-name");

        TableColumn<ColumnInfo, String> typeCol = new TableColumn<>("Data Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDisplayType()));

        TableColumn<ColumnInfo, String> nullableCol = new TableColumn<>("Nullable");
        nullableCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isNullable() ? "YES" : "NO"));

        TableColumn<ColumnInfo, String> defaultCol = new TableColumn<>("Default");
        defaultCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDefaultValue() != null ? data.getValue().getDefaultValue() : ""));

        TableColumn<ColumnInfo, String> extraCol = new TableColumn<>("Extra");
        extraCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isAutoIncrement() ? "AUTO_INCREMENT" : ""));

        columnsTableView.getColumns().addAll(nameCol, typeCol, nullableCol, defaultCol, extraCol);

        content.getChildren().addAll(titleLabel, columnsTableView);
        VBox.setVgrow(columnsTableView, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private ScrollPane createDataTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // Header with controls
        HBox header = new HBox(15);
        header.getStyleClass().add("data-header");

        Label titleLabel = new Label("Table Data");
        titleLabel.getStyleClass().add("section-title");

        Button refreshDataBtn = new Button("🔄 Refresh");
        refreshDataBtn.getStyleClass().add("refresh-data-btn");
        refreshDataBtn.setOnAction(e -> refreshTableData());

        ComboBox<String> limitCombo = new ComboBox<>();
        limitCombo.getItems().addAll("10", "50", "100", "500", "1000");
        limitCombo.setValue("100");
        limitCombo.setOnAction(e -> refreshTableData());

        Label limitLabel = new Label("Limit:");
        limitLabel.getStyleClass().add("limit-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, limitLabel, limitCombo, refreshDataBtn);

        // Data table
        dataTableView = new TableView<>();
        dataTableView.getStyleClass().add("data-table");
        dataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dataTableView.setPlaceholder(new Label("Select a table to view its data"));

        // Enable row selection and context menu
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataTableView.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();
            MenuItem copyItem = new MenuItem("Copy Selected Rows");
            copyItem.setOnAction(e -> copySelectedData());
            contextMenu.getItems().add(copyItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });

        content.getChildren().addAll(header, dataTableView);
        VBox.setVgrow(dataTableView, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private ScrollPane createInfoTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Table Information");
        titleLabel.getStyleClass().add("section-title");

        tableInfoArea = new TextArea();
        tableInfoArea.setEditable(false);
        tableInfoArea.setWrapText(true);
        tableInfoArea.getStyleClass().add("table-info-area");
        tableInfoArea.setPromptText("Select a table to view detailed information");

        content.getChildren().addAll(titleLabel, tableInfoArea);
        VBox.setVgrow(tableInfoArea, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private HBox createStatusPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(8, 15, 8, 15));
        panel.getStyleClass().add("db-status-bar");

        statusLabel = new Label("Database browser ready");
        statusLabel.getStyleClass().add("status-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Connection info
        Label connectionInfo = new Label(String.format("Connected | User: %s | Time: %s",
                "SithuHan-SithuHan",
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        ));
        connectionInfo.getStyleClass().add("connection-info");

        panel.getChildren().addAll(statusLabel, spacer, connectionInfo);

        return panel;
    }

    // ===== PRIVATE METHODS =====

    private void loadTables() {
        try {
            List<TableInfo> tables = databaseBrowserService.getAllTables();
            tablesListView.setItems(FXCollections.observableArrayList(tables));

            statusLabel.setText(String.format("Loaded %d tables", tables.size()));

            // Select first table if available
            if (!tables.isEmpty()) {
                tablesListView.getSelectionModel().selectFirst();
            }

            log.info("Loaded {} database tables", tables.size());

        } catch (Exception e) {
            statusLabel.setText("Failed to load tables: " + e.getMessage());
            log.error("Error loading database tables", e);
        }
    }

    private void refreshTables() {
        statusLabel.setText("Refreshing tables...");
        loadTables();
    }

    private void loadTableDetails(TableInfo table) {
        this.currentTable = table;

        try {
            // Load complete table info
            TableInfo completeInfo = databaseBrowserService.getTableInfo(table.getName());
            if (completeInfo != null) {
                this.currentTable = completeInfo;
            }

            // Update structure tab
            columnsTableView.setItems(FXCollections.observableArrayList(currentTable.getColumns()));

            // Update info tab
            updateTableInfo();

            // Load sample data
            loadTableData();

            statusLabel.setText(String.format("Loaded table: %s (%d columns, %d rows)",
                    table.getName(), table.getColumns().size(), table.getRowCount()));

        } catch (Exception e) {
            statusLabel.setText("Error loading table details: " + e.getMessage());
            log.error("Error loading table details for: {}", table.getName(), e);
        }
    }

    private void loadTableData() {
        if (currentTable == null) return;

        try {
            // Get limit from combo box
            ComboBox<String> limitCombo = findLimitCombo();
            int limit = limitCombo != null ? Integer.parseInt(limitCombo.getValue()) : 100;

            List<Map<String, Object>> sampleData = databaseBrowserService.getTableSampleData(
                    currentTable.getName(), limit);

            // Clear existing columns
            dataTableView.getColumns().clear();

            if (!sampleData.isEmpty()) {
                // Create columns dynamically
                Map<String, Object> firstRow = sampleData.get(0);
                for (String columnName : firstRow.keySet()) {
                    TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
                    column.setCellValueFactory(data -> {
                        Object value = data.getValue().get(columnName);
                        return new SimpleStringProperty(value != null ? value.toString() : "NULL");
                    });

                    // Style cells based on data type
                    column.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);

                            getStyleClass().removeAll("null-cell", "number-cell", "text-cell");

                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item);

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

                    dataTableView.getColumns().add(column);
                }

                // Set data
                dataTableView.setItems(FXCollections.observableArrayList(sampleData));
            } else {
                dataTableView.setPlaceholder(new Label("No data found in table"));
            }

        } catch (Exception e) {
            statusLabel.setText("Error loading table data: " + e.getMessage());
            log.error("Error loading table data for: {}", currentTable.getName(), e);
        }
    }

    private void refreshTableData() {
        if (currentTable != null) {
            loadTableData();
        }
    }

    private void updateTableInfo() {
        if (currentTable == null) return;

        StringBuilder info = new StringBuilder();

        // Basic info
        info.append("📋 TABLE INFORMATION\n");
        info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        info.append(String.format("Table Name: %s\n", currentTable.getName()));
        info.append(String.format("Table Type: %s\n", currentTable.getType()));
        info.append(String.format("Row Count: %,d\n", currentTable.getRowCount()));
        info.append(String.format("Column Count: %d\n\n", currentTable.getColumns().size()));

        // Primary Keys
        if (currentTable.getPrimaryKeys() != null && !currentTable.getPrimaryKeys().isEmpty()) {
            info.append("🔑 PRIMARY KEYS\n");
            info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (String pk : currentTable.getPrimaryKeys()) {
                info.append("• ").append(pk).append("\n");
            }
            info.append("\n");
        }

        // Foreign Keys
        if (currentTable.getForeignKeys() != null && !currentTable.getForeignKeys().isEmpty()) {
            info.append("🔗 FOREIGN KEYS\n");
            info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (var fk : currentTable.getForeignKeys()) {
                info.append(String.format("• %s → %s.%s\n",
                        fk.getColumnName(), fk.getReferencedTable(), fk.getReferencedColumn()));
            }
            info.append("\n");
        }

        // Indexes
        if (currentTable.getIndexes() != null && !currentTable.getIndexes().isEmpty()) {
            info.append("📇 INDEXES\n");
            info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            currentTable.getIndexes().forEach(index -> {
                info.append(String.format("• %s on %s %s\n",
                        index.getName(), index.getColumnName(),
                        index.isUnique() ? "(UNIQUE)" : ""));
            });
            info.append("\n");
        }

        // Detailed columns
        info.append("📊 COLUMN DETAILS\n");
        info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        for (ColumnInfo column : currentTable.getColumns()) {
            info.append(String.format("%-20s %s", column.getName(), column.getDisplayType()));

            if (!column.isNullable()) info.append(" NOT NULL");
            if (column.isAutoIncrement()) info.append(" AUTO_INCREMENT");
            if (column.getDefaultValue() != null) {
                info.append(" DEFAULT ").append(column.getDefaultValue());
            }

            info.append("\n");
        }

        // Metadata
        info.append("\n📋 METADATA\n");
        info.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        info.append(String.format("Schema: %s\n", currentTable.getSchema()));
        info.append(String.format("Last Updated: %s\n",
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        info.append(String.format("Viewed by: SithuHan-SithuHan\n"));

        tableInfoArea.setText(info.toString());
    }

    private void copySelectedData() {
        var selectedItems = dataTableView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) return;

        StringBuilder sb = new StringBuilder();

        // Add header
        for (int i = 0; i < dataTableView.getColumns().size(); i++) {
            if (i > 0) sb.append("\t");
            sb.append(dataTableView.getColumns().get(i).getText());
        }
        sb.append("\n");

        // Add selected rows
        for (Map<String, Object> row : selectedItems) {
            for (int i = 0; i < dataTableView.getColumns().size(); i++) {
                if (i > 0) sb.append("\t");

                String columnName = dataTableView.getColumns().get(i).getText();
                Object value = row.get(columnName);
                sb.append(value != null ? value.toString() : "");
            }
            sb.append("\n");
        }

        // Copy to clipboard
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(sb.toString());
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

        statusLabel.setText(String.format("Copied %d rows to clipboard", selectedItems.size()));
    }

    private ComboBox<String> findLimitCombo() {
        // Find the limit combo box in the data tab
        try {
            TabPane tabPane = (TabPane) getCenter();
            Tab dataTab = tabPane.getTabs().get(1); // Data tab is second
            ScrollPane scrollPane = (ScrollPane) dataTab.getContent();
            VBox content = (VBox) scrollPane.getContent();
            HBox header = (HBox) content.getChildren().get(0);

            for (var child : header.getChildren()) {
                if (child instanceof ComboBox<?>) {
                    return (ComboBox<String>) child;
                }
            }
        } catch (Exception e) {
            log.debug("Could not find limit combo box");
        }
        return null;
    }

    private String generateDatabaseStats() {
        try {
            List<TableInfo> tables = databaseBrowserService.getAllTables();
            int totalTables = tables.size();
            int totalRows = tables.stream().mapToInt(TableInfo::getRowCount).sum();
            int totalColumns = tables.stream().mapToInt(t -> t.getColumns().size()).sum();

            return String.format("""
                Tables: %d
                Total Rows: %,d
                Total Columns: %d
                Database: H2 (Practice)
                """, totalTables, totalRows, totalColumns);

        } catch (Exception e) {
            return "Statistics unavailable";
        }
    }

    // ===== CUSTOM CELL =====

    private static class TableListCell extends ListCell<TableInfo> {
        @Override
        protected void updateItem(TableInfo item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().removeAll("table-cell");
            } else {
                getStyleClass().add("table-cell");
                setText(String.format("📄 %s (%,d rows)", item.getName(), item.getRowCount()));
                setTooltip(new Tooltip(String.format(
                        "Table: %s\nColumns: %d\nRows: %,d\nType: %s",
                        item.getName(), item.getColumns().size(),
                        item.getRowCount(), item.getType())));
            }
        }
    }

    // ===== PUBLIC METHODS =====

    public void refreshBrowser() {
        refreshTables();
    }

    public void selectTable(String tableName) {
        for (TableInfo table : tablesListView.getItems()) {
            if (table.getName().equals(tableName)) {
                tablesListView.getSelectionModel().select(table);
                break;
            }
        }
    }

    public TableInfo getCurrentTable() {
        return currentTable;
    }

    public boolean hasTableSelected() {
        return currentTable != null;
    }
}