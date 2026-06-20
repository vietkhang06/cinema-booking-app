package com.cinemabooking.backend.features.user.repository;

import com.cinemabooking.backend.features.user.dto.ViolationDTO;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ViolationRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "violations";

    public void save(ViolationDTO violation) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(violation.getId()).set(violation).get();
    }

    public List<QueryDocumentSnapshot> findByStaffId(String staffId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("staffId", staffId)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();
    }

    public List<QueryDocumentSnapshot> findWithFilters(String staffId, String status, String severity) throws ExecutionException, InterruptedException {
        Query query = firestore.collection(COLLECTION).whereEqualTo("deleted", false);
        if (staffId != null && !staffId.isEmpty()) {
            query = query.whereEqualTo("staffId", staffId);
        }
        if (status != null && !status.isEmpty()) {
            query = query.whereEqualTo("status", status);
        }
        if (severity != null && !severity.isEmpty()) {
            query = query.whereEqualTo("severity", severity);
        }
        return query.get().get().getDocuments();
    }

    public DocumentSnapshot findById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(id).get().get();
    }

    public void update(String id, java.util.Map<String, Object> fields) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).update(fields).get();
    }

    public void softDelete(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).update("deleted", true).get();
    }
}
