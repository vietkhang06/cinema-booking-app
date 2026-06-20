package com.cinemabooking.backend.features.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    public static final String COLLECTION_NAME = "audit_logs";

    private String logId;
    private String action; // CREATE, UPDATE, DELETE, CANCEL
    private String targetType; // SHOWTIME, BOOKING, VOUCHER, MOVIE, USER
    private String targetId;
    private String actorId;
    private String details;
    private long createdAt;
}
