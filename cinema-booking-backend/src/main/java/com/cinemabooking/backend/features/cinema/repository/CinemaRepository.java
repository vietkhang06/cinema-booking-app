package com.cinemabooking.backend.features.cinema.repository;

import com.cinemabooking.backend.features.cinema.dto.CinemaDTO;
import com.cinemabooking.backend.features.cinema.dto.RoomDTO;
import com.cinemabooking.backend.features.cinema.dto.SnackDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class CinemaRepository {

    @Autowired
    private Firestore firestore;

    private static final String CINEMAS_COL = "cinemas";
    private static final String ROOMS_COL = "rooms";
    private static final String SNACKS_COL = "snacks";

    public List<QueryDocumentSnapshot> findAllCinemas() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(CINEMAS_COL).get();
        return future.get().getDocuments();
    }

    public DocumentSnapshot findCinemaById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(CINEMAS_COL).document(id).get().get();
    }

    public DocumentSnapshot findRoomById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(ROOMS_COL).document(id).get().get();
    }

    public List<QueryDocumentSnapshot> findAllRooms() throws ExecutionException, InterruptedException {
        return firestore.collection(ROOMS_COL).get().get().getDocuments();
    }

    public List<SnackDTO> findSnacksByIds(List<String> snackIds) throws ExecutionException, InterruptedException {
        if (snackIds == null || snackIds.isEmpty()) {
            return new ArrayList<>();
        }
        return firestore.collection(SNACKS_COL)
                .whereIn("snackId", snackIds)
                .get()
                .get()
                .toObjects(SnackDTO.class);
    }
}
