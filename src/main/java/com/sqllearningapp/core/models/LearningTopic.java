package com.sqllearningapp.core.models;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Learning Topic Model for organizing content
 */
@Data
@Builder
public class LearningTopic {
    private String id;
    private String title;
    private String content; // HTML content
    private String category; // DDL, DML, DCL, TCL, Normalization
    private int orderIndex;
    private boolean isLeaf;
    private String parentId;
    private LocalDateTime lastViewed;
    private int viewCount;
    private EstimatedDuration estimatedDuration;

    @Data
    @Builder
    public static class EstimatedDuration {
        private int minutes;
        private String description;

        public String getDisplayText() {
            return minutes + " min read";
        }
    }

    public enum Category {
        DDL("Data Definition Language", "#3b82f6"),
        DML("Data Manipulation Language", "#059669"),
        DCL("Data Control Language", "#d97706"),
        TCL("Transaction Control Language", "#dc2626"),
        NORMALIZATION("Database Normalization", "#7c3aed");

        private final String displayName;
        private final String color;

        Category(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
}