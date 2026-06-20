package com.cinemabooking.backend.features.cinema.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ShowtimeScheduleRepository {

    private static final String COLLECTION = "showtime_schedules";

    @Autowired
    private Firestore firestore;

    public List<QueryDocumentSnapshot> findPendingSchedules() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("executed", false)
                .get()
                .get()
                .getDocuments();
    }

    public WriteBatch getBatch() {
        return firestore.batch();
    }
}
