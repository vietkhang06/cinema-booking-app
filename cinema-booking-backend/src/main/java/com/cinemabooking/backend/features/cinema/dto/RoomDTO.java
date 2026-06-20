package com.cinemabooking.backend.features.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    public static final String COLLECTION_NAME = "rooms";

    private String roomId;
    private String cinemaId;
    private String name;
    private String type; // 2D, 3D, IMAX, VIP
    private Integer seatRows;
    private Integer seatCols;
    private Integer totalSeats;
    private String status; // active, maintenance
    private Long createdAt;
    private Long updatedAt;
    private Boolean deleted;
}
