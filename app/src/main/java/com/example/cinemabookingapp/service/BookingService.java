package com.example.cinemabookingapp.service;

import com.example.cinemabookingapp.data.repository.BookingRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.repository.BookingRepository;

import java.util.List;

public class BookingService {
    private final BookingRepository bookingRepo;

    public BookingService() {
        this.bookingRepo = new BookingRepositoryImpl();
    }

    public BookingService(BookingRepository bookingRepo) {
        this.bookingRepo = bookingRepo;
    }

    /**
     * Get bookings for the current authenticated user.
     */
    public void getMyBookings(ResultCallback<List<Booking>> callback) {
        // userId is ignored because the backend /my endpoint uses the auth token
        bookingRepo.getBookingsByUserId(null, callback);
    }

    /**
     * Get details of a specific booking.
     */
    public void getBookingDetails(String bookingId, ResultCallback<Booking> callback) {
        bookingRepo.getBookingById(bookingId, callback);
    }
}
