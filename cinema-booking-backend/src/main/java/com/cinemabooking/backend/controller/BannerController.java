package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.BannerDTO;
import com.cinemabooking.backend.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/banners")
@Tag(name = "Banners", description = "Endpoints for advertising banners (Read-Only)")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    @Operation(summary = "Get all banners")
    public ApiResponse<List<BannerDTO>> getAllBanners() throws ExecutionException, InterruptedException {
        List<BannerDTO> banners = bannerService.getAllBanners();
        return ApiResponse.<List<BannerDTO>>builder()
                .success(true)
                .message("Banners fetched successfully")
                .data(banners)
                .build();
    }
}
