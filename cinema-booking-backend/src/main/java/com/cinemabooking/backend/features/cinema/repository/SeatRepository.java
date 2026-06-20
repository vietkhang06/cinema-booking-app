package com.cinemabooking.backend.features.cinema.repository;

import com.cinemabooking.backend.features.cinema.dto.SeatDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class SeatRepository {

    private static final Logger logger = LoggerFactory.getLogger(SeatRepository.class);
    private static final String SEATS_COL = "seats";
    private static final String TEMPLATES_COL = "seat_templates";

    @Autowired
    private Firestore firestore;

    public List<QueryDocumentSnapshot> findByShowtimeId(String showtimeId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(SEATS_COL)
                .whereEqualTo("showtimeId", showtimeId)
                .get();
        return future.get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findTemplatesByRoomId(String roomId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(TEMPLATES_COL)
                .whereEqualTo("roomId", roomId)
                .get();
        return future.get().getDocuments();
    }

    public void saveSeatsBatch(List<SeatDTO> seats) throws ExecutionException, InterruptedException {
        if (seats == null || seats.isEmpty()) return;
        WriteBatch batch = firestore.batch();
        for (SeatDTO seat : seats) {
            batch.set(firestore.collection(SEATS_COL).document(seat.getSeatId()), seat);
        }
        batch.commit().get();
    }

    public List<SeatDTO> findSeatsByIds(List<String> seatIds) throws ExecutionException, InterruptedException {
        if (seatIds == null || seatIds.isEmpty()) return new ArrayList<>();
        return firestore.collection(SEATS_COL)
                .whereIn("seatId", seatIds)
                .get()
                .get()
                .toObjects(SeatDTO.class);
    }

    public void lockSeats(String userId, String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        firestore.runTransaction(transaction -> {
            long now = System.currentTimeMillis();
            long holdUntil = now + (7 * 60 * 1000); // 7 minutes
            
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(SEATS_COL).document(seatId);
                refs.add(ref);
            }
            
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
            
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) {
                    throw new RuntimeException("Seat " + doc.getId() + " does not exist");
                }
                
                String status = doc.getString("status");
                String heldBy = doc.getString("heldBy");
                Long heldUntilVal = doc.getLong("heldUntil");
                long heldUntilTime = heldUntilVal != null ? heldUntilVal : 0L;
                
                if ("booked".equalsIgnoreCase(status)) {
                    logger.warn("[SEAT_CONFLICT] Seat {} is already booked", doc.getId());
                    throw new RuntimeException("Seat " + doc.getId() + " is already booked");
                }
                
                if ("held".equalsIgnoreCase(status) && heldUntilTime > now && !userId.equals(heldBy)) {
                    logger.warn("[SEAT_CONFLICT] Seat {} is held by another user {} until {}", doc.getId(), heldBy, heldUntilTime);
                    throw new RuntimeException("Seat " + doc.getId() + " is held by another user");
                }
            }
            
            for (DocumentReference ref : refs) {
                logger.info("[SEAT_LOCK] Locking seat {} for user {} until {}", ref.getId(), userId, holdUntil);
                transaction.update(ref, 
                    "status", "held",
                    "heldBy", userId,
                    "heldUntil", holdUntil
                );
            }
            return null;
        }).get();
    }

    public void releaseSeats(String userId, String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        firestore.runTransaction(transaction -> {
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(SEATS_COL).document(seatId);
                refs.add(ref);
            }
            
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
            
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) continue;
                
                String status = doc.getString("status");
                String heldBy = doc.getString("heldBy");
                
                if ("held".equalsIgnoreCase(status) && userId.equals(heldBy)) {
                    logger.info("[SEAT_RELEASE] Releasing seat {} held by user {}", doc.getId(), userId);
                    transaction.update(doc.getReference(), 
                        "status", "available",
                        "heldBy", null,
                        "heldUntil", 0L
                    );
                }
            }
            return null;
        }).get();
    }

    public void releaseSeatsByStaff(String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        firestore.runTransaction(transaction -> {
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(SEATS_COL).document(seatId);
                refs.add(ref);
            }
            
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
            
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) continue;
                
                String status = doc.getString("status");
                
                if ("held".equalsIgnoreCase(status)) {
                    logger.info("[STAFF_SEAT_RELEASE] Staff releasing seat {} held by {}", doc.getId(), doc.getString("heldBy"));
                    transaction.update(doc.getReference(), 
                        "status", "available",
                        "heldBy", null,
                        "heldUntil", 0L
                    );
                }
            }
            return null;
        }).get();
    }

    public void confirmBookingSeats(String bookingId, List<String> seatIds, String userId, long now) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        for (String seatId : seatIds) {
            logger.info("[PAYMENT_SUCCESS] Booking {} confirmed. Marking seat {} as booked by {}", bookingId, seatId, userId);
            batch.update(firestore.collection(SEATS_COL).document(seatId),
                    "status", "booked",
                    "bookedBy", userId,
                    "bookedAt", now
            );
        }
        batch.commit().get();
    }

    public void releaseBookingSeats(String bookingId, List<String> seatIds) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        for (String seatId : seatIds) {
            logger.info("[PAYMENT_FAILED] Booking {} failed/cancelled. Releasing seat {}", bookingId, seatId);
            batch.update(firestore.collection(SEATS_COL).document(seatId),
                    "status", "available",
                    "heldBy", null,
                    "heldUntil", 0L
            );
        }
        batch.commit().get();
    }
}
