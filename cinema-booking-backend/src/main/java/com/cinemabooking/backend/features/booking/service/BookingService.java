package com.cinemabooking.backend.features.booking.service;

import com.cinemabooking.backend.features.booking.dto.BookingDTO;
import com.cinemabooking.backend.features.booking.repository.BookingRepository;
import com.cinemabooking.backend.features.cinema.repository.SeatRepository;
import com.cinemabooking.backend.features.voucher.service.VoucherService;
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

    @Autowired
    private VoucherService voucherService;

    public BookingDTO getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        return bookingRepository.findById(bookingId);
    }

    public BookingDTO createBooking(BookingDTO data) throws ExecutionException, InterruptedException {
        return bookingRepository.save(data);
    }

    public void updatePaymentStatus(String bookingId, String paymentStatus, String bookingStatus) throws ExecutionException, InterruptedException {
        bookingRepository.updatePaymentStatus(bookingId, paymentStatus, bookingStatus);
    }

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

        long now = System.currentTimeMillis();
        seatRepository.confirmBookingSeats(bookingId, seatIds, booking.getUserId(), now);

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

    public List<BookingDTO> getAllBookings() throws ExecutionException, InterruptedException {
        return bookingRepository.findAll();
    }

    public List<BookingDTO> getBookingsCreatedAfter(long timestamp) throws ExecutionException, InterruptedException {
        return bookingRepository.findCreatedAfter(timestamp);
    }

    public List<BookingDTO> getBookingsByUserId(String userId) throws ExecutionException, InterruptedException {
        return bookingRepository.findByUserId(userId);
    }
}
