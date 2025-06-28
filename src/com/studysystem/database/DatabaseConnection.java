package com.studysystem.database;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/smart_study_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Jatin@123"; 
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
    
    public static void createDatabase() {
        try {
            // First connect without database
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USERNAME, PASSWORD);
            Statement stmt = conn.createStatement();
            
            // Create database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS smart_study_db");
            conn.close();
            
            // Now connect to the database and create tables
            conn = getConnection();
            stmt = conn.createStatement();
            
            // Create users table with reset token columns
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    email VARCHAR(100),
                    reset_token VARCHAR(100),
                    token_expiry DATETIME,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            // Create study_sessions table
            String createStudySessionsTable = """
                CREATE TABLE IF NOT EXISTS study_sessions (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT,
                    subject VARCHAR(100) NOT NULL,
                    topic VARCHAR(200) NOT NULL,
                    study_date DATE NOT NULL,
                    difficulty INT DEFAULT 3,
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
            
            // Create revisions table
            String createRevisionsTable = """
                CREATE TABLE IF NOT EXISTS revisions (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    session_id INT,
                    revision_date DATE NOT NULL,
                    next_revision_date DATE NOT NULL,
                    interval_days INT DEFAULT 1,
                    recall_quality INT DEFAULT 3,
                    completed BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (session_id) REFERENCES study_sessions(id) ON DELETE CASCADE
                )
                """;
            
            // Execute table creation
            stmt.executeUpdate(createUsersTable);
            stmt.executeUpdate(createStudySessionsTable);
            stmt.executeUpdate(createRevisionsTable);
            
            stmt.close();
            conn.close();
            
            System.out.println("Database and tables created successfully!");
            
        } catch (SQLException e) {
            System.out.println("Database initialization error:");
            e.printStackTrace();
        }
    }
    
    // Optional: Add method to check if tables exist (for startup validation)
    public static boolean checkTablesExist() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet usersTable = meta.getTables(null, null, "users", null);
            ResultSet sessionsTable = meta.getTables(null, null, "study_sessions", null);
            ResultSet revisionsTable = meta.getTables(null, null, "revisions", null);
            
            return usersTable.next() && sessionsTable.next() && revisionsTable.next();
        } catch (SQLException e) {
            System.out.println("Error checking tables:");
            e.printStackTrace();
            return false;
        }
    }
}