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
        logger.info("Scanning for expired seat holds, bookings, and payments...");
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
                    .get().get().getDocuments();
            
            for (DocumentSnapshot bookingDoc : pendingBookings) {
                Long createdAtVal = bookingDoc.getLong("createdAt");
                long createdAt = createdAtVal != null ? createdAtVal : 0L;

                String paymentMethod = bookingDoc.getString("paymentMethod");
                boolean shouldCancel = false;

                if (createdAt > 0 && createdAt < timeoutBoundary) {
                    if (paymentMethod != null && java.util.List.of("credit_card", "momo", "bank", "bank_transfer").contains(paymentMethod.toLowerCase())) {
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

            // Task 3: Scan and cancel pending payments that have timed out (older than 5 mins)
            List<QueryDocumentSnapshot> pendingPayments = paymentRepository.getFirestore()
                    .collection("payments")
                    .whereIn("status", java.util.List.of("PENDING", "pending", "WAITING_CONFIRMATION", "waiting_confirmation"))
                    .get().get().getDocuments();

            for (DocumentSnapshot paymentDoc : pendingPayments) {
                Long createdAtVal = paymentDoc.getLong("createdAt");
                long createdAt = createdAtVal != null ? createdAtVal : 0L;

                if (createdAt > 0 && createdAt < timeoutBoundary) {
                    String paymentId = paymentDoc.getId();
                    String bookingId = paymentDoc.getString("bookingId");
                    logger.info("[PAYMENT_TIMEOUT] Payment {} (Booking: {}) has expired. Cancelling...", paymentId, bookingId);

                    WriteBatch batch = paymentRepository.getFirestore().batch();

                    // Cancel payment
                    batch.update(paymentDoc.getReference(),
                            "status", "FAILED",
                            "updatedAt", now
                    );

                    if (bookingId != null) {
                        if (bookingId.startsWith("cso_")) {
                            // Cancel CineShop order
                            batch.update(paymentRepository.getFirestore().collection("cine_shop_orders").document(bookingId),
                                    "status", "FAILED",
                                    "updatedAt", now
                            );
                        } else {
                            // Fetch booking doc to see if it needs cancelling
                            DocumentSnapshot bookingDoc = bookingRepository.getFirestore()
                                    .collection("bookings").document(bookingId).get().get();
                            
                            if (bookingDoc.exists()) {
                                String bookingStatus = bookingDoc.getString("bookingStatus");
                                if (!"CANCELLED".equalsIgnoreCase(bookingStatus)) {
                                    // Cancel booking
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

                                    // Release seats
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

                                    // Decrease bookedSeatsCount for showtime
                                    String showtimeId = bookingDoc.getString("showtimeId");
                                    if (showtimeId != null && seatIds != null) {
                                        batch.update(showtimeRepository.getDocumentReference(showtimeId),
                                                "bookedSeatsCount", FieldValue.increment(-seatIds.size())
                                        );
                                    }
                                }
                            }
                        }
                    }

                    batch.commit().get();
                    logger.info("[PAYMENT_TIMEOUT_COMPLETE] Payment {} and associated entities updated", paymentId);
                }
            }

        } catch (Exception e) {
            logger.error("Error during scanning/releasing expired seats, bookings, and payments: {}", e.getMessage(), e);
        }
    }
}
