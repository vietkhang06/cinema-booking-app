package com.cinemabooking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    public String roomId;
    public String cinemaId;
    public String name;
    public String layoutType;
    public int seatRows;
    public int seatCols;
    public int totalSeats;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

}