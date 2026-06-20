package com.cinemabooking.backend.features.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    public static final String COLLECTION_NAME = "seats";

    private String seatId;
    private String showtimeId;
    private String seatCode; // E.g., A01, B12
    private String rowName;  // A, B, C...
    private Integer columnNo;
    private String seatType; // STANDARD, VIP, COUPLE
    private String status;   // available, held, booked
    private String heldBy;   // userId
    private long heldUntil;  // timestamp
    private String bookedBy; // userId
    private long bookedAt;   // timestamp
    private Double priceOverride;
}
