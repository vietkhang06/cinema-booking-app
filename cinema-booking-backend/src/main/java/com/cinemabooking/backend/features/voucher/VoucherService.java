package com.cinemabooking.backend.features.voucher;
import com.cinemabooking.backend.features.payment.model.Payment;

import com.cinemabooking.backend.features.booking.BookingDTO;
import com.cinemabooking.backend.features.voucher.VoucherDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class VoucherService {

    private static final Logger logger = LoggerFactory.getLogger(VoucherService.class);
    
    @Autowired
    private Firestore firestore;

    public void generateVouchersForCancelledShowtime(String showtimeId, int discountPercent, int validDays) {
        logger.info("Generating vouchers for cancelled showtime: {}", showtimeId);
        try {
            // Find all valid bookings for this showtime
            ApiFuture<QuerySnapshot> future = firestore.collection(BookingDTO.COLLECTION_NAME)
                    .whereEqualTo("showtimeId", showtimeId)
                    .get();
            
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                BookingDTO booking = doc.toObject(BookingDTO.class);
                // Only generate for bookings that were paid/confirmed
                if ("SUCCESS".equalsIgnoreCase(booking.getPaymentStatus()) || "CONFIRMED".equalsIgnoreCase(booking.getBookingStatus())) {
                    grantVoucherToUser(booking.getUserId(), discountPercent, validDays, "COMP-" + showtimeId.substring(Math.max(0, showtimeId.length() - 5)));
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error generating vouchers for cancelled showtime: {}", e.getMessage(), e);
        }
    }

    public VoucherDTO generateVoucherForPaymentError(String userId, String bookingId) {
        logger.info("Generating voucher for payment error for user: {}, booking: {}", userId, bookingId);
        // Default compensation for payment error: 100% discount, valid for 30 days
        return grantVoucherToUser(userId, 100, 30, "ERR-" + bookingId.substring(Math.max(0, bookingId.length() - 5)));
    }

    public VoucherDTO grantVoucherToUser(String userId, int discountPercent, int validDays) {
        return grantVoucherToUser(userId, discountPercent, validDays, "GRANT");
    }

    private VoucherDTO grantVoucherToUser(String userId, int discountPercent, int validDays, String prefix) {
        String voucherId = UUID.randomUUID().toString();
        String code = prefix + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        long now = System.currentTimeMillis();
        long expiredAt = now + (validDays * 24L * 60L * 60L * 1000L);

        VoucherDTO voucher = VoucherDTO.builder()
                .voucherId(voucherId)
                .userId(userId)
                .code(code)
                .discountPercent(discountPercent)
                .expiredAt(expiredAt)
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            firestore.collection(VoucherDTO.COLLECTION_NAME).document(voucherId).set(voucher).get();
            logger.info("Granted voucher {} to user {}", code, userId);
            return voucher;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error granting voucher: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<VoucherDTO> getUserVouchers(String userId) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(VoucherDTO.COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get();
            
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<VoucherDTO> vouchers = new ArrayList<>();
            long now = System.currentTimeMillis();

            for (QueryDocumentSnapshot doc : documents) {
                VoucherDTO voucher = doc.toObject(VoucherDTO.class);
                
                // Auto-expire logic if needed when fetching
                if ("ACTIVE".equals(voucher.getStatus()) && voucher.getExpiredAt() < now) {
                    voucher.setStatus("EXPIRED");
                    firestore.collection(VoucherDTO.COLLECTION_NAME).document(voucher.getVoucherId())
                            .update("status", "EXPIRED", "updatedAt", now);
                }
                
                vouchers.add(voucher);
            }
            
            // Sort by expiredAt descending (or ascending depending on preference, let's do latest expiring first)
            vouchers.sort((v1, v2) -> Long.compare(v2.getExpiredAt(), v1.getExpiredAt()));
            return vouchers;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error fetching user vouchers: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public VoucherDTO validateVoucher(String code, String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(VoucherDTO.COLLECTION_NAME)
                .whereEqualTo("code", code)
                .whereEqualTo("userId", userId)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Mã voucher không tồn tại hoặc không thuộc về bạn.");
        }

        VoucherDTO voucher = documents.get(0).toObject(VoucherDTO.class);
        long now = System.currentTimeMillis();

        if (!"ACTIVE".equals(voucher.getStatus())) {
             if (voucher.getExpiredAt() < now && !"USED".equals(voucher.getStatus())) {
                 throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Mã voucher đã hết hạn.");
             }
             throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Mã voucher đã được sử dụng hoặc không còn hiệu lực.");
        }

        if (voucher.getExpiredAt() < now) {
            voucher.setStatus("EXPIRED");
            firestore.collection(VoucherDTO.COLLECTION_NAME).document(voucher.getVoucherId())
                    .update("status", "EXPIRED", "updatedAt", now);
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Mã voucher đã hết hạn.");
        }

        return voucher;
    }

    public void markVoucherAsUsed(String code) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(VoucherDTO.COLLECTION_NAME)
                    .whereEqualTo("code", code)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                VoucherDTO voucher = documents.get(0).toObject(VoucherDTO.class);
                firestore.collection(VoucherDTO.COLLECTION_NAME).document(voucher.getVoucherId())
                        .update("status", "USED", "updatedAt", System.currentTimeMillis());
                logger.info("Marked voucher {} as USED", code);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error marking voucher as used: {}", e.getMessage(), e);
        }
    }
}
