package com.cinemabooking.backend.features.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    public static final String COLLECTION_NAME = "attendance";

    private String id;
    private String staffId;
    private String staffName;
    private String cinemaId;
    private String cinemaName;
    private String shiftName; // Ca Sáng, Ca Chiều, Ca Tối
    private String date; // yyyy-MM-dd
    private long checkInTime;
    private long checkOutTime;
    private long durationMinutes;
    private long lateMinutes;
    private long earlyLeaveMinutes;
    private String status; // present, late, early_leave, absent, completed
    private String notes;
}
