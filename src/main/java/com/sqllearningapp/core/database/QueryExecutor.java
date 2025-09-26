package com.sqllearningapp.core.database;

import com.sqllearningapp.core.models.QueryResult;
import lombok.extern.slf4j.Slf4j;
import java.sql.*;
import java.util.*;

/**
 * SQL Query Execution Engine with enhanced error handling and result processing
 */
@Slf4j
public class QueryExecutor {

    private final EmbeddedDatabase database;
    private static final int QUERY_TIMEOUT_SECONDS = 30;
    private static final int MAX_RESULT_ROWS = 1000;

    public QueryExecutor(EmbeddedDatabase database) {
        this.database = database;
    }

    /**
     * Execute SQL query on practice database
     */
    public QueryResult executeQuery(String sql) {
        return executeQuery(sql, true); // Default to practice database
    }

    /**
     * Execute SQL query with database selection
     * @param sql The SQL query to execute
     * @param usePracticeDb true for practice database, false for main database
     */
    public QueryResult executeQuery(String sql, boolean usePracticeDb) {
        if (sql == null || sql.trim().isEmpty()) {
            return QueryResult.builder()
                    .success(false)
                    .message("Empty query")
                    .executionTimeMs(0)
                    .build();
        }

        long startTime = System.currentTimeMillis();
        Connection connection = usePracticeDb ?
                database.getPracticeConnection() : database.getMainConnection();

        try {
            sql = sql.trim();
            String queryType = determineQueryType(sql);

            log.debug("Executing {} query: {}", queryType, sql);

            // Handle different query types
            switch (queryType.toUpperCase()) {
                case "SELECT":
                    return executeSelectQuery(connection, sql, startTime);
                case "INSERT":
                case "UPDATE":
                case "DELETE":
                    return executeModificationQuery(connection, sql, queryType, startTime);
                case "CREATE":
                case "DROP":
                case "ALTER":
                    return executeDDLQuery(connection, sql, queryType, startTime);
                default:
                    return executeGenericQuery(connection, sql, queryType, startTime);
            }

        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("SQL execution error: {}", e.getMessage());

            return QueryResult.builder()
                    .success(false)
                    .message("SQL Error: " + e.getMessage())
                    .queryType(determineQueryType(sql))
                    .executionTimeMs(executionTime)
                    .build();

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Unexpected error during query execution", e);

            return QueryResult.builder()
                    .success(false)
                    .message("Unexpected error: " + e.getMessage())
                    .queryType(determineQueryType(sql))
                    .executionTimeMs(executionTime)
                    .build();
        }
    }

    private QueryResult executeSelectQuery(Connection connection, String sql, long startTime)
            throws SQLException {

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            stmt.setMaxRows(MAX_RESULT_ROWS);

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> columnNames = extractColumnNames(rs.getMetaData());
                List<Map<String, Object>> rows = extractRows(rs, columnNames);
                long executionTime = System.currentTimeMillis() - startTime;

                return QueryResult.builder()
                        .success(true)
                        .message(String.format("Query executed successfully. Retrieved %d row(s).", rows.size()))
                        .columnNames(columnNames)
                        .rows(rows)
                        .rowCount(rows.size())
                        .queryType("SELECT")
                        .executionTimeMs(executionTime)
                        .build();
            }
        }
    }

    private QueryResult executeModificationQuery(Connection connection, String sql,
                                                 String queryType, long startTime) throws SQLException {

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            int affectedRows = stmt.executeUpdate();
            long executionTime = System.currentTimeMillis() - startTime;

            String message = String.format("%s executed successfully. %d row(s) affected.",
                    queryType.toUpperCase(), affectedRows);

            return QueryResult.builder()
                    .success(true)
                    .message(message)
                    .rowCount(affectedRows)
                    .queryType(queryType.toUpperCase())
                    .executionTimeMs(executionTime)
                    .build();
        }
    }

    private QueryResult executeDDLQuery(Connection connection, String sql,
                                        String queryType, long startTime) throws SQLException {

        try (Statement stmt = connection.createStatement()) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            stmt.execute(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            String message = String.format("%s statement executed successfully.",
                    queryType.toUpperCase());

            return QueryResult.builder()
                    .success(true)
                    .message(message)
                    .queryType(queryType.toUpperCase())
                    .executionTimeMs(executionTime)
                    .build();
        }
    }

    private QueryResult executeGenericQuery(Connection connection, String sql,
                                            String queryType, long startTime) throws SQLException {

        try (Statement stmt = connection.createStatement()) {
            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            boolean hasResultSet = stmt.execute(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    List<String> columnNames = extractColumnNames(rs.getMetaData());
                    List<Map<String, Object>> rows = extractRows(rs, columnNames);

                    return QueryResult.builder()
                            .success(true)
                            .message("Query executed successfully.")
                            .columnNames(columnNames)
                            .rows(rows)
                            .rowCount(rows.size())
                            .queryType(queryType)
                            .executionTimeMs(executionTime)
                            .build();
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                return QueryResult.builder()
                        .success(true)
                        .message("Statement executed successfully.")
                        .rowCount(updateCount)
                        .queryType(queryType)
                        .executionTimeMs(executionTime)
                        .build();
            }
        }
    }

    private List<String> extractColumnNames(ResultSetMetaData metaData) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        return columnNames;
    }

    private List<Map<String, Object>> extractRows(ResultSet rs, List<String> columnNames)
            throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();
        int rowCount = 0;

        while (rs.next() && rowCount < MAX_RESULT_ROWS) {
            Map<String, Object> row = new LinkedHashMap<>();

            for (String columnName : columnNames) {
                Object value = rs.getObject(columnName);
                row.put(columnName, value);
            }

            rows.add(row);
            rowCount++;
        }

        return rows;
    }

    private String determineQueryType(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "UNKNOWN";
        }

        String trimmedSql = sql.trim().toUpperCase();

        if (trimmedSql.startsWith("SELECT")) return "SELECT";
        if (trimmedSql.startsWith("INSERT")) return "INSERT";
        if (trimmedSql.startsWith("UPDATE")) return "UPDATE";
        if (trimmedSql.startsWith("DELETE")) return "DELETE";
        if (trimmedSql.startsWith("CREATE")) return "CREATE";
        if (trimmedSql.startsWith("DROP")) return "DROP";
        if (trimmedSql.startsWith("ALTER")) return "ALTER";
        if (trimmedSql.startsWith("TRUNCATE")) return "TRUNCATE";
        if (trimmedSql.startsWith("COMMIT")) return "COMMIT";
        if (trimmedSql.startsWith("ROLLBACK")) return "ROLLBACK";
        if (trimmedSql.startsWith("GRANT")) return "GRANT";
        if (trimmedSql.startsWith("REVOKE")) return "REVOKE";

        return "OTHER";
    }

    /**
     * Validate SQL syntax without execution
     */
    public ValidationResult validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new ValidationResult(false, "Empty query");
        }

        try {
            Connection connection = database.getPracticeConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                // If we can prepare the statement, syntax is likely valid
                return new ValidationResult(true, "SQL syntax is valid");
            }
        } catch (SQLException e) {
            return new ValidationResult(false, "SQL syntax error: " + e.getMessage());
        }
    }

    /**
     * Simple validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}