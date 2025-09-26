package com.sqllearningapp.core.models;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * User Progress Model - Enhanced version of your progress tracking
 */
@Data
@Builder
public class UserProgress {
    private String userId;
    private Set<String> completedQuestions;
    private Set<String> viewedTopics;
    private Map<String, Integer> questionAttempts;
    private Map<String, LocalDateTime> lastAttempted;
    private Statistics statistics;
    private Achievements achievements;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;

    @Data
    @Builder
    public static class Statistics {
        private int totalQueriesExecuted;
        private int successfulQueries;
        private int currentStreak;
        private int bestStreak;
        private long totalTimeSpentMs;
        private int totalPointsEarned;
        private Map<String, Integer> categoryProgress; // DDL: 5/10, DML: 8/15, etc.

        public double getSuccessRate() {
            return totalQueriesExecuted > 0 ?
                    (double) successfulQueries / totalQueriesExecuted * 100 : 0;
        }

        public double getCompletionPercentage() {
            int totalQuestions = categoryProgress.values().stream()
                    .mapToInt(Integer::intValue).sum();
            return totalQuestions > 0 ?
                    (double) completedQuestions.size() / totalQuestions * 100 : 0;
        }
    }

    @Data
    @Builder
    public static class Achievements {
        private Set<String> unlockedBadges;
        private int level;
        private int experiencePoints;
        private Map<String, LocalDateTime> badgeEarnedDates;

        public int getNextLevelXP() {
            return (level + 1) * 1000; // Each level requires 1000 more XP
        }

        public double getLevelProgress() {
            int currentLevelBase = level * 1000;
            int nextLevelXP = getNextLevelXP();
            return (double) (experiencePoints - currentLevelBase) /
                    (nextLevelXP - currentLevelBase) * 100;
        }
    }
}