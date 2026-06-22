package com.cinemabooking.backend.features.cinema.repository;

import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ShowtimeRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "showtimes";
    private static final String SCHEDULE_COLLECTION = "showtime_schedules";

    public List<ShowtimeDTO> findAllOrderedByStartAt() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get().get().toObjects(ShowtimeDTO.class);
    }

    public List<ShowtimeDTO> findShowtimesBetween(long start, long end) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereGreaterThanOrEqualTo("startAt", start)
                .whereLessThanOrEqualTo("startAt", end)
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get().get().toObjects(ShowtimeDTO.class);
    }

    public DocumentSnapshot findById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(id).get().get();
    }

    public List<QueryDocumentSnapshot> findByMovieId(String movieId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("movieId", movieId)
                .get().get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findByCinemaId(String cinemaId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("cinemaId", cinemaId)
                .get().get().getDocuments();
    }

    public void save(ShowtimeDTO showtime) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(showtime.getShowtimeId()).set(showtime).get();
    }

    public void saveMap(String id, java.util.Map<String, Object> data) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).set(data).get();
    }

    public void updateMap(String id, java.util.Map<String, Object> data) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).update(data).get();
    }

    public void updateStatus(String showtimeId, String status, long updatedAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(showtimeId)
                .update("status", status, "updatedAt", updatedAt)
                .get();
    }

    public <T> T runTransaction(com.google.cloud.firestore.Transaction.Function<T> function) throws ExecutionException, InterruptedException {
        return firestore.runTransaction(function).get();
    }

    public DocumentReference getDocumentReference(String id) {
        return firestore.collection(COLLECTION).document(id);
    }

    // Showtime Schedules for ShowtimeScheduler
    public List<QueryDocumentSnapshot> findPendingSchedules(long now) throws ExecutionException, InterruptedException {
        return firestore.collection(SCHEDULE_COLLECTION)
                .whereEqualTo("executed", false)
                .whereLessThanOrEqualTo("startAt", now)
                .get().get().getDocuments();
    }

    public DocumentReference getScheduleReference(String id) {
        return firestore.collection(SCHEDULE_COLLECTION).document(id);
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
