package com.cinemabooking.backend.features.user.controller;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.user.dto.AuditLogDTO;
import com.cinemabooking.backend.features.user.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "AuditLogs", description = "Endpoints for managing audit logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createAuditLog(
            @AuthenticationPrincipal String userId,
            @RequestBody AuditLogDTO logDTO
    ) throws ExecutionException, InterruptedException {
        auditLogService.createAuditLog(userId, logDTO);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Audit log created successfully")
                        .build()
        );
    }
}
