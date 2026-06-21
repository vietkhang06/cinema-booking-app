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
    private String logId;
    private String actorId;
    private String actorRole;
    private String action;
    private String targetType;
    private String targetId;
    private String note;
    private long createdAt;
}
