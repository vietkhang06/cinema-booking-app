package com.cinemabooking.backend.features.booking.service;
import com.cinemabooking.backend.features.booking.dto.BookingDTO;
import com.cinemabooking.backend.features.booking.repository.BookingRepository;
import com.cinemabooking.backend.features.cinema.repository.SeatRepository;
import com.cinemabooking.backend.features.voucher.service.VoucherService;


import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatRepository seatRepository;

    public BookingDTO getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = bookingRepository.findById(bookingId);

        return booking;
    }

    public BookingDTO createBooking(
            BookingDTO data
    ) throws ExecutionException, InterruptedException{
        bookingRepository.save(data);
        return data;
    }

    public void updatePaymentStatus(String bookingId, String paymentStatus, String bookingStatus) throws ExecutionException, InterruptedException {
        bookingRepository.updateStatus(bookingId, paymentStatus, bookingStatus);
    }

    @Autowired
    private VoucherService voucherService;

    public void confirmBookingSeats(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = getBookingById(bookingId);
        if (booking == null) {
            logger.error("[PAYMENT_FAILED] Booking not found for ID: {}", bookingId);
            throw new RuntimeException("Booking not found");
        }
        
        List<String> seatIds = booking.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            logger.warn("Booking {} has no seat IDs associated", bookingId);
            return;
        }
        
        seatRepository.confirmBookingSeats(bookingId, booking.getUserId(), seatIds);

        if (booking.getAppliedVoucherCode() != null && !booking.getAppliedVoucherCode().isEmpty()) {
            voucherService.markVoucherAsUsed(booking.getAppliedVoucherCode());
        }
    }

    public void releaseBookingSeats(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = getBookingById(bookingId);
        if (booking == null) return;
        
        List<String> seatIds = booking.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) return;
        
        seatRepository.releaseBookingSeats(bookingId, seatIds);
    }

    public void updateCheckInTime(String bookingId, long checkInAt) throws ExecutionException, InterruptedException {
        bookingRepository.updateCheckInTime(bookingId, checkInAt);
    }
}
