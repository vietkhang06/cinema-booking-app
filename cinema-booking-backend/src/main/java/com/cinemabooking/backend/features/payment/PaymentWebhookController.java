package com.cinemabooking.backend.features.payment;
import com.cinemabooking.backend.features.payment.model.Payment;
import com.cinemabooking.backend.features.payment.model.PaymentMethod;
import com.cinemabooking.backend.features.payment.model.PaymentStatus;

import com.cinemabooking.backend.features.payment.request.PaymentWebhookPayload;
import com.cinemabooking.backend.features.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Endpoints for external service webhooks")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    @Operation(summary = "Receive payment webhook from payment gateway")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody PaymentWebhookPayload payload) {
        log.info("[WEBHOOK] Received payment webhook for paymentId: {}, status: {}", payload.getPaymentId(), payload.getStatus());

        if ("FAILED".equalsIgnoreCase(payload.getStatus())) {
            paymentService.handleFailedPayment(payload.getPaymentId());
        } else if ("SUCCESS".equalsIgnoreCase(payload.getStatus())) {
            // Further implementation could update payment status or booking status to confirmed.
            log.info("[WEBHOOK] Payment {} succeeded.", payload.getPaymentId());
            paymentService.handleSuccessPayment(payload.getPaymentId(), payload.getTransactionId());
        }

        return ResponseEntity.ok("OK");
    }
}
