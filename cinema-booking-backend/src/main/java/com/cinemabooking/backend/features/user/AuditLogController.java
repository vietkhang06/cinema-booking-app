package com.cinemabooking.backend.features.user;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.user.AuditLogDTO;
import com.google.cloud.firestore.Firestore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "AuditLogs", description = "Endpoints for managing audit logs")
public class AuditLogController {

    @Autowired
    private Firestore firestore;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createAuditLog(
            @AuthenticationPrincipal String userId,
            @RequestBody AuditLogDTO logDTO
    ) throws ExecutionException, InterruptedException {
        String logId = "log_" + UUID.randomUUID().toString();
        logDTO.setLogId(logId);
        logDTO.setActorId(userId);
        if (logDTO.getCreatedAt() <= 0) {
            logDTO.setCreatedAt(System.currentTimeMillis());
        }

        firestore.collection("audit_logs").document(logId).set(logDTO).get();

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Audit log created successfully")
                        .build()
        );
    }
}
