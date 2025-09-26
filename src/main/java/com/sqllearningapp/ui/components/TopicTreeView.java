package com.sqllearningapp.ui.components;

import com.sqllearningapp.core.services.LearningContentService;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced Topic Tree View - Displays learning topics with modern styling
 * Preserves your original topic tree structure with visual improvements
 */
@Slf4j
public class TopicTreeView extends TreeView<String> {

    private final LearningContentService learningContentService;

    public TopicTreeView(LearningContentService learningContentService) {
        this.learningContentService = learningContentService;
        setupTreeView();
        loadTopics();
    }

    private void setupTreeView() {
        getStyleClass().add("topic-tree");
        setShowRoot(false);

        // Custom cell factory for enhanced display (includes tooltips)
        setCellFactory(tv -> new TopicTreeCell());
    }

    private void loadTopics() {
        TreeItem<String> rootItem = learningContentService.getLearningTopicsTree();
        setRoot(rootItem);

        // Expand all categories by default
        expandAll(rootItem);

        log.debug("Loaded learning topics tree with {} items", countTreeItems(rootItem));
    }

    private void expandAll(TreeItem<String> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<String> child : item.getChildren()) {
                expandAll(child);
            }
        }
    }

    private int countTreeItems(TreeItem<String> item) {
        int count = 1;
        for (TreeItem<String> child : item.getChildren()) {
            count += countTreeItems(child);
        }
        return count;
    }

    private String createTooltipText(String topicName) {
        boolean viewed = learningContentService.isTopicViewed(topicName);
        int viewCount = learningContentService.getTopicViewCount(topicName);

        StringBuilder tooltip = new StringBuilder();
        tooltip.append("Topic: ").append(topicName).append("\n");

        if (viewed) {
            tooltip.append("Status: ✅ Viewed (").append(viewCount).append(" times)\n");
        } else {
            tooltip.append("Status: ⭕ Not viewed\n");
        }

        tooltip.append("Click to view content");

        return tooltip.toString();
    }

    // ===== CUSTOM TREE CELL =====

    private class TopicTreeCell extends TreeCell<String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setTooltip(null);
                getStyleClass().removeAll("category-item", "topic-item", "viewed-topic", "unviewed-topic");
            } else {
                setText(item);

                // Style based on tree level and content
                getStyleClass().removeAll("category-item", "topic-item", "viewed-topic", "unviewed-topic");

                TreeItem<String> treeItem = getTreeItem();
                if (treeItem != null) {
                    if (treeItem.isLeaf()) {
                        // Topic item
                        getStyleClass().add("topic-item");

                        // Add tooltip for topic items
                        Tooltip tooltip = new Tooltip(createTooltipText(item));
                        setTooltip(tooltip);

                        // Check if viewed
                        boolean viewed = learningContentService.isTopicViewed(item);
                        if (viewed) {
                            getStyleClass().add("viewed-topic");
                            setText("✅ " + item);
                        } else {
                            getStyleClass().add("unviewed-topic");
                            setText("📖 " + item);
                        }

                    } else {
                        // Category item
                        getStyleClass().add("category-item");
                        setTooltip(null); // No tooltip for categories

                        // Add category icons
                        String icon = getCategoryIcon(item);
                        setText(icon + " " + item);
                    }
                }
            }
        }

        private String getCategoryIcon(String categoryName) {
            return switch (categoryName) {
                case "Data Definition Language (DDL)" -> "🏗️";
                case "Data Manipulation Language (DML)" -> "🔄";
                case "Data Control Language (DCL)" -> "🔐";
                case "Transaction Control Language (TCL)" -> "💾";
                case "Database Normalization" -> "📐";
                default -> "📁";
            };
        }
    }

    // ===== PUBLIC METHODS =====

    public void refreshTopicStatus() {
        // Refresh the display to show updated view status
        TreeItem<String> root = getRoot();
        if (root != null) {
            refreshTreeItem(root);
        }
    }

    private void refreshTreeItem(TreeItem<String> item) {
        // Force refresh by temporarily changing the value
        if (item != null) {
            String value = item.getValue();
            item.setValue(null);
            item.setValue(value);

            for (TreeItem<String> child : item.getChildren()) {
                refreshTreeItem(child);
            }
        }
    }

    public void selectTopic(String topicName) {
        TreeItem<String> item = findTreeItem(getRoot(), topicName);
        if (item != null) {
            getSelectionModel().select(item);
            scrollTo(getSelectionModel().getSelectedIndex());
        }
    }

    private TreeItem<String> findTreeItem(TreeItem<String> parent, String value) {
        if (parent.getValue() != null && parent.getValue().equals(value)) {
            return parent;
        }

        for (TreeItem<String> child : parent.getChildren()) {
            TreeItem<String> result = findTreeItem(child, value);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public String getSelectedTopic() {
        TreeItem<String> selected = getSelectionModel().getSelectedItem();
        return selected != null && selected.isLeaf() ? selected.getValue() : null;
    }

    public boolean hasSelection() {
        return getSelectionModel().getSelectedItem() != null;
    }
}