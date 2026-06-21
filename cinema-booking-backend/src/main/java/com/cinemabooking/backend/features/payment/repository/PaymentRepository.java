package com.cinemabooking.backend.features.payment.repository;
import com.cinemabooking.backend.features.payment.model.PaymentMethod;
import com.cinemabooking.backend.features.payment.model.PaymentStatus;

import com.cinemabooking.backend.features.payment.model.Payment;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class PaymentRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "payments";

    public Payment save(Payment payment) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(payment.getPaymentId()).set(payment).get();
        return payment;
    }

    public Payment findById(String paymentId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(paymentId).get().get().toObject(Payment.class);
    }

    public List<com.google.cloud.firestore.QueryDocumentSnapshot> findByBookingId(String bookingId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).whereEqualTo("bookingId", bookingId).get().get().getDocuments();
    }

    public void updateStatus(String paymentDocId, String status, long updatedAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(paymentDocId).update("status", status, "updatedAt", updatedAt).get();
    }

    public com.google.cloud.firestore.DocumentReference getDocumentReference(String id) {
        return firestore.collection(COLLECTION).document(id);
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
