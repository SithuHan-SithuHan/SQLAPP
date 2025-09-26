package com.sqllearningapp.core.services;

import com.sqllearningapp.core.database.EmbeddedDatabase;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

/**
 * Database Browser Service - Provides database exploration functionality
 * Preserves your original database browser features with enhancements
 */
@Slf4j
public class DatabaseBrowserService {

    private final EmbeddedDatabase database;

    public DatabaseBrowserService(EmbeddedDatabase database) {
        this.database = database;
    }

    /**
     * Get all tables in the practice database
     */
    public List<TableInfo> getAllTables() {
        List<TableInfo> tables = new ArrayList<>();

        try {
            Connection connection = database.getPracticeConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");

                    // Skip system tables
                    if (!tableName.startsWith("INFORMATION_SCHEMA") &&
                            !tableName.startsWith("SYS_") &&
                            !tableName.startsWith("SYSTEM_")) {

                        TableInfo tableInfo = TableInfo.builder()
                                .name(tableName)
                                .type(rs.getString("TABLE_TYPE"))
                                .schema(rs.getString("TABLE_SCHEM"))
                                .remarks(rs.getString("REMARKS"))
                                .columns(getTableColumns(tableName))
                                .rowCount(getTableRowCount(tableName))
                                .build();

                        tables.add(tableInfo);
                    }
                }
            }

            log.debug("Found {} tables in practice database", tables.size());
            return tables;

        } catch (SQLException e) {
            log.error("Error retrieving table list", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get detailed information about a specific table
     */
    public TableInfo getTableInfo(String tableName) {
        try {
            Connection connection = database.getPracticeConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            // Get basic table info
            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return TableInfo.builder()
                            .name(tableName)
                            .type(rs.getString("TABLE_TYPE"))
                            .schema(rs.getString("TABLE_SCHEM"))
                            .remarks(rs.getString("REMARKS"))
                            .columns(getTableColumns(tableName))
                            .foreignKeys(getTableForeignKeys(tableName))
                            .primaryKeys(getTablePrimaryKeys(tableName))
                            .indexes(getTableIndexes(tableName))
                            .rowCount(getTableRowCount(tableName))
                            .build();
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving table info for: {}", tableName, e);
        }

        return null;
    }

    /**
     * Get column information for a table
     */
    public List<ColumnInfo> getTableColumns(String tableName) {
        List<ColumnInfo> columns = new ArrayList<>();

        try {
            Connection connection = database.getPracticeConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    ColumnInfo column = ColumnInfo.builder()
                            .name(rs.getString("COLUMN_NAME"))
                            .dataType(rs.getString("TYPE_NAME"))
                            .size(rs.getInt("COLUMN_SIZE"))
                            .nullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                            .autoIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")))
                            .defaultValue(rs.getString("COLUMN_DEF"))
                            .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                            .remarks(rs.getString("REMARKS"))
                            .build();

                    columns.add(column);
                }
            }

            // Sort by ordinal position
            columns.sort(Comparator.comparingInt(ColumnInfo::getOrdinalPosition));

        } catch (SQLException e) {
            log.error("Error retrieving columns for table: {}", tableName, e);
        }

        return columns;
    }

    /**
     * Get foreign key information for a table
     */
    public List<ForeignKeyInfo> getTableForeignKeys(String tableName) {
        List<ForeignKeyInfo> foreignKeys = new ArrayList<>();

        try {
            Connection connection = database.getPracticeConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            try (ResultSet rs = metaData.getImportedKeys(null, null, tableName)) {
                while (rs.next()) {
                    ForeignKeyInfo fk = ForeignKeyInfo.builder()
                            .constraintName(rs.getString("FK_NAME"))
                            .columnName(rs.getString("FKCOLUMN_NAME"))
                            .referencedTable(rs.getString("PKTABLE_NAME"))
                            .referencedColumn(rs.getString("PKCOLUMN_NAME"))
                            .updateRule(rs.getInt("UPDATE_RULE"))
                            .deleteRule(rs.getInt("DELETE_RULE"))
                            .build();

                    foreignKeys.add(fk);
                }
            }

        } catch (SQLException e) {
            log.error("Error retrieving foreign keys for table: {}", tableName, e);
        }

        return foreignKeys;
    }

    /**
     * Get primary key information for a table
     */
    public List<String> getTablePrimaryKeys(String tableName) {
        List<String> primaryKeys = new ArrayList<>();

        try {
            Connection connection = database.getPracticeConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
            }

        } catch (SQLException e) {
            log.error("Error retrieving primary keys for table: {}", tableName, e);
        }

        return primaryKeys;
    }

    /**
     * Get index information for a table
     */
    public List<IndexInfo> getTableIndexes(String tableName) {
        List<IndexInfo> indexes = new ArrayList<>();

        try {
            Connection connection = database.getPracticeConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    if (indexName != null) {
                        IndexInfo index = IndexInfo.builder()
                                .name(indexName)
                                .columnName(rs.getString("COLUMN_NAME"))
                                .unique(!rs.getBoolean("NON_UNIQUE"))
                                .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                                .build();

                        indexes.add(index);
                    }
                }
            }

        } catch (SQLException e) {
            log.error("Error retrieving indexes for table: {}", tableName, e);
        }

        return indexes;
    }

    /**
     * Get row count for a table
     */
    public int getTableRowCount(String tableName) {
        try {
            Connection connection = database.getPracticeConnection();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error getting row count for table: {}", tableName, e);
        }

        return 0;
    }

    /**
     * Get sample data from a table
     */
    public List<Map<String, Object>> getTableSampleData(String tableName, int limit) {
        List<Map<String, Object>> sampleData = new ArrayList<>();

        try {
            Connection connection = database.getPracticeConnection();
            String sql = "SELECT * FROM " + tableName + " LIMIT " + limit;

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }

                    sampleData.add(row);
                }
            }

        } catch (SQLException e) {
            log.error("Error retrieving sample data from table: {}", tableName, e);
        }

        return sampleData;
    }

    // ===== DATA CLASSES =====

    @Data
    @Builder
    public static class TableInfo {
        private String name;
        private String type;
        private String schema;
        private String remarks;
        private List<ColumnInfo> columns;
        private List<ForeignKeyInfo> foreignKeys;
        private List<String> primaryKeys;
        private List<IndexInfo> indexes;
        private int rowCount;
    }

    @Data
    @Builder
    public static class ColumnInfo {
        private String name;
        private String dataType;
        private int size;
        private boolean nullable;
        private boolean autoIncrement;
        private String defaultValue;
        private int ordinalPosition;
        private String remarks;

        public String getDisplayType() {
            if (size > 0 && (dataType.contains("CHAR") || dataType.contains("VARCHAR"))) {
                return dataType + "(" + size + ")";
            }
            return dataType;
        }
    }

    @Data
    @Builder
    public static class ForeignKeyInfo {
        private String constraintName;
        private String columnName;
        private String referencedTable;
        private String referencedColumn;
        private int updateRule;
        private int deleteRule;

        public String getDisplayText() {
            return columnName + " → " + referencedTable + "(" + referencedColumn + ")";
        }
    }

    @Data
    @Builder
    public static class IndexInfo {
        private String name;
        private String columnName;
        private boolean unique;
        private int ordinalPosition;

        public String getDisplayText() {
            return name + (unique ? " (UNIQUE)" : "");
        }
    }
}