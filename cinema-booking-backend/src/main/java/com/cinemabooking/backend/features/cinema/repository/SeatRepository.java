package com.cinemabooking.backend.features.cinema.repository;

import com.cinemabooking.backend.features.cinema.dto.SeatDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
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
    private static final String COLLECTION = "seats";

    @Autowired
    private Firestore firestore;

    public List<QueryDocumentSnapshot> findByShowtimeId(String showtimeId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("showtimeId", showtimeId)
                .get().get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findByIds(List<String> seatIds) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereIn("seatId", seatIds)
                .get().get().getDocuments();
    }

    public void saveAll(List<SeatDTO> seats) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        for (SeatDTO seat : seats) {
            batch.set(firestore.collection(COLLECTION).document(seat.getSeatId()), seat);
        }
        batch.commit().get();
    }

    public void confirmBookingSeats(String bookingId, String userId, List<String> seatIds) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        WriteBatch batch = firestore.batch();
        for (String seatId : seatIds) {
            logger.info("[PAYMENT_SUCCESS] Booking {} confirmed. Marking seat {} as booked by {}", bookingId, seatId, userId);
            batch.update(firestore.collection(COLLECTION).document(seatId),
                    "status", "booked",
                    "bookedBy", userId,
                    "bookedAt", now,
                    "heldBy", null,
                    "heldUntil", 0L
            );
        }
        batch.commit().get();
    }

    public void extendSeatHolds(List<String> seatIds, long newHeldUntil) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        for (String seatId : seatIds) {
            logger.info("[SEAT_HOLD_EXTEND] Extending hold for seat {} until {}", seatId, newHeldUntil);
            batch.update(firestore.collection(COLLECTION).document(seatId),
                    "heldUntil", newHeldUntil
            );
        }
        batch.commit().get();
    }

    public void releaseBookingSeats(String bookingId, List<String> seatIds) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        for (String seatId : seatIds) {
            logger.info("[PAYMENT_FAILED] Booking {} failed/cancelled. Releasing seat {}", bookingId, seatId);
            batch.update(firestore.collection(COLLECTION).document(seatId),
                    "status", "available",
                    "heldBy", null,
                    "heldUntil", 0L
            );
        }
        batch.commit().get();
    }

    public void lockSeats(String userId, List<String> seatIds) throws ExecutionException, InterruptedException {
        firestore.runTransaction(transaction -> {
            long now = System.currentTimeMillis();
            long holdUntil = now + (5 * 60 * 1000); // 5 minutes

            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(COLLECTION).document(seatId);
                refs.add(ref);
            }

            // Read all docs in transaction
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();

            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) {
                    throw new RuntimeException("Seat " + doc.getId() + " does not exist");
                }

                String status = doc.getString("status");
                String heldBy = doc.getString("heldBy");
                Long heldUntilVal = doc.getLong("heldUntil");
                long heldUntilTime = heldUntilVal != null ? heldUntilVal : 0L;

                // Concurrency checks
                if ("booked".equalsIgnoreCase(status)) {
                    logger.warn("[SEAT_CONFLICT] Seat {} is already booked", doc.getId());
                    throw new RuntimeException("Seat " + doc.getId() + " is already booked");
                }

                if ("held".equalsIgnoreCase(status) && heldUntilTime > now && !userId.equals(heldBy)) {
                    logger.warn("[SEAT_CONFLICT] Seat {} is held by another user {} until {}", doc.getId(), heldBy, heldUntilTime);
                    throw new RuntimeException("Seat " + doc.getId() + " is held by another user");
                }
            }

            // If all checks pass, write update
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

    public void releaseSeats(String userId, List<String> seatIds) throws ExecutionException, InterruptedException {
        firestore.runTransaction(transaction -> {
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(COLLECTION).document(seatId);
                refs.add(ref);
            }

            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();

            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) continue;

                String status = doc.getString("status");
                String heldBy = doc.getString("heldBy");

                // Only release if held by this user and not booked
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

    public void releaseSeatsByStaff(List<String> seatIds) throws ExecutionException, InterruptedException {
        firestore.runTransaction(transaction -> {
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(COLLECTION).document(seatId);
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

    public List<QueryDocumentSnapshot> findExpiredHeldSeats(long now) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("status", "held")
                .whereLessThan("heldUntil", now)
                .get().get().getDocuments();
    }

    public int releaseExpiredSeats(long now) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> expiredSeats = findExpiredHeldSeats(now);
        WriteBatch seatBatch = firestore.batch();
        int count = 0;

        for (DocumentSnapshot doc : expiredSeats) {
            logger.info("[SEAT_RELEASE] Seat hold expired for seatId={}. Releasing seat...", doc.getId());
            seatBatch.update(doc.getReference(),
                    "status", "available",
                    "heldBy", null,
                    "heldUntil", 0L
            );
            count++;
        }

        if (count > 0) {
            seatBatch.commit().get();
        }
        return count;
    }

    public DocumentReference getDocumentReference(String seatId) {
        return firestore.collection(COLLECTION).document(seatId);
    }
}
