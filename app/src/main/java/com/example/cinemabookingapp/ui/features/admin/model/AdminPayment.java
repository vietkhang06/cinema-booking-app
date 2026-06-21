package com.example.cinemabookingapp.ui.features.admin.model;

public class AdminPayment {
    public String paymentId;
    public String bookingId;
    public String paymentCode;
    public String userId;
    public String provider;
    public double amount;
    public String status;
    public String transactionId;
    public String payUrl;
    public Long createdAt;
    public Long updatedAt;

    public AdminPayment() {
        // Default constructor required for Firestore toObject()
    }
}
