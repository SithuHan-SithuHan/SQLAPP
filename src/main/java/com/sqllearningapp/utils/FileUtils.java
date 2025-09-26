package com.sqllearningapp.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Enhanced File Utilities for SQL Learning Application
 * Handles file operations, backups, and data management
 */
@Slf4j
public class FileUtils {

    private static final String APP_DATA_DIR = "data";
    private static final String BACKUP_DIR = "backups";
    private static final String EXPORTS_DIR = "exports";
    private static final String LOGS_DIR = "logs";
    private static final String TEMP_DIR = "temp";

    // Current user and timestamp for file operations
    private static final String CURRENT_USER = "SithuHan-SithuHan";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    static {
        initializeDirectories();
    }

    private static void initializeDirectories() {
        try {
            createDirectoryIfNotExists(APP_DATA_DIR);
            createDirectoryIfNotExists(BACKUP_DIR);
            createDirectoryIfNotExists(EXPORTS_DIR);
            createDirectoryIfNotExists(LOGS_DIR);
            createDirectoryIfNotExists(TEMP_DIR);

            log.info("File system directories initialized for user: {}", CURRENT_USER);
        } catch (IOException e) {
            log.error("Failed to initialize directories", e);
        }
    }

    // ===== DIRECTORY OPERATIONS =====

    public static void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.debug("Created directory: {}", path.toAbsolutePath());
        }
    }

    public static boolean directoryExists(String dirPath) {
        return Files.exists(Paths.get(dirPath)) && Files.isDirectory(Paths.get(dirPath));
    }

    public static long getDirectorySize(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) return 0;

        try (Stream<Path> files = Files.walk(path)) {
            return files
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
        }
    }

    // ===== FILE OPERATIONS =====

    public static String readFile(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    public static List<String> readLines(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    public static void writeFile(String filePath, String content) throws IOException {
        writeFile(filePath, content, false);
    }

    public static void writeFile(String filePath, String content, boolean append) throws IOException {
        Path path = Paths.get(filePath);

        // Create parent directories if they don't exist
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        OpenOption[] options = append ?
                new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND} :
                new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

        Files.writeString(path, content, StandardCharsets.UTF_8, options);

        log.debug("File {} by user {}: {}", append ? "appended to" : "written",
                CURRENT_USER, path.toAbsolutePath());
    }

    public static void writeLines(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Files.write(path, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath)) && Files.isRegularFile(Paths.get(filePath));
    }

    public static void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.debug("File deleted by {}: {}", CURRENT_USER, path.toAbsolutePath());
        }
    }

    public static void copyFile(String sourcePath, String destinationPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);

        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }

        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        log.debug("File copied by {}: {} -> {}", CURRENT_USER,
                source.toAbsolutePath(), destination.toAbsolutePath());
    }

    public static void moveFile(String sourcePath, String destinationPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);

        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }

        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        log.debug("File moved by {}: {} -> {}", CURRENT_USER,
                source.toAbsolutePath(), destination.toAbsolutePath());
    }

    // ===== BACKUP OPERATIONS =====

    public static String createBackup(String sourceFile) throws IOException {
        return createBackup(sourceFile, null);
    }

    public static String createBackup(String sourceFile, String description) throws IOException {
        Path source = Paths.get(sourceFile);
        if (!Files.exists(source)) {
            throw new FileNotFoundException("Source file not found: " + sourceFile);
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String fileName = source.getFileName().toString();
        String backupName = String.format("%s_%s_%s.bak",
                fileName, CURRENT_USER, timestamp);

        Path backupPath = Paths.get(BACKUP_DIR, backupName);
        Files.copy(source, backupPath, StandardCopyOption.REPLACE_EXISTING);

        // Create backup metadata
        String metadataContent = String.format("""
            # Backup Metadata
            Original File: %s
            Backup Created: %s
            Created By: %s
            Description: %s
            File Size: %d bytes
            """,
                source.toAbsolutePath(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                CURRENT_USER,
                description != null ? description : "Automatic backup",
                Files.size(source)
        );

        writeFile(backupPath.toString() + ".meta", metadataContent);

        log.info("Backup created by {}: {}", CURRENT_USER, backupPath.toAbsolutePath());
        return backupPath.toString();
    }

    public static void cleanupOldBackups(int retentionDays) throws IOException {
        Path backupDir = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupDir)) return;

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        try (Stream<Path> files = Files.list(backupDir)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant()
                                    .isBefore(java.time.Instant.from(cutoffDate.atZone(java.time.ZoneId.systemDefault())));
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted old backup: {}", path.getFileName());
                        } catch (IOException e) {
                            log.warn("Failed to delete old backup: {}", path.getFileName());
                        }
                    });
        }

        log.info("Backup cleanup completed by {} (retention: {} days)", CURRENT_USER, retentionDays);
    }

    // ===== EXPORT OPERATIONS =====

    public static String exportData(String data, String filename, ExportFormat format) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String exportFileName = String.format("%s_%s_%s.%s",
                filename, CURRENT_USER, timestamp, format.getExtension());

        Path exportPath = Paths.get(EXPORTS_DIR, exportFileName);

        switch (format) {
            case CSV:
                writeFile(exportPath.toString(), data);
                break;
            case JSON:
                writeFile(exportPath.toString(), data);
                break;
            case XML:
                writeFile(exportPath.toString(), data);
                break;
            case TXT:
                writeFile(exportPath.toString(), data);
                break;
        }

        log.info("Data exported by {} in {} format: {}",
                CURRENT_USER, format, exportPath.toAbsolutePath());

        return exportPath.toString();
    }

    public static String createZipArchive(List<String> filePaths, String archiveName) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String zipFileName = String.format("%s_%s_%s.zip",
                archiveName, CURRENT_USER, timestamp);

        Path zipPath = Paths.get(EXPORTS_DIR, zipFileName);

        try (ZipOutputStream zos = new ZipOutputStream(
                Files.newOutputStream(zipPath, StandardOpenOption.CREATE))) {

            for (String filePath : filePaths) {
                Path file = Paths.get(filePath);
                if (Files.exists(file)) {
                    ZipEntry entry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(entry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        }

        log.info("ZIP archive created by {}: {} ({} files)",
                CURRENT_USER, zipPath.toAbsolutePath(), filePaths.size());

        return zipPath.toString();
    }

    // ===== TEMPORARY FILE OPERATIONS =====

    public static String createTempFile(String prefix, String suffix) throws IOException {
        String tempFileName = String.format("%s_%s_%s%s",
                prefix, CURRENT_USER, System.currentTimeMillis(), suffix);

        Path tempPath = Paths.get(TEMP_DIR, tempFileName);
        Files.createFile(tempPath);

        // Schedule for deletion on JVM exit
        tempPath.toFile().deleteOnExit();

        log.debug("Temporary file created by {}: {}", CURRENT_USER, tempPath.toAbsolutePath());
        return tempPath.toString();
    }

    public static void cleanupTempFiles() throws IOException {
        Path tempDir = Paths.get(TEMP_DIR);
        if (!Files.exists(tempDir)) return;

        try (Stream<Path> files = Files.list(tempDir)) {
            files.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted temp file: {}", path.getFileName());
                        } catch (IOException e) {
                            log.debug("Could not delete temp file: {}", path.getFileName());
                        }
                    });
        }

        log.info("Temporary files cleanup completed by {}", CURRENT_USER);
    }

    // ===== LOGGING OPERATIONS =====

    public static void writeLogEntry(String logLevel, String component, String message) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String logFileName = String.format("sqllearning_%s.log",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        String logEntry = String.format("[%s] [%s] [%s] [%s] %s%n",
                timestamp, logLevel, component, CURRENT_USER, message);

        Path logPath = Paths.get(LOGS_DIR, logFileName);
        writeFile(logPath.toString(), logEntry, true);
    }

    public static List<String> getLogEntries(String date) throws IOException {
        String logFileName = String.format("sqllearning_%s.log", date);
        Path logPath = Paths.get(LOGS_DIR, logFileName);

        if (Files.exists(logPath)) {
            return readLines(logPath.toString());
        } else {
            return List.of();
        }
    }

    // ===== FILE SYSTEM INFO =====

    public static FileSystemInfo getFileSystemInfo() {
        try {
            Path currentDir = Paths.get(".");
            FileStore store = Files.getFileStore(currentDir);

            return FileSystemInfo.builder()
                    .totalSpace(store.getTotalSpace())
                    .freeSpace(store.getUsableSpace())
                    .usedSpace(store.getTotalSpace() - store.getUsableSpace())
                    .appDataSize(directoryExists(APP_DATA_DIR) ? getDirectorySize(APP_DATA_DIR) : 0)
                    .backupSize(directoryExists(BACKUP_DIR) ? getDirectorySize(BACKUP_DIR) : 0)
                    .exportSize(directoryExists(EXPORTS_DIR) ? getDirectorySize(EXPORTS_DIR) : 0)
                    .logSize(directoryExists(LOGS_DIR) ? getDirectorySize(LOGS_DIR) : 0)
                    .tempSize(directoryExists(TEMP_DIR) ? getDirectorySize(TEMP_DIR) : 0)
                    .currentUser(CURRENT_USER)
                    .lastChecked(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            log.error("Failed to get file system info", e);
            return FileSystemInfo.builder()
                    .currentUser(CURRENT_USER)
                    .lastChecked(LocalDateTime.now())
                    .build();
        }
    }

    // ===== UTILITY METHODS =====

    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    public static String sanitizeFileName(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // ===== ENUMS AND DATA CLASSES =====

    public enum ExportFormat {
        CSV("csv"),
        JSON("json"),
        XML("xml"),
        TXT("txt");

        private final String extension;

        ExportFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class FileSystemInfo {
        private long totalSpace;
        private long freeSpace;
        private long usedSpace;
        private long appDataSize;
        private long backupSize;
        private long exportSize;
        private long logSize;
        private long tempSize;
        private String currentUser;
        private LocalDateTime lastChecked;

        public double getFreeSpacePercent() {
            return totalSpace > 0 ? (double) freeSpace / totalSpace * 100 : 0;
        }

        public double getUsedSpacePercent() {
            return totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
        }

        public long getTotalAppSize() {
            return appDataSize + backupSize + exportSize + logSize + tempSize;
        }
    }
}