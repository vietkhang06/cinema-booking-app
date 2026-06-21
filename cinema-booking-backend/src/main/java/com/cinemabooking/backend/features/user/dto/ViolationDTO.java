package com.cinemabooking.backend.features.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationDTO {
    public static final String COLLECTION_NAME = "staff_violations";

    private String id;
    private String staffId;
    private String staffName;
    private String violationType; // "LATE", "EARLY_LEAVE", "NO_CHECKIN", "NO_CHECKOUT", "OTHER"
    private String description;
    private String severity; // "LOW", "MEDIUM", "HIGH"
    private long createdAt;
    private String createdBy;
    private String createdByName;
    private String status; // "PENDING", "RESOLVED", "CANCELLED"
    private double penaltyAmount;
    private int penaltyPoints;
    private String notes;
    private boolean deleted;
}
