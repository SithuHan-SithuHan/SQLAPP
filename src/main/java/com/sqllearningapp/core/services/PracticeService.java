package com.sqllearningapp.core.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sqllearningapp.core.database.QueryExecutor;
import com.sqllearningapp.core.models.PracticeQuestion;
import com.sqllearningapp.core.models.QueryResult;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced Practice Service - Preserves ALL your existing questions and functionality
 * Added better organization, statistics, validation, and progress tracking
 */
@Slf4j
public class PracticeService {

    private final List<PracticeQuestion> questions;
    private final Set<String> completedQuestions;
    private final Map<String, Integer> questionAttempts;
    private final Map<String, LocalDateTime> lastAttempted;
    private final ObjectMapper objectMapper;
    private final Map<String, Integer> userStats;
    private final QueryExecutor queryExecutor;

    public PracticeService(QueryExecutor queryExecutor) {
        this.questions = new ArrayList<>();
        this.completedQuestions = ConcurrentHashMap.newKeySet();
        this.questionAttempts = new ConcurrentHashMap<>();
        this.lastAttempted = new ConcurrentHashMap<>();
        this.objectMapper = createObjectMapper();
        this.userStats = new ConcurrentHashMap<>();
        this.queryExecutor = queryExecutor;

        initializeStats();
        loadQuestions();
        loadUserProgress();

        log.info("Practice service initialized with {} questions", questions.size());
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Initialize user statistics (enhanced from your original)
     */
    private void initializeStats() {
        userStats.put("totalQueriesExecuted", 0);
        userStats.put("successfulQueries", 0);
        userStats.put("currentStreak", 0);
        userStats.put("bestStreak", 0);
        userStats.put("totalTimeSpentMs", 0);
        userStats.put("averageScore", 0);
        userStats.put("totalPointsEarned", 0);
    }

    /**
     * Load all practice questions (preserving your exact questions)
     */
    private void loadQuestions() {
        log.info("Loading practice questions...");

        // Easy Questions (exactly as in your original)
        questions.add(createEasyQuestionCombineTwoTables());
        questions.add(createEasyQuestion2());
        questions.add(createEasyQuestion3());
        questions.add(createEasyQuestion4());
        questions.add(createEasyQuestion5());

        // Medium Questions (exactly as in your original)
        questions.add(createMediumQuestion1());
        questions.add(createMediumQuestion2());
        questions.add(createMediumQuestion3());
        questions.add(createMediumQuestion4());
        questions.add(createMediumQuestion5());

        // Hard Questions (exactly as in your original)
        questions.add(createHardQuestion1());
        questions.add(createHardQuestion2());
        questions.add(createHardQuestion3());
        questions.add(createHardQuestion4());

        // Pro Questions (exactly as in your original)
        questions.add(createProQuestion1());
        questions.add(createProQuestion2());
        questions.add(createProQuestion3());
        questions.add(createProQuestion4());

        log.info("Loaded {} practice questions", questions.size());
    }

    /**
     * Enhanced answer validation (improved from your original)
     */
    public ValidationResult validateAnswer(String questionId, String userQuery) {
        PracticeQuestion question = getQuestionById(questionId);
        if (question == null) {
            return ValidationResult.builder()
                    .correct(false)
                    .message("Question not found")
                    .questionId(questionId)
                    .build();
        }

        long startTime = System.currentTimeMillis();

        try {
            // Record attempt
            recordAttempt(questionId);

            // Execute user query
            QueryResult userResult = queryExecutor.executeQuery(userQuery, true);
            if (!userResult.isSuccess()) {
                return ValidationResult.builder()
                        .correct(false)
                        .message("Query execution failed: " + userResult.getMessage())
                        .questionId(questionId)
                        .userQuery(userQuery)
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Execute expected query for comparison
            QueryResult expectedResult = queryExecutor.executeQuery(question.getSolution(), true);

            // Enhanced comparison logic
            boolean isCorrect = compareResults(userResult, expectedResult);

            // Update statistics
            updateStats("totalQueriesExecuted", getStat("totalQueriesExecuted") + 1);

            ValidationResult result;
            if (isCorrect) {
                updateStats("successfulQueries", getStat("successfulQueries") + 1);
                markQuestionCompleted(questionId);
                incrementCurrentStreak();

                // Award points
                int points = question.getDefaultPoints();
                updateStats("totalPointsEarned", getStat("totalPointsEarned") + points);

                result = ValidationResult.builder()
                        .correct(true)
                        .message("🎉 Excellent! Your solution is correct!")
                        .questionId(questionId)
                        .userQuery(userQuery)
                        .userResult(userResult)
                        .expectedResult(expectedResult)
                        .pointsEarned(points)
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            } else {
                resetCurrentStreak();
                result = ValidationResult.builder()
                        .correct(false)
                        .message("❌ Not quite right. Compare your output with the expected result.")
                        .questionId(questionId)
                        .userQuery(userQuery)
                        .userResult(userResult)
                        .expectedResult(expectedResult)
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .hint(generateHint(question, userResult, expectedResult))
                        .build();
            }

            // Save progress
            saveUserProgress();
            return result;

        } catch (Exception e) {
            log.error("Error validating answer for question: {}", questionId, e);
            return ValidationResult.builder()
                    .correct(false)
                    .message("Validation error: " + e.getMessage())
                    .questionId(questionId)
                    .userQuery(userQuery)
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private boolean compareResults(QueryResult userResult, QueryResult expectedResult) {
        // Compare row counts
        if (userResult.getRowCount() != expectedResult.getRowCount()) {
            return false;
        }

        // Compare column counts
        if (userResult.getColumnCount() != expectedResult.getColumnCount()) {
            return false;
        }

        // Compare column names (case-insensitive)
        if (!compareColumnNames(userResult.getColumnNames(), expectedResult.getColumnNames())) {
            return false;
        }

        // Compare data rows
        return compareDataRows(userResult.getRows(), expectedResult.getRows());
    }

    private boolean compareColumnNames(List<String> userColumns, List<String> expectedColumns) {
        if (userColumns.size() != expectedColumns.size()) {
            return false;
        }

        for (int i = 0; i < userColumns.size(); i++) {
            if (!userColumns.get(i).equalsIgnoreCase(expectedColumns.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean compareDataRows(List<Map<String, Object>> userRows, List<Map<String, Object>> expectedRows) {
        if (userRows.size() != expectedRows.size()) {
            return false;
        }

        // Sort both result sets for consistent comparison
        List<Map<String, Object>> sortedUserRows = sortRows(userRows);
        List<Map<String, Object>> sortedExpectedRows = sortRows(expectedRows);

        for (int i = 0; i < sortedUserRows.size(); i++) {
            if (!compareRowValues(sortedUserRows.get(i), sortedExpectedRows.get(i))) {
                return false;
            }
        }

        return true;
    }

    private List<Map<String, Object>> sortRows(List<Map<String, Object>> rows) {
        return rows.stream()
                .sorted((row1, row2) -> {
                    // Simple sorting by first column value
                    Object val1 = row1.values().iterator().next();
                    Object val2 = row2.values().iterator().next();

                    if (val1 == null && val2 == null) return 0;
                    if (val1 == null) return -1;
                    if (val2 == null) return 1;

                    return val1.toString().compareTo(val2.toString());
                })
                .collect(Collectors.toList());
    }

    private boolean compareRowValues(Map<String, Object> userRow, Map<String, Object> expectedRow) {
        for (String key : expectedRow.keySet()) {
            Object expectedValue = expectedRow.get(key);
            Object userValue = userRow.get(key);

            if (!Objects.equals(normalizeValue(expectedValue), normalizeValue(userValue))) {
                return false;
            }
        }
        return true;
    }

    private Object normalizeValue(Object value) {
        if (value == null) return null;

        // Convert numbers to strings for consistent comparison
        if (value instanceof Number) {
            return value.toString();
        }

        // Trim strings
        if (value instanceof String) {
            return ((String) value).trim();
        }

        return value;
    }

    private String generateHint(PracticeQuestion question, QueryResult userResult, QueryResult expectedResult) {
        StringBuilder hint = new StringBuilder();

        // Row count hint
        if (userResult.getRowCount() != expectedResult.getRowCount()) {
            hint.append("💡 Row count mismatch: Expected ")
                    .append(expectedResult.getRowCount())
                    .append(" rows, got ")
                    .append(userResult.getRowCount())
                    .append(". ");
        }

        // Column count hint
        if (userResult.getColumnCount() != expectedResult.getColumnCount()) {
            hint.append("💡 Column count mismatch: Expected ")
                    .append(expectedResult.getColumnCount())
                    .append(" columns, got ")
                    .append(userResult.getColumnCount())
                    .append(". ");
        }

        // Add the original hint
        if (question.getHint() != null && !question.getHint().isEmpty()) {
            hint.append("\n").append(question.getHint());
        }

        return hint.toString();
    }

    private void recordAttempt(String questionId) {
        questionAttempts.put(questionId, questionAttempts.getOrDefault(questionId, 0) + 1);
        lastAttempted.put(questionId, LocalDateTime.now());
    }

    // ===== ALL YOUR EXISTING METHODS (PRESERVED AND ENHANCED) =====

    public List<PracticeQuestion> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    public List<PracticeQuestion> getQuestionsByDifficulty(String difficulty) {
        if ("all".equalsIgnoreCase(difficulty)) {
            return getAllQuestions();
        }

        return questions.stream()
                .filter(q -> q.getDifficulty().name().equalsIgnoreCase(difficulty))
                .collect(Collectors.toList());
    }

    public List<PracticeQuestion> getQuestionsByDifficulty(PracticeQuestion.Difficulty difficulty) {
        return questions.stream()
                .filter(q -> q.getDifficulty() == difficulty)
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getQuestionCountsByDifficulty() {
        Map<String, Integer> counts = new HashMap<>();
        for (PracticeQuestion.Difficulty diff : PracticeQuestion.Difficulty.values()) {
            counts.put(diff.name().toLowerCase(),
                    (int) questions.stream().filter(q -> q.getDifficulty() == diff).count());
        }
        return counts;
    }

    public PracticeQuestion getQuestionById(String questionId) {
        return questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElse(null);
    }

    public void markQuestionCompleted(String questionId) {
        if (!completedQuestions.contains(questionId)) {
            completedQuestions.add(questionId);
            log.info("Question {} marked as completed", questionId);
        }
    }

    public boolean isQuestionCompleted(String questionId) {
        return completedQuestions.contains(questionId);
    }

    public int getCompletedQuestionsCount() {
        return completedQuestions.size();
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    public Map<String, Integer> getCompletedCountsByDifficulty() {
        Map<String, Integer> counts = new HashMap<>();
        for (PracticeQuestion.Difficulty diff : PracticeQuestion.Difficulty.values()) {
            String diffName = diff.name().toLowerCase();
            counts.put(diffName, (int) completedQuestions.stream()
                    .map(this::getQuestionById)
                    .filter(Objects::nonNull)
                    .filter(q -> q.getDifficulty() == diff)
                    .count());
        }
        return counts;
    }

    public void updateStats(String statName, int value) {
        userStats.put(statName, value);
    }

    public int getStat(String statName) {
        return userStats.getOrDefault(statName, 0);
    }

    public Map<String, Integer> getAllStats() {
        return new HashMap<>(userStats);
    }

    private void incrementCurrentStreak() {
        int currentStreak = userStats.get("currentStreak") + 1;
        userStats.put("currentStreak", currentStreak);

        int bestStreak = userStats.get("bestStreak");
        if (currentStreak > bestStreak) {
            userStats.put("bestStreak", currentStreak);
        }
    }

    public void resetCurrentStreak() {
        userStats.put("currentStreak", 0);
    }

    // Progress persistence (enhanced from your original)
    private void loadUserProgress() {
        try {
            File progressFile = new File("user_progress.json");
            if (progressFile.exists()) {
                Map<String, Object> progressData = objectMapper.readValue(
                        progressFile, new TypeReference<Map<String, Object>>() {});

                if (progressData.containsKey("completedQuestions")) {
                    List<String> completed = (List<String>) progressData.get("completedQuestions");
                    completedQuestions.addAll(completed);
                }

                if (progressData.containsKey("userStats")) {
                    Map<String, Object> stats = (Map<String, Object>) progressData.get("userStats");
                    stats.forEach((key, value) -> {
                        if (value instanceof Number) {
                            userStats.put(key, ((Number) value).intValue());
                        }
                    });
                }

                log.info("User progress loaded: {} completed questions", completedQuestions.size());
            }
        } catch (IOException e) {
            log.warn("Could not load user progress, starting fresh: {}", e.getMessage());
        }
    }

    public void saveUserProgress() {
        try {
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("completedQuestions", new ArrayList<>(completedQuestions));
            progressData.put("questionAttempts", new HashMap<>(questionAttempts));
            progressData.put("userStats", new HashMap<>(userStats));
            progressData.put("lastUpdated", LocalDateTime.now().toString());
            progressData.put("version", "2.0");

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("user_progress.json"), progressData);

            log.debug("User progress saved successfully");

        } catch (IOException e) {
            log.error("Failed to save user progress", e);
        }
    }

// Continuing PracticeService.java from createEasyQuestionCombineTwoTables()

    private PracticeQuestion createEasyQuestionCombineTwoTables() {
        return PracticeQuestion.builder()
                .id("easy_1")
                .title("Combine Two Tables")
                .description("""
                    <div style='font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; padding: 20px; line-height: 1.6;'>
                        <h3>Problem Description</h3>
                        <p>Write a solution to report the <strong>first name</strong>, <strong>last name</strong>, <strong>city</strong>, and <strong>state</strong> of each person in the Person table.</p>
                        <p>If the address of a personId is not present in the Address table, report <em>null</em> instead.</p>
                        
                        <h4>Table Schemas</h4>
                        
                        <p><strong>Person Table:</strong></p>
                        <table border="1" style="border-collapse: collapse; margin: 10px 0;">
                            <tr style="background-color: #f0f0f0;">
                                <th style="padding: 8px;">Column</th>
                                <th style="padding: 8px;">Type</th>
                            </tr>
                            <tr>
                                <td style="padding: 6px;">personId</td>
                                <td style="padding: 6px;">int</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px;">lastName</td>
                                <td style="padding: 6px;">varchar</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px;">firstName</td>
                                <td style="padding: 6px;">varchar</td>
                            </tr>
                        </table>
                        
                        <p><strong>Address Table:</strong></p>
                        <table border="1" style="border-collapse: collapse; margin: 10px 0;">
                            <tr style="background-color: #f0f0f0;">
                                <th style="padding: 8px;">Column</th>
                                <th style="padding: 8px;">Type</th>
                            </tr>
                            <tr>
                                <td style="padding: 6px;">addressId</td>
                                <td style="padding: 6px;">int</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px;">personId</td>
                                <td style="padding: 6px;">int</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px;">city</td>
                                <td style="padding: 6px;">varchar</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px;">state</td>
                                <td style="padding: 6px;">varchar</td>
                            </tr>
                        </table>
                        
                        <h4>Key Points</h4>
                        <ul>
                            <li>Use <strong>LEFT JOIN</strong> to include all persons</li>
                            <li>Join on <strong>personId</strong></li>
                            <li>Missing addresses will show as null</li>
                        </ul>
                    </div>
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT p.firstName, p.lastName, a.city, a.state \nFROM Person p \nLEFT JOIN Address a ON p.personId = a.personId;")
                .difficulty(PracticeQuestion.Difficulty.EASY)
                .hint("💡 Use LEFT JOIN to include all persons and get null for missing addresses.")
                .solution("SELECT p.firstName, p.lastName, a.city, a.state FROM Person p LEFT JOIN Address a ON p.personId = a.personId;")
                .category("DML")
                .points(10)
                .build();
    }

    private PracticeQuestion createEasyQuestion2() {
        return PracticeQuestion.builder()
                .id("easy_2")
                .title("Find High Salary Employees")
                .description("""
                    Write a SQL query to find all employees with salary greater than 50000.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | salary      | decimal |
                    +-------------+---------+
                    
                    Expected output: All employees with salary > 50000
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT * FROM employees WHERE salary > 50000;")
                .difficulty(PracticeQuestion.Difficulty.EASY)
                .hint("💡 Use WHERE clause with > operator to filter by salary.")
                .solution("SELECT * FROM employees WHERE salary > 50000;")
                .category("DML")
                .points(10)
                .build();
    }

    private PracticeQuestion createEasyQuestion3() {
        return PracticeQuestion.builder()
                .id("easy_3")
                .title("Count Total Employees")
                .description("""
                    Write a SQL query to count the total number of employees.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    +-------------+---------+
                    
                    Expected output: A single number showing total count
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT COUNT(*) as total_employees FROM employees;")
                .difficulty(PracticeQuestion.Difficulty.EASY)
                .hint("💡 Use COUNT(*) function to count all rows.")
                .solution("SELECT COUNT(*) FROM employees;")
                .category("DML")
                .points(10)
                .build();
    }

    private PracticeQuestion createEasyQuestion4() {
        return PracticeQuestion.builder()
                .id("easy_4")
                .title("Select Employees from Engineering")
                .description("""
                    Write a SQL query to find all employees who work in the 'Engineering' department.
                    
                    Table: employees
                    +---------------+---------+
                    | Column Name   | Type    |
                    +---------------+---------+
                    | id            | int     |
                    | first_name    | varchar |
                    | last_name     | varchar |
                    | department_id | int     |
                    +---------------+---------+
                    
                    Table: departments
                    +-----------------+---------+
                    | Column Name     | Type    |
                    +-----------------+---------+
                    | id              | int     |
                    | department_name | varchar |
                    +-----------------+---------+
                    
                    Expected output: All employees in Engineering department
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT e.* FROM employees e \nJOIN departments d ON e.department_id = d.id \nWHERE d.department_name = 'Engineering';")
                .difficulty(PracticeQuestion.Difficulty.EASY)
                .hint("💡 Use JOIN to connect tables and WHERE to filter by department name.")
                .solution("SELECT e.* FROM employees e JOIN departments d ON e.department_id = d.id WHERE d.department_name = 'Engineering';")
                .category("DML")
                .points(10)
                .build();
    }

    private PracticeQuestion createEasyQuestion5() {
        return PracticeQuestion.builder()
                .id("easy_5")
                .title("Find Employees Hired After 2020")
                .description("""
                    Write a SQL query to find all employees hired after January 1, 2020.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | hire_date   | date    |
                    +-------------+---------+
                    
                    Expected output: All employees hired after 2020-01-01
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT * FROM employees WHERE hire_date > '2020-01-01';")
                .difficulty(PracticeQuestion.Difficulty.EASY)
                .hint("💡 Use WHERE clause with date comparison.")
                .solution("SELECT * FROM employees WHERE hire_date > '2020-01-01';")
                .category("DML")
                .points(10)
                .build();
    }

    // MEDIUM LEVEL QUESTIONS
    private PracticeQuestion createMediumQuestion1() {
        return PracticeQuestion.builder()
                .id("medium_1")
                .title("Average Salary by Department")
                .description("""
                    Write a SQL query to find the average salary for each department.
                    
                    Table: employees
                    +---------------+---------+
                    | Column Name   | Type    |
                    +---------------+---------+
                    | id            | int     |
                    | salary        | decimal |
                    | department_id | int     |
                    +---------------+---------+
                    
                    Table: departments
                    +-----------------+---------+
                    | Column Name     | Type    |
                    +-----------------+---------+
                    | id              | int     |
                    | department_name | varchar |
                    +-----------------+---------+
                    
                    Expected output: Department name and average salary
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT d.department_name, AVG(e.salary) as avg_salary \nFROM employees e \nJOIN departments d ON e.department_id = d.id \nGROUP BY d.id, d.department_name;")
                .difficulty(PracticeQuestion.Difficulty.MEDIUM)
                .hint("💡 Use GROUP BY to group by department and AVG() to calculate average salary.")
                .solution("SELECT d.department_name, AVG(e.salary) as avg_salary FROM employees e JOIN departments d ON e.department_id = d.id GROUP BY d.id, d.department_name;")
                .category("DML")
                .points(20)
                .build();
    }

    private PracticeQuestion createMediumQuestion2() {
        return PracticeQuestion.builder()
                .id("medium_2")
                .title("Top 5 Highest Paid Employees")
                .description("""
                    Write a SQL query to find the top 5 highest paid employees with their names and salaries.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | salary      | decimal |
                    +-------------+---------+
                    
                    Expected output: Top 5 employees sorted by salary (highest first)
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT first_name, last_name, salary \nFROM employees \nORDER BY salary DESC \nLIMIT 5;")
                .difficulty(PracticeQuestion.Difficulty.MEDIUM)
                .hint("💡 Use ORDER BY DESC to sort by salary in descending order and LIMIT to get top 5.")
                .solution("SELECT first_name, last_name, salary FROM employees ORDER BY salary DESC LIMIT 5;")
                .category("DML")
                .points(20)
                .build();
    }

    private PracticeQuestion createMediumQuestion3() {
        return PracticeQuestion.builder()
                .id("medium_3")
                .title("Employees with Names Starting with 'J'")
                .description("""
                    Write a SQL query to find all employees whose first name starts with 'J'.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    +-------------+---------+
                    
                    Expected output: All employees with first name starting with 'J'
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT * FROM employees WHERE first_name LIKE 'J%';")
                .difficulty(PracticeQuestion.Difficulty.MEDIUM)
                .hint("💡 Use LIKE operator with wildcard % to match names starting with 'J'.")
                .solution("SELECT * FROM employees WHERE first_name LIKE 'J%';")
                .category("DML")
                .points(20)
                .build();
    }

    private PracticeQuestion createMediumQuestion4() {
        return PracticeQuestion.builder()
                .id("medium_4")
                .title("Department Employee Count")
                .description("""
                    Write a SQL query to show each department with the number of employees in it.
                    
                    Table: employees
                    +---------------+---------+
                    | Column Name   | Type    |
                    +---------------+---------+
                    | id            | int     |
                    | department_id | int     |
                    +---------------+---------+
                    
                    Table: departments
                    +-----------------+---------+
                    | Column Name     | Type    |
                    +-----------------+---------+
                    | id              | int     |
                    | department_name | varchar |
                    +-----------------+---------+
                    
                    Expected output: Department name and employee count
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT d.department_name, COUNT(e.id) as employee_count \nFROM departments d \nLEFT JOIN employees e ON d.id = e.department_id \nGROUP BY d.id, d.department_name;")
                .difficulty(PracticeQuestion.Difficulty.MEDIUM)
                .hint("💡 Use LEFT JOIN to include departments with 0 employees and COUNT() to count employees.")
                .solution("SELECT d.department_name, COUNT(e.id) as employee_count FROM departments d LEFT JOIN employees e ON d.id = e.department_id GROUP BY d.id, d.department_name;")
                .category("DML")
                .points(20)
                .build();
    }

    private PracticeQuestion createMediumQuestion5() {
        return PracticeQuestion.builder()
                .id("medium_5")
                .title("Employees Hired This Year")
                .description("""
                    Write a SQL query to find employees hired in the current year.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | hire_date   | date    |
                    +-------------+---------+
                    
                    Expected output: All employees hired in the current year
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT * FROM employees WHERE YEAR(hire_date) = YEAR(CURDATE());")
                .difficulty(PracticeQuestion.Difficulty.MEDIUM)
                .hint("💡 Use YEAR() function to extract year from dates and CURDATE() for current date.")
                .solution("SELECT * FROM employees WHERE YEAR(hire_date) = YEAR(CURDATE());")
                .category("DML")
                .points(20)
                .build();
    }

    // HARD LEVEL QUESTIONS
    private PracticeQuestion createHardQuestion1() {
        return PracticeQuestion.builder()
                .id("hard_1")
                .title("Department with Most Employees")
                .description("""
                    Write a SQL query to find which department has the most employees.
                    
                    Table: employees
                    +---------------+---------+
                    | Column Name   | Type    |
                    +---------------+---------+
                    | id            | int     |
                    | department_id | int     |
                    +---------------+---------+
                    
                    Table: departments
                    +-----------------+---------+
                    | Column Name     | Type    |
                    +-----------------+---------+
                    | id              | int     |
                    | department_name | varchar |
                    +-----------------+---------+
                    
                    Expected output: Department name with highest employee count
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT d.department_name, COUNT(e.id) as employee_count \nFROM departments d \nJOIN employees e ON d.id = e.department_id \nGROUP BY d.id, d.department_name \nORDER BY employee_count DESC \nLIMIT 1;")
                .difficulty(PracticeQuestion.Difficulty.HARD)
                .hint("🔥 Group by department, count employees, order by count descending, and take the first result.")
                .solution("SELECT d.department_name, COUNT(e.id) as employee_count FROM departments d JOIN employees e ON d.id = e.department_id GROUP BY d.id, d.department_name ORDER BY employee_count DESC LIMIT 1;")
                .category("DML")
                .points(30)
                .build();
    }

    private PracticeQuestion createHardQuestion2() {
        return PracticeQuestion.builder()
                .id("hard_2")
                .title("Employees Earning More Than Average")
                .description("""
                    Write a SQL query to find employees who earn more than the average salary.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | salary      | decimal |
                    +-------------+---------+
                    
                    Expected output: All employees with salary above average
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT * FROM employees \nWHERE salary > (SELECT AVG(salary) FROM employees);")
                .difficulty(PracticeQuestion.Difficulty.HARD)
                .hint("🔥 Use a subquery to calculate the average salary and compare it in the WHERE clause.")
                .solution("SELECT * FROM employees WHERE salary > (SELECT AVG(salary) FROM employees);")
                .category("DML")
                .points(30)
                .build();
    }

    private PracticeQuestion createHardQuestion3() {
        return PracticeQuestion.builder()
                .id("hard_3")
                .title("Second Highest Salary")
                .description("""
                    Write a SQL query to find the second highest salary.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | salary      | decimal |
                    +-------------+---------+
                    
                    Expected output: The second highest salary value
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT MAX(salary) as second_highest \nFROM employees \nWHERE salary < (SELECT MAX(salary) FROM employees);")
                .difficulty(PracticeQuestion.Difficulty.HARD)
                .hint("🔥 Find the maximum salary that is less than the overall maximum salary.")
                .solution("SELECT MAX(salary) FROM employees WHERE salary < (SELECT MAX(salary) FROM employees);")
                .category("DML")
                .points(30)
                .build();
    }

    private PracticeQuestion createHardQuestion4() {
        return PracticeQuestion.builder()
                .id("hard_4")
                .title("Employees with No Manager")
                .description("""
                    Write a SQL query to find all employees who don't have a manager.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | manager_id  | int     |
                    +-------------+---------+
                    
                    Expected output: All employees where manager_id is NULL
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT * FROM employees WHERE manager_id IS NULL;")
                .difficulty(PracticeQuestion.Difficulty.HARD)
                .hint("🔥 Use IS NULL to find employees without managers.")
                .solution("SELECT * FROM employees WHERE manager_id IS NULL;")
                .category("DML")
                .points(30)
                .build();
    }

    // PRO LEVEL QUESTIONS
    private PracticeQuestion createProQuestion1() {
        return PracticeQuestion.builder()
                .id("pro_1")
                .title("Complex Join with Aggregation")
                .description("""
                    Write a SQL query to find departments with their employee count and average salary, 
                    only for departments with more than 2 employees.
                    
                    Table: employees
                    +---------------+---------+
                    | Column Name   | Type    |
                    +---------------+---------+
                    | id            | int     |
                    | salary        | decimal |
                    | department_id | int     |
                    +---------------+---------+
                    
                    Table: departments
                    +-----------------+---------+
                    | Column Name     | Type    |
                    +-----------------+---------+
                    | id              | int     |
                    | department_name | varchar |
                    +-----------------+---------+
                    
                    Expected output: Department name, employee count, and average salary (only departments with >2 employees)
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT d.department_name, \n       COUNT(e.id) as employee_count, \n       AVG(e.salary) as avg_salary \nFROM departments d \nJOIN employees e ON d.id = e.department_id \nGROUP BY d.id, d.department_name \nHAVING COUNT(e.id) > 2;")
                .difficulty(PracticeQuestion.Difficulty.PRO)
                .hint("⭐ Use JOIN to connect tables, GROUP BY for aggregation, and HAVING to filter groups.")
                .solution("SELECT d.department_name, COUNT(e.id) as employee_count, AVG(e.salary) as avg_salary FROM departments d JOIN employees e ON d.id = e.department_id GROUP BY d.id, d.department_name HAVING COUNT(e.id) > 2;")
                .category("DML")
                .points(50)
                .build();
    }

    private PracticeQuestion createProQuestion2() {
        return PracticeQuestion.builder()
                .id("pro_2")
                .title("Window Function - Rank Employees")
                .description("""
                    Write a SQL query to rank employees by salary within their department.
                    
                    Table: employees
                    +---------------+---------+
                    | Column Name   | Type    |
                    +---------------+---------+
                    | id            | int     |
                    | first_name    | varchar |
                    | last_name     | varchar |
                    | salary        | decimal |
                    | department_id | int     |
                    +---------------+---------+
                    
                    Expected output: Employee details with salary rank within department
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT first_name, \n       last_name, \n       salary, \n       department_id, \n       RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) as salary_rank \nFROM employees;")
                .difficulty(PracticeQuestion.Difficulty.PRO)
                .hint("⭐ Use RANK() window function with PARTITION BY and ORDER BY clauses.")
                .solution("SELECT first_name, last_name, salary, department_id, RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) as salary_rank FROM employees;")
                .category("DML")
                .points(50)
                .build();
    }

    private PracticeQuestion createProQuestion3() {
        return PracticeQuestion.builder()
                .id("pro_3")
                .title("Self Join - Find Manager Hierarchy")
                .description("""
                    Write a SQL query to find all employees and their managers.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | manager_id  | int     |
                    +-------------+---------+
                    
                    Expected output: Employee name and their manager's name
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT CONCAT(e.first_name, ' ', e.last_name) as employee_name, \n       CONCAT(m.first_name, ' ', m.last_name) as manager_name \nFROM employees e \nLEFT JOIN employees m ON e.manager_id = m.id;")
                .difficulty(PracticeQuestion.Difficulty.PRO)
                .hint("⭐ Use self-join with LEFT JOIN to include employees without managers.")
                .solution("SELECT CONCAT(e.first_name, ' ', e.last_name) as employee_name, CONCAT(m.first_name, ' ', m.last_name) as manager_name FROM employees e LEFT JOIN employees m ON e.manager_id = m.id;")
                .category("DML")
                .points(50)
                .build();
    }

    private PracticeQuestion createProQuestion4() {
        return PracticeQuestion.builder()
                .id("pro_4")
                .title("Running Total of Salaries")
                .description("""
                    Write a SQL query to calculate running total of salaries ordered by employee ID.
                    
                    Table: employees
                    +-------------+---------+
                    | Column Name | Type    |
                    +-------------+---------+
                    | id          | int     |
                    | first_name  | varchar |
                    | last_name   | varchar |
                    | salary      | decimal |
                    +-------------+---------+
                    
                    Expected output: Employee details with running total of salaries
                    """)
                .exampleSql("-- Write your SQL query here\nSELECT id, \n       first_name, \n       last_name, \n       salary, \n       SUM(salary) OVER (ORDER BY id) as running_total \nFROM employees \nORDER BY id;")
                .difficulty(PracticeQuestion.Difficulty.PRO)
                .hint("⭐ Use SUM() window function with ORDER BY to calculate running total.")
                .solution("SELECT id, first_name, last_name, salary, SUM(salary) OVER (ORDER BY id) as running_total FROM employees ORDER BY id;")
                .category("DML")
                .points(50)
                .build();
    }

    // ===== VALIDATION RESULT CLASS =====

    @Data
    @Builder
    public static class ValidationResult {
        private boolean correct;
        private String message;
        private String questionId;
        private String userQuery;
        private QueryResult userResult;
        private QueryResult expectedResult;
        private int pointsEarned;
        private long executionTimeMs;
        private String hint;
        private LocalDateTime timestamp;

        public String getExecutionTimeDisplay() {
            return executionTimeMs + "ms";
        }

        public String getFormattedMessage() {
            if (correct) {
                return String.format("🎉 %s (+%d points)", message, pointsEarned);
            } else {
                return String.format("❌ %s", message);
            }
        }
    }
}
