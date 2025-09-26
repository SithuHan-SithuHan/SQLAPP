package com.sqllearningapp.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * About Dialog - Application information and credits
 * Updated with current date and user information
 */
public class AboutDialog extends Dialog<Void> {

    public AboutDialog() {
        setupDialog();
    }

    private void setupDialog() {
        setTitle("About SQL Learning Application");
        setHeaderText(null);

        // Create content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        // App info section
        VBox appInfo = createAppInfoSection();

        // User info section
        VBox userInfo = createUserInfoSection();

        // System info section
        VBox systemInfo = createSystemInfoSection();

        // Credits section
        VBox credits = createCreditsSection();

        content.getChildren().addAll(appInfo, new Separator(), userInfo,
                new Separator(), systemInfo, new Separator(), credits);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.OK);

        // Style the dialog
        getDialogPane().getStylesheets().add(
                getClass().getResource("/themes/modern-theme.css").toExternalForm());
        getDialogPane().getStyleClass().add("about-dialog");
    }

    private VBox createAppInfoSection() {
        VBox section = new VBox(10);

        // App icon and title
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Try to load app icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/sql-icon.png"));
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(64);
            iconView.setFitHeight(64);
            iconView.setPreserveRatio(true);
            header.getChildren().add(iconView);
        } catch (Exception e) {
            // Use text icon if image not available
            Label iconLabel = new Label("🗄️");
            iconLabel.setFont(Font.font(48));
            header.getChildren().add(iconLabel);
        }

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("SQL Learning Professional Edition");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

        Label versionLabel = new Label("Version 2.0.0 (Rewritten)");
        versionLabel.setStyle("-fx-text-fill: #666;");

        titleBox.getChildren().addAll(titleLabel, versionLabel);
        header.getChildren().add(titleBox);

        // Description
        Label descriptionLabel = new Label(
                "A comprehensive desktop application for learning SQL through interactive " +
                        "theory lessons and hands-on practice exercises. Built with modern JavaFX " +
                        "architecture for optimal performance and user experience.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #444;");

        section.getChildren().addAll(header, descriptionLabel);

        return section;
    }

    private VBox createUserInfoSection() {
        VBox section = new VBox(8);

        Label titleLabel = new Label("👤 Current Session");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        addGridRow(grid, 0, "User:", "SithuHan-SithuHan");
        addGridRow(grid, 1, "Session Start:", "2025-09-26 04:44:12 UTC");
        addGridRow(grid, 2, "GitHub Repository:", "SQL_Learning_APP");
        addGridRow(grid, 3, "Repository URL:", "github.com/SithuHan-SithuHan/SQL_Learning_APP");

        section.getChildren().addAll(titleLabel, grid);

        return section;
    }

    private VBox createSystemInfoSection() {
        VBox section = new VBox(8);

        Label titleLabel = new Label("⚙️ System Information");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        addGridRow(grid, 0, "Java Version:", System.getProperty("java.version"));
        addGridRow(grid, 1, "JavaFX Version:", getJavaFXVersion());
        addGridRow(grid, 2, "Operating System:",
                System.getProperty("os.name") + " " + System.getProperty("os.version"));
        addGridRow(grid, 3, "Architecture:", System.getProperty("os.arch"));
        addGridRow(grid, 4, "Memory:",
                String.format("%.0f MB / %.0f MB",
                        Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0,
                        Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0));
        addGridRow(grid, 5, "Database:", "H2 Database Engine (Embedded)");

        section.getChildren().addAll(titleLabel, grid);

        return section;
    }

    private VBox createCreditsSection() {
        VBox section = new VBox(8);

        Label titleLabel = new Label("👨‍💻 Credits & Acknowledgments");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

        VBox creditsBox = new VBox(5);

        addCreditItem(creditsBox, "Original Developer:", "SithuHan-SithuHan");
        addCreditItem(creditsBox, "Framework:", "JavaFX 21+ with modern architecture");
        addCreditItem(creditsBox, "Database:", "H2 Database Engine");
        addCreditItem(creditsBox, "Syntax Highlighting:", "RichTextFX Library");
        addCreditItem(creditsBox, "UI Theme:", "AtlantaFX Base Theme");
        addCreditItem(creditsBox, "Icons:", "Unicode Emoji & Custom Icons");
        addCreditItem(creditsBox, "Rewrite Date:", "September 2025");

        // GitHub link
        Hyperlink githubLink = new Hyperlink("🔗 View on GitHub");
        githubLink.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(
                        java.net.URI.create("https://github.com/SithuHan-SithuHan/SQL_Learning_APP"));
            } catch (Exception ex) {
                // Copy to clipboard as fallback
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString("https://github.com/SithuHan-SithuHan/SQL_Learning_APP");
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
            }
        });

        section.getChildren().addAll(titleLabel, creditsBox, githubLink);

        return section;
    }

    private void addGridRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #666;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void addCreditItem(VBox parent, String label, String value) {
        HBox item = new HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        labelNode.setPrefWidth(120);

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #666;");

        item.getChildren().addAll(labelNode, valueNode);
        parent.getChildren().add(item);
    }

    private String getJavaFXVersion() {
        try {
            return System.getProperty("javafx.version", "Unknown");
        } catch (Exception e) {
            return "21.0+";
        }
    }
}