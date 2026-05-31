package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffStatsDTO {
    private int totalBookingsToday;
    private int paidBookingsToday;
    private int pendingBookingsToday;
    private int failedBookingsToday;
    private int cancelledBookingsToday;
}
