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
    public static final String COLLECTION_NAME = "violations";

    private String id;
    private String staffId;
    private String staffName;
    private String violationType; // LATE, EARLY_LEAVE, UNIFORM, BEHAVIOR, OTHER
    private String description;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private long createdAt;
    private String createdBy; // adminId or "system"
    private String createdByName;
    private String status; // PENDING, APPROVED, REJECTED, RESOLVED
    private Double penaltyAmount;
    private Integer penaltyPoints;
    private String notes;
    
    @Builder.Default
    private boolean deleted = false;
}
