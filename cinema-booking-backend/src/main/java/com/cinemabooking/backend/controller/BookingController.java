package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.BookingDTO;
import com.cinemabooking.backend.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Endpoints for customer booking history and details")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @GetMapping("/my")
    @Operation(summary = "Get current user's booking history", description = "Fetches all non-deleted bookings for the authenticated user.")
    public ApiResponse<List<BookingDTO>> getMyBookings(@AuthenticationPrincipal Object principal) throws ExecutionException, InterruptedException {
        if (principal == null) {
            return ApiResponse.<List<BookingDTO>>builder()
                    .success(false)
                    .message("Unauthorized: No principal found in security context")
                    .build();
        }

        String uid = principal.toString();
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(uid);
        
        return ApiResponse.<List<BookingDTO>>builder()
                .success(true)
                .message("Fetched " + bookings.size() + " bookings")
                .data(bookings)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking detail by ID", description = "Fetches details of a specific booking. Ownership check included.")
    public ApiResponse<BookingDTO> getBookingById(
            @PathVariable String id,
            @AuthenticationPrincipal Object principal) throws ExecutionException, InterruptedException {
        
        if (principal == null) {
            return ApiResponse.<BookingDTO>builder()
                    .success(false)
                    .message("Unauthorized")
                    .build();
        }

        String uid = principal.toString();
        BookingDTO booking = bookingService.getBookingById(id);

        if (booking == null) {
            return ApiResponse.<BookingDTO>builder()
                    .success(false)
                    .message("Booking not found")
                    .build();
        }

        // Security Check: Only allow the owner to see the booking
        if (!uid.equals(booking.getUserId())) {
            logger.warn("Unauthorized access attempt: User {} tried to access booking {} owned by {}", uid, id, booking.getUserId());
            return ApiResponse.<BookingDTO>builder()
                    .success(false)
                    .message("Unauthorized: You do not own this booking")
                    .build();
        }

        return ApiResponse.<BookingDTO>builder()
                .success(true)
                .message("Booking details fetched")
                .data(booking)
                .build();
    }
}
