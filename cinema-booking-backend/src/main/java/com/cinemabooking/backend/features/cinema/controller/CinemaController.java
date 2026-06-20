package com.cinemabooking.backend.features.cinema.controller;

import com.cinemabooking.backend.features.cinema.dto.CinemaDTO;
import com.cinemabooking.backend.features.cinema.service.CinemaService;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/cinemas")
@Tag(name = "Cinemas", description = "Endpoints for managing cinemas")
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    @GetMapping
    @Operation(summary = "Get list of cinemas")
    public ApiResponse<List<CinemaDTO>> getAllCinemas() throws ExecutionException, InterruptedException {
        List<CinemaDTO> cinemas = cinemaService.getAllCinemas();
        return ApiResponse.<List<CinemaDTO>>builder()
                .success(true)
                .message("Cinemas fetched successfully")
                .data(cinemas)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cinema details by ID")
    public ApiResponse<CinemaDTO> getCinemaById(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        CinemaDTO cinema = cinemaService.getCinemaById(id);
        return ApiResponse.<CinemaDTO>builder()
                .success(true)
                .message("Cinema fetched successfully")
                .data(cinema)
                .build();
    }
}
