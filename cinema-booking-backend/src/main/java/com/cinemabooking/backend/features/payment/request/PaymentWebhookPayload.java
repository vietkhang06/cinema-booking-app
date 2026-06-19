package com.cinemabooking.backend.features.payment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookPayload {
    private String paymentId;
    private String transactionId;
    private String status;
}
