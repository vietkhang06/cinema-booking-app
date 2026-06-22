package com.cinemabooking.backend.features.payment.service;
import com.cinemabooking.backend.features.payment.model.PaymentMethod;

import com.cinemabooking.backend.features.payment.model.Payment;
import com.cinemabooking.backend.features.payment.model.PaymentStatus;
import com.cinemabooking.backend.features.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import com.cinemabooking.backend.features.voucher.service.VoucherService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.DocumentSnapshot;
import com.cinemabooking.backend.features.booking.service.BookingService;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private Firestore firestore;

    public Payment createPendingPayment(String bookingId, String userId, String provider, double amount) throws ExecutionException, InterruptedException {
        String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();

        String suffix = bookingId.contains("_") ? bookingId.substring(bookingId.indexOf("_") + 1) : bookingId;
        if (suffix.length() > 8) {
            suffix = suffix.substring(0, 8);
        }
        String paymentCode = ("BK" + suffix).toUpperCase();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .paymentCode(paymentCode)
                .userId(userId)
                .provider(provider)
                .amount(amount)
                .status(PaymentStatus.PENDING.name())
                .transactionId("TXN_" + UUID.randomUUID().toString().substring(0, 8))
                .payUrl("http://fake-payurl.com/pay/" + paymentId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        log.info("[PAYMENT_FLOW] Creating payment record: paymentId={}, bookingId={}, paymentCode={}, amount={}, status=PENDING",
                paymentId, bookingId, paymentCode, amount);

        return paymentRepository.save(payment);
    }

    public void handleFailedPayment(String paymentId) {
        log.info("[PAYMENT_FLOW] Handling failed payment: {}", paymentId);
        try {
            Payment payment = paymentRepository.findById(paymentId);
            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED.name());
                payment.setUpdatedAt(System.currentTimeMillis());
                paymentRepository.save(payment);

                String bookingId = payment.getBookingId();
                if (bookingId != null && bookingId.startsWith("cso_")) {
                    // Update CineShop order status in Firestore to FAILED
                    java.util.Map<String, Object> updateData = new java.util.HashMap<>();
                    updateData.put("status", "FAILED");
                    updateData.put("updatedAt", System.currentTimeMillis());

                    firestore.collection("cine_shop_orders").document(bookingId)
                            .set(updateData, SetOptions.merge()).get();
                } else {
                    bookingService.updatePaymentStatus(bookingId, "FAILED", "CANCELLED");
                    bookingService.releaseBookingSeats(bookingId);
                    // Generate compensation voucher
                    voucherService.generateVoucherForPaymentError(payment.getUserId(), bookingId);
                }
            }
        } catch (Exception e) {
            log.error("Error handling failed payment {}", paymentId, e);
        }
    }

    public void handleSuccessPayment(String paymentId, String transactionId) {
        log.info("[PAYMENT_FLOW] Handling success payment: {}, transactionId: {}", paymentId, transactionId);
        try {
            Payment payment = paymentRepository.findById(paymentId);
            if (payment != null) {
                payment.setStatus(PaymentStatus.PAID.name());
                payment.setTransactionId(transactionId);
                payment.setUpdatedAt(System.currentTimeMillis());
                paymentRepository.save(payment);

                String bookingId = payment.getBookingId();
                if (bookingId != null && bookingId.startsWith("cso_")) {
                    // Update CineShop order status in Firestore to SUCCESS
                    java.util.Map<String, Object> updateData = new java.util.HashMap<>();
                    updateData.put("status", "SUCCESS");
                    updateData.put("updatedAt", System.currentTimeMillis());

                    firestore.collection("cine_shop_orders").document(bookingId)
                            .set(updateData, SetOptions.merge()).get();

                    // Create CineShop order notification
                    createCineShopSuccessNotification(bookingId);
                } else {
                    // Update booking status in Firestore to SUCCESS & CONFIRMED, and confirm seats
                    bookingService.updatePaymentStatus(bookingId, "SUCCESS", "CONFIRMED");
                    bookingService.confirmBookingSeats(bookingId);
                }
            } else {
                log.warn("[PAYMENT_FLOW] Payment record not found for ID: {}", paymentId);
            }
        } catch (Exception e) {
            log.error("Error handling success payment {}", paymentId, e);
        }
    }

    private void createCineShopSuccessNotification(String orderId) {
        try {
            DocumentSnapshot orderDoc = firestore.collection("cine_shop_orders").document(orderId).get().get();
            if (orderDoc.exists()) {
                String targetUserId = orderDoc.getString("userId");
                if (targetUserId != null) {
                    String notifId = "notif_" + java.util.UUID.randomUUID().toString();
                    java.util.Map<String, Object> notif = new java.util.HashMap<>();
                    notif.put("notificationId", notifId);
                    notif.put("userId", targetUserId);
                    notif.put("title", "Mua bắp nước thành công");
                    notif.put("message", "Đơn hàng bắp nước của bạn đã thanh toán thành công. Vui lòng nhận bắp nước tại quầy!");
                    notif.put("type", "BOOKING_SUCCESS");
                    notif.put("refId", orderId);
                    notif.put("isRead", false);
                    notif.put("createdAt", System.currentTimeMillis());
                    notif.put("updatedAt", System.currentTimeMillis());

                    firestore.collection("notifications").document(notifId).set(notif);
                    log.info("[NOTIFICATION] Created CineShop success notification {} for user {}", notifId, targetUserId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to create CineShop success notification for order: " + orderId, e);
        }
    }
}
