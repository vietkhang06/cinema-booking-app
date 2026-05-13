package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private String bookingId;
    private String userId;
    private String movieId;
    private String cinemaId;
    private String roomId;
    private String showtimeId;
    private String movieTitleSnapshot;
    private String movieImageUrlSnapshot;
    private String cinemaNameSnapshot;
    private String roomNameSnapshot;
    private Long showtimeStartAtSnapshot;
    private List<String> seatCodes;
    private List<String> seatIds;
    private String snackOrderId;
    private Double subtotal;
    private Double discount;
    private Double total;
    private String paymentMethod;
    private String paymentStatus;
    private String bookingStatus;
    private String qrCodeValue;
    private Long checkInAt;
    private Long createdAt;
    private Long updatedAt;
    private Boolean deleted;
}