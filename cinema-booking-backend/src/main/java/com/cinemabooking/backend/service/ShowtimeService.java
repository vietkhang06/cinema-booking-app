package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.ShowtimeDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

    private static final Logger logger = LoggerFactory.getLogger(ShowtimeService.class);
    private static final String COLLECTION = "showtimes";

    @Autowired
    private Firestore firestore;

    public List<ShowtimeDTO> getAllShowtimes() throws ExecutionException, InterruptedException {
        logger.info("Fetching all showtimes from Firestore");
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get();
        List<ShowtimeDTO> showtimes = processQuerySnapshot(future.get());
        logger.info("Loaded {} showtimes", showtimes.size());
        return showtimes;
    }

    public ShowtimeDTO getShowtimeById(String id) throws ExecutionException, InterruptedException {
        logger.info("Fetching showtime by ID: {}", id);
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        if (doc.exists()) {
            ShowtimeDTO dto = mapToDTO(doc);
            if (dto != null && !Boolean.TRUE.equals(dto.isDeleted())) {
                return dto;
            }
        }
        return null;
    }

    public List<ShowtimeDTO> getShowtimesByMovieId(String movieId) throws ExecutionException, InterruptedException {
        logger.info("Fetching showtimes for movieId: {}", movieId);
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("movieId", movieId)
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get();
        List<ShowtimeDTO> showtimes = processQuerySnapshot(future.get());
        logger.info("Loaded {} showtimes for movie {}", showtimes.size(), movieId);
        return showtimes;
    }

    public List<ShowtimeDTO> getShowtimesByCinemaId(String cinemaId) throws ExecutionException, InterruptedException {
        logger.info("Fetching showtimes for cinemaId: {}", cinemaId);
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("cinemaId", cinemaId)
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get();
        List<ShowtimeDTO> showtimes = processQuerySnapshot(future.get());
        logger.info("Loaded {} showtimes for cinema {}", showtimes.size(), cinemaId);
        return showtimes;
    }

    private List<ShowtimeDTO> processQuerySnapshot(QuerySnapshot querySnapshot) {
        return querySnapshot.getDocuments().stream()
                .map(this::mapToDTO)
                .filter(dto -> dto != null && !dto.isDeleted())
                .collect(Collectors.toList());
    }

    private ShowtimeDTO mapToDTO(DocumentSnapshot doc) {
        try {
            return ShowtimeDTO.builder()
                    .showtimeId(doc.getId())
                    .movieId(doc.getString("movieId"))
                    .cinemaId(doc.getString("cinemaId"))
                    .roomId(doc.getString("roomId"))
                    .startAt(doc.getLong("startAt") != null ? doc.getLong("startAt") : 0L)
                    .endAt(doc.getLong("endAt") != null ? doc.getLong("endAt") : 0L)
                    .basePrice(doc.getDouble("basePrice") != null ? doc.getDouble("basePrice") : 0.0)
                    .format(doc.getString("format"))
                    .language(doc.getString("language"))
                    .status(doc.getString("status"))
                    .totalSeats(doc.getLong("totalSeats") != null ? doc.getLong("totalSeats").intValue() : 0)
                    .bookedSeatsCount(doc.getLong("bookedSeatsCount") != null ? doc.getLong("bookedSeatsCount").intValue() : 0)
                    .createdAt(doc.getLong("createdAt") != null ? doc.getLong("createdAt") : 0L)
                    .updatedAt(doc.getLong("updatedAt") != null ? doc.getLong("updatedAt") : 0L)
                    .deleted(Boolean.TRUE.equals(doc.getBoolean("deleted")))
                    .build();
        } catch (Exception e) {
            logger.warn("Error mapping showtime doc {}: {}", doc.getId(), e.getMessage());
            return null;
        }
    }
}
