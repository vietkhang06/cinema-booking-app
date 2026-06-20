package com.cinemabooking.backend.features.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
    public static final String COLLECTION_NAME = "showtimes";

    private String showtimeId;
    private String movieId;
    private String cinemaId;
    private String roomId;
    private long startAt;
    private long endAt;
    private Double basePrice;
    private String format; // 2D, 3D, IMAX
    private String language;
    private String status; // active, inactive, CANCELLED
    private Integer totalSeats;
    private Integer bookedSeatsCount;
    private Long createdAt;
    private Long updatedAt;
    private boolean deleted;
}
