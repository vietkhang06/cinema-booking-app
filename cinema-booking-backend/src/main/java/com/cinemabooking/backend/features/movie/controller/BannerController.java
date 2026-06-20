package com.cinemabooking.backend.features.movie.controller;

import com.cinemabooking.backend.features.movie.dto.BannerDTO;
import com.cinemabooking.backend.features.movie.service.BannerService;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/banners")
@Tag(name = "Banners", description = "Endpoints for advertising banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    @Operation(summary = "Get list of active banners")
    public ApiResponse<List<BannerDTO>> getAllBanners() throws ExecutionException, InterruptedException {
        List<BannerDTO> banners = bannerService.getAllBanners();
        return ApiResponse.<List<BannerDTO>>builder()
                .success(true)
                .message("Banners fetched successfully")
                .data(banners)
                .build();
    }

    @PostMapping("/seed")
    @Operation(summary = "Temporary endpoint to seed database with mock banners")
    public ApiResponse<Void> seedMockBanners() throws ExecutionException, InterruptedException {
        bannerService.seedMockBanners();
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Banners seeded successfully")
                .build();
    }
}
