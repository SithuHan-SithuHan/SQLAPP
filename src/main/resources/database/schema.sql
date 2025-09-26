-- SQL Learning Application Database Schema
-- Created: 2025-09-26 05:02:00 UTC
-- User: SithuHan-SithuHan
-- Repository: https://github.com/SithuHan-SithuHan/SQL_Learning_APP

-- =====================================================
-- MAIN DATABASE SCHEMA (User Progress & Settings)
-- =====================================================

-- User progress tracking table
CREATE TABLE IF NOT EXISTS user_progress (
                                             id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                             user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    session_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Learning topics progress
CREATE TABLE IF NOT EXISTS topic_progress (
                                              id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                              user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    topic_name VARCHAR(200) NOT NULL,
    view_count INTEGER DEFAULT 0,
    first_viewed_at TIMESTAMP,
    last_viewed_at TIMESTAMP,
    completed BOOLEAN DEFAULT FALSE,
    time_spent_seconds INTEGER DEFAULT 0,
    UNIQUE KEY unique_user_topic (user_id, topic_name)
    );

-- Practice questions progress
CREATE TABLE IF NOT EXISTS question_progress (
                                                 id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                                 user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    question_id VARCHAR(50) NOT NULL,
    attempt_count INTEGER DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    first_attempted_at TIMESTAMP,
    last_attempted_at TIMESTAMP,
    completed_at TIMESTAMP,
    best_execution_time_ms INTEGER,
    points_earned INTEGER DEFAULT 0,
    UNIQUE KEY unique_user_question (user_id, question_id)
    );

-- User statistics
CREATE TABLE IF NOT EXISTS user_statistics (
                                               id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                               user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    stat_name VARCHAR(50) NOT NULL,
    stat_value INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_stat (user_id, stat_name)
    );

-- User achievements and badges
CREATE TABLE IF NOT EXISTS user_achievements (
                                                 id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                                 user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    badge_id VARCHAR(50) NOT NULL,
    badge_name VARCHAR(100) NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_badge (user_id, badge_id)
    );

-- Query history
CREATE TABLE IF NOT EXISTS query_history (
                                             id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                             user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    query_text TEXT NOT NULL,
    execution_time_ms INTEGER,
    success BOOLEAN,
    error_message TEXT,
    result_rows INTEGER DEFAULT 0,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Application settings
CREATE TABLE IF NOT EXISTS app_settings (
                                            id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                            user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_setting (user_id, setting_key)
    );

-- Session tracking
CREATE TABLE IF NOT EXISTS user_sessions (
                                             id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                             user_id VARCHAR(100) NOT NULL DEFAULT 'SithuHan-SithuHan',
    session_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMP,
    duration_seconds INTEGER,
    queries_executed INTEGER DEFAULT 0,
    topics_viewed INTEGER DEFAULT 0,
    questions_attempted INTEGER DEFAULT 0
    );

-- Insert initial user statistics
INSERT IGNORE INTO user_statistics (user_id, stat_name, stat_value) VALUES
('SithuHan-SithuHan', 'totalQueriesExecuted', 0),
('SithuHan-SithuHan', 'successfulQueries', 0),
('SithuHan-SithuHan', 'currentStreak', 0),
('SithuHan-SithuHan', 'bestStreak', 0),
('SithuHan-SithuHan', 'totalTimeSpentMs', 0),
('SithuHan-SithuHan', 'totalPointsEarned', 0),
('SithuHan-SithuHan', 'level', 1),
('SithuHan-SithuHan', 'experiencePoints', 0);

-- Insert initial session
INSERT INTO user_sessions (user_id, session_start) VALUES
    ('SithuHan-SithuHan', '2025-09-26 05:02:00');