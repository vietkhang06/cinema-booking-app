package com.cinemabooking.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatBookingRequestDTO {
    private String showtimeId;
    private List<String> seatIds;
    private List<SnackOrder> snackOrders;

    public record SnackOrder(String snackId, int quantity) { }
}
