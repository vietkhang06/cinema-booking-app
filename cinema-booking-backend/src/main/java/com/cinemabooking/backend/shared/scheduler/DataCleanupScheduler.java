package com.cinemabooking.backend.shared.scheduler;

import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanupScheduler.class);

    @Autowired
    private Firestore firestore;

    @Scheduled(cron = "0 0 2 * * ?") // Chạy mỗi ngày vào lúc 2:00 sáng
    public void cleanupHistoricalData() {
        logger.info("[CLEANUP] Starting daily historical data cleanup...");
        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000L);

        try {
            // 1. Dọn dẹp Showtime kết thúc quá 30 ngày
            cleanupExpiredShowtimesAndSeats(thirtyDaysAgo);

            // 2. Dọn dẹp Booking CANCELLED quá 30 ngày
            cleanupCancelledBookings(thirtyDaysAgo);

            // 3. Dọn dẹp Payment FAILED quá 30 ngày
            cleanupFailedPayments(thirtyDaysAgo);

            logger.info("[CLEANUP] Daily historical data cleanup completed successfully.");
        } catch (Exception e) {
            logger.error("[CLEANUP] Error occurred during historical data cleanup: {}", e.getMessage(), e);
        }
    }

    private void cleanupExpiredShowtimesAndSeats(long threshold) throws Exception {
        List<QueryDocumentSnapshot> expiredShowtimes = firestore.collection("showtimes")
                .whereLessThan("endAt", threshold)
                .get().get().getDocuments();

        logger.info("[CLEANUP] Found {} expired showtimes to delete", expiredShowtimes.size());

        for (DocumentSnapshot showtimeDoc : expiredShowtimes) {
            String showtimeId = showtimeDoc.getId();
            
            // Xóa toàn bộ Seat thuộc Showtime này
            List<QueryDocumentSnapshot> seats = firestore.collection("seats")
                    .whereEqualTo("showtimeId", showtimeId)
                    .get().get().getDocuments();
                    
            logger.info("[CLEANUP] Deleting {} seats associated with showtime {}", seats.size(), showtimeId);
            deleteInBatches(seats);

            // Xóa Showtime
            showtimeDoc.getReference().delete().get();
        }
    }

    private void cleanupCancelledBookings(long threshold) throws Exception {
        List<QueryDocumentSnapshot> cancelledBookings = firestore.collection("bookings")
                .whereEqualTo("bookingStatus", "CANCELLED")
                .whereLessThan("updatedAt", threshold)
                .get().get().getDocuments();

        logger.info("[CLEANUP] Found {} cancelled bookings to delete", cancelledBookings.size());
        deleteInBatches(cancelledBookings);
    }

    private void cleanupFailedPayments(long threshold) throws Exception {
        List<QueryDocumentSnapshot> failedPayments = firestore.collection("payments")
                .whereEqualTo("status", "FAILED")
                .whereLessThan("updatedAt", threshold)
                .get().get().getDocuments();

        logger.info("[CLEANUP] Found {} failed payments to delete", failedPayments.size());
        deleteInBatches(failedPayments);
    }

    private void deleteInBatches(List<? extends DocumentSnapshot> documents) throws Exception {
        if (documents.isEmpty()) return;

        int batchSize = 500;
        for (int i = 0; i < documents.size(); i += batchSize) {
            List<? extends DocumentSnapshot> subList = documents.subList(i, Math.min(i + batchSize, documents.size()));
            WriteBatch batch = firestore.batch();
            for (DocumentSnapshot doc : subList) {
                batch.delete(doc.getReference());
            }
            batch.commit().get();
        }
    }
}
