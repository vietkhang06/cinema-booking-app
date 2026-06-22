package com.cinemabooking.backend.shared.scheduler;

import com.cinemabooking.backend.features.booking.repository.BookingRepository;
import com.cinemabooking.backend.features.cinema.repository.SeatRepository;
import com.cinemabooking.backend.features.cinema.repository.ShowtimeRepository;
import com.cinemabooking.backend.features.user.repository.UserRepository;
import com.cinemabooking.backend.features.payment.repository.PaymentRepository;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeatReleaseScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SeatReleaseScheduler.class);

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void releaseExpiredSeatsAndBookings() {
        logger.info("Scanning for expired seat holds and bookings...");
        long now = System.currentTimeMillis();

        try {
            // Task 1: Scan and release expired seat holds
            int releasedSeatsCount = seatRepository.releaseExpiredSeats(now);
            if (releasedSeatsCount > 0) {
                logger.info("[SEAT_RELEASE] Released {} expired held seats", releasedSeatsCount);
            }

            // Task 2: Scan and cancel pending bookings that have timed out (online payments only, older than 5 mins)
            long timeoutBoundary = now - 300000;
            List<QueryDocumentSnapshot> pendingBookings = bookingRepository.getFirestore()
                    .collection("bookings")
                    .whereEqualTo("bookingStatus", "PENDING")
                    .whereIn("paymentMethod", java.util.List.of("credit_card", "momo", "bank", "bank_transfer"))
                    .whereLessThan("createdAt", timeoutBoundary)
                    .get().get().getDocuments();
            
            for (DocumentSnapshot bookingDoc : pendingBookings) {
                Long createdAtVal = bookingDoc.getLong("createdAt");
                long createdAt = createdAtVal != null ? createdAtVal : 0L;

                String paymentMethod = bookingDoc.getString("paymentMethod");
                boolean shouldCancel = false;

                if ("cash".equalsIgnoreCase(paymentMethod)) {
                    shouldCancel = false; // Do not cancel cash bookings automatically
                } else {
                    // Online payment (momo, bank transfer) cancels after 5 mins if unpaid
                    if (createdAt > 0 && (createdAt + 300000) < now) {
                        shouldCancel = true;
                    }
                }

                if (shouldCancel) {
                    String bookingId = bookingDoc.getId();
                    logger.info("[BOOKING_TIMEOUT] Booking {} (Method: {}) has expired. Cancelling dynamically...", bookingId, paymentMethod);

                    WriteBatch batch = bookingRepository.getFirestore().batch();

                    // 1. Cancel booking
                    batch.update(bookingDoc.getReference(),
                            "bookingStatus", "CANCELLED",
                            "paymentStatus", "FAILED",
                            "updatedAt", now
                    );

                    // Refund points if consumed
                    Long ptsConsumedVal = bookingDoc.getLong("pointsConsumed");
                    int ptsConsumed = ptsConsumedVal != null ? ptsConsumedVal.intValue() : 0;
                    String userId = bookingDoc.getString("userId");
                    if (ptsConsumed > 0 && userId != null) {
                        batch.update(userRepository.getDocumentReference(userId),
                                "points", FieldValue.increment(ptsConsumed)
                        );
                        logger.info("[LOYALTY_REFUND] Scheduler refunded {} points to user {}", ptsConsumed, userId);
                    }

                    // 2. Find and cancel payment
                    List<QueryDocumentSnapshot> paymentDocs = paymentRepository.findByBookingId(bookingId);
                    for (DocumentSnapshot paymentDoc : paymentDocs) {
                        batch.update(paymentDoc.getReference(),
                                "status", "FAILED",
                                "updatedAt", now
                        );
                    }

                    // 3. Release seats for this booking
                    List<String> seatIds = (List<String>) bookingDoc.get("seatIds");
                    if (seatIds != null) {
                        for (String seatId : seatIds) {
                            batch.update(seatRepository.getDocumentReference(seatId),
                                    "status", "available",
                                    "heldBy", null,
                                    "heldUntil", 0L
                            );
                        }
                    }

                    // 4. Decrease bookedSeatsCount for showtime
                    String showtimeId = bookingDoc.getString("showtimeId");
                    if (showtimeId != null && seatIds != null) {
                        batch.update(showtimeRepository.getDocumentReference(showtimeId),
                                "bookedSeatsCount", FieldValue.increment(-seatIds.size())
                        );
                    }

                    batch.commit().get();
                    logger.info("[BOOKING_TIMEOUT_COMPLETE] Booking {}, seats and payments updated dynamically", bookingId);
                }
            }

        } catch (Exception e) {
            logger.error("Error during scanning/releasing expired seats and bookings: {}", e.getMessage(), e);
        }
    }
}
