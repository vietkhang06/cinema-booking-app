package com.example.cinemabookingapp.data.dto;

public class ShowtimeDTO {
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

    public ShowtimeDTO() {
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}