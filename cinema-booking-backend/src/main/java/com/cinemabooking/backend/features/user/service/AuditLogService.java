package com.cinemabooking.backend.features.user.service;

import com.cinemabooking.backend.features.user.dto.AuditLogDTO;
import com.cinemabooking.backend.features.user.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void createAuditLog(String actorId, AuditLogDTO logDTO) throws ExecutionException, InterruptedException {
        String logId = "log_" + UUID.randomUUID().toString();
        logDTO.setLogId(logId);
        logDTO.setActorId(actorId);
        if (logDTO.getCreatedAt() <= 0) {
            logDTO.setCreatedAt(System.currentTimeMillis());
        }
        auditLogRepository.save(logDTO);
    }
}
