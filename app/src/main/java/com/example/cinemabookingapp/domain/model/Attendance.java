package com.example.cinemabookingapp.domain.model;

public class Attendance {
    public String id;
    public String staffId;
    public String staffName;
    public String cinemaId;
    public String cinemaName;
    public String shiftName;
    public String date;
    public long checkInTime;
    public long checkOutTime;
    public long durationMinutes;
    public long lateMinutes;
    public long earlyLeaveMinutes;
    public String status; // "present", "late", "early_leave", "completed"
    public String notes;

    public Attendance() {
    }
}
