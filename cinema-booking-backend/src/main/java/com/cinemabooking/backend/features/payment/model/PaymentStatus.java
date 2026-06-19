package com.cinemabooking.backend.features.payment.model;

public enum PaymentStatus {
    PENDING,
    WAITING_CONFIRMATION,
    PAID,
    FAILED,
    EXPIRED,
    CANCELLED
}

