package com.cinemabooking.backend.features.user;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.user.UserDTO;
import com.cinemabooking.backend.features.user.UserService;
import com.google.cloud.firestore.*;
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
@Tag(name = "Admin User Management", description = "Endpoints for administrators to manage all user accounts (Admins and Customers)")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Firestore firestore;

    @Data
    public static class CreateUserRequest {
        private UserDTO user;
        private String password;
    }

    private boolean isAdmin(String uid) throws ExecutionException, InterruptedException {
        UserDTO user = userService.getUserById(uid);
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    // ==========================================
    // USER CRUD
    // ==========================================

    @PostMapping("/users")
    @Operation(summary = "Create a new user account (Admin or Customer)")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @AuthenticationPrincipal Object principal,
            @RequestBody CreateUserRequest request
    ) {
        try {
            logger.info("POST /api/v1/admin/users invoked by principal: {}", principal);
            if (principal == null || !isAdmin(principal.toString())) {
                logger.warn("Access denied for principal: {}. Admin role required.", principal);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Forbidden: Admin role required").build());
            }

            if (request.getUser() == null || request.getPassword() == null || request.getPassword().length() < 6) {
                logger.warn("Invalid user creation request. Missing details or password too short.");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Invalid request: password must be at least 6 characters").build());
            }

            UserDTO created = userService.createUser(request.getUser(), request.getPassword());
            logger.info("Successfully created user with UID: {}", created.getUid());
            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .success(true)
                    .message("User account created successfully")
                    .data(created)
                    .build());
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @GetMapping("/users")
    @Operation(summary = "List all user accounts with filters")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "cinemaId", required = false) String cinemaId
    ) {
        try {
            logger.info("GET /api/v1/admin/users invoked by principal: {}, search={}, status={}, cinemaId={}", principal, search, status, cinemaId);
            if (principal == null || !isAdmin(principal.toString())) {
                logger.warn("Access denied for principal: {}. Admin role required.", principal);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<List<UserDTO>>builder().success(false).message("Forbidden: Admin role required").build());
            }

            List<UserDTO> allUsers = userService.getAllUsers();
            List<UserDTO> filtered = new ArrayList<>();

            for (UserDTO u : allUsers) {
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

                // Apply cinema filter (if assigned to any branch)
                if (cinemaId != null && !cinemaId.isBlank()) {
                    if (u.getCinemaId() == null || !u.getCinemaId().equals(cinemaId)) {
                        continue;
                    }
                }

                filtered.add(u);
            }

            logger.info("Returning {} user profiles after filtering", filtered.size());
            return ResponseEntity.ok(ApiResponse.<List<UserDTO>>builder()
                    .success(true)
                    .message("User list fetched successfully")
                    .data(filtered)
                    .build());
        } catch (Exception e) {
            logger.error("Error listing users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<UserDTO>>builder().success(false).message(e.getMessage()).build());
        }
    }

    @GetMapping("/users/{uid}")
    @Operation(summary = "Get user detail including profile")
    public ResponseEntity<ApiResponse<UserDTO>> getUserDetail(
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
                        .body(ApiResponse.<UserDTO>builder().success(false).message("User profile not found").build());
            }

            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .success(true)
                    .message("User detail fetched successfully")
                    .data(user)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PutMapping("/users/{uid}")
    @Operation(summary = "Update a user account")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @AuthenticationPrincipal Object principal,
            @PathVariable("uid") String uid,
            @RequestBody UserDTO user
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<UserDTO>builder().success(false).message("Forbidden: Admin role required").build());
            }

            UserDTO updated = userService.updateUser(uid, user);
            return ResponseEntity.ok(ApiResponse.<UserDTO>builder()
                    .success(true)
                    .message("User account updated successfully")
                    .data(updated)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<UserDTO>builder().success(false).message(e.getMessage()).build());
        }
    }

    @DeleteMapping("/users/{uid}")
    @Operation(summary = "Deactivate/soft delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal Object principal,
            @PathVariable("uid") String uid
    ) {
        try {
            if (principal == null || !isAdmin(principal.toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<Void>builder().success(false).message("Forbidden: Admin role required").build());
            }

            userService.deleteUser(uid);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("User account deactivated successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/users/{uid}/reset-password")
    @Operation(summary = "Reset password for a user")
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
}
