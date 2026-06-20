package com.cinemabooking.backend.features.movie.repository;

import com.cinemabooking.backend.features.movie.dto.MovieDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class MovieRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "movies";

    public List<QueryDocumentSnapshot> findAllLimit(int limit) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .limit(limit)
                .get();
        return future.get().getDocuments();
    }

    public DocumentSnapshot findById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(id).get().get();
    }
}
