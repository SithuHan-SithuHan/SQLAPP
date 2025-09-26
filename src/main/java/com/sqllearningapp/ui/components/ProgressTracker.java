package com.sqllearningapp.ui.components;

import com.sqllearningapp.core.services.ProgressTrackingService;
import com.sqllearningapp.core.models.UserProgress;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Enhanced Progress Tracker - Visual progress tracking with achievements
 * Real-time progress updates with modern animations and GitHub integration
 */
@Slf4j
public class ProgressTracker extends VBox {

    private final ProgressTrackingService progressService;

    // Progress Components
    private ProgressBar overallProgressBar;
    private Label progressLabel;
    private Label levelLabel;
    private ProgressBar xpProgressBar;
    private Label xpLabel;
    private FlowPane badgesPane;
    private VBox statisticsBox;
    private Label streakLabel;

    // Current progress state
    private UserProgress currentProgress;
    private double lastOverallProgress = 0.0;

    public ProgressTracker(ProgressTrackingService progressService) {
        this.progressService = progressService;
        setupUI();
        updateProgress();
        getStyleClass().add("progress-tracker");

        log.debug("Progress tracker initialized for user: SithuHan-SithuHan");
    }

    private void setupUI() {
        setSpacing(12);
        setPadding(new Insets(15));
        getStyleClass().add("progress-container");

        // Header with user info
        HBox header = createHeader();

        // Overall progress section
        VBox progressSection = createProgressSection();

        // Level and XP section
        VBox levelSection = createLevelSection();

        // Statistics section
        VBox statsSection = createStatisticsSection();

        // Achievements/Badges section
        VBox achievementsSection = createAchievementsSection();

        getChildren().addAll(header, progressSection, levelSection, statsSection, achievementsSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("progress-header");

        // User avatar (placeholder circle)
        Circle avatar = new Circle(20);
        avatar.setFill(Color.web("#2563eb"));
        avatar.getStyleClass().add("user-avatar");

        VBox userInfo = new VBox(2);
        Label userLabel = new Label("SithuHan-SithuHan");
        userLabel.getStyleClass().add("user-name");

        Label sessionLabel = new Label("Session: 2025-09-26 04:44:12 UTC");
        sessionLabel.getStyleClass().add("session-info");

        userInfo.getChildren().addAll(userLabel, sessionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // GitHub link
        Hyperlink githubLink = new Hyperlink("🔗 GitHub Repo");
        githubLink.getStyleClass().add("github-link");
        githubLink.setOnAction(e -> openGitHubRepo());

        header.getChildren().addAll(avatar, userInfo, spacer, githubLink);

        return header;
    }

    private VBox createProgressSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("progress-section");

        Label titleLabel = new Label("📈 Overall Progress");
        titleLabel.getStyleClass().add("section-title");

        // Progress bar with animation
        overallProgressBar = new ProgressBar(0);
        overallProgressBar.setMaxWidth(Double.MAX_VALUE);
        overallProgressBar.getStyleClass().add("overall-progress");

        progressLabel = new Label("0% Complete");
        progressLabel.getStyleClass().add("progress-text");

        section.getChildren().addAll(titleLabel, overallProgressBar, progressLabel);

        return section;
    }

    private VBox createLevelSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("level-section");

        HBox levelHeader = new HBox(10);
        levelHeader.setAlignment(Pos.CENTER_LEFT);

        levelLabel = new Label("🏆 Level 1");
        levelLabel.getStyleClass().add("level-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        xpLabel = new Label("0 / 1000 XP");
        xpLabel.getStyleClass().add("xp-label");

        levelHeader.getChildren().addAll(levelLabel, spacer, xpLabel);

        // XP Progress bar
        xpProgressBar = new ProgressBar(0);
        xpProgressBar.setMaxWidth(Double.MAX_VALUE);
        xpProgressBar.getStyleClass().add("xp-progress");

        section.getChildren().addAll(levelHeader, xpProgressBar);

        return section;
    }

    private VBox createStatisticsSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("statistics-section");

        Label titleLabel = new Label("📊 Statistics");
        titleLabel.getStyleClass().add("section-title");

        statisticsBox = new VBox(4);
        statisticsBox.getStyleClass().add("statistics-content");

        // Streak indicator
        HBox streakBox = new HBox(8);
        streakBox.setAlignment(Pos.CENTER_LEFT);

        Label streakIcon = new Label("🔥");
        streakIcon.getStyleClass().add("streak-icon");

        streakLabel = new Label("Current Streak: 0");
        streakLabel.getStyleClass().add("streak-label");

        streakBox.getChildren().addAll(streakIcon, streakLabel);

        section.getChildren().addAll(titleLabel, statisticsBox, streakBox);

        return section;
    }

    private VBox createAchievementsSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("achievements-section");

        Label titleLabel = new Label("🏅 Achievements");
        titleLabel.getStyleClass().add("section-title");

        badgesPane = new FlowPane();
        badgesPane.setHgap(6);
        badgesPane.setVgap(6);
        badgesPane.getStyleClass().add("badges-pane");

        section.getChildren().addAll(titleLabel, badgesPane);

        return section;
    }

    // ===== UPDATE METHODS =====

    public void updateProgress() {
        currentProgress = progressService.getUserProgress();

        if (currentProgress != null) {
            updateOverallProgress();
            updateLevelProgress();
            updateStatistics();
            updateAchievements();
        }
    }

    private void updateOverallProgress() {
        double progress = progressService.getOverallProgress();

        // Animate progress change
        if (progress != lastOverallProgress) {
            animateProgressChange(overallProgressBar, lastOverallProgress / 100.0, progress / 100.0);
            lastOverallProgress = progress;
        }

        progressLabel.setText(String.format("%.1f%% Complete", progress));

        // Update color based on progress
        overallProgressBar.getStyleClass().removeAll("low-progress", "medium-progress", "high-progress");
        if (progress < 30) {
            overallProgressBar.getStyleClass().add("low-progress");
        } else if (progress < 70) {
            overallProgressBar.getStyleClass().add("medium-progress");
        } else {
            overallProgressBar.getStyleClass().add("high-progress");
        }
    }

    private void updateLevelProgress() {
        UserProgress.Achievements achievements = currentProgress.getAchievements();

        int level = achievements.getLevel();
        int currentXP = achievements.getExperiencePoints();
        int nextLevelXP = achievements.getNextLevelXP();
        double levelProgress = achievements.getLevelProgress();

        levelLabel.setText(String.format("🏆 Level %d", level));
        xpLabel.setText(String.format("%d / %d XP", currentXP, nextLevelXP));
        xpProgressBar.setProgress(levelProgress / 100.0);

        // Update level styling
        levelLabel.getStyleClass().removeAll("level-1", "level-2", "level-3", "level-high");
        if (level == 1) {
            levelLabel.getStyleClass().add("level-1");
        } else if (level <= 3) {
            levelLabel.getStyleClass().add("level-2");
        } else if (level <= 5) {
            levelLabel.getStyleClass().add("level-3");
        } else {
            levelLabel.getStyleClass().add("level-high");
        }
    }

    private void updateStatistics() {
        Map<String, Object> stats = progressService.getStatisticsSummary();

        statisticsBox.getChildren().clear();

        // Create statistics labels
        addStatistic("Questions Completed", stats.get("totalQuestions") + " / 24");
        addStatistic("Topics Viewed", stats.get("totalTopics") + " / 24");
        addStatistic("Success Rate", String.format("%.1f%%", (Double) stats.get("successRate")));
        addStatistic("Total Points", String.format("%,d", (Integer) stats.get("totalPoints")));
        addStatistic("Badges Earned", stats.get("badges") + " / 10");

        // Update streak
        int currentStreak = (Integer) stats.get("currentStreak");
        int bestStreak = (Integer) stats.get("bestStreak");

        streakLabel.setText(String.format("Current: %d | Best: %d", currentStreak, bestStreak));

        // Animate streak if it's high
        if (currentStreak >= 5) {
            animateStreakFlame();
        }
    }

    private void addStatistic(String label, String value) {
        HBox statBox = new HBox(8);
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.getStyleClass().add("stat-item");

        Label statLabel = new Label(label + ":");
        statLabel.getStyleClass().add("stat-label");
        statLabel.setPrefWidth(120);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statValue = new Label(value);
        statValue.getStyleClass().add("stat-value");

        statBox.getChildren().addAll(statLabel, spacer, statValue);
        statisticsBox.getChildren().add(statBox);
    }

    private void updateAchievements() {
        badgesPane.getChildren().clear();

        var unlockedBadges = currentProgress.getAchievements().getUnlockedBadges();

        // Add earned badges
        for (String badgeId : unlockedBadges) {
            Button badge = createBadge(badgeId, true);
            badgesPane.getChildren().add(badge);
        }

        // Add some locked badges as placeholders
        String[] allBadges = {
                "first_five", "ten_questions", "twenty_questions",
                "streak_five", "streak_ten", "high_accuracy",
                "level_5", "level_10", "sql_master", "perfect_score"
        };

        for (String badgeId : allBadges) {
            if (!unlockedBadges.contains(badgeId)) {
                Button badge = createBadge(badgeId, false);
                badgesPane.getChildren().add(badge);
            }
        }
    }

    private Button createBadge(String badgeId, boolean earned) {
        Button badge = new Button();
        badge.getStyleClass().addAll("achievement-badge", earned ? "earned-badge" : "locked-badge");

        String icon = getBadgeIcon(badgeId);
        String description = getBadgeDescription(badgeId);

        badge.setText(icon);
        badge.setTooltip(new Tooltip(description + (earned ? " ✓" : " (Locked)")));

        // Add click handler for earned badges
        if (earned) {
            badge.setOnAction(e -> showBadgeDetails(badgeId, description));
        }

        return badge;
    }

    private String getBadgeIcon(String badgeId) {
        return switch (badgeId) {
            case "first_five" -> "🎯";
            case "ten_questions" -> "📚";
            case "twenty_questions" -> "🎓";
            case "streak_five" -> "🔥";
            case "streak_ten" -> "⚡";
            case "high_accuracy" -> "🎯";
            case "level_5" -> "⭐";
            case "level_10" -> "🌟";
            case "sql_master" -> "👑";
            case "perfect_score" -> "💎";
            default -> "🏅";
        };
    }

    private String getBadgeDescription(String badgeId) {
        return switch (badgeId) {
            case "first_five" -> "First 5 Questions";
            case "ten_questions" -> "10 Questions Completed";
            case "twenty_questions" -> "20 Questions Expert";
            case "streak_five" -> "5 Question Streak";
            case "streak_ten" -> "10 Question Streak";
            case "high_accuracy" -> "90% Accuracy Rate";
            case "level_5" -> "Reached Level 5";
            case "level_10" -> "Reached Level 10";
            case "sql_master" -> "SQL Master";
            case "perfect_score" -> "Perfect Score";
            default -> "Achievement Badge";
        };
    }

    // ===== ANIMATION METHODS =====

    private void animateProgressChange(ProgressBar progressBar, double fromValue, double toValue) {
        // Simple progress animation
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();

        javafx.animation.KeyValue keyValue = new javafx.animation.KeyValue(
                progressBar.progressProperty(), toValue);
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                Duration.seconds(1.0), keyValue);

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    private void animateStreakFlame() {
        // Animate the streak flame icon
        Label streakIcon = (Label) ((HBox) getChildren().get(3).getChildren().get(1)).getChildren().get(0);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), streakIcon);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        scale.play();
    }

    public void animateNewAchievement(String badgeId) {
        // Find the badge and animate it
        for (var node : badgesPane.getChildren()) {
            if (node instanceof Button badge) {
                if (badge.getTooltip().getText().contains(getBadgeDescription(badgeId))) {
                    // Scale animation
                    ScaleTransition scale = new ScaleTransition(Duration.seconds(0.3), badge);
                    scale.setFromX(0.5);
                    scale.setFromY(0.5);
                    scale.setToX(1.0);
                    scale.setToY(1.0);

                    // Fade animation
                    FadeTransition fade = new FadeTransition(Duration.seconds(0.3), badge);
                    fade.setFromValue(0.0);
                    fade.setToValue(1.0);

                    scale.play();
                    fade.play();
                    break;
                }
            }
        }
    }

    // ===== EVENT HANDLERS =====

    private void openGitHubRepo() {
        try {
            java.awt.Desktop.getDesktop().browse(
                    java.net.URI.create("https://github.com/SithuHan-SithuHan/SQL_Learning_APP"));
        } catch (Exception e) {
            log.warn("Could not open GitHub repository", e);

            // Fallback: copy URL to clipboard
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString("https://github.com/SithuHan-SithuHan/SQL_Learning_APP");
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

            // Show notification
            showNotification("GitHub URL copied to clipboard!");
        }
    }

    private void showBadgeDetails(String badgeId, String description) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Achievement Unlocked!");
        dialog.setHeaderText(getBadgeIcon(badgeId) + " " + description);

        var earnedDate = currentProgress.getAchievements().getBadgeEarnedDates().get(badgeId);
        String content = String.format("""
            Congratulations, SithuHan-SithuHan!
            
            You've earned the "%s" achievement!
            
            Earned on: %s
            
            Keep up the excellent progress in your SQL learning journey!
            """,
                description,
                earnedDate != null ? earnedDate.format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")) : "Unknown");

        dialog.setContentText(content);
        dialog.showAndWait();
    }

    private void showNotification(String message) {
        // Simple notification using tooltip-like approach
        Tooltip notification = new Tooltip(message);
        notification.setAutoHide(true);
        notification.show(this, 0, 0);

        // Hide after 3 seconds
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> notification.hide());
        pause.play();
    }

    // ===== PUBLIC METHODS =====

    public void refreshProgress() {
        updateProgress();
    }

    public void showAchievementEarned(String badgeId) {
        updateAchievements();
        animateNewAchievement(badgeId);
        showBadgeDetails(badgeId, getBadgeDescription(badgeId));
    }

    public double getCurrentProgress() {
        return lastOverallProgress;
    }

    public UserProgress getUserProgress() {
        return currentProgress;
    }

    public void resetProgress() {
        progressService.resetProgress();
        updateProgress();
        showNotification("Progress has been reset!");
    }
}