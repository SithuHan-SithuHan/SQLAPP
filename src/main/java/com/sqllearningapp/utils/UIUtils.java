package com.sqllearningapp.ui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * UI Utility Methods - Common dialog and UI helper functions
 * Updated with modern styling and user context
 */
public class UIUtils {

    // Standard message dialogs
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("⚠️ Warning");
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("❌ Error");
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅ Success");
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    // Confirmation dialogs
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("🤔 Confirmation Required");
        alert.setContentText(message);
        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean showConfirmation(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Input dialogs
    public static Optional<String> showTextInput(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText("📝 Input Required");
        dialog.setContentText(message);
        styleDialog(dialog);

        return dialog.showAndWait();
    }

    public static Optional<String> showTextInput(String title, String message) {
        return showTextInput(title, message, "");
    }

    // File choosers
    public static File showOpenFileDialog(Stage parent, String title, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if (extensions.length > 0) {
            for (int i = 0; i < extensions.length; i += 2) {
                if (i + 1 < extensions.length) {
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter(extensions[i], extensions[i + 1]));
                }
            }
        }

        return fileChooser.showOpenDialog(parent);
    }

    public static File showSaveFileDialog(Stage parent, String title, String defaultName, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if (defaultName != null) {
            fileChooser.setInitialFileName(defaultName);
        }

        if (extensions.length > 0) {
            for (int i = 0; i < extensions.length; i += 2) {
                if (i + 1 < extensions.length) {
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter(extensions[i], extensions[i + 1]));
                }
            }
        }

        return fileChooser.showSaveDialog(parent);
    }

    // User-specific dialogs
    public static void showUserWelcome() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Welcome to SQL Learning!");
        alert.setHeaderText("👋 Hello, SithuHan-SithuHan!");
        alert.setContentText(String.format(
                "Welcome back to SQL Learning Professional Edition!\n\n" +
                        "Session Details:\n" +
                        "• User: SithuHan-SithuHan\n" +
                        "• Date: 2025-09-26 04:44:12 UTC\n" +
                        "• Repository: SQL_Learning_APP\n\n" +
                        "Ready to continue your SQL learning journey?"
        ));
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showAchievementUnlocked(String achievementTitle, String description) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Achievement Unlocked!");
        alert.setHeaderText("🏆 " + achievementTitle);
        alert.setContentText(String.format(
                "Congratulations, SithuHan-SithuHan!\n\n" +
                        "%s\n\n" +
                        "You're making excellent progress in your SQL learning journey!\n" +
                        "Date: 2025-09-26 04:44:12 UTC",
                description
        ));
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showProgressMilestone(String milestone, int current, int total) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Progress Milestone!");
        alert.setHeaderText("🎯 " + milestone);
        alert.setContentText(String.format(
                "Great work, SithuHan-SithuHan!\n\n" +
                        "You've reached a new milestone:\n" +
                        "Progress: %d / %d (%s)\n\n" +
                        "Keep up the excellent work!\n" +
                        "Session: 2025-09-26 04:44:12 UTC",
                current, total, milestone
        ));
        styleAlert(alert);
        alert.showAndWait();
    }

    // GitHub integration dialogs
    public static void showGitHubInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("GitHub Repository");
        alert.setHeaderText("🔗 SQL Learning APP Repository");
        alert.setContentText(
                "Repository: SQL_Learning_APP\n" +
                        "Owner: SithuHan-SithuHan\n" +
                        "URL: https://github.com/SithuHan-SithuHan/SQL_Learning_APP\n\n" +
                        "This application is part of your GitHub portfolio.\n" +
                        "Last updated: 2025-09-26 04:44:12 UTC"
        );
        styleAlert(alert);
        alert.showAndWait();
    }

    // Utility styling methods
    private static void styleAlert(Alert alert) {
        try {
            alert.getDialogPane().getStylesheets().add(
                    UIUtils.class.getResource("/themes/modern-theme.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("modern-alert");
        } catch (Exception e) {
            // Continue without styling if CSS not available
        }

        // Set minimum width for better appearance
        alert.getDialogPane().setMinWidth(400);
        alert.setResizable(true);
    }

    private static void styleDialog(javafx.scene.control.Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    UIUtils.class.getResource("/themes/modern-theme.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("modern-dialog");
        } catch (Exception e) {
            // Continue without styling if CSS not available
        }

        // Set minimum width for better appearance
        dialog.getDialogPane().setMinWidth(350);
        dialog.setResizable(true);
    }

    // Clipboard utilities
    public static void copyToClipboard(String text) {
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
    }

    public static String getFromClipboard() {
        return javafx.scene.input.Clipboard.getSystemClipboard().getString();
    }

    // User session utilities
    public static String getCurrentUserSession() {
        return String.format("User: %s | Session: %s",
                "SithuHan-SithuHan", "2025-09-26 04:44:12 UTC");
    }

    public static String getRepositoryInfo() {
        return "Repository: SithuHan-SithuHan/SQL_Learning_APP";
    }

    // URL utilities
    public static void openURL(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            copyToClipboard(url);
            showInfo("URL Copied", "Could not open browser. URL copied to clipboard:\n" + url);
        }
    }

    public static void openGitHubRepository() {
        openURL("https://github.com/SithuHan-SithuHan/SQL_Learning_APP");
    }
}