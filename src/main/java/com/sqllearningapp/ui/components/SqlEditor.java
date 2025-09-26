package com.sqllearningapp.ui.components;

import javafx.concurrent.Task;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Enhanced SQL Editor - Preserves and improves your syntax highlighting
 * with modern features like autocomplete, context menu, and advanced shortcuts
 */
public class SqlEditor extends CodeArea {

    // Enhanced SQL Keywords (preserving your SqlSyntaxHighlighter.java keywords)
    private static final String[] KEYWORDS = {
            // Core SQL Keywords
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER",
            "TABLE", "DATABASE", "INDEX", "VIEW", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER",
            "GROUP", "ORDER", "BY", "HAVING", "DISTINCT", "UNION", "AS", "AND", "OR", "NOT",
            "NULL", "IS", "IN", "LIKE", "BETWEEN", "EXISTS", "CASE", "WHEN", "THEN", "ELSE", "END",

            // DDL Keywords
            "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT", "DEFAULT", "AUTO_INCREMENT",
            "UNIQUE", "CHECK", "CASCADE", "RESTRICT", "SET", "NULL_ACTION",

            // Data Types
            "INT", "INTEGER", "VARCHAR", "CHAR", "TEXT", "DATE", "DATETIME", "TIMESTAMP",
            "DECIMAL", "FLOAT", "DOUBLE", "BOOLEAN", "BIGINT", "SMALLINT", "TINYINT",

            // Functions
            "COUNT", "SUM", "AVG", "MIN", "MAX", "CONCAT", "SUBSTRING", "LENGTH", "UPPER", "LOWER",
            "TRIM", "LTRIM", "RTRIM", "REPLACE", "COALESCE", "ISNULL", "ROUND", "FLOOR", "CEIL",

            // Transaction Control
            "COMMIT", "ROLLBACK", "SAVEPOINT", "BEGIN", "TRANSACTION", "START",

            // Data Control
            "GRANT", "REVOKE", "PRIVILEGES", "ALL", "USAGE", "EXECUTE", "REFERENCES_PRIVILEGE",

            // Window Functions
            "ROW_NUMBER", "RANK", "DENSE_RANK", "PARTITION", "OVER", "ROWS", "RANGE", "PRECEDING", "FOLLOWING",

            // Additional Keywords
            "LIMIT", "OFFSET", "TOP", "FETCH", "FIRST", "LAST", "WITH", "RECURSIVE"
    };

    // Enhanced Patterns
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "\\b(" + String.join("|", KEYWORDS) + ")\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern STRING_PATTERN = Pattern.compile("'([^'\\\\]|\\\\.)*'");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "--[^\r\n]*|/\\*([^*]|\\*+[^*/])*\\*+/");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile(
            "[=<>!]+|\\+|\\-|\\*|\\/|\\%");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "\\b[A-Za-z_][A-Za-z0-9_]*(?=\\s*\\()");

    // Context menu
    private ContextMenu contextMenu;

    // Auto-completion
    private final ArrayList<String> autoCompleteWords;  // Use ArrayList specifically

    // Syntax highlighting timer
    private java.util.Timer syntaxTimer;

    public SqlEditor() {
        super();
        this.autoCompleteWords = new ArrayList<>();

        setupEditor();
        setupSyntaxHighlighting();
        setupKeyboardShortcuts();
        setupContextMenu();
        setupAutoCompletion();

        // Load auto-complete words
        Collections.addAll(autoCompleteWords, KEYWORDS);
    }

    private void setupEditor() {
        // Enable line numbers (preserving your original approach)
        setParagraphGraphicFactory(LineNumberFactory.get(this));

        // Set welcome message with current user
        String welcomeMessage = String.format(
                "-- Welcome to SQL Learning Professional Edition!\n" +
                        "-- User: %s\n" +
                        "-- Date: %s\n" +
                        "-- Write your SQL queries here...\n\n",
                "SithuHan-SithuHan",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        replaceText(0, 0, welcomeMessage);

        // Enhanced styling
        getStyleClass().add("sql-editor");
        setStyle("""
            -fx-font-family: 'JetBrains Mono', 'Consolas', 'Monaco', 'Courier New', monospace;
            -fx-font-size: 14px;
            -fx-background-color: #fafafa;
            -fx-text-fill: #2d3748;
            """);

        // Enable text wrapping
        setWrapText(false);

        // Set tab size
        setStyle(getStyle() + "-fx-tab-size: 4;");
    }

    private void setupSyntaxHighlighting() {
        // FIX: Use Timer instead of ReactFX to avoid module issues
        textProperty().addListener((obs, oldText, newText) -> {
            // Cancel previous timer
            if (syntaxTimer != null) {
                syntaxTimer.cancel();
            }

            // Start new timer with 300ms delay
            syntaxTimer = new java.util.Timer(true);
            syntaxTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        Task<StyleSpans<Collection<String>>> task = new Task<>() {
                            @Override
                            protected StyleSpans<Collection<String>> call() {
                                return computeHighlighting(getText());
                            }
                        };

                        task.setOnSucceeded(e -> {
                            try {
                                setStyleSpans(0, task.getValue());
                            } catch (Exception ex) {
                                // Handle any styling errors gracefully
                                System.err.println("Syntax highlighting error: " + ex.getMessage());
                            }
                        });

                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();
                    });
                }
            }, 300);
        });
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        // Combined pattern for all syntax elements
        Pattern pattern = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN.pattern() + ")"
                        + "|(?<STRING>" + STRING_PATTERN.pattern() + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN.pattern() + ")"
                        + "|(?<NUMBER>" + NUMBER_PATTERN.pattern() + ")"
                        + "|(?<FUNCTION>" + FUNCTION_PATTERN.pattern() + ")"
                        + "|(?<OPERATOR>" + OPERATOR_PATTERN.pattern() + ")",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String styleClass = null;

            if (matcher.group("KEYWORD") != null) {
                styleClass = "sql-keyword";
            } else if (matcher.group("STRING") != null) {
                styleClass = "sql-string";
            } else if (matcher.group("COMMENT") != null) {
                styleClass = "sql-comment";
            } else if (matcher.group("NUMBER") != null) {
                styleClass = "sql-number";
            } else if (matcher.group("FUNCTION") != null) {
                styleClass = "sql-function";
            } else if (matcher.group("OPERATOR") != null) {
                styleClass = "sql-operator";
            }

            if (styleClass != null) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastEnd = matcher.end();
            }
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
        return spansBuilder.create();
    }

    private void setupKeyboardShortcuts() {
        setOnKeyPressed(event -> {
            // F5 for execution (preserving your original)
            if (event.getCode() == KeyCode.F5) {
                event.consume();
                fireEvent(new SqlEditorEvent(SqlEditorEvent.EXECUTE_QUERY, getText()));
            }

            // Ctrl+Enter for submission (preserving your original)
            else if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                event.consume();
                fireEvent(new SqlEditorEvent(SqlEditorEvent.SUBMIT_ANSWER, getText()));
            }

            // Ctrl+/ for comment toggle
            else if (event.getCode() == KeyCode.SLASH && event.isControlDown()) {
                event.consume();
                toggleLineComment();
            }

            // Tab for auto-completion
            else if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                if (handleAutoCompletion()) {
                    event.consume();
                }
            }

            // Ctrl+Space for manual auto-completion
            else if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
                event.consume();
                showAutoCompletion();
            }

            // Auto-indent on Enter
            else if (event.getCode() == KeyCode.ENTER && !event.isControlDown()) {
                handleAutoIndent();
            }

            // Ctrl+D for duplicate line
            else if (event.getCode() == KeyCode.D && event.isControlDown()) {
                event.consume();
                duplicateCurrentLine();
            }

            // Ctrl+Shift+K for delete line
            else if (event.getCode() == KeyCode.K && event.isControlDown() && event.isShiftDown()) {
                event.consume();
                deleteCurrentLine();
            }
        });
    }

    private void setupContextMenu() {
        contextMenu = new ContextMenu();

        // Standard editing operations
        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setOnAction(e -> cut());
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> copy());
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> paste());
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));

        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> selectAll());
        selectAllItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));

        // SQL-specific operations
        MenuItem formatItem = new MenuItem("Format SQL");
        formatItem.setOnAction(e -> formatSQL());
        formatItem.setAccelerator(new KeyCodeCombination(KeyCode.F,
                KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

        MenuItem commentItem = new MenuItem("Toggle Comment");
        commentItem.setOnAction(e -> toggleLineComment());
        commentItem.setAccelerator(new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN));

        MenuItem executeItem = new MenuItem("Execute Query");
        executeItem.setOnAction(e -> fireEvent(new SqlEditorEvent(SqlEditorEvent.EXECUTE_QUERY, getText())));
        executeItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));

        contextMenu.getItems().addAll(
                cutItem, copyItem, pasteItem, selectAllItem,
                new SeparatorMenuItem(),
                formatItem, commentItem,
                new SeparatorMenuItem(),
                executeItem
        );

        // Show context menu on right click
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
    }

    private void setupAutoCompletion() {
        // Auto-completion will be triggered by Tab or Ctrl+Space
        // Implementation is in handleAutoCompletion() and showAutoCompletion()
    }

    // ===== AUTO-COMPLETION METHODS =====

    private boolean handleAutoCompletion() {
        int caretPos = getCaretPosition();
        String text = getText();

        // Find the word being typed
        int wordStart = findWordStart(text, caretPos);
        if (wordStart == caretPos) {
            return false; // No word to complete
        }

        String partialWord = text.substring(wordStart, caretPos).toUpperCase();

        // Find matching completions - FIX: Use ArrayList methods directly
        ArrayList<String> matches = new ArrayList<>();
        for (String word : autoCompleteWords) {
            if (word.startsWith(partialWord)) {
                matches.add(word);
            }
        }
        Collections.sort(matches);

        int matchCount = matches.size();  // FIX: Direct access to ArrayList size
        if (matchCount == 1) {
            // Single match - complete it
            String completion = matches.get(0).substring(partialWord.length());
            insertText(caretPos, completion);
            return true;
        } else if (matchCount > 1) {
            // Multiple matches - show the first common prefix
            String commonPrefix = findCommonPrefix(matches);
            if (commonPrefix.length() > partialWord.length()) {
                String completion = commonPrefix.substring(partialWord.length());
                insertText(caretPos, completion);
                return true;
            }
        }

        return false;
    }

    private void showAutoCompletion() {
        int caretPos = getCaretPosition();
        String text = getText();

        int wordStart = findWordStart(text, caretPos);
        String partialWord = wordStart < caretPos ?
                text.substring(wordStart, caretPos).toUpperCase() : "";

        // FIX: Use ArrayList methods directly with manual limiting
        ArrayList<String> matches = new ArrayList<>();
        int count = 0;
        for (String word : autoCompleteWords) {
            if (word.startsWith(partialWord) && count < 10) {
                matches.add(word);
                count++;
            }
        }
        Collections.sort(matches);

        int matchCount = matches.size();  // FIX: Direct access to ArrayList size
        if (matchCount > 0) {
            // For now, just complete with the first match
            // In a full implementation, you'd show a popup list
            if (matchCount == 1 || partialWord.isEmpty()) {
                String completion = matches.get(0);
                if (!partialWord.isEmpty()) {
                    completion = completion.substring(partialWord.length());
                }
                insertText(caretPos, completion);
            }
        }
    }

    private int findWordStart(String text, int pos) {
        int start = pos;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }
        return start;
    }

    private String findCommonPrefix(ArrayList<String> words) {  // FIX: Use ArrayList directly
        int wordCount = words.size();  // FIX: Direct access to ArrayList size
        if (wordCount == 0) return "";

        String first = words.get(0);
        int prefixLen = first.length();

        for (String word : words) {
            prefixLen = Math.min(prefixLen, word.length());
            for (int i = 0; i < prefixLen; i++) {
                if (first.charAt(i) != word.charAt(i)) {
                    prefixLen = i;
                    break;
                }
            }
        }

        return first.substring(0, prefixLen);
    }

    // ===== EDITING HELPER METHODS =====

    private void toggleLineComment() {
        int caretPos = getCaretPosition();
        int currentParagraph = getCurrentParagraph();
        String lineText = getParagraph(currentParagraph).getText();

        if (lineText.trim().startsWith("--")) {
            // Remove comment
            String newText = lineText.replaceFirst("^(\\s*)--\\s?", "$1");
            selectRange(currentParagraph, 0, currentParagraph, lineText.length());
            replaceSelection(newText);
        } else {
            // Add comment
            String indent = lineText.replaceAll("^(\\s*).*", "$1");
            String newText = indent + "-- " + lineText.substring(indent.length());
            selectRange(currentParagraph, 0, currentParagraph, lineText.length());
            replaceSelection(newText);
        }
    }

    private void handleAutoIndent() {
        int caretPos = getCaretPosition();
        int currentParagraph = getCurrentParagraph();

        if (currentParagraph > 0) {
            String prevLine = getParagraph(currentParagraph - 1).getText();
            String indent = prevLine.replaceAll("^(\\s*).*", "$1");

            // Add extra indent for certain SQL keywords
            if (prevLine.trim().toUpperCase().matches(".*(SELECT|FROM|WHERE|GROUP BY|ORDER BY|HAVING).*")) {
                indent += "    "; // 4 spaces
            }

            insertText(caretPos, "\n" + indent);
        }
    }

    private void duplicateCurrentLine() {
        int currentParagraph = getCurrentParagraph();
        String lineText = getParagraph(currentParagraph).getText();

        // Move to end of line and add duplicated content
        moveTo(currentParagraph, lineText.length());
        insertText(getCaretPosition(), "\n" + lineText);
    }

    private void deleteCurrentLine() {
        int currentParagraph = getCurrentParagraph();

        if (getParagraphs().size() > 1) {
            int start = getAbsolutePosition(currentParagraph, 0);
            int end = currentParagraph < getParagraphs().size() - 1 ?
                    getAbsolutePosition(currentParagraph + 1, 0) :
                    getAbsolutePosition(currentParagraph, getParagraph(currentParagraph).length());

            deleteText(start, end);
        } else {
            // Last line - just clear it
            selectRange(currentParagraph, 0, currentParagraph, getParagraph(currentParagraph).length());
            replaceSelection("");
        }
    }

    private void formatSQL() {
        String sql = getText();
        if (sql.trim().isEmpty()) return;

        // Basic SQL formatting
        String formatted = sql
                .replaceAll("(?i)\\b(SELECT)\\b", "\nSELECT")
                .replaceAll("(?i)\\b(FROM)\\b", "\nFROM")
                .replaceAll("(?i)\\b(WHERE)\\b", "\nWHERE")
                .replaceAll("(?i)\\b(GROUP BY)\\b", "\nGROUP BY")
                .replaceAll("(?i)\\b(HAVING)\\b", "\nHAVING")
                .replaceAll("(?i)\\b(ORDER BY)\\b", "\nORDER BY")
                .replaceAll("(?i)\\b(INNER JOIN|LEFT JOIN|RIGHT JOIN|FULL JOIN)\\b", "\n$1")
                .replaceAll("(?i)\\b(UNION)\\b", "\n$1")
                .replaceAll("\\s*,\\s*", ",\n       ")  // Format column lists
                .replaceAll("\\n\\s*\\n", "\n")         // Remove extra blank lines
                .trim();

        replaceText(formatted);
    }

    // ===== TEMPLATE INSERTION =====

    public void insertTemplate(String templateName) {
        String template = getTemplate(templateName);
        if (template != null) {
            int caretPos = getCaretPosition();
            insertText(caretPos, template);

            // Position cursor at placeholder if exists
            int placeholderPos = template.indexOf("${cursor}");
            if (placeholderPos != -1) {
                selectRange(caretPos + placeholderPos, caretPos + placeholderPos + "${cursor}".length());
                replaceSelection("");
            }
        }
    }

    private String getTemplate(String name) {
        return switch (name.toLowerCase()) {
            case "select" -> """
                SELECT ${cursor}
                FROM table_name
                WHERE condition;
                """;
            case "insert" -> """
                INSERT INTO table_name (column1, column2)
                VALUES (${cursor}, value2);
                """;
            case "update" -> """
                UPDATE table_name
                SET column1 = ${cursor}
                WHERE condition;
                """;
            case "delete" -> """
                DELETE FROM table_name
                WHERE ${cursor};
                """;
            case "create" -> """
                CREATE TABLE table_name (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    ${cursor} VARCHAR(255) NOT NULL
                );
                """;
            case "join" -> """
                SELECT *
                FROM table1 t1
                INNER JOIN table2 t2 ON t1.id = t2.${cursor};
                """;
            default -> null;
        };
    }

    // ===== UTILITY METHODS =====

    public void setQuery(String query) {
        replaceText(query);
    }

    public String getQuery() {
        return getText();
    }

    public void clearEditor() {
        clear();
    }

    public boolean hasUnsavedChanges() {
        // This would track if the content has been modified since last save
        // Implementation depends on your save/load requirements
        return !getText().trim().isEmpty();
    }

    public void setReadOnly(boolean readOnly) {
        setEditable(!readOnly);
        if (readOnly) {
            getStyleClass().add("read-only");
        } else {
            getStyleClass().remove("read-only");
        }
    }

    // Clean up timer when editor is disposed
    public void dispose() {
        if (syntaxTimer != null) {
            syntaxTimer.cancel();
        }
    }

    // ===== CUSTOM EVENT SYSTEM =====

    public static class SqlEditorEvent extends javafx.event.Event {
        public static final javafx.event.EventType<SqlEditorEvent> EXECUTE_QUERY =
                new javafx.event.EventType<>("EXECUTE_QUERY");
        public static final javafx.event.EventType<SqlEditorEvent> SUBMIT_ANSWER =
                new javafx.event.EventType<>("SUBMIT_ANSWER");
        public static final javafx.event.EventType<SqlEditorEvent> CONTENT_CHANGED =
                new javafx.event.EventType<>("CONTENT_CHANGED");

        private final String query;

        public SqlEditorEvent(javafx.event.EventType<SqlEditorEvent> eventType, String query) {
            super(eventType);
            this.query = query;
        }

        public String getQuery() {
            return query;
        }
    }
}