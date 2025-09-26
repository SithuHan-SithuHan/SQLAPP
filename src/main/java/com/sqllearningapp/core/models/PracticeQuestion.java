package com.sqllearningapp.core.models;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Practice Question Model - Enhanced version of your original PracticeQuestion
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PracticeQuestion {
    private String id;
    private String title;
    private String description;
    private String exampleSql;
    private Difficulty difficulty;
    private String hint;
    private String solution;
    private String category; // New: DDL, DML, DCL, TCL
    private int points;
    private QueryResult expectedResult;

    // Enhanced difficulty levels
    public enum Difficulty {
        EASY("Easy", "#059669", 10),     // Green, 10 points
        MEDIUM("Medium", "#d97706", 20), // Orange, 20 points
        HARD("Hard", "#dc2626", 30),     // Red, 30 points
        PRO("Pro", "#7c3aed", 50);       // Purple, 50 points

        private final String displayName;
        private final String color;
        private final int defaultPoints;

        Difficulty(String displayName, String color, int defaultPoints) {
            this.displayName = displayName;
            this.color = color;
            this.defaultPoints = defaultPoints;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
        public int getDefaultPoints() { return defaultPoints; }
    }

    public String getDifficultyDisplay() {
        return difficulty != null ? difficulty.getDisplayName() : "Unknown";
    }

    public String getDifficultyColor() {
        return difficulty != null ? difficulty.getColor() : "#666666";
    }

    public int getDefaultPoints() {
        return difficulty != null ? difficulty.getDefaultPoints() : 10;
    }
}