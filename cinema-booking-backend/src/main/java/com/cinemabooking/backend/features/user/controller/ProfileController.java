package com.cinemabooking.backend.features.user.controller;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.request.UpdateProfileRequestDTO;
import com.cinemabooking.backend.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Endpoints for authenticated user profile (Read-Only)")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get current authenticated user profile")
    public ApiResponse<UserDTO> getMyProfile(@AuthenticationPrincipal Object principal) throws ExecutionException, InterruptedException {
        if (principal == null) {
            return ApiResponse.<UserDTO>builder()
                    .success(false)
                    .message("Unauthorized: No principal found in security context")
                    .build();
        }
        
        String uid = principal.toString();
        UserDTO user = userService.getUserById(uid);
        
        if (user == null) {
            return ApiResponse.<UserDTO>builder()
                    .success(false)
                    .message("User profile not found in database for UID: " + uid)
                    .build();
        }
        
        return ApiResponse.<UserDTO>builder()
                .success(true)
                .message("User profile fetched successfully")
                .data(user)
                .build();
    }

    @PutMapping
    @Operation(summary = "Update current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal Object principal,
            @RequestBody UpdateProfileRequestDTO request
    ) throws ExecutionException, InterruptedException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.<UserDTO>builder()
                    .success(false)
                    .message("Unauthorized: No principal found in security context")
                    .build());
        }

        if(request == null)
            return ResponseEntity.badRequest().body(ApiResponse.<UserDTO>builder()
                    .success(false)
                    .message("Bad request.")
                    .build());

        String uid = principal.toString();
        UserDTO user = userService.updateUserProfile(uid, request);

        return ResponseEntity.ok()
                .body(ApiResponse.<UserDTO>builder()
                    .success(true)
                    .message("User profile updated successfully")
                    .data(user)
                    .build());
    }
}
