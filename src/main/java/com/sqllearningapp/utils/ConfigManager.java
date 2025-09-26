package com.sqllearningapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Configuration Manager for SQL Learning Application
 * Manages application settings, preferences, and user configuration
 */
@Slf4j
public class ConfigManager {

    private static final String CONFIG_FILE = "app_config.json";
    private static final String CONFIG_VERSION = "2.0";

    private final ObjectMapper objectMapper;
    private AppConfig config;

    public ConfigManager() {
        this.objectMapper = createObjectMapper();
        loadConfiguration();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public void loadConfiguration() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                config = objectMapper.readValue(configFile, AppConfig.class);
                log.info("Configuration loaded from: {}", CONFIG_FILE);

                // Migrate configuration if needed
                if (!CONFIG_VERSION.equals(config.getConfigVersion())) {
                    migrateConfiguration();
                }
            } else {
                config = createDefaultConfig();
                log.info("Created default configuration");
            }

            // Update last access
            config.setLastAccessed(LocalDateTime.now());

        } catch (IOException e) {
            log.error("Failed to load configuration, using defaults", e);
            config = createDefaultConfig();
        }
    }

    public void saveConfiguration() {
        try {
            config.setLastSaved(LocalDateTime.now());
            config.setConfigVersion(CONFIG_VERSION);

            objectMapper.writeValue(new File(CONFIG_FILE), config);
            log.debug("Configuration saved successfully");

        } catch (IOException e) {
            log.error("Failed to save configuration", e);
        }
    }

    private AppConfig createDefaultConfig() {
        AppConfig defaultConfig = new AppConfig();

        // Application info with current user and date
        defaultConfig.setConfigVersion(CONFIG_VERSION);
        defaultConfig.setCreatedDate(LocalDateTime.now());
        defaultConfig.setLastAccessed(LocalDateTime.now());
        defaultConfig.setLastSaved(LocalDateTime.now());

        // User information
        defaultConfig.setUserLogin("SithuHan-SithuHan");
        defaultConfig.setGitHubRepository("https://github.com/SithuHan-SithuHan/SQL_Learning_APP");

        // Window settings
        defaultConfig.setWindowWidth(1200);
        defaultConfig.setWindowHeight(800);
        defaultConfig.setMaximized(true);
        defaultConfig.setAlwaysOnTop(false);

        // UI preferences
        defaultConfig.setTheme("modern-light");
        defaultConfig.setFontSize(14);
        defaultConfig.setShowLineNumbers(true);
        defaultConfig.setWordWrap(false);
        defaultConfig.setAutoSave(true);
        defaultConfig.setAutoIndent(true);

        // Database settings
        defaultConfig.setAutoResetDatabase(false);
        defaultConfig.setQueryTimeout(30);
        defaultConfig.setMaxResultRows(1000);

        // Learning preferences
        defaultConfig.setAutoAdvanceTopics(false);
        defaultConfig.setShowHints(true);
        defaultConfig.setTrackProgress(true);
        defaultConfig.setPlaySound(false); // Fixed: changed from setPlaySounds() to setPlaySound()

        // Developer settings
        defaultConfig.setDebugMode(false);
        defaultConfig.setShowExecutionTime(true);
        defaultConfig.setLogQueries(true);
        defaultConfig.setEnableMetrics(true);

        // Advanced settings
        defaultConfig.setCustomSettings(new HashMap<>());
        defaultConfig.getCustomSettings().put("export_format", "csv");
        defaultConfig.getCustomSettings().put("date_format", "yyyy-MM-dd HH:mm:ss");
        defaultConfig.getCustomSettings().put("backup_enabled", "true");

        return defaultConfig;
    }

    private void migrateConfiguration() {
        log.info("Migrating configuration from version {} to {}",
                config.getConfigVersion(), CONFIG_VERSION);

        // Add migration logic here as the application evolves
        switch (config.getConfigVersion()) {
            case "1.0":
                // Migrate from v1.0 to v2.0
                if (config.getGitHubRepository() == null) {
                    config.setGitHubRepository("https://github.com/SithuHan-SithuHan/SQL_Learning_APP");
                }
                if (config.getUserLogin() == null) {
                    config.setUserLogin("SithuHan-SithuHan");
                }
                break;
            default:
                log.warn("Unknown configuration version: {}", config.getConfigVersion());
        }

        config.setConfigVersion(CONFIG_VERSION);
        saveConfiguration();
        log.info("Configuration migration completed");
    }

    // ===== GETTER METHODS =====

    public AppConfig getConfig() {
        return config;
    }

    // ===== CONVENIENCE METHODS =====

    public String getUserLogin() {
        return config.getUserLogin();
    }

    public String getGitHubRepository() {
        return config.getGitHubRepository();
    }

    public String getTheme() {
        return config.getTheme();
    }

    public void setTheme(String theme) {
        config.setTheme(theme);
        saveConfiguration();
    }

    public int getFontSize() {
        return config.getFontSize();
    }

    public void setFontSize(int fontSize) {
        config.setFontSize(Math.max(8, Math.min(72, fontSize)));
        saveConfiguration();
    }

    public boolean isDebugMode() {
        return config.isDebugMode();
    }

    public void setDebugMode(boolean debugMode) {
        config.setDebugMode(debugMode);
        saveConfiguration();
        log.info("Debug mode {}", debugMode ? "enabled" : "disabled");
    }

    public String getCustomSetting(String key) {
        return config.getCustomSettings().get(key);
    }

    public void setCustomSetting(String key, String value) {
        config.getCustomSettings().put(key, value);
        saveConfiguration();
    }

    // Add convenience method for sound settings
    public boolean isPlaySound() {
        return config.isPlaySound();
    }

    public void setPlaySound(boolean playSound) {
        config.setPlaySound(playSound);
        saveConfiguration();
    }

    // ===== STATISTICS =====

    public void incrementUsageStat(String statName) {
        Map<String, Integer> usageStats = config.getUsageStats();
        if (usageStats == null) {
            usageStats = new HashMap<>();
            config.setUsageStats(usageStats);
        }

        usageStats.put(statName, usageStats.getOrDefault(statName, 0) + 1);

        // Save usage stats periodically (every 10 increments)
        int totalStats = usageStats.values().stream().mapToInt(Integer::intValue).sum();
        if (totalStats % 10 == 0) {
            saveConfiguration();
        }
    }

    public Map<String, Object> getConfigSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("configVersion", config.getConfigVersion());
        summary.put("userLogin", config.getUserLogin());
        summary.put("theme", config.getTheme());
        summary.put("fontSize", config.getFontSize());
        summary.put("debugMode", config.isDebugMode());
        summary.put("playSound", config.isPlaySound());
        summary.put("createdDate", config.getCreatedDate());
        summary.put("lastAccessed", config.getLastAccessed());
        summary.put("usageStats", config.getUsageStats());

        return summary;
    }

    // ===== CONFIGURATION DATA CLASS =====

    @Data
    public static class AppConfig {
        // Metadata
        private String configVersion;
        private LocalDateTime createdDate;
        private LocalDateTime lastAccessed;
        private LocalDateTime lastSaved;

        // User information (using current GitHub user)
        private String userLogin = "SithuHan-SithuHan";
        private String gitHubRepository = "https://github.com/SithuHan-SithuHan/SQL_Learning_APP";

        // Window settings
        private double windowWidth = 1200;
        private double windowHeight = 800;
        private boolean maximized = true;
        private boolean alwaysOnTop = false;
        private double windowX = -1;
        private double windowY = -1;

        // UI preferences
        private String theme = "modern-light";
        private int fontSize = 14;
        private boolean showLineNumbers = true;
        private boolean wordWrap = false;
        private boolean autoSave = true;
        private boolean autoIndent = true;
        private boolean showWelcome = true;

        // Database settings
        private boolean autoResetDatabase = false;
        private int queryTimeout = 30;
        private int maxResultRows = 1000;
        private boolean enableQueryLogging = true;

        // Learning preferences
        private boolean autoAdvanceTopics = false;
        private boolean showHints = true;
        private boolean trackProgress = true;
        private boolean playSound = false; // Note: field name is playSound (singular)
        private String learningMode = "guided"; // guided, free-form

        // Practice settings
        private String preferredDifficulty = "all";
        private boolean showSolutions = true;
        private boolean allowRetries = true;
        private int maxRetries = 3;

        // Developer settings
        private boolean debugMode = false;
        private boolean showExecutionTime = true;
        private boolean logQueries = true;
        private boolean enableMetrics = true;
        private boolean verboseLogging = false;

        // Export/Import settings
        private String exportFormat = "csv";
        private String exportDirectory = System.getProperty("user.home");
        private boolean autoBackup = false;
        private int backupRetentionDays = 30;

        // Advanced settings
        private Map<String, String> customSettings = new HashMap<>();
        private Map<String, Integer> usageStats = new HashMap<>();
        private Map<String, Object> experimentalFeatures = new HashMap<>();
    }
}