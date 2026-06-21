package com.cinemabooking.backend.features.movie.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class MovieRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "movies";

    public List<QueryDocumentSnapshot> findAll() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .limit(100) // limit matching MovieService limit
                .get().get().getDocuments();
    }

    public DocumentSnapshot findById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(id).get().get();
    }
}
