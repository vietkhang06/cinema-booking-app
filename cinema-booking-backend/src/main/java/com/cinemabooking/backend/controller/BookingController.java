package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.*;
import com.cinemabooking.backend.dto.request.SeatBookingRequestDTO;
import com.cinemabooking.backend.model.*;
import com.cinemabooking.backend.service.BookingService;
import com.cinemabooking.backend.service.MovieService;
import com.cinemabooking.backend.service.ShowtimeService;
import com.cinemabooking.backend.service.UserService;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.auth.FirebaseToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ObjectInputFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Endpoints for advertising booking")
public class BookingController {

    @Autowired private Firestore firestore;

    @Autowired private BookingService bookingService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private UserService userService;
    @Autowired private MovieService movieService;

    @GetMapping("{id}")
    @Operation(summary = "Get booking detail by id")
    public ApiResponse<BookingDTO> getBookingDetailById(@PathVariable("id") String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if(booking == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        if(booking.getShowtimeId() != null){
            ShowtimeDTO showTime = showtimeService.getShowtimeById(booking.getShowtimeId());
            booking.setShowtime(showTime);
        }
        UserDTO user = userService.getUserById(booking.getUserId());

        booking.setUser(user);
        return ApiResponse.<BookingDTO>builder()
                .success(true)
                .message("Booking fetched successfully")
                .data(booking)
                .build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatBookingRequestDTO bookingRequest
    ) throws ExecutionException, InterruptedException {
        SeatBookingRequestDTO data = bookingRequest;
        List<SnackOrderSnapshot> orders = new ArrayList<>();
        if (data.getSnackOrders() != null && data.getSnackOrders().size() > 0){
            List<Snack> snacks = firestore.collection("snacks")
                    .whereIn("snackId", data.getSnackOrders().stream().map(item -> item.snackId()).collect(Collectors.toList()) )
                    .get()
                    .get().toObjects(Snack.class);
            snacks.stream().forEach(snack -> {
                SeatBookingRequestDTO.SnackOrder order = data.getSnackOrders().stream().filter(snackOrder -> snackOrder.snackId().equals(snack.snackId)).findFirst().orElse(null);
                orders.add(
                        SnackOrderSnapshot.builder()
                                .snackId(snack.getSnackId())
                                .snackName(snack.getName())
                                .snackImgURL(snack.getImageUrl())
                                .price(snack.getPrice())
                                .quantity(order.quantity())
                                .build()
                );
            });
        }

        String uniqueID = UUID.randomUUID().toString();

        ShowtimeDTO showtime = showtimeService.getShowtimeById(data.getShowtimeId());
        MovieDTO movie = movieService.getMovieById(showtime.getMovieId());

        List<DocumentSnapshot> taskResult = ApiFutures.allAsList(Arrays.asList(
                firestore.collection("rooms").document(showtime.getRoomId()).get(),
                firestore.collection("cinemas").document(showtime.getCinemaId()).get()
        )).get();

        Room room = taskResult.get(0).toObject(Room.class);
        Cinema cinema = taskResult.get(1).toObject(Cinema.class);

        List<Seat> seats = firestore.collection("seats")
                .whereIn("seatId", data.getSeatIds())
                // kiem tra ghe co thuoc phong chieu khong
                .get()
                .get().toObjects(Seat.class);

        double subTotal = showtime.getBasePrice() * seats.size() + orders.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        double discount = 0;

        Booking booking = Booking.builder()
//                .bookingId(uniqueID)
                .userId(userId)
                .movieId(showtime.getMovieId())
                .showtimeId(data.getShowtimeId())
                .showtimeStartAtSnapshot(showtime.getStartAt())
                .movieTitleSnapshot(movie.getTitle())
                .roomNameSnapshot(room.getName())
                .cinemaNameSnapshot(cinema.getName())
                .seatIds(data.getSeatIds())
                .seatCodes(seats.stream().map(seat -> seat.getSeatCode()).collect(Collectors.toList()))
                .snackOrder(orders)
                .subtotal(subTotal)
                .discount(discount)
                .total(subTotal - discount)
                // cap nhat payment
                .paymentMethod("")
                .paymentStatus("pending")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        BookingDTO bookingDTO = bookingService.createSeatBooking(booking);
        return ResponseEntity.ok(
            ApiResponse.<BookingDTO>builder()
                    .success(true)
                    .message("Booking fetched successfully")
                    .data(bookingDTO)
                    .build()
        );
    }

    @PutMapping("/payment/{id}/confirmed")
    public ResponseEntity<ApiResponse<?>> updatePaymentStatus(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String bookingId
    ) throws ExecutionException, InterruptedException {
        bookingService.updatePaymentStatus(bookingId, "confirmed");
        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                    .success(true)
                    .message("Booking fetched successfully")
                    .build()
        );

    }


}
