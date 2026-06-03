package com.example.cinemabookingapp.data.dto;

public class ViolationDTO {
    public String id;
    public String staffId;
    public String staffName;
    public String violationType; // "LATE", "EARLY_LEAVE", "NO_CHECKIN", "NO_CHECKOUT", "OTHER"
    public String description;
    public String severity; // "LOW", "MEDIUM", "HIGH"
    public long createdAt;
    public String createdBy;
    public String createdByName;
    public String status; // "PENDING", "RESOLVED", "CANCELLED"
    public double penaltyAmount;
    public int penaltyPoints;
    public String notes;
    public boolean deleted;

    public ViolationDTO() {
    }
}
