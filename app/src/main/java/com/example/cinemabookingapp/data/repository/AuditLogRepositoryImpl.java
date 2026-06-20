package com.example.cinemabookingapp.data.repository;

import android.util.Log;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.AuditLogDTO;
import com.example.cinemabookingapp.data.mapper.AuditLogMapper;
import com.example.cinemabookingapp.data.remote.api.AuditLogApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.AuditLog;
import com.example.cinemabookingapp.domain.repository.AuditLogRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuditLogRepositoryImpl implements AuditLogRepository {
    private static final String TAG = "AuditLogRepoImpl";
    private final FirebaseFirestore firestore;
    private final AuditLogApiService auditLogApi;

    public AuditLogRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auditLogApi = RetrofitClient.getInstance().create(AuditLogApiService.class);
    }

    @Override
    public void createLog(AuditLog log, ResultCallback<AuditLog> callback) {
        AuditLogDTO dto = AuditLogMapper.toDTO(log);
        if (dto.createdAt <= 0) {
            dto.createdAt = System.currentTimeMillis();
        }

        // Call the API backend first (standard Clean Architecture/REST client pattern)
        auditLogApi.createAuditLog(dto).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Audit log created via API");
                    if (callback != null) callback.onSuccess(log);
                } else {
                    Log.w(TAG, "API failed, falling back to writing direct to Firestore: " + response.message());
                    writeDirectToFirestore(log, dto, callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.w(TAG, "API request failed, falling back to writing direct to Firestore", t);
                writeDirectToFirestore(log, dto, callback);
            }
        });
    }

    private void writeDirectToFirestore(AuditLog log, AuditLogDTO dto, ResultCallback<AuditLog> callback) {
        String logId = dto.logId != null ? dto.logId : "log_" + java.util.UUID.randomUUID().toString();
        dto.logId = logId;
        log.logId = logId;

        firestore.collection("audit_logs").document(logId)
                .set(dto)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(log);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getAllLogs(ResultCallback<List<AuditLog>> callback) {
        firestore.collection("audit_logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(150)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AuditLog> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            AuditLogDTO dto = doc.toObject(AuditLogDTO.class);
                            if (dto != null) {
                                if (dto.logId == null) {
                                    dto.logId = doc.getId();
                                }
                                list.add(AuditLogMapper.toDomain(dto));
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getLogsByActorId(String actorId, ResultCallback<List<AuditLog>> callback) {
        firestore.collection("audit_logs")
                .whereEqualTo("actorId", actorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AuditLog> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            AuditLogDTO dto = doc.toObject(AuditLogDTO.class);
                            if (dto != null) {
                                if (dto.logId == null) {
                                    dto.logId = doc.getId();
                                }
                                list.add(AuditLogMapper.toDomain(dto));
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getLogsByTargetId(String targetId, ResultCallback<List<AuditLog>> callback) {
        firestore.collection("audit_logs")
                .whereEqualTo("targetId", targetId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AuditLog> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            AuditLogDTO dto = doc.toObject(AuditLogDTO.class);
                            if (dto != null) {
                                if (dto.logId == null) {
                                    dto.logId = doc.getId();
                                }
                                list.add(AuditLogMapper.toDomain(dto));
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}
