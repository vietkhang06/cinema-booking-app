package com.example.cinemabookingapp.domain.model;

public class AuditLog {
    public String logId;
    public String actorId;
    public String actorRole;
    public String action;
    public String targetType;
    public String targetId;
    public String note;
    
    // New fields requested for Cancel Showtime
    public String adminId;
    public String showtimeId;
    public Integer bookingCount;

    public Long createdAt;

    public AuditLog() {
    }
}