package com.cinemabooking.backend.features.user.controller;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.user.dto.AttendanceDTO;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.dto.ViolationDTO;
import com.cinemabooking.backend.features.user.service.UserService;
import com.cinemabooking.backend.features.user.service.AttendanceService;
import com.cinemabooking.backend.features.user.service.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Staff", description = "Endpoints for administrators to manage staff, attendance and violations")
public class AdminStaffController {

    private static final Logger logger = LoggerFactory.getLogger(AdminStaffController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ViolationService violationService;

    @Data
    public static class CreateStaffRequest {
        private UserDTO staff;
        private String password;
    }

    private boolean isAdmin(String uid) throws ExecutionException, InterruptedException {
        UserDTO user = userService.getUserById(uid);
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    // ==========================================
    // 1. STAFF CRUD
    // ==========================================

    @PostMapping("/staff")
    @Operation(summary = "Create a new staff account")
    public ResponseEntity<ApiResponse<UserDTO>> createStaff(
            @AuthenticationPrincipal Object principal,
            @RequestBody CreateStaffRequest request
    ) {
        try {
            logger.info("POST /api/v1/admin/staff invoked by principal: {}", principal);
            if (principal == null || !isAdmin(principal.toString())) {
                logger.warn("Access denied for principal: {}. Admin role required.", principal);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Forbidden: Admin role required").build());
            }

            if (request.getStaff() == null || request.getPassword() == null || request.getPassword().length() < 6) {
                logger.warn("Invalid staff creation request. Missing details or password too short.");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Invalid request: password must be at least 6 characters").build());
            }

            UserDTO created = userService.createStaff(request.getStaff(), request.getPassword());
            logger.info("Successfully created staff member with UID: {}", created.getUid());
            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .success(true)
                    .message("Staff account created successfully")
                    .data(created)
                    .build());
        } catch (Exception e) {
            logger.error("Error creating staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @GetMapping("/staff")
    @Operation(summary = "List all staff accounts with filters")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllStaffs(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "cinemaId", required = false) String cinemaId
    ) {
        try {
            logger.info("GET /api/v1/admin/staff invoked by principal: {}, search={}, status={}, cinemaId={}", principal, search, status, cinemaId);
            if (principal == null || !isAdmin(principal.toString())) {
                logger.warn("Access denied for principal: {}. Admin role required.", principal);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<List<UserDTO>>builder().success(false).message("Forbidden: Admin role required").build());
            }

            List<UserDTO> allStaffs = userService.getAllStaffs();
            List<UserDTO> filtered = new ArrayList<>();

            for (UserDTO u : allStaffs) {
                // Apply search filter (name, email, phone)
                if (search != null && !search.isBlank()) {
                    String keyword = search.toLowerCase();
                    boolean match = (u.getName() != null && u.getName().toLowerCase().contains(keyword))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword))
                            || (u.getPhone() != null && u.getPhone().toLowerCase().contains(keyword));
                    if (!match) continue;
                }

                // Apply status filter
                if (status != null && !status.isBlank()) {
                    if (!status.equalsIgnoreCase(u.getStatus())) {
                        continue;
                    }
                }

                // Apply cinema filter
                if (cinemaId != null && !cinemaId.isBlank()) {
                    if (u.getCinemaId() == null || !u.getCinemaId().equals(cinemaId)) {
                        continue;
                    }
                }

                filtered.add(u);
            }

            logger.info("Returning {} staff profiles after filtering", filtered.size());
            return ResponseEntity.ok(ApiResponse.<List<UserDTO>>builder()
                    .success(true)
                    .message("Staff list fetched successfully")
                    .data(filtered)
                    .build());
        } catch (Exception e) {
            logger.error("Error listing staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<UserDTO>>builder().success(false).message(e.getMessage()).build());
        }
    }

    @GetMapping("/staff/{uid}")
    @Operation(summary = "Get staff detail including profile")
    public ResponseEntity<ApiResponse<UserDTO>> getStaffDetail(
            @AuthenticationPrincipal Object principal,
            @PathVariable("uid") String uid
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Forbidden: Admin role required").build());
            }

            UserDTO user = userService.getUserById(uid);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Staff profile not found").build());
            }

            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .success(true)
                    .message("Staff detail fetched successfully")
                    .data(user)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PutMapping("/staff/{uid}")
    @Operation(summary = "Update a staff account")
    public ResponseEntity<ApiResponse<UserDTO>> updateStaff(
            @AuthenticationPrincipal Object principal,
            @PathVariable("uid") String uid,
            @RequestBody UserDTO staff
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Forbidden: Admin role required").build());
            }

            UserDTO updated = userService.updateStaff(uid, staff);
            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .success(true)
                    .message("Staff account updated successfully")
                    .data(updated)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @DeleteMapping("/staff/{uid}")
    @Operation(summary = "Deactivate/soft delete a staff account")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @AuthenticationPrincipal Object principal,
            @PathVariable("uid") String uid
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Void>builder().success(false).message("Forbidden: Admin role required").build());
            }

            userService.deleteStaff(uid);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Staff account deactivated successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/staff/{uid}/reset-password")
    @Operation(summary = "Reset password for a staff member")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @AuthenticationPrincipal Object principal,
            @PathVariable("uid") String uid,
            @RequestParam("newPassword") String newPassword
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Void>builder().success(false).message("Forbidden: Admin role required").build());
            }

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Void>builder().success(false).message("Password must be at least 6 characters").build());
            }

            userService.resetPassword(uid, newPassword);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Password reset successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }

    // ==========================================
    // 2. ATTENDANCE MANAGEMENT
    // ==========================================

    @GetMapping("/attendance")
    @Operation(summary = "List all attendance records with filters")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAllAttendances(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "staffId", required = false) String staffId,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "cinemaId", required = false) String cinemaId
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<List<AttendanceDTO>>builder().success(false).message("Forbidden: Admin role required").build());
            }

            List<AttendanceDTO> attendances = attendanceService.getAllAttendancesWithFilters(staffId, date, cinemaId);
            return ResponseEntity.ok(ApiResponse.<List<AttendanceDTO>>builder()
                    .success(true)
                    .message("Attendance logs fetched successfully")
                    .data(attendances)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<AttendanceDTO>>builder().success(false).message(e.getMessage()).build());
        }
    }

    // ==========================================
    // 3. VIOLATION CRUD
    // ==========================================

    @GetMapping("/violations")
    @Operation(summary = "List all violations with filters")
    public ResponseEntity<ApiResponse<List<ViolationDTO>>> getAllViolations(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "staffId", required = false) String staffId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "severity", required = false) String severity
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<List<ViolationDTO>>builder().success(false).message("Forbidden: Admin role required").build());
            }

            List<ViolationDTO> violations = violationService.getAllViolationsWithFilters(staffId, status, severity);
            return ResponseEntity.ok(ApiResponse.<List<ViolationDTO>>builder()
                    .success(true)
                    .message("Violations fetched successfully")
                    .data(violations)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ViolationDTO>>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/violations")
    @Operation(summary = "Log a manual violation")
    public ResponseEntity<ApiResponse<ViolationDTO>> createViolation(
            @AuthenticationPrincipal Object principal,
            @RequestBody ViolationDTO violation
    ) {
        try {
            String adminUid = principal.toString();
            UserDTO adminUser = userService.getUserById(adminUid);
            if (adminUser == null || !"admin".equalsIgnoreCase(adminUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<ViolationDTO>builder().success(false).message("Forbidden: Admin role required").build());
            }

            UserDTO staff = userService.getUserById(violation.getStaffId());
            if (staff == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<ViolationDTO>builder().success(false).message("Staff member not found").build());
            }

            String adminName = adminUser.getName() != null ? adminUser.getName() : adminUser.getEmail();
            violation.setStaffName(staff.getName());
            ViolationDTO created = violationService.createViolation(adminUid, adminName, violation);

            return ResponseEntity.ok(ApiResponse.<ViolationDTO>builder()
                    .success(true)
                    .message("Violation logged successfully")
                    .data(created)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ViolationDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PutMapping("/violations/{id}")
    @Operation(summary = "Update violation status or notes")
    public ResponseEntity<ApiResponse<ViolationDTO>> updateViolation(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") String id,
            @RequestBody ViolationDTO updateData
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<ViolationDTO>builder().success(false).message("Forbidden: Admin role required").build());
            }

            ViolationDTO updated = violationService.updateViolation(id, updateData);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<ViolationDTO>builder().success(false).message("Violation record not found").build());
            }

            return ResponseEntity.ok(ApiResponse.<ViolationDTO>builder()
                    .success(true)
                    .message("Violation updated successfully")
                    .data(updated)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ViolationDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @DeleteMapping("/violations/{id}")
    @Operation(summary = "Soft delete a violation log")
    public ResponseEntity<ApiResponse<Void>> deleteViolation(
            @AuthenticationPrincipal Object principal,
            @PathVariable("id") String id
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Void>builder().success(false).message("Forbidden: Admin role required").build());
            }

            boolean deleted = violationService.deleteViolation(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<Void>builder().success(false).message("Violation record not found").build());
            }

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Violation soft deleted successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }
}
