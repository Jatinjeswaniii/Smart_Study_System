package com.studysystem.model;

import java.time.LocalDate;

public class StudySession {
    private int id;
    private int userId;
    private String subject;
    private String topic;
    private LocalDate studyDate;
    private int difficulty;
    private String notes;
    
    public StudySession(int userId, String subject, String topic, LocalDate studyDate, int difficulty, String notes) {
        this.userId = userId;
        this.subject = subject;
        this.topic = topic;
        this.studyDate = studyDate;
        this.difficulty = difficulty;
        this.notes = notes;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    
    public LocalDate getStudyDate() { return studyDate; }
    public void setStudyDate(LocalDate studyDate) { this.studyDate = studyDate; }
    
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}