package com.cinemabooking.backend.features.user.repository;

import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "users";
    private static final String ATTENDANCE_COLLECTION = "attendance";

    public DocumentSnapshot findById(String uid) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(uid).get().get();
    }

    public <T> T runTransaction(com.google.cloud.firestore.Transaction.Function<T> function) throws ExecutionException, InterruptedException {
        return firestore.runTransaction(function).get();
    }

    public DocumentReference getDocumentReference(String uid) {
        return firestore.collection(COLLECTION).document(uid);
    }

    public void save(UserDTO user) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(user.getUid()).set(user).get();
    }

    public void update(String uid, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(uid).update(updates).get();
    }

    public List<QueryDocumentSnapshot> findAll() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).get().get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findByRole(String role) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).whereEqualTo("role", role).get().get().getDocuments();
    }

    // Attendance for ChatService (Staff Online Allocation)
    public List<QueryDocumentSnapshot> findAttendanceByDateAndNoCheckOut(String date) throws ExecutionException, InterruptedException {
        return firestore.collection(ATTENDANCE_COLLECTION)
                .whereEqualTo("date", date)
                .whereEqualTo("checkOutTime", 0L)
                .get().get().getDocuments();
    }
}
