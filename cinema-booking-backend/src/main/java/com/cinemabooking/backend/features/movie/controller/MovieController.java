package com.cinemabooking.backend.features.movie.controller;

import com.cinemabooking.backend.features.movie.dto.MovieDTO;
import com.cinemabooking.backend.features.movie.service.MovieService;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "Endpoints for managing movie details")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    @Operation(summary = "Get list of movies with status filter and pagination")
    public ApiResponse<List<MovieDTO>> getAllMovies(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) throws ExecutionException, InterruptedException {
        List<MovieDTO> movies;
        if (status != null && !status.isBlank()) {
            movies = movieService.getMoviesByStatus(status, page, size);
        } else {
            movies = movieService.getAllMovies(page, size);
        }

        return ApiResponse.<List<MovieDTO>>builder()
                .success(true)
                .message("Movies fetched successfully")
                .data(movies)
                .build();
    }

    @GetMapping("{id}")
    @Operation(summary = "Get movie detail by id")
    public ApiResponse<MovieDTO> getMovieById(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        MovieDTO movie = movieService.getMovieById(id);
        if (movie == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
        }

        return ApiResponse.<MovieDTO>builder()
                .success(true)
                .message("Movie fetched successfully")
                .data(movie)
                .build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search movie details")
    public ApiResponse<List<MovieDTO>> searchMovie(@RequestParam("query") String query) throws ExecutionException, InterruptedException {
        List<MovieDTO> movies = movieService.searchMovies(query);
        return ApiResponse.<List<MovieDTO>>builder()
                .success(true)
                .message("Movies fetched successfully")
                .data(movies)
                .build();
    }
}
