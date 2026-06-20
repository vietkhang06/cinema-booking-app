package com.cinemabooking.backend.features.user.repository;

import com.cinemabooking.backend.features.user.dto.AuditLogDTO;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class AuditLogRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "audit_logs";

    public void save(AuditLogDTO logDTO) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(logDTO.getLogId()).set(logDTO).get();
    }
}
