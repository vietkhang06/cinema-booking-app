package com.cinemabooking.backend.features.cinema.repository;

import com.cinemabooking.backend.features.cinema.dto.CinemaDTO;
import com.cinemabooking.backend.features.cinema.dto.RoomDTO;
import com.cinemabooking.backend.features.cinema.dto.SnackDTO;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class CinemaRepository {

    @Autowired
    private Firestore firestore;

    private static final String CINEMAS_COLLECTION = "cinemas";
    private static final String ROOMS_COLLECTION = "rooms";
    private static final String SNACKS_COLLECTION = "snacks";
    private static final String SEAT_TEMPLATES_COLLECTION = "seat_templates";

    public List<QueryDocumentSnapshot> findAllCinemas() throws ExecutionException, InterruptedException {
        return firestore.collection(CINEMAS_COLLECTION).get().get().getDocuments();
    }

    public DocumentSnapshot findCinemaDocumentById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(CINEMAS_COLLECTION).document(id).get().get();
    }

    public DocumentSnapshot findRoomDocumentById(String roomId) throws ExecutionException, InterruptedException {
        return firestore.collection(ROOMS_COLLECTION).document(roomId).get().get();
    }

    public List<QueryDocumentSnapshot> findAllRooms() throws ExecutionException, InterruptedException {
        return firestore.collection(ROOMS_COLLECTION).get().get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findSnacksByIds(List<String> snackIds) throws ExecutionException, InterruptedException {
        return firestore.collection(SNACKS_COLLECTION)
                .whereIn("snackId", snackIds)
                .get()
                .get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findSeatTemplatesByRoomId(String roomId) throws ExecutionException, InterruptedException {
        return firestore.collection(SEAT_TEMPLATES_COLLECTION)
                .whereEqualTo("roomId", roomId)
                .get().get().getDocuments();
    }
}
