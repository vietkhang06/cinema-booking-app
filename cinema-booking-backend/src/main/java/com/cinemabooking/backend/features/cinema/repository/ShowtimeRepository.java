package com.cinemabooking.backend.features.cinema.repository;

import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ShowtimeRepository {

    private static final String COLLECTION = "showtimes";

    @Autowired
    private Firestore firestore;

    public List<QueryDocumentSnapshot> findAllOrderedByStartAt() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get();
        return future.get().getDocuments();
    }

    public DocumentSnapshot findById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(id).get().get();
    }

    public List<QueryDocumentSnapshot> findByMovieId(String movieId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("movieId", movieId)
                .get()
                .get()
                .getDocuments();
    }

    public List<QueryDocumentSnapshot> findByCinemaId(String cinemaId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("cinemaId", cinemaId)
                .get()
                .get()
                .getDocuments();
    }

    public void cancelShowtime(String showtimeId, long updatedAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(showtimeId)
                .update("status", "CANCELLED", "updatedAt", updatedAt)
                .get();
    }

    public void save(String id, java.util.Map<String, Object> data) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).set(data).get();
    }

    public void updateStatus(String id, String status) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).update("status", status).get();
    }

    public ShowtimeDTO updateShowtimeInTransaction(ShowtimeDTO showtime) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection(COLLECTION).document(showtime.getShowtimeId());
        return firestore.runTransaction(transaction -> {
            ShowtimeDTO t_showtime = transaction.get(documentReference).get().toObject(ShowtimeDTO.class);
            if (t_showtime == null) {
                throw new RuntimeException("Showtime not found with ID: " + showtime.getShowtimeId());
            }

            t_showtime.setBasePrice(showtime.getBasePrice());
            t_showtime.setLanguage(showtime.getLanguage());
            t_showtime.setFormat(showtime.getFormat());
            t_showtime.setUpdatedAt(System.currentTimeMillis());

            if(t_showtime.getBookedSeatsCount() <= 0){
                t_showtime.setStartAt(showtime.getStartAt());
                t_showtime.setEndAt(showtime.getEndAt());
                t_showtime.setRoomId(showtime.getRoomId());
                t_showtime.setCinemaId(showtime.getCinemaId());

                transaction.set(documentReference, t_showtime, SetOptions.mergeFields("startAt", "endAt", "roomId", "cinemaId", "basePrice", "language", "format", "updatedAt"));
            }else{
                transaction.set(documentReference, t_showtime, SetOptions.mergeFields("basePrice", "language", "format", "updatedAt"));
            }

            return t_showtime;
        }).get();
    }
}
