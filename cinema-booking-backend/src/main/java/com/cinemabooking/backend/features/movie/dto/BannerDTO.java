package com.cinemabooking.backend.features.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {
    public static final String COLLECTION_NAME = "banners";

    private String bannerId;
    private String imageUrl;
}
