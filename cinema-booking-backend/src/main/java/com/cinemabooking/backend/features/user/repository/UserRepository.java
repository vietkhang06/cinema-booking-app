package com.cinemabooking.backend.features.user.repository;

import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "users";

    public DocumentSnapshot findById(String uid) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(uid).get().get();
    }

    public DocumentReference getDocumentReference(String uid) {
        return firestore.collection(COLLECTION).document(uid);
    }

    public void save(String uid, UserDTO user) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(uid).set(user).get();
    }

    public void updateFields(String uid, java.util.Map<String, Object> fields) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(uid).update(fields).get();
    }

    public List<com.google.cloud.firestore.QueryDocumentSnapshot> findAllStaffsAndAdmins() throws ExecutionException, InterruptedException {
        Query query = firestore.collection(COLLECTION).whereIn("role", List.of("staff", "admin"));
        return query.get().get().getDocuments();
    }

    public List<com.google.cloud.firestore.QueryDocumentSnapshot> findAllUsers() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).get().get().getDocuments();
    }

    public List<UserDTO> findUsersByIds(List<String> uids) throws ExecutionException, InterruptedException {
        if (uids == null || uids.isEmpty()) return new ArrayList<>();
        return firestore.collection(COLLECTION).whereIn("uid", uids).get().get().toObjects(UserDTO.class);
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
