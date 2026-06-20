package com.cinemabooking.backend.features.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    public static final String COLLECTION_NAME = "movies";

    private String movieId;
    private String title;
    private String description;
    private String language;
    private String ageRating;
    private String posterUrl;
    private String trailerUrl;
    private Double ratingAvg;
    private Integer ratingCount;
    private String status; // UPCOMING, NOW_SHOWING, ENDED
    private List<String> genres;
    private Integer durationMinutes;
    private Long createdAt;
    private Long updatedAt;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private Boolean deleted = false;
}
