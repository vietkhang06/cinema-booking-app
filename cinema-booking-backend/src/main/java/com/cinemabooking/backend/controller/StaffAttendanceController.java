package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.AttendanceDTO;
import com.cinemabooking.backend.dto.UserDTO;
import com.cinemabooking.backend.dto.ViolationDTO;
import com.cinemabooking.backend.service.UserService;
import com.google.cloud.firestore.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "Staff Attendance", description = "Endpoints for staff members to manage their attendance and log check-in/out")
public class StaffAttendanceController {

    @Autowired
    private UserService userService;

    @Autowired
    private Firestore firestore;

    // Helper to format date in Vietnam local timezone (GMT+7)
    private String getVietnamDateString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        return sdf.format(new Date(time));
    }

    private String getVietnamTimeString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        return sdf.format(new Date(time));
    }

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
            UserDTO user = userService.getUserById(staffId);
            if (user == null || !"staff".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Forbidden: Chỉ nhân viên mới được phép thao tác.").build());
            }

            String today = getVietnamDateString(System.currentTimeMillis());

            List<QueryDocumentSnapshot> docs = firestore.collection(AttendanceDTO.COLLECTION_NAME)
                    .whereEqualTo("staffId", staffId)
                    .whereEqualTo("date", today)
                    .get().get().getDocuments();

            if (docs.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.<AttendanceDTO>builder()
                        .success(true)
                        .message("No attendance log found for today")
                        .data(null)
                        .build());
            }

            AttendanceDTO todayLog = docs.get(0).toObject(AttendanceDTO.class);
            return ResponseEntity.ok(ApiResponse.<AttendanceDTO>builder()
                    .success(true)
                    .message("Today attendance log retrieved")
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
            UserDTO staff = userService.getUserById(staffId);
            
            if (staff == null || !"staff".equalsIgnoreCase(staff.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Forbidden: Chỉ nhân viên mới được phép thao tác.").build());
            }

            // Constraint: Deactivated staff cannot check-in
            if (!"active".equalsIgnoreCase(staff.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Tài khoản của bạn đã bị vô hiệu hóa, không thể điểm danh!").build());
            }

            long now = System.currentTimeMillis();
            String todayDate = getVietnamDateString(now);

            // Constraint: No double check-in on the same day
            List<QueryDocumentSnapshot> existingLogs = firestore.collection(AttendanceDTO.COLLECTION_NAME)
                    .whereEqualTo("staffId", staffId)
                    .whereEqualTo("date", todayDate)
                    .get().get().getDocuments();

            if (!existingLogs.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Bạn đã điểm danh check-in ngày hôm nay rồi!").build());
            }

            // Calculate lateness based on shift
            long lateMinutes = 0;
            String timeStr = getVietnamTimeString(now);
            String[] timeParts = timeStr.split(":");
            int currentHour = Integer.parseInt(timeParts[0]);
            int currentMinute = Integer.parseInt(timeParts[1]);
            int currentTotalMinutes = currentHour * 60 + currentMinute;

            int shiftStartHour = 8;
            if ("Ca Chiều".equalsIgnoreCase(shiftName)) {
                shiftStartHour = 12;
            } else if ("Ca Tối".equalsIgnoreCase(shiftName)) {
                shiftStartHour = 17;
            }
            int shiftStartTotalMinutes = shiftStartHour * 60;

            if (currentTotalMinutes > shiftStartTotalMinutes) {
                lateMinutes = currentTotalMinutes - shiftStartTotalMinutes;
            }

            String status = "present";
            if (lateMinutes > 5) {
                status = "late";
            }

            String docId = "att_" + UUID.randomUUID().toString();
            AttendanceDTO attendance = AttendanceDTO.builder()
                    .id(docId)
                    .staffId(staffId)
                    .staffName(staff.getName() != null ? staff.getName() : staff.getEmail())
                    .cinemaId(cinemaId)
                    .cinemaName(cinemaName)
                    .shiftName(shiftName)
                    .date(todayDate)
                    .checkInTime(now)
                    .checkOutTime(0L)
                    .durationMinutes(0L)
                    .lateMinutes(lateMinutes)
                    .earlyLeaveMinutes(0L)
                    .status(status)
                    .notes("")
                    .build();

            firestore.collection(AttendanceDTO.COLLECTION_NAME).document(docId).set(attendance).get();

            // Auto-create a violation for being late
            if (lateMinutes > 5) {
                autoCreateViolation(staffId, staff.getName(), "LATE", 
                        "Đi trễ " + lateMinutes + " phút ở " + shiftName + " (" + todayDate + ")", 
                        50000.0, 1);
            }

            return ResponseEntity.ok(ApiResponse.<AttendanceDTO>builder()
                    .success(true)
                    .message("Check-in thành công!")
                    .data(attendance)
                    .build());

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
            UserDTO staff = userService.getUserById(staffId);
            if (staff == null || !"staff".equalsIgnoreCase(staff.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Forbidden: Chỉ nhân viên mới được phép thao tác.").build());
            }
            if (!"active".equalsIgnoreCase(staff.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Tài khoản của bạn đã bị vô hiệu hóa, không thể thao tác!").build());
            }

            String todayDate = getVietnamDateString(System.currentTimeMillis());

            // Query active check-in (checkOutTime = 0)
            List<QueryDocumentSnapshot> logs = firestore.collection(AttendanceDTO.COLLECTION_NAME)
                    .whereEqualTo("staffId", staffId)
                    .whereEqualTo("date", todayDate)
                    .get().get().getDocuments();

            if (docsEmptyOrCheckedOut(logs)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<AttendanceDTO>builder().success(false).message("Lỗi: Bạn chưa check-in hoặc đã check-out rồi!").build());
            }

            DocumentSnapshot doc = logs.get(0);
            AttendanceDTO attendance = doc.toObject(AttendanceDTO.class);
            
            long now = System.currentTimeMillis();
            attendance.setCheckOutTime(now);

            // Compute working duration
            long durationMs = now - attendance.getCheckInTime();
            long durationMinutes = durationMs / (1000 * 60);
            attendance.setDurationMinutes(durationMinutes);

            // Determine early leave
            long earlyLeaveMinutes = 0;
            String timeStr = getVietnamTimeString(now);
            String[] timeParts = timeStr.split(":");
            int currentHour = Integer.parseInt(timeParts[0]);
            int currentMinute = Integer.parseInt(timeParts[1]);
            int currentTotalMinutes = currentHour * 60 + currentMinute;

            int shiftEndHour = 12;
            if ("Ca Chiều".equalsIgnoreCase(attendance.getShiftName())) {
                shiftEndHour = 17;
            } else if ("Ca Tối".equalsIgnoreCase(attendance.getShiftName())) {
                shiftEndHour = 22;
            }
            int shiftEndTotalMinutes = shiftEndHour * 60;

            if (currentTotalMinutes < shiftEndTotalMinutes) {
                earlyLeaveMinutes = shiftEndTotalMinutes - currentTotalMinutes;
            }
            attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);

            // Set final status
            String finalStatus = "completed";
            if (attendance.getLateMinutes() > 5 && earlyLeaveMinutes > 5) {
                finalStatus = "late & early_leave";
            } else if (attendance.getLateMinutes() > 5) {
                finalStatus = "late";
            } else if (earlyLeaveMinutes > 5) {
                finalStatus = "early_leave";
            }
            attendance.setStatus(finalStatus);

            firestore.collection(AttendanceDTO.COLLECTION_NAME).document(attendance.getId()).set(attendance).get();

            // Auto-create violation for early leave
            if (earlyLeaveMinutes > 5) {
                autoCreateViolation(staffId, attendance.getStaffName(), "EARLY_LEAVE",
                        "Về sớm " + earlyLeaveMinutes + " phút ở " + attendance.getShiftName() + " (" + todayDate + ")",
                        50000.0, 1);
            }

            return ResponseEntity.ok(ApiResponse.<AttendanceDTO>builder()
                    .success(true)
                    .message("Check-out thành công!")
                    .data(attendance)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<AttendanceDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    private boolean docsEmptyOrCheckedOut(List<QueryDocumentSnapshot> logs) {
        if (logs.isEmpty()) return true;
        AttendanceDTO att = logs.get(0).toObject(AttendanceDTO.class);
        return att.getCheckOutTime() > 0;
    }

    private void autoCreateViolation(String staffId, String staffName, String type, String desc, double amount, int points) throws ExecutionException, InterruptedException {
        String id = "vio_" + UUID.randomUUID().toString();
        ViolationDTO v = ViolationDTO.builder()
                .id(id)
                .staffId(staffId)
                .staffName(staffName)
                .violationType(type)
                .description(desc)
                .severity("LOW")
                .createdAt(System.currentTimeMillis())
                .createdBy("system")
                .createdByName("Hệ thống")
                .status("PENDING")
                .penaltyAmount(amount)
                .penaltyPoints(points)
                .notes("Tự động ghi nhận bởi hệ thống điểm danh.")
                .deleted(false)
                .build();
        firestore.collection(ViolationDTO.COLLECTION_NAME).document(id).set(v).get();
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
            UserDTO user = userService.getUserById(staffId);
            if (user == null || !"staff".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<List<AttendanceDTO>>builder().success(false).message("Forbidden: Chỉ nhân viên mới được phép xem lịch sử.").build());
            }
            List<QueryDocumentSnapshot> docs = firestore.collection(AttendanceDTO.COLLECTION_NAME)
                    .whereEqualTo("staffId", staffId)
                    .get().get().getDocuments();

            List<AttendanceDTO> list = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                list.add(doc.toObject(AttendanceDTO.class));
            }
            
            // Sort descending
            list.sort((a1, a2) -> Long.compare(a2.getCheckInTime(), a1.getCheckInTime()));

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
            UserDTO user = userService.getUserById(staffId);
            if (user == null || !"staff".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<List<ViolationDTO>>builder().success(false).message("Forbidden: Chỉ nhân viên mới được phép xem lịch sử.").build());
            }
            List<QueryDocumentSnapshot> docs = firestore.collection(ViolationDTO.COLLECTION_NAME)
                    .whereEqualTo("staffId", staffId)
                    .whereEqualTo("deleted", false)
                    .get().get().getDocuments();

            List<ViolationDTO> list = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                list.add(doc.toObject(ViolationDTO.class));
            }
            
            // Sort descending
            list.sort((v1, v2) -> Long.compare(v2.getCreatedAt(), v1.getCreatedAt()));

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
