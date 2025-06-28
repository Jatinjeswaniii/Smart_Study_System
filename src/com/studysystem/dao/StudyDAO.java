package com.studysystem.dao;

import com.studysystem.database.DatabaseConnection;
import com.studysystem.model.StudySession;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyDAO {
    
    public boolean addStudySession(StudySession session) {
        String sql = "INSERT INTO study_sessions (user_id, subject, topic, study_date, difficulty, notes) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, session.getUserId());
            pstmt.setString(2, session.getSubject());
            pstmt.setString(3, session.getTopic());
            pstmt.setDate(4, Date.valueOf(session.getStudyDate()));
            pstmt.setInt(5, session.getDifficulty());
            pstmt.setString(6, session.getNotes());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int sessionId = rs.getInt(1);
                    createRevisionSchedule(sessionId, session.getStudyDate(), session.getDifficulty());
                    return true;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private void createRevisionSchedule(int sessionId, LocalDate studyDate, int difficulty) {
        // Spaced repetition intervals based on difficulty
        int[] intervals = {1, 3, 7, 15, 30}; // days
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO revisions (session_id, revision_date, next_revision_date, interval_days) VALUES (?, ?, ?, ?)";
            
            LocalDate currentDate = studyDate;
            
            for (int i = 0; i < intervals.length; i++) {
                LocalDate revisionDate = currentDate.plusDays(intervals[i]);
                LocalDate nextRevisionDate = (i == intervals.length - 1) ? 
                    revisionDate : revisionDate.plusDays(intervals[i + 1]);
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, sessionId);
                    pstmt.setDate(2, Date.valueOf(revisionDate));
                    pstmt.setDate(3, Date.valueOf(nextRevisionDate));
                    pstmt.setInt(4, intervals[i]);
                    pstmt.executeUpdate();
                }
                
                currentDate = revisionDate;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<String[]> getTodaysRevisions(int userId) {
        List<String[]> revisions = new ArrayList<>();
        String sql = """
            SELECT s.subject, s.topic, s.study_date, r.revision_date, r.interval_days, r.id
            FROM revisions r
            JOIN study_sessions s ON r.session_id = s.id
            WHERE s.user_id = ? AND r.revision_date = CURDATE() AND r.completed = FALSE
            ORDER BY s.subject, s.topic
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String[] revision = {
                    rs.getString("subject"),
                    rs.getString("topic"),
                    rs.getDate("study_date").toString(),
                    rs.getDate("revision_date").toString(),
                    String.valueOf(rs.getInt("interval_days")),
                    String.valueOf(rs.getInt("id"))
                };
                revisions.add(revision);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return revisions;
    }
    
    public List<String[]> getUpcomingRevisions(int userId) {
        List<String[]> revisions = new ArrayList<>();
        String sql = """
            SELECT s.subject, s.topic, s.study_date, r.revision_date, r.interval_days
            FROM revisions r
            JOIN study_sessions s ON r.session_id = s.id
            WHERE s.user_id = ? AND r.revision_date > CURDATE() AND r.completed = FALSE
            ORDER BY r.revision_date, s.subject
            LIMIT 10
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String[] revision = {
                    rs.getString("subject"),
                    rs.getString("topic"),
                    rs.getDate("study_date").toString(),
                    rs.getDate("revision_date").toString(),
                    String.valueOf(rs.getInt("interval_days"))
                };
                revisions.add(revision);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return revisions;
    }
    
    public boolean markRevisionCompleted(int revisionId, int recallQuality) {
        String sql = "UPDATE revisions SET completed = TRUE, recall_quality = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recallQuality);
            pstmt.setInt(2, revisionId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public List<String[]> getStudyHistory(int userId) {
        List<String[]> history = new ArrayList<>();
        String sql = """
            SELECT subject, topic, study_date, difficulty, notes
            FROM study_sessions
            WHERE user_id = ?
            ORDER BY study_date DESC
            LIMIT 20
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String[] session = {
                    rs.getString("subject"),
                    rs.getString("topic"),
                    rs.getDate("study_date").toString(),
                    String.valueOf(rs.getInt("difficulty")),
                    rs.getString("notes") != null ? rs.getString("notes") : ""
                };
                history.add(session);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return history;
    }
}
