package com.cinemabooking.backend.features.cinema.controller;

import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.cinemabooking.backend.features.cinema.service.ShowtimeService;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/showtimes")
@Tag(name = "Showtimes", description = "Endpoints for movie showtime metadata and schedules")
public class ShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

    @GetMapping
    @Operation(summary = "Get list of all showtimes")
    public ApiResponse<List<ShowtimeDTO>> getAllShowtimes() throws ExecutionException, InterruptedException {
        List<ShowtimeDTO> showtimes = showtimeService.getAllShowtimes();
        return ApiResponse.<List<ShowtimeDTO>>builder()
                .success(true)
                .message("Showtimes fetched successfully")
                .data(showtimes)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get showtime details by ID")
    public ApiResponse<ShowtimeDTO> getShowtimeById(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        ShowtimeDTO showtime = showtimeService.getShowtimeById(id);
        return ApiResponse.<ShowtimeDTO>builder()
                .success(true)
                .message("Showtime fetched successfully")
                .data(showtime)
                .build();
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Get showtimes by Movie ID")
    public ApiResponse<List<ShowtimeDTO>> getShowtimesByMovieId(@PathVariable("movieId") String movieId) throws ExecutionException, InterruptedException {
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByMovieId(movieId);
        return ApiResponse.<List<ShowtimeDTO>>builder()
                .success(true)
                .message("Showtimes fetched successfully")
                .data(showtimes)
                .build();
    }

    @GetMapping("/cinema/{cinemaId}")
    @Operation(summary = "Get showtimes by Cinema ID")
    public ApiResponse<List<ShowtimeDTO>> getShowtimesByCinemaId(@PathVariable("cinemaId") String cinemaId) throws ExecutionException, InterruptedException {
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByCinemaId(cinemaId);
        return ApiResponse.<List<ShowtimeDTO>>builder()
                .success(true)
                .message("Showtimes fetched successfully")
                .data(showtimes)
                .build();
    }

    @PostMapping("/seed")
    @Operation(summary = "Temporary endpoint to seed database with mock showtimes")
    public ApiResponse<Void> seedShowtimes() throws ExecutionException, InterruptedException {
        showtimeService.seedShowtimes();
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Showtimes seeded successfully")
                .build();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a showtime and compensate customers")
    public ApiResponse<Void> cancelShowtime(@PathVariable("id") String showtimeId) throws ExecutionException, InterruptedException {
        showtimeService.cancelShowtime(showtimeId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Showtime cancelled and users compensated")
                .build();
    }

    @PutMapping
    @Operation(summary = "Admin update basePrice or detail showtime")
    public ApiResponse<ShowtimeDTO> updateShowtime(@RequestBody ShowtimeDTO showtime) throws ExecutionException, InterruptedException {
        ShowtimeDTO updated = showtimeService.updateShowtime(showtime);
        return ApiResponse.<ShowtimeDTO>builder()
                .success(true)
                .message("Showtime updated successfully")
                .data(updated)
                .build();
    }
}
