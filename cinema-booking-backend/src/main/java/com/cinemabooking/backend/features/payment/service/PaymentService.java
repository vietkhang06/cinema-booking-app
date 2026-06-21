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
        // TODO: Hook this up to actual payment webhook later.
        // Assuming we look up the bookingId and userId from the payment record
        try {
            Payment payment = paymentRepository.findById(paymentId);
            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED.name());
                payment.setUpdatedAt(System.currentTimeMillis());
                paymentRepository.save(payment);

                // Generate compensation voucher
                voucherService.generateVoucherForPaymentError(payment.getUserId(), payment.getBookingId());
            }
        } catch (ExecutionException | InterruptedException e) {
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

                // Update booking status in Firestore to SUCCESS & CONFIRMED, and confirm seats
                bookingService.updatePaymentStatus(payment.getBookingId(), "SUCCESS", "CONFIRMED");
                bookingService.confirmBookingSeats(payment.getBookingId());
            } else {
                log.warn("[PAYMENT_FLOW] Payment record not found for ID: {}", paymentId);
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error handling success payment {}", paymentId, e);
        }
    }
}
