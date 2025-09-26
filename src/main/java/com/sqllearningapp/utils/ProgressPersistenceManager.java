package com.sqllearningapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sqllearningapp.core.models.UserProgress;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Progress Persistence Manager - Enhanced progress data management
 * Created: 2025-09-26 05:14:01 UTC
 * User: SithuHan-SithuHan
 * Repository: https://github.com/SithuHan-SithuHan/SQL_Learning_APP
 */
@Slf4j
public class ProgressPersistenceManager {

    private static final String PROGRESS_DIR = "progress";
    private static final String PROGRESS_FILE = "user_progress_v2.json";
    private static final String BACKUP_DIR = "progress/backups";
    private static final String EXPORT_DIR = "exports";
    private static final int MAX_BACKUPS = 10;

    private final ObjectMapper objectMapper;
    private final String userId;
    private final Path progressPath;
    private final Path backupPath;
    private final Path exportPath;

    public ProgressPersistenceManager() {
        this("SithuHan-SithuHan"); // Default to current user
    }

    public ProgressPersistenceManager(String userId) {
        this.userId = userId;
        this.objectMapper = createObjectMapper();
        this.progressPath = Paths.get(PROGRESS_DIR);
        this.backupPath = Paths.get(BACKUP_DIR);
        this.exportPath = Paths.get(EXPORT_DIR);

        initializeDirectories();

        log.info("Progress persistence manager initialized for user: {} at {}",
                userId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(progressPath);
            Files.createDirectories(backupPath);
            Files.createDirectories(exportPath);

            log.debug("Created directories: progress={}, backup={}, export={}",
                    progressPath, backupPath, exportPath);

        } catch (IOException e) {
            log.error("Failed to create progress directories", e);
            throw new RuntimeException("Cannot initialize progress persistence", e);
        }
    }

    // ===== CORE PERSISTENCE METHODS =====

    /**
     * Save user progress with automatic backup
     */
    public void saveProgress(UserProgress progress) {
        if (progress == null) {
            log.warn("Attempted to save null progress");
            return;
        }

        File progressFile = getProgressFile();

        try {
            // Create backup if file exists
            if (progressFile.exists()) {
                createBackup(progressFile);
            }

            // Update progress metadata
            progress.setUserId(userId);
            progress.setLastUpdated(LocalDateTime.now());

            // Save progress
            objectMapper.writeValue(progressFile, progress);

            log.debug("Progress saved for user: {} ({} completed questions)",
                    userId, progress.getCompletedQuestions().size());

        } catch (IOException e) {
            log.error("Failed to save progress for user: {}", userId, e);
            throw new RuntimeException("Progress save failed", e);
        }
    }

    /**
     * Load user progress with fallback handling
     */
    public UserProgress loadProgress() {
        File progressFile = getProgressFile();

        if (!progressFile.exists()) {
            log.info("No existing progress file found, creating new progress for user: {}", userId);
            return createDefaultProgress();
        }

        try {
            UserProgress progress = objectMapper.readValue(progressFile, UserProgress.class);

            // Validate and migrate if necessary
            progress = validateAndMigrate(progress);

            log.info("Progress loaded for user: {} (last updated: {})",
                    userId, progress.getLastUpdated());

            return progress;

        } catch (IOException e) {
            log.error("Failed to load progress, attempting backup recovery for user: {}", userId, e);
            return loadFromBackup();
        }
    }

    /**
     * Export progress to specified format
     */
    public File exportProgress(UserProgress progress, ExportFormat format) throws IOException {
        if (progress == null) {
            throw new IllegalArgumentException("Cannot export null progress");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("progress_export_%s_%s.%s",
                userId, timestamp, format.getExtension());

        File exportFile = exportPath.resolve(filename).toFile();

        switch (format) {
            case JSON -> exportToJson(progress, exportFile);
            case CSV -> exportToCsv(progress, exportFile);
            case HTML -> exportToHtml(progress, exportFile);
            case XML -> exportToXml(progress, exportFile);
        }

        log.info("Progress exported to: {} (format: {})", exportFile.getAbsolutePath(), format);
        return exportFile;
    }

    /**
     * Import progress from file
     */
    public UserProgress importProgress(File importFile, ImportOptions options) throws IOException {
        if (!importFile.exists()) {
            throw new IllegalArgumentException("Import file does not exist: " + importFile.getPath());
        }

        UserProgress imported;
        String extension = getFileExtension(importFile.getName()).toLowerCase();

        switch (extension) {
            case "json" -> imported = importFromJson(importFile);
            default -> throw new UnsupportedOperationException("Import format not supported: " + extension);
        }

        // Apply import options
        if (options.isMergeWithExisting()) {
            UserProgress existing = loadProgress();
            imported = mergeProgress(existing, imported, options);
        }

        // Validate imported data
        imported = validateAndMigrate(imported);

        // Create backup before importing
        if (options.isCreateBackupBeforeImport()) {
            File currentFile = getProgressFile();
            if (currentFile.exists()) {
                createBackup(currentFile);
            }
        }

        // Save imported progress
        saveProgress(imported);

        log.info("Progress imported from: {} for user: {}", importFile.getName(), userId);
        return imported;
    }

    // ===== BACKUP MANAGEMENT =====

    private void createBackup(File originalFile) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFilename = String.format("progress_backup_%s_%s.json", userId, timestamp);
            Path backupFile = backupPath.resolve(backupFilename);

            Files.copy(originalFile.toPath(), backupFile, StandardCopyOption.REPLACE_EXISTING);

            // Cleanup old backups
            cleanupOldBackups();

            log.debug("Created backup: {}", backupFile.getFileName());

        } catch (IOException e) {
            log.warn("Failed to create backup", e);
        }
    }

    private void cleanupOldBackups() {
        try {
            List<Path> backupFiles = Files.list(backupPath)
                    .filter(path -> path.getFileName().toString().startsWith("progress_backup_" + userId))
                    .sorted((p1, p2) -> p2.toFile().getName().compareTo(p1.toFile().getName())) // Newest first
                    .toList();

            if (backupFiles.size() > MAX_BACKUPS) {
                for (int i = MAX_BACKUPS; i < backupFiles.size(); i++) {
                    Files.deleteIfExists(backupFiles.get(i));
                    log.debug("Deleted old backup: {}", backupFiles.get(i).getFileName());
                }
            }

        } catch (IOException e) {
            log.warn("Failed to cleanup old backups", e);
        }
    }

    private UserProgress loadFromBackup() {
        try {
            List<Path> backupFiles = Files.list(backupPath)
                    .filter(path -> path.getFileName().toString().startsWith("progress_backup_" + userId))
                    .sorted((p1, p2) -> p2.toFile().getName().compareTo(p1.toFile().getName())) // Newest first
                    .toList();

            for (Path backupFile : backupFiles) {
                try {
                    UserProgress progress = objectMapper.readValue(backupFile.toFile(), UserProgress.class);
                    log.info("Successfully recovered progress from backup: {}", backupFile.getFileName());
                    return progress;

                } catch (IOException e) {
                    log.warn("Failed to load backup: {}", backupFile.getFileName());
                }
            }

        } catch (IOException e) {
            log.error("Failed to list backup files", e);
        }

        log.warn("No valid backups found, creating default progress");
        return createDefaultProgress();
    }

    // ===== EXPORT METHODS =====

    private void exportToJson(UserProgress progress, File exportFile) throws IOException {
        // Add export metadata
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("metadata", createExportMetadata());
        exportData.put("progress", progress);

        objectMapper.writeValue(exportFile, exportData);
    }

    private void exportToCsv(UserProgress progress, File exportFile) throws IOException {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Export Type,User Progress Export\n");
        csv.append("User ID,").append(progress.getUserId()).append("\n");
        csv.append("Export Date,").append(LocalDateTime.now()).append("\n");
        csv.append("Export By,SithuHan-SithuHan\n");
        csv.append("Repository,https://github.com/SithuHan-SithuHan/SQL_Learning_APP\n\n");

        // Completed Questions
        csv.append("Completed Questions\n");
        csv.append("Question ID,Completion Status\n");
        for (String questionId : progress.getCompletedQuestions()) {
            csv.append(questionId).append(",Completed\n");
        }

        csv.append("\nViewed Topics\n");
        csv.append("Topic Name,View Status\n");
        for (String topic : progress.getViewedTopics()) {
            csv.append(topic).append(",Viewed\n");
        }

        // Statistics
        csv.append("\nStatistics\n");
        csv.append("Metric,Value\n");
        UserProgress.Statistics stats = progress.getStatistics();
        csv.append("Total Queries Executed,").append(stats.getTotalQueriesExecuted()).append("\n");
        csv.append("Successful Queries,").append(stats.getSuccessfulQueries()).append("\n");
        csv.append("Current Streak,").append(stats.getCurrentStreak()).append("\n");
        csv.append("Best Streak,").append(stats.getBestStreak()).append("\n");
        csv.append("Total Points,").append(stats.getTotalPointsEarned()).append("\n");

        Files.writeString(exportFile.toPath(), csv.toString());
    }

    private void exportToHtml(UserProgress progress, File exportFile) throws IOException {
        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>SQL Learning Progress Report - %s</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; line-height: 1.6; }
                    .header { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; padding: 30px; border-radius: 12px; margin-bottom: 30px; }
                    .header h1 { margin: 0; font-size: 2.5em; }
                    .header p { margin: 10px 0 0 0; opacity: 0.9; }
                    .section { background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .section h2 { color: #2563eb; margin-top: 0; }
                    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; }
                    .stat-card { background: #f8fafc; padding: 20px; border-radius: 8px; text-align: center; }
                    .stat-number { font-size: 2em; font-weight: bold; color: #2563eb; }
                    .stat-label { color: #64748b; margin-top: 5px; }
                    .progress-list { list-style: none; padding: 0; }
                    .progress-list li { padding: 8px 0; border-bottom: 1px solid #e2e8f0; }
                    .completed { color: #059669; }
                    .footer { text-align: center; margin-top: 40px; color: #64748b; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>📊 SQL Learning Progress Report</h1>
                    <p>User: %s | Generated: %s</p>
                    <p>Repository: <a href="https://github.com/SithuHan-SithuHan/SQL_Learning_APP" style="color: white;">SQL_Learning_APP</a></p>
                </div>
                
                <div class="section">
                    <h2>📈 Statistics Overview</h2>
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-number">%d</div>
                            <div class="stat-label">Completed Questions</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">%d</div>
                            <div class="stat-label">Topics Viewed</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">%d</div>
                            <div class="stat-label">Total Queries</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">%.1f%%</div>
                            <div class="stat-label">Success Rate</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">%d</div>
                            <div class="stat-label">Current Streak</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">%d</div>
                            <div class="stat-label">Total Points</div>
                        </div>
                    </div>
                </div>
                
                <div class="section">
                    <h2>✅ Completed Questions</h2>
                    <ul class="progress-list">
                        %s
                    </ul>
                </div>
                
                <div class="section">
                    <h2>📚 Viewed Topics</h2>
                    <ul class="progress-list">
                        %s
                    </ul>
                </div>
                
                <div class="footer">
                    <p>Generated by SQL Learning Professional Edition v2.0</p>
                    <p>Created: 2025-09-26 05:14:01 UTC | User: SithuHan-SithuHan</p>
                </div>
            </body>
            </html>
            """,
                progress.getUserId(),
                progress.getUserId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                progress.getCompletedQuestions().size(),
                progress.getViewedTopics().size(),
                progress.getStatistics().getTotalQueriesExecuted(),
                progress.getStatistics().getSuccessRate(),
                progress.getStatistics().getCurrentStreak(),
                progress.getStatistics().getTotalPointsEarned(),
                progress.getCompletedQuestions().stream()
                        .map(q -> "<li class=\"completed\">✅ " + q + "</li>")
                        .reduce("", String::concat),
                progress.getViewedTopics().stream()
                        .map(t -> "<li class=\"completed\">📖 " + t + "</li>")
                        .reduce("", String::concat)
        );

        Files.writeString(exportFile.toPath(), html);
    }

    private void exportToXml(UserProgress progress, File exportFile) throws IOException {
        // Basic XML export implementation
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<progress-export>\n");
        xml.append("  <metadata>\n");
        xml.append("    <user-id>").append(progress.getUserId()).append("</user-id>\n");
        xml.append("    <export-date>").append(LocalDateTime.now()).append("</export-date>\n");
        xml.append("    <exported-by>SithuHan-SithuHan</exported-by>\n");
        xml.append("    <repository>https://github.com/SithuHan-SithuHan/SQL_Learning_APP</repository>\n");
        xml.append("  </metadata>\n");
        xml.append("  <completed-questions>\n");
        for (String question : progress.getCompletedQuestions()) {
            xml.append("    <question>").append(question).append("</question>\n");
        }
        xml.append("  </completed-questions>\n");
        xml.append("  <viewed-topics>\n");
        for (String topic : progress.getViewedTopics()) {
            xml.append("    <topic>").append(topic).append("</topic>\n");
        }
        xml.append("  </viewed-topics>\n");
        xml.append("</progress-export>\n");

        Files.writeString(exportFile.toPath(), xml.toString());
    }

    // ===== IMPORT METHODS =====

    private UserProgress importFromJson(File importFile) throws IOException {
        Map<String, Object> importData = objectMapper.readValue(importFile, Map.class);

        if (importData.containsKey("progress")) {
            // New format with metadata
            return objectMapper.convertValue(importData.get("progress"), UserProgress.class);
        } else {
            // Legacy format
            return objectMapper.readValue(importFile, UserProgress.class);
        }
    }

    // ===== UTILITY METHODS =====

    private UserProgress createDefaultProgress() {
        return UserProgress.builder()
                .userId(userId)
                .completedQuestions(ConcurrentHashMap.newKeySet())
                .viewedTopics(ConcurrentHashMap.newKeySet())
                .questionAttempts(new ConcurrentHashMap<>())
                .lastAttempted(new ConcurrentHashMap<>())
                .statistics(UserProgress.Statistics.builder()
                        .totalQueriesExecuted(0)
                        .successfulQueries(0)
                        .currentStreak(0)
                        .bestStreak(0)
                        .totalTimeSpentMs(0)
                        .totalPointsEarned(0)
                        .categoryProgress(new ConcurrentHashMap<>())
                        .build())
                .achievements(UserProgress.Achievements.builder()
                        .unlockedBadges(ConcurrentHashMap.newKeySet())
                        .level(1)
                        .experiencePoints(0)
                        .badgeEarnedDates(new ConcurrentHashMap<>())
                        .build())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private UserProgress validateAndMigrate(UserProgress progress) {
        if (progress == null) {
            return createDefaultProgress();
        }

        // Ensure user ID is set
        if (progress.getUserId() == null || progress.getUserId().isEmpty()) {
            progress.setUserId(userId);
        }

        // Initialize collections if null
        if (progress.getCompletedQuestions() == null) {
            progress.setCompletedQuestions(ConcurrentHashMap.newKeySet());
        }
        if (progress.getViewedTopics() == null) {
            progress.setViewedTopics(ConcurrentHashMap.newKeySet());
        }
        if (progress.getQuestionAttempts() == null) {
            progress.setQuestionAttempts(new ConcurrentHashMap<>());
        }
        if (progress.getLastAttempted() == null) {
            progress.setLastAttempted(new ConcurrentHashMap<>());
        }

        // Initialize statistics if null
        if (progress.getStatistics() == null) {
            progress.setStatistics(UserProgress.Statistics.builder()
                    .totalQueriesExecuted(0)
                    .successfulQueries(0)
                    .currentStreak(0)
                    .bestStreak(0)
                    .totalTimeSpentMs(0)
                    .totalPointsEarned(0)
                    .categoryProgress(new ConcurrentHashMap<>())
                    .build());
        }

        // Initialize achievements if null
        if (progress.getAchievements() == null) {
            progress.setAchievements(UserProgress.Achievements.builder()
                    .unlockedBadges(ConcurrentHashMap.newKeySet())
                    .level(1)
                    .experiencePoints(0)
                    .badgeEarnedDates(new ConcurrentHashMap<>())
                    .build());
        }

        return progress;
    }

    private UserProgress mergeProgress(UserProgress existing, UserProgress imported, ImportOptions options) {
        UserProgress merged = existing;

        if (options.isMergeCompletedQuestions()) {
            merged.getCompletedQuestions().addAll(imported.getCompletedQuestions());
        }

        if (options.isMergeViewedTopics()) {
            merged.getViewedTopics().addAll(imported.getViewedTopics());
        }

        if (options.isMergeStatistics()) {
            // Merge statistics (take the higher values)
            UserProgress.Statistics existingStats = merged.getStatistics();
            UserProgress.Statistics importedStats = imported.getStatistics();

            existingStats.setTotalQueriesExecuted(
                    Math.max(existingStats.getTotalQueriesExecuted(), importedStats.getTotalQueriesExecuted()));
            existingStats.setSuccessfulQueries(
                    Math.max(existingStats.getSuccessfulQueries(), importedStats.getSuccessfulQueries()));
            existingStats.setBestStreak(
                    Math.max(existingStats.getBestStreak(), importedStats.getBestStreak()));
            existingStats.setTotalPointsEarned(
                    Math.max(existingStats.getTotalPointsEarned(), importedStats.getTotalPointsEarned()));
        }

        return merged;
    }

    private Map<String, Object> createExportMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exportDate", LocalDateTime.now());
        metadata.put("exportedBy", "SithuHan-SithuHan");
        metadata.put("applicationVersion", "2.0.0");
        metadata.put("repository", "https://github.com/SithuHan-SithuHan/SQL_Learning_APP");
        metadata.put("exportFormat", "JSON");
        metadata.put("exportTimestamp", "2025-09-26 05:14:01 UTC");
        return metadata;
    }

    private File getProgressFile() {
        return progressPath.resolve(PROGRESS_FILE).toFile();
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    // ===== PUBLIC UTILITY METHODS =====

    public List<File> getAvailableBackups() {
        try {
            return Files.list(backupPath)
                    .filter(path -> path.getFileName().toString().startsWith("progress_backup_" + userId))
                    .map(Path::toFile)
                    .sorted((f1, f2) -> f2.getName().compareTo(f1.getName())) // Newest first
                    .toList();
        } catch (IOException e) {
            log.error("Failed to list backups", e);
            return new ArrayList<>();
        }
    }

    public boolean hasExistingProgress() {
        return getProgressFile().exists();
    }

    public long getProgressFileSize() {
        File file = getProgressFile();
        return file.exists() ? file.length() : 0;
    }

    public LocalDateTime getLastModifiedTime() {
        File file = getProgressFile();
        if (file.exists()) {
            return LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(file.lastModified()),
                    java.time.ZoneId.systemDefault());
        }
        return null;
    }

    // ===== ENUMS AND INNER CLASSES =====

    public enum ExportFormat {
        JSON("json"),
        CSV("csv"),
        HTML("html"),
        XML("xml");

        private final String extension;

        ExportFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    public static class ImportOptions {
        private boolean mergeWithExisting = false;
        private boolean createBackupBeforeImport = true;
        private boolean mergeCompletedQuestions = true;
        private boolean mergeViewedTopics = true;
        private boolean mergeStatistics = false;
        private boolean overwriteAchievements = false;

        // Getters and setters
        public boolean isMergeWithExisting() { return mergeWithExisting; }
        public void setMergeWithExisting(boolean mergeWithExisting) { this.mergeWithExisting = mergeWithExisting; }

        public boolean isCreateBackupBeforeImport() { return createBackupBeforeImport; }
        public void setCreateBackupBeforeImport(boolean createBackupBeforeImport) { this.createBackupBeforeImport = createBackupBeforeImport; }

        public boolean isMergeCompletedQuestions() { return mergeCompletedQuestions; }
        public void setMergeCompletedQuestions(boolean mergeCompletedQuestions) { this.mergeCompletedQuestions = mergeCompletedQuestions; }

        public boolean isMergeViewedTopics() { return mergeViewedTopics; }
        public void setMergeViewedTopics(boolean mergeViewedTopics) { this.mergeViewedTopics = mergeViewedTopics; }

        public boolean isMergeStatistics() { return mergeStatistics; }
        public void setMergeStatistics(boolean mergeStatistics) { this.mergeStatistics = mergeStatistics; }

        public boolean isOverwriteAchievements() { return overwriteAchievements; }
        public void setOverwriteAchievements(boolean overwriteAchievements) { this.overwriteAchievements = overwriteAchievements; }
    }
}