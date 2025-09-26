package com.sqllearningapp.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sqllearningapp.core.models.UserProgress;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Progress Tracking Service
 * Tracks user learning progress across both theory and practice sections
 */
@Slf4j
public class ProgressTrackingService {

    private final ObjectMapper objectMapper;
    private UserProgress userProgress;
    private final Map<String, LocalDateTime> sessionData;
    private final String PROGRESS_FILE = "user_progress_v2.json";

    public ProgressTrackingService() {
        this.objectMapper = createObjectMapper();
        this.sessionData = new ConcurrentHashMap<>();
        loadProgress();

        log.info("Progress tracking service initialized for user: {}",
                userProgress.getUserId());
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private void loadProgress() {
        try {
            File progressFile = new File(PROGRESS_FILE);
            if (progressFile.exists()) {
                userProgress = objectMapper.readValue(progressFile, UserProgress.class);
                log.info("Loaded progress for user: {} with {} completed questions",
                        userProgress.getUserId(), userProgress.getCompletedQuestions().size());
            } else {
                userProgress = createDefaultProgress();
                log.info("Created new progress profile for user: {}", userProgress.getUserId());
            }
        } catch (IOException e) {
            log.warn("Failed to load progress, creating new profile: {}", e.getMessage());
            userProgress = createDefaultProgress();
        }
    }

    private UserProgress createDefaultProgress() {
        return UserProgress.builder()
                .userId("default_user")
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

    // ===== PROGRESS TRACKING METHODS =====

    public void recordQuestionCompletion(String questionId, boolean success, int points) {
        if (success) {
            userProgress.getCompletedQuestions().add(questionId);
            userProgress.getStatistics().setTotalPointsEarned(
                    userProgress.getStatistics().getTotalPointsEarned() + points);

            // Update achievements
            updateAchievements(points);

            log.debug("Recorded question completion: {} (+{} points)", questionId, points);
        }

        // Record attempt
        userProgress.getQuestionAttempts().put(questionId,
                userProgress.getQuestionAttempts().getOrDefault(questionId, 0) + 1);
        userProgress.getLastAttempted().put(questionId, LocalDateTime.now());

        userProgress.setLastUpdated(LocalDateTime.now());
        saveProgress();
    }

    public void recordTopicView(String topicId) {
        userProgress.getViewedTopics().add(topicId);
        userProgress.setLastUpdated(LocalDateTime.now());
        saveProgress();

        log.debug("Recorded topic view: {}", topicId);
    }

    public void updateStatistics(String statName, int value) {
        UserProgress.Statistics stats = userProgress.getStatistics();
        switch (statName.toLowerCase()) {
            case "totalqueriesexecuted" -> stats.setTotalQueriesExecuted(value);
            case "successfulqueries" -> stats.setSuccessfulQueries(value);
            case "currentstreak" -> stats.setCurrentStreak(value);
            case "beststreak" -> stats.setBestStreak(value);
            case "totaltimespentms" -> stats.setTotalTimeSpentMs(value);
            case "totalpointsearned" -> stats.setTotalPointsEarned(value);
        }

        userProgress.setLastUpdated(LocalDateTime.now());
        saveProgress();
    }

    private void updateAchievements(int points) {
        UserProgress.Achievements achievements = userProgress.getAchievements();

        // Add experience points
        achievements.setExperiencePoints(achievements.getExperiencePoints() + points);

        // Check for level up
        int currentLevel = achievements.getLevel();
        int newLevel = calculateLevel(achievements.getExperiencePoints());

        if (newLevel > currentLevel) {
            achievements.setLevel(newLevel);
            unlockBadge("level_" + newLevel, "Reached Level " + newLevel);
            log.info("User leveled up to level {}", newLevel);
        }

        // Check for milestone badges
        checkMilestoneBadges();
    }

    private int calculateLevel(int experiencePoints) {
        // Level up every 1000 XP
        return Math.max(1, experiencePoints / 1000 + 1);
    }

    private void checkMilestoneBadges() {
        UserProgress.Statistics stats = userProgress.getStatistics();
        Set<String> badges = userProgress.getAchievements().getUnlockedBadges();

        // Completion milestones
        int completed = userProgress.getCompletedQuestions().size();
        if (completed >= 5 && !badges.contains("first_five")) {
            unlockBadge("first_five", "Completed first 5 questions");
        }
        if (completed >= 10 && !badges.contains("ten_questions")) {
            unlockBadge("ten_questions", "Completed 10 questions");
        }
        if (completed >= 20 && !badges.contains("twenty_questions")) {
            unlockBadge("twenty_questions", "Completed 20 questions - Expert!");
        }

        // Streak milestones
        if (stats.getCurrentStreak() >= 5 && !badges.contains("streak_five")) {
            unlockBadge("streak_five", "5 questions streak");
        }
        if (stats.getBestStreak() >= 10 && !badges.contains("streak_ten")) {
            unlockBadge("streak_ten", "10 questions streak - Unstoppable!");
        }

        // Success rate milestones
        double successRate = stats.getTotalQueriesExecuted() > 0 ?
                (double) stats.getSuccessfulQueries() / stats.getTotalQueriesExecuted() * 100 : 0;

        if (successRate >= 90 && stats.getTotalQueriesExecuted() >= 10 && !badges.contains("high_accuracy")) {
            unlockBadge("high_accuracy", "90%+ Success Rate");
        }
    }

    private void unlockBadge(String badgeId, String description) {
        userProgress.getAchievements().getUnlockedBadges().add(badgeId);
        userProgress.getAchievements().getBadgeEarnedDates().put(badgeId, LocalDateTime.now());
        log.info("Badge unlocked: {} - {}", badgeId, description);
    }

    // ===== GETTER METHODS =====

    public UserProgress getUserProgress() {
        return userProgress;
    }

    public Set<String> getCompletedQuestions() {
        return new HashSet<>(userProgress.getCompletedQuestions());
    }

    public Set<String> getViewedTopics() {
        return new HashSet<>(userProgress.getViewedTopics());
    }

    public boolean isQuestionCompleted(String questionId) {
        return userProgress.getCompletedQuestions().contains(questionId);
    }

    public boolean isTopicViewed(String topicId) {
        return userProgress.getViewedTopics().contains(topicId);
    }

    public int getQuestionAttempts(String questionId) {
        return userProgress.getQuestionAttempts().getOrDefault(questionId, 0);
    }

    public double getOverallProgress() {
        // Assuming 24 total questions (5 easy + 5 medium + 4 hard + 4 pro + 6 topics)
        int totalItems = 24;
        int completedItems = userProgress.getCompletedQuestions().size() +
                userProgress.getViewedTopics().size();
        return Math.min(100.0, (double) completedItems / totalItems * 100);
    }

    public Map<String, Object> getStatisticsSummary() {
        UserProgress.Statistics stats = userProgress.getStatistics();
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalQuestions", userProgress.getCompletedQuestions().size());
        summary.put("totalTopics", userProgress.getViewedTopics().size());
        summary.put("successRate", stats.getTotalQueriesExecuted() > 0 ?
                (double) stats.getSuccessfulQueries() / stats.getTotalQueriesExecuted() * 100 : 0);
        summary.put("currentStreak", stats.getCurrentStreak());
        summary.put("bestStreak", stats.getBestStreak());
        summary.put("totalPoints", stats.getTotalPointsEarned());
        summary.put("level", userProgress.getAchievements().getLevel());
        summary.put("experiencePoints", userProgress.getAchievements().getExperiencePoints());
        summary.put("badges", userProgress.getAchievements().getUnlockedBadges().size());

        return summary;
    }

    // ===== PERSISTENCE =====

    public void saveProgress() {
        try {
            userProgress.setLastUpdated(LocalDateTime.now());
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(PROGRESS_FILE), userProgress);

            log.debug("Progress saved successfully");

        } catch (IOException e) {
            log.error("Failed to save progress", e);
        }
    }

    public void resetProgress() {
        log.info("Resetting user progress");
        userProgress = createDefaultProgress();
        saveProgress();
    }

    public void exportProgress() throws IOException {
        String exportFileName = "progress_export_" + LocalDateTime.now().toString().replace(":", "-") + ".json";
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(exportFileName), userProgress);
        log.info("Progress exported to: {}", exportFileName);
    }
}