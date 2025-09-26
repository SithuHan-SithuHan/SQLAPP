package com.sqllearningapp.utils;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced Icon Generator for SQL Learning Application
 * Creates modern, scalable icons for the application
 */
@Slf4j
public class IconGenerator {

    private static final String APP_NAME = "SQL Learning";
    private static final String USER_SIGNATURE = "SithuHan-SithuHan";

    /**
     * Create default application icon with modern design
     */
    public static Image createDefaultIcon() {
        return createDefaultIcon(64, 64);
    }

    /**
     * Create default application icon with specified dimensions
     */
    public static Image createDefaultIcon(int width, int height) {
        try {
            Canvas canvas = new Canvas(width, height);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Modern gradient background
            Stop[] stops = new Stop[] {
                    new Stop(0, Color.web("#667eea")),
                    new Stop(1, Color.web("#764ba2"))
            };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true,
                    CycleMethod.NO_CYCLE, stops);

            gc.setFill(gradient);
            gc.fillRoundRect(0, 0, width, height, 12, 12);

            // Add border
            gc.setStroke(Color.web("#4c5fd7"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(1, 1, width-2, height-2, 12, 12);

            // SQL text
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(12, width * 0.25)));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("SQL", width / 2.0, height * 0.45);

            // Database icon elements
            double iconSize = Math.min(width, height) * 0.15;
            gc.setFill(Color.web("#e8f4ff"));

            // Draw database cylinders
            for (int i = 0; i < 3; i++) {
                double y = height * 0.55 + i * iconSize * 0.8;
                gc.fillOval(width * 0.3, y, iconSize * 2, iconSize * 0.6);
            }

            // Add subtle app signature
            if (width >= 48) {
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 6));
                gc.setFill(Color.web("#ffffff80"));
                gc.fillText("v2.0", width * 0.85, height * 0.95);
            }

            WritableImage image = new WritableImage(width, height);
            canvas.snapshot(null, image);

            log.debug("Generated default icon ({}x{}) for user: {}",
                    width, height, USER_SIGNATURE);

            return image;

        } catch (Exception e) {
            log.error("Failed to generate default icon", e);
            return createFallbackIcon(width, height);
        }
    }

    /**
     * Create a simple fallback icon if generation fails
     */
    private static Image createFallbackIcon(int width, int height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Simple colored rectangle
        gc.setFill(Color.web("#2563eb"));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, width * 0.3));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("S", width / 2.0, height * 0.7);

        WritableImage image = new WritableImage(width, height);
        canvas.snapshot(null, image);

        return image;
    }

    /**
     * Create status icons for different states
     */
    public static Image createStatusIcon(StatusType type, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color backgroundColor;
        Color iconColor = Color.WHITE;
        String symbol;

        switch (type) {
            case SUCCESS:
                backgroundColor = Color.web("#059669");
                symbol = "✓";
                break;
            case ERROR:
                backgroundColor = Color.web("#dc2626");
                symbol = "✗";
                break;
            case WARNING:
                backgroundColor = Color.web("#d97706");
                symbol = "!";
                break;
            case INFO:
                backgroundColor = Color.web("#2563eb");
                symbol = "i";
                break;
            case LOADING:
                backgroundColor = Color.web("#6b7280");
                symbol = "⟳";
                break;
            default:
                backgroundColor = Color.GRAY;
                symbol = "?";
        }

        // Draw circle background
        gc.setFill(backgroundColor);
        gc.fillOval(2, 2, size-4, size-4);

        // Add border
        gc.setStroke(backgroundColor.darker());
        gc.setLineWidth(1);
        gc.strokeOval(2, 2, size-4, size-4);

        // Draw symbol
        gc.setFill(iconColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.5));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(symbol, size / 2.0, size * 0.7);

        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);

        log.debug("Generated {} status icon ({}px) for user: {}",
                type, size, USER_SIGNATURE);

        return image;
    }

    /**
     * Create difficulty level icons for practice questions
     */
    public static Image createDifficultyIcon(String difficulty, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color backgroundColor;
        String emoji;

        switch (difficulty.toLowerCase()) {
            case "easy":
                backgroundColor = Color.web("#059669");
                emoji = "🟢";
                break;
            case "medium":
                backgroundColor = Color.web("#d97706");
                emoji = "🟡";
                break;
            case "hard":
                backgroundColor = Color.web("#dc2626");
                emoji = "🔴";
                break;
            case "pro":
                backgroundColor = Color.web("#7c3aed");
                emoji = "🟣";
                break;
            default:
                backgroundColor = Color.web("#6b7280");
                emoji = "⚪";
        }

        // Draw background circle
        gc.setFill(backgroundColor.deriveColor(0, 0.3, 1.2, 0.3));
        gc.fillOval(0, 0, size, size);

        // Draw main circle
        gc.setFill(backgroundColor);
        gc.fillOval(size * 0.1, size * 0.1, size * 0.8, size * 0.8);

        // Add difficulty text
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.25));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(difficulty.toUpperCase().substring(0, 1),
                size / 2.0, size * 0.6);

        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);

        log.debug("Generated difficulty icon for '{}' ({}px) by user: {}",
                difficulty, size, USER_SIGNATURE);

        return image;
    }

    /**
     * Create learning topic category icons
     */
    public static Image createCategoryIcon(String category, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color backgroundColor;
        String symbol;

        switch (category.toUpperCase()) {
            case "DDL":
                backgroundColor = Color.web("#3b82f6");
                symbol = "🏗";
                break;
            case "DML":
                backgroundColor = Color.web("#059669");
                symbol = "🔄";
                break;
            case "DCL":
                backgroundColor = Color.web("#d97706");
                symbol = "🔐";
                break;
            case "TCL":
                backgroundColor = Color.web("#dc2626");
                symbol = "💾";
                break;
            case "NORMALIZATION":
                backgroundColor = Color.web("#7c3aed");
                symbol = "📐";
                break;
            default:
                backgroundColor = Color.web("#6b7280");
                symbol = "📁";
        }

        // Modern card-like background
        gc.setFill(backgroundColor.deriveColor(0, 0.1, 1.1, 0.9));
        gc.fillRoundRect(0, 0, size, size, 8, 8);

        // Add subtle border
        gc.setStroke(backgroundColor);
        gc.setLineWidth(2);
        gc.strokeRoundRect(1, 1, size-2, size-2, 8, 8);

        // Category abbreviation
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.2));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(category, size / 2.0, size * 0.7);

        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);

        log.debug("Generated category icon for '{}' ({}px) by user: {}",
                category, size, USER_SIGNATURE);

        return image;
    }

    /**
     * Create progress indicator icons
     */
    public static Image createProgressIcon(double progressPercent, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Background circle
        gc.setFill(Color.web("#e5e7eb"));
        gc.fillOval(2, 2, size-4, size-4);

        // Progress arc
        double progressAngle = (progressPercent / 100.0) * 360.0;
        Color progressColor;

        if (progressPercent < 30) {
            progressColor = Color.web("#dc2626"); // Red
        } else if (progressPercent < 70) {
            progressColor = Color.web("#d97706"); // Orange
        } else {
            progressColor = Color.web("#059669"); // Green
        }

        gc.setFill(progressColor);
        gc.fillArc(2, 2, size-4, size-4, 90, -progressAngle, javafx.scene.shape.ArcType.ROUND);

        // Center circle
        gc.setFill(Color.WHITE);
        gc.fillOval(size * 0.3, size * 0.3, size * 0.4, size * 0.4);

        // Progress text
        gc.setFill(progressColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.0f%%", progressPercent),
                size / 2.0, size * 0.57);

        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);

        log.debug("Generated progress icon ({}%) ({}px) for user: {}",
                progressPercent, size, USER_SIGNATURE);

        return image;
    }

    /**
     * Create user avatar icon with initials
     */
    public static Image createUserAvatarIcon(String userLogin, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Generate consistent color based on username
        int hash = userLogin.hashCode();
        Color backgroundColor = Color.hsb(
                Math.abs(hash) % 360,
                0.6,
                0.7
        );

        // Background circle
        gc.setFill(backgroundColor);
        gc.fillOval(0, 0, size, size);

        // Border
        gc.setStroke(backgroundColor.darker());
        gc.setLineWidth(2);
        gc.strokeOval(1, 1, size-2, size-2);

        // User initials (first two characters of login)
        String initials = userLogin.length() >= 2 ?
                userLogin.substring(0, 2).toUpperCase() :
                userLogin.toUpperCase();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.35));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(initials, size / 2.0, size * 0.65);

        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);

        log.debug("Generated user avatar icon for '{}' ({}px)", userLogin, size);

        return image;
    }

    // ===== ENUMS =====

    public enum StatusType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO,
        LOADING
    }
}