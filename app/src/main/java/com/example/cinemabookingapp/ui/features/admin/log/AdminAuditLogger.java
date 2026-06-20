package com.example.cinemabookingapp.ui.features.admin.log;

import android.util.Log;
import com.example.cinemabookingapp.data.repository.AuditLogRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.AuditLog;
import com.example.cinemabookingapp.domain.repository.AuditLogRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAuditLogger {
    private static final String TAG = "AdminAuditLogger";
    private static final AuditLogRepository auditLogRepository = new AuditLogRepositoryImpl();

    public static void log(String action, String targetType, String targetId, String note) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            uid = "unknown_admin";
        }

        final String finalUid = uid;
        // Fetch user info from Firestore to get their display name
        FirebaseFirestore.getInstance().collection("users").document(finalUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String actorName = "Admin";
                    String role = "admin";
                    if (documentSnapshot.exists()) {
                        String nameVal = documentSnapshot.getString("name");
                        if (nameVal != null && !nameVal.isEmpty()) {
                            actorName = nameVal;
                        } else {
                            String emailVal = documentSnapshot.getString("email");
                            if (emailVal != null && !emailVal.isEmpty()) {
                                actorName = emailVal;
                            }
                        }
                        String roleVal = documentSnapshot.getString("role");
                        if (roleVal != null) {
                            role = roleVal;
                        }
                    }
                    writeLog(finalUid, actorName, role, action, targetType, targetId, note);
                })
                .addOnFailureListener(e -> {
                    writeLog(finalUid, "Admin", "admin", action, targetType, targetId, note);
                });
    }

    private static void writeLog(String uid, String actorName, String role, String action, String targetType, String targetId, String note) {
        AuditLog log = new AuditLog();
        log.actorId = actorName;
        log.actorRole = role;
        log.action = action;
        log.targetType = targetType;
        log.targetId = targetId;
        log.note = note;
        log.createdAt = System.currentTimeMillis();

        auditLogRepository.createLog(log, new ResultCallback<AuditLog>() {
            @Override
            public void onSuccess(AuditLog result) {
                Log.d(TAG, "Audit log added successfully: " + action);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error adding audit log: " + message);
            }
        });
    }
}
