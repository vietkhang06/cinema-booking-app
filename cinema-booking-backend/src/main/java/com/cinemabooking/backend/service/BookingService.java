package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.BookingDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "bookings";

    /**
     * Get all bookings for a specific user, sorted by createdAt descending.
     */
    public List<BookingDTO> getBookingsByUserId(String userId) throws ExecutionException, InterruptedException {
        logger.info("Fetching bookings for user: {}", userId);
        
        try {
            // Query Firestore for bookings by userId
            Query query = firestore.collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING);

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            logger.info("Found {} bookings for user: {}", documents.size(), userId);

            return documents.stream()
                    .map(this::mapToDTO)
                    .filter(b -> !Boolean.TRUE.equals(b.getDeleted()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error fetching bookings for user {}: {}", userId, e.getMessage());
            // If orderBy fails due to missing index, fallback to manual sorting
            if (e.getMessage().contains("INDEX_REQD")) {
                logger.warn("Firestore index required for createdAt sorting. Falling back to manual sort.");
                return fetchAndSortManually(userId);
            }
            throw e;
        }
    }

    private List<BookingDTO> fetchAndSortManually(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .get();
        
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(this::mapToDTO)
                .filter(b -> !Boolean.TRUE.equals(b.getDeleted()))
                .sorted((b1, b2) -> {
                    long t1 = b1.getCreatedAt() != null ? b1.getCreatedAt() : 0L;
                    long t2 = b2.getCreatedAt() != null ? b2.getCreatedAt() : 0L;
                    return Long.compare(t2, t1); // Descending
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a specific booking by ID.
     */
    public BookingDTO getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        logger.info("Fetching booking by ID: {}", bookingId);
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(bookingId).get().get();
            if (doc.exists()) {
                BookingDTO dto = mapToDTO(doc);
                if (!Boolean.TRUE.equals(dto.getDeleted())) {
                    return dto;
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching booking {}: {}", bookingId, e.getMessage());
        }
        return null;
    }

    private BookingDTO mapToDTO(DocumentSnapshot doc) {
        return BookingDTO.builder()
                .bookingId(doc.getId())
                .userId(doc.getString("userId"))
                .movieId(doc.getString("movieId"))
                .cinemaId(doc.getString("cinemaId"))
                .roomId(doc.getString("roomId"))
                .showtimeId(doc.getString("showtimeId"))
                .movieTitleSnapshot(doc.getString("movieTitleSnapshot"))
                .movieImageUrlSnapshot(doc.getString("movieImageUrlSnapshot"))
                .cinemaNameSnapshot(doc.getString("cinemaNameSnapshot"))
                .roomNameSnapshot(doc.getString("roomNameSnapshot"))
                .showtimeStartAtSnapshot(doc.getLong("showtimeStartAtSnapshot"))
                .seatCodes((List<String>) doc.get("seatCodes"))
                .seatIds((List<String>) doc.get("seatIds"))
                .snackOrderId(doc.getString("snackOrderId"))
                .subtotal(doc.getDouble("subtotal"))
                .discount(doc.getDouble("discount"))
                .total(doc.getDouble("total"))
                .paymentMethod(doc.getString("paymentMethod"))
                .paymentStatus(doc.getString("paymentStatus"))
                .bookingStatus(doc.getString("bookingStatus"))
                .qrCodeValue(doc.getString("qrCodeValue"))
                .checkInAt(doc.contains("checkInAt") ? doc.getLong("checkInAt") : null)
                .createdAt(doc.getLong("createdAt"))
                .updatedAt(doc.getLong("updatedAt"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .build();
    }
}
