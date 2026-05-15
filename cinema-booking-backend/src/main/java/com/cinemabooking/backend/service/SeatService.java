package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.SeatDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatService.class);
    private static final String COLLECTION = "seats";

    @Autowired
    private Firestore firestore;

    public List<SeatDTO> getSeatsByShowtimeId(String showtimeId) throws ExecutionException, InterruptedException {
        logger.info("Fetching seats for showtimeId: {}", showtimeId);
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("showtimeId", showtimeId)
                .get();
        List<SeatDTO> seats = future.get().getDocuments().stream()
                .map(this::mapToDTO)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        logger.info("Loaded {} seats for showtime {}", seats.size(), showtimeId);
        return seats;
    }

    public void holdSeats(List<String> seatIds, String userId, long durationMinutes) throws ExecutionException, InterruptedException {
        long heldUntil = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        for (String id : seatIds) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "HOLDING");
            updates.put("heldBy", userId);
            updates.put("heldUntil", heldUntil);
            firestore.collection(COLLECTION).document(id).update(updates).get();
        }
    }

    public void releaseSeats(List<String> seatIds, String userId) throws ExecutionException, InterruptedException {
        for (String id : seatIds) {
            // Only release if held by this user or if status is HOLDING
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            String currentHolder = doc.getString("heldBy");
            if (userId.equals(currentHolder) || "HOLDING".equals(doc.getString("status"))) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "AVAILABLE");
                updates.put("heldBy", null);
                updates.put("heldUntil", 0L);
                firestore.collection(COLLECTION).document(id).update(updates).get();
            }
        }
    }

    private SeatDTO mapToDTO(DocumentSnapshot doc) {
        try {
            return SeatDTO.builder()
                    .seatId(doc.getId())
                    .showtimeId(doc.getString("showtimeId"))
                    .seatCode(doc.getString("seatCode"))
                    .rowName(doc.getString("rowName"))
                    .columnNo(doc.getLong("columnNo") != null ? doc.getLong("columnNo").intValue() : 0)
                    .seatType(doc.getString("seatType"))
                    .status(doc.getString("status"))
                    .heldBy(doc.getString("heldBy"))
                    .heldUntil(doc.getLong("heldUntil") != null ? doc.getLong("heldUntil") : 0L)
                    .bookedBy(doc.getString("bookedBy"))
                    .bookedAt(doc.getLong("bookedAt") != null ? doc.getLong("bookedAt") : 0L)
                    .priceOverride(doc.getDouble("priceOverride") != null ? doc.getDouble("priceOverride") : 0.0)
                    .build();
        } catch (Exception e) {
            logger.warn("Error mapping seat doc {}: {}", doc.getId(), e.getMessage());
            return null;
        }
    }
}
