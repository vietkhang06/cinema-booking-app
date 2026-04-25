package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.AuditLog;

import java.util.List;

public interface AuditLogRepository {
    void createLog(AuditLog log, ResultCallback<AuditLog> callback);
    void getAllLogs(ResultCallback<List<AuditLog>> callback);
    void getLogsByActorId(String actorId, ResultCallback<List<AuditLog>> callback);
    void getLogsByTargetId(String targetId, ResultCallback<List<AuditLog>> callback);
}