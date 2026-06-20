package com.cinemabooking.backend.features.booking.repository;

import com.cinemabooking.backend.features.booking.dto.BookingDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class BookingRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = BookingDTO.COLLECTION_NAME;

    public BookingDTO findById(String bookingId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(bookingId).get()
                .get().toObject(BookingDTO.class);
    }

    public BookingDTO save(BookingDTO data) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(data.getBookingId()).set(data).get();
        return data;
    }

    public void updatePaymentStatus(String bookingId, String paymentStatus, String bookingStatus) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                BookingDTO.builder()
                        .paymentStatus(paymentStatus)
                        .bookingStatus(bookingStatus)
                        .paymentAt("SUCCESS".equalsIgnoreCase(paymentStatus) || "confirmed".equalsIgnoreCase(paymentStatus) ? System.currentTimeMillis() : 0)
                        .updatedAt(System.currentTimeMillis())
                        .build(),
                SetOptions.mergeFields("paymentStatus", "bookingStatus", "paymentAt", "updatedAt"))
                .get();
    }

    public void updateCheckInTime(String bookingId, long checkInAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                BookingDTO.builder()
                        .checkInAt(checkInAt)
                        .updatedAt(System.currentTimeMillis())
                        .build(),
                SetOptions.mergeFields("checkInAt", "updatedAt"))
                .get();
    }

    public List<BookingDTO> findByShowtimeId(String showtimeId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("showtimeId", showtimeId)
                .get();
        return future.get().toObjects(BookingDTO.class);
    }

    public List<BookingDTO> findAll() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).get().get().toObjects(BookingDTO.class);
    }

    public List<BookingDTO> findCreatedAfter(long timestamp) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereGreaterThanOrEqualTo("createdAt", timestamp)
                .get()
                .get()
                .toObjects(BookingDTO.class);
    }

    public List<BookingDTO> findByUserId(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .get()
                .toObjects(BookingDTO.class);
    }
}
