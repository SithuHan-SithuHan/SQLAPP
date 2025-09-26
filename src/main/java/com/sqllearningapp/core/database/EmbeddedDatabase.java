package com.sqllearningapp.core.database;

import lombok.extern.slf4j.Slf4j;
import java.sql.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Enhanced Embedded Database Manager - Zero external dependencies
 */
@Slf4j
public class EmbeddedDatabase {

    // FIX: Remove conflicting parameters for H2 v2.2.224 compatibility
    private static final String MAIN_DB_URL = "jdbc:h2:./data/sqllearning;AUTO_SERVER=TRUE";

    // Practice database for SQL exercises (in-memory, resetable)
    private static final String PRACTICE_DB_URL = "jdbc:h2:mem:practice;DB_CLOSE_DELAY=-1";

    private Connection mainConnection;
    private Connection practiceConnection;
    private boolean isInitialized = false;

    public void initialize() throws SQLException {
        log.info("Initializing embedded database system...");

        try {
            // Ensure data directory exists
            createDataDirectory();

            // Initialize main database (for user progress, settings, etc.)
            initializeMainDatabase();

            // Initialize practice database (for SQL exercises)
            initializePracticeDatabase();

            isInitialized = true;
            log.info("Database system initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize database", e);
            throw new SQLException("Database initialization failed", e);
        }
    }

    private void createDataDirectory() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                log.info("Created data directory: {}", dataDir.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to create data directory", e);
            throw new RuntimeException("Cannot create data directory", e);
        }
    }

    private void initializeMainDatabase() throws SQLException {
        log.info("Initializing main database...");
        mainConnection = DriverManager.getConnection(MAIN_DB_URL, "sa", "");

        // Check if main tables exist
        if (!mainTablesExist()) {
            log.info("Creating main database schema...");
            executeScript("/database/schema.sql", mainConnection);
            log.info("Main database schema created successfully");
        } else {
            log.info("Main database schema already exists");
        }
    }

    private void initializePracticeDatabase() throws SQLException {
        log.info("Initializing practice database...");
        practiceConnection = DriverManager.getConnection(PRACTICE_DB_URL, "sa", "");

        // Instead of using script files, use your DatabaseInitializer
        try {
            DatabaseInitializer initializer = new DatabaseInitializer(this);
            initializer.initializeSampleData();
            log.info("Practice database initialized using DatabaseInitializer");
        } catch (SQLException e) {
            log.error("Failed to initialize practice database with DatabaseInitializer", e);
            throw e;
        }
    }

    private boolean mainTablesExist() throws SQLException {
        DatabaseMetaData metaData = mainConnection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, "USER_PROGRESS", null)) {
            return rs.next();
        }
    }

    private void executeScript(String scriptPath, Connection connection) throws SQLException {
        try (InputStream is = getClass().getResourceAsStream(scriptPath)) {
            if (is == null) {
                throw new SQLException("Script not found: " + scriptPath);
            }

            String script = new Scanner(is, "UTF-8").useDelimiter("\\A").next();

            // Better script parsing
            String[] statements = script.split(";");

            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    // Clean the statement
                    statement = statement.trim();

                    // Skip empty statements and comments
                    if (statement.isEmpty() ||
                            statement.startsWith("--") ||
                            statement.matches("^\\s*--.*$")) {
                        continue;
                    }

                    // Remove single-line comments from the statement
                    String[] lines = statement.split("\n");
                    StringBuilder cleanStatement = new StringBuilder();
                    for (String line : lines) {
                        String cleanLine = line.trim();
                        if (!cleanLine.startsWith("--") && !cleanLine.isEmpty()) {
                            cleanStatement.append(cleanLine).append("\n");
                        }
                    }

                    String finalStatement = cleanStatement.toString().trim();
                    if (!finalStatement.isEmpty()) {
                        log.debug("Executing SQL: {}", finalStatement);
                        stmt.execute(finalStatement);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute script: {}", scriptPath, e);
            throw new SQLException("Failed to execute script: " + scriptPath, e);
        }
    }

    // Connection getters
    public Connection getMainConnection() {
        if (!isInitialized || mainConnection == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return mainConnection;
    }

    public Connection getPracticeConnection() {
        if (practiceConnection == null) {
            throw new IllegalStateException("Practice database connection not available");
        }
        return practiceConnection;
    }

    // Practice database management
    public void resetPracticeDatabase() throws SQLException {
        log.info("Resetting practice database...");

        if (practiceConnection != null && !practiceConnection.isClosed()) {
            practiceConnection.close();
        }

        // Recreate practice database
        initializePracticeDatabase();
        log.info("Practice database reset completed");
    }

    // Database information methods
    public boolean isConnected() {
        try {
            return mainConnection != null && !mainConnection.isClosed() &&
                    practiceConnection != null && !practiceConnection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public String getMainDatabaseInfo() throws SQLException {
        DatabaseMetaData metaData = mainConnection.getMetaData();
        return String.format("H2 Database %s (Main)", metaData.getDatabaseProductVersion());
    }

    public String getPracticeDatabaseInfo() throws SQLException {
        DatabaseMetaData metaData = practiceConnection.getMetaData();
        return String.format("H2 Database %s (Practice)", metaData.getDatabaseProductVersion());
    }

    // Graceful shutdown
    public void close() {
        log.info("Closing database connections...");

        try {
            if (practiceConnection != null && !practiceConnection.isClosed()) {
                practiceConnection.close();
                log.debug("Practice database connection closed");
            }
        } catch (SQLException e) {
            log.error("Error closing practice connection", e);
        }

        try {
            if (mainConnection != null && !mainConnection.isClosed()) {
                mainConnection.close();
                log.debug("Main database connection closed");
            }
        } catch (SQLException e) {
            log.error("Error closing main connection", e);
        }

        isInitialized = false;
        log.info("Database shutdown completed");
    }
}