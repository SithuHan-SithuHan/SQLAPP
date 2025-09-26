package com.sqllearningapp.core.models;



import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a SQL query execution
 */
@Data
@Builder
public class QueryResult {
    private boolean success;
    private String message;
    private List<String> columnNames;
    private List<Map<String, Object>> rows;
    private int rowCount;
    private long executionTimeMs;
    private String queryType; // SELECT, INSERT, UPDATE, DELETE, etc.

    public int getColumnCount() {
        return columnNames != null ? columnNames.size() : 0;
    }

    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }

    public Object getValueAt(int row, int column) {
        if (rows != null && row < rows.size() && column < getColumnCount()) {
            return rows.get(row).get(columnNames.get(column));
        }
        return null;
    }
}