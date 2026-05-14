package com.cinemabooking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    public String seatId;
//    public String showtimeId;
    public String seatCode;
    public String rowName;
    public int columnNo;
    public String seatType;
    public String status;
    public String roomId;
//    public String heldBy;
//    public long heldUntil;
//    public String bookedBy;
//    public long bookedAt;
//    public double priceOverride;

}