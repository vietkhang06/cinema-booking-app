package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.BannerDTO;
import com.cinemabooking.backend.dto.BookingDTO;
import com.cinemabooking.backend.dto.BookingDetailDTO;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BookingService {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "bookings";

    public List<BannerDTO> getAllBanners() throws ExecutionException, InterruptedException {

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<BannerDTO> banners = new ArrayList<>();

        for (DocumentSnapshot doc : documents) {
            Boolean isActive = doc.getBoolean("isActive");
            if (isActive != null && !isActive) continue;

            banners.add(BannerDTO.builder()
                    .bannerId(doc.getId())
                    .imageUrl(doc.getString("imageUrl"))
                    .build());
        }
        return banners;
    }

    public BookingDTO getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = firestore.collection(COLLECTION).document(bookingId).get();
        BookingDTO booking = future.get().toObject(BookingDTO.class);

        return booking;
    }

    public BookingDetailDTO getBookingDetailById(String bookingId) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = firestore.collection(COLLECTION).document(bookingId).get();
        BookingDetailDTO booking = future.get().toObject(BookingDetailDTO.class);

        return booking;
    }
}
