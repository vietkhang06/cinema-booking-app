package com.cinemabooking.backend.features.movie.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class BannerRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "banners";

    public List<QueryDocumentSnapshot> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION).get();
        return future.get().getDocuments();
    }

    public void save(String bannerId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bannerId).set(data).get();
    }
}
