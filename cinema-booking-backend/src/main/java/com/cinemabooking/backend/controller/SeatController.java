package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.SeatDTO;
import com.cinemabooking.backend.dto.SeatActionRequest;
import com.cinemabooking.backend.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seats", description = "Endpoints for seat layout and status (Read-Only)")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/showtime/{showtimeId}")
    @Operation(summary = "Get seats by Showtime ID")
    public ApiResponse<List<SeatDTO>> getSeatsByShowtimeId(@PathVariable String showtimeId) throws ExecutionException, InterruptedException {
        List<SeatDTO> seats = seatService.getSeatsByShowtimeId(showtimeId);
        return ApiResponse.<List<SeatDTO>>builder()
                .success(true)
                .message("Seats for showtime fetched successfully")
                .data(seats)
                .build();
    }

    @PostMapping("/lock")
    @Operation(summary = "Lock selected seats")
    public ApiResponse<Void> lockSeats(@RequestBody SeatActionRequest request) throws ExecutionException, InterruptedException {
        seatService.holdSeats(request.getSeatIds(), request.getUserId(), 7); // 7 minutes
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Seats locked successfully")
                .build();
    }

    @PostMapping("/unlock")
    @Operation(summary = "Unlock selected seats")
    public ApiResponse<Void> unlockSeats(@RequestBody SeatActionRequest request) throws ExecutionException, InterruptedException {
        seatService.releaseSeats(request.getSeatIds(), request.getUserId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Seats unlocked successfully")
                .build();
    }
}
