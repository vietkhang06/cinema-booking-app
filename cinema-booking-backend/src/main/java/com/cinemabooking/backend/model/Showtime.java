package com.cinemabooking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {
    public String showtimeId;
    public String movieId;
    public String cinemaId;
    public String roomId;
    public long startAt;
    public long endAt;
    public double basePrice;
    public String format;
    public String language;
    public String status;
    public int totalSeats;
    public int bookedSeatsCount;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
}