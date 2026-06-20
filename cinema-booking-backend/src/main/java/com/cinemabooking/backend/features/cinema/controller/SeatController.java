package com.cinemabooking.backend.features.cinema.controller;

import com.cinemabooking.backend.features.cinema.dto.SeatDTO;
import com.cinemabooking.backend.features.cinema.request.SeatLockRequestDTO;
import com.cinemabooking.backend.features.cinema.service.SeatService;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seats", description = "Endpoints for seat status, locking and unlocking seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/showtime/{showtimeId}")
    @Operation(summary = "Get seat layout for showtime ID")
    public ApiResponse<List<SeatDTO>> getSeatsByShowtimeId(@PathVariable("showtimeId") String showtimeId) throws ExecutionException, InterruptedException {
        List<SeatDTO> seats = seatService.getSeatsByShowtimeId(showtimeId);
        return ApiResponse.<List<SeatDTO>>builder()
                .success(true)
                .message("Seats layout fetched successfully")
                .data(seats)
                .build();
    }

    @PostMapping("/lock")
    @Operation(summary = "Hold/Lock selected seats for user")
    public ResponseEntity<ApiResponse<Void>> holdSeats(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatLockRequestDTO request
    ) throws ExecutionException, InterruptedException {
        
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Vui lòng đăng nhập.")
                    .build());
        }

        try {
            seatService.lockSeats(userId, request.getShowtimeId(), request.getSeatIds());
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Ghế đã được giữ thành công (Hiệu lực: 7 phút)")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(409).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage() != null ? e.getMessage() : "Một hoặc nhiều ghế đã được giữ hoặc đã đặt!")
                    .build());
        }
    }

    @PostMapping("/release")
    @Operation(summary = "Release locked seats held by user")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatLockRequestDTO request
    ) throws ExecutionException, InterruptedException {
        
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Vui lòng đăng nhập.")
                    .build());
        }

        seatService.releaseSeats(userId, request.getShowtimeId(), request.getSeatIds());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Giải phóng ghế giữ thành công.")
                .build());
    }

    @PostMapping("/staff/release")
    @Operation(summary = "Staff release seats held by another user")
    public ResponseEntity<ApiResponse<Void>> releaseSeatsByStaff(
            @RequestBody SeatLockRequestDTO request
    ) throws ExecutionException, InterruptedException {
        seatService.releaseSeatsByStaff(request.getShowtimeId(), request.getSeatIds());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Staff giải phóng ghế giữ thành công.")
                .build());
    }
}
