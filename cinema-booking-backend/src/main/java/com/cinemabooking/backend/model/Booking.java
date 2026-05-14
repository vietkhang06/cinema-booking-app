package com.cinemabooking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private String bookingId;
    private String userId;
    private String showtimeId;

    private String movieId;
    private String movieTitleSnapshot;
    public String movieImageUrlSnapshot;
    private String cinemaNameSnapshot;
    private String roomNameSnapshot;
    private long showtimeStartAtSnapshot;

    private List<String> seatCodes;
    private List<String> seatIds;

    private String snackOrderId;

    private double subtotal;
    private double discount;
    private double total;

    private String paymentMethod;
    private String paymentStatus;
    private String bookingStatus;

    private String qrCodeValue;
    private long checkInAt;
    private long createdAt;
    private long updatedAt;
    private boolean deleted;

    private Long paymentAt;
    private List<SnackOrderSnapshot> snackOrder;
}
