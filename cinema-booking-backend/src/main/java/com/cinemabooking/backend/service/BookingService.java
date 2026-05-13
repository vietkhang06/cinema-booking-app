package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.*;
import com.cinemabooking.backend.dto.request.SeatBookingRequestDTO;
import com.cinemabooking.backend.model.*;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "bookings";

    public BookingDTO getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        Booking booking = firestore.collection(COLLECTION).document(bookingId).get()
                .get().toObject(Booking.class);
        if(booking == null)
            return null;

        BookingDTO bookingDto = mapToDTO(booking, null, null);

        return bookingDto;
    }

    public BookingDTO createSeatBooking(
            Booking data
    ) throws ExecutionException, InterruptedException{
        Booking booking = firestore.collection(COLLECTION).add(data)
                .get()
                .get()
                .get().toObject(Booking.class);

        return mapToDTO(booking, null, null);
    }

    public BookingDTO mapToDTO(
            Booking booking,
            @Nullable UserDTO user,
            @Nullable ShowtimeDTO showtime
    ){
        return BookingDTO.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                // Mapping Snapshots
                .movieTitleSnapshot(booking.getMovieTitleSnapshot())
                .cinemaNameSnapshot(booking.getCinemaNameSnapshot())
                .roomNameSnapshot(booking.getRoomNameSnapshot())
                .showtimeStartAtSnapshot(booking.getShowtimeStartAtSnapshot())
                // Mapping Collections and Totals
                .seatCodes(booking.getSeatCodes())
                .total(booking.getTotal())
                .paymentStatus(booking.getPaymentStatus())
                .bookingStatus(booking.getBookingStatus())
                .qrCodeValue(booking.getQrCodeValue())
                .createdAt(booking.getCreatedAt())
                // Mapping the "Join"
                .user(user != null ? user : UserDTO.builder().uid(booking.getUserId()).build())
                .showtime(showtime != null ? showtime : ShowtimeDTO.builder().showtimeId(booking.getShowtimeId()).build())
                .build();
    }

    public void updatePaymentStatus(String bookingId, String status) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                Booking.builder()
                    .paymentStatus(status)
                    .paymentAt(status.equals("confirmed") ? System.currentTimeMillis() : 0),
                SetOptions.mergeFields("paymentStatus", "paymentAt"))
        .get();
    }

    public void mapToEntity(){

    }
}
