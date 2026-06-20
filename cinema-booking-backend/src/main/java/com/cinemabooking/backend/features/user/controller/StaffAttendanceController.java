package com.cinemabooking.backend.features.user.controller;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.user.dto.AttendanceDTO;
import com.cinemabooking.backend.features.user.dto.ViolationDTO;
import com.cinemabooking.backend.features.user.service.AttendanceService;
import com.cinemabooking.backend.features.user.service.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "Staff Attendance", description = "Endpoints for staff members to manage their attendance and log check-in/out")
public class StaffAttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ViolationService violationService;

    // ==========================================
    // ATTENDANCE & SHIFTS
    // ==========================================

    @GetMapping("/attendance/today")
    @Operation(summary = "Check if staff checked in today")
    public ResponseEntity<ApiResponse<AttendanceDTO>> getTodayAttendance(
            @AuthenticationPrincipal Object principal
    ) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Unauthorized").build());
            }

            String staffId = principal.toString();
            AttendanceDTO todayLog = attendanceService.getTodayAttendance(staffId);
            
            return ResponseEntity.ok(ApiResponse.<AttendanceDTO>builder()
                    .success(true)
                    .message(todayLog != null ? "Today attendance log retrieved" : "No attendance log found for today")
                    .data(todayLog)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/attendance/checkin")
    @Operation(summary = "Staff check-in for shift")
    public ResponseEntity<ApiResponse<AttendanceDTO>> checkIn(
            @AuthenticationPrincipal Object principal,
            @RequestParam("shiftName") String shiftName,
            @RequestParam("cinemaId") String cinemaId,
            @RequestParam("cinemaName") String cinemaName
    ) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Unauthorized").build());
            }

            String staffId = principal.toString();
            AttendanceDTO attendance = attendanceService.checkIn(staffId, shiftName, cinemaId, cinemaName);

            return ResponseEntity.ok(ApiResponse.<AttendanceDTO>builder()
                    .success(true)
                    .message("Check-in thành công!")
                    .data(attendance)
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/attendance/checkout")
    @Operation(summary = "Staff check-out for shift")
    public ResponseEntity<ApiResponse<AttendanceDTO>> checkOut(
            @AuthenticationPrincipal Object principal
    ) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Unauthorized").build());
            }

            String staffId = principal.toString();
            AttendanceDTO attendance = attendanceService.checkOut(staffId);

            return ResponseEntity.ok(ApiResponse.<AttendanceDTO>builder()
                    .success(true)
                    .message("Check-out thành công!")
                    .data(attendance)
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    // ==========================================
    // STAFF LOG HISTORY
    // ==========================================

    @GetMapping("/attendance/history")
    @Operation(summary = "View staff own attendance history")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getMyAttendanceHistory(
            @AuthenticationPrincipal Object principal
    ) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<List<AttendanceDTO>>builder().success(false).message("Unauthorized").build());
            }

            String staffId = principal.toString();
            List<AttendanceDTO> list = attendanceService.getMyAttendanceHistory(staffId);

            return ResponseEntity.ok(ApiResponse.<List<AttendanceDTO>>builder()
                    .success(true)
                    .message("Attendance history fetched")
                    .data(list)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<AttendanceDTO>>builder().success(false).message(e.getMessage()).build());
        }
    }

    @GetMapping("/violations/history")
    @Operation(summary = "View staff own violation history")
    public ResponseEntity<ApiResponse<List<ViolationDTO>>> getMyViolationHistory(
            @AuthenticationPrincipal Object principal
    ) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<List<ViolationDTO>>builder().success(false).message("Unauthorized").build());
            }

            String staffId = principal.toString();
            List<ViolationDTO> list = violationService.getMyViolationHistory(staffId);

            return ResponseEntity.ok(ApiResponse.<List<ViolationDTO>>builder()
                    .success(true)
                    .message("Violation history fetched")
                    .data(list)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ViolationDTO>>builder().success(false).message(e.getMessage()).build());
        }
    }
}
