package com.cinemabooking.backend.features.voucher.controller;

import com.cinemabooking.backend.features.voucher.request.ValidateVoucherRequest;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.voucher.dto.VoucherDTO;
import com.cinemabooking.backend.features.user.service.UserService;
import com.cinemabooking.backend.features.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/vouchers")
@Tag(name = "Vouchers", description = "Endpoints for managing user vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get vouchers for current user")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> getMyVouchers(@AuthenticationPrincipal String userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        List<VoucherDTO> vouchers = voucherService.getUserVouchers(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<VoucherDTO>>builder()
                        .success(true)
                        .message("Fetched vouchers successfully")
                        .data(vouchers)
                        .build()
        );
    }

    @PostMapping("/admin/grant")
    @Operation(summary = "Manually grant a voucher to a user (Admin/Staff only)")
    public ResponseEntity<ApiResponse<VoucherDTO>> grantVoucher(
            @AuthenticationPrincipal String adminId,
            @RequestParam String targetUserId,
            @RequestParam int discountPercent,
            @RequestParam int validDays) throws ExecutionException, InterruptedException {
        
        UserDTO adminUser = userService.getUserById(adminId);
        if (adminUser == null || (!"staff".equalsIgnoreCase(adminUser.getRole()) && !"admin".equalsIgnoreCase(adminUser.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này.");
        }

        VoucherDTO voucher = voucherService.grantVoucherToUser(targetUserId, discountPercent, validDays);
        if (voucher == null) {
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo voucher.");
        }

        return ResponseEntity.ok(
                ApiResponse.<VoucherDTO>builder()
                        .success(true)
                        .message("Voucher granted successfully")
                        .data(voucher)
                        .build()
        );
    }

    @PostMapping("/test-grant")
    @Operation(summary = "Temporary endpoint to generate a test voucher for the current user")
    public ResponseEntity<ApiResponse<VoucherDTO>> testGrantVoucher(
            @AuthenticationPrincipal String userId) {
        
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }

        try {
            VoucherDTO voucher = voucherService.grantVoucherToUser(userId, 10, 30);
            if (voucher == null) {
                 throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo voucher test.");
            }

            return ResponseEntity.ok(
                    ApiResponse.<VoucherDTO>builder()
                            .success(true)
                            .message("Test voucher generated successfully")
                            .data(voucher)
                            .build()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate a voucher code for the current user")
    public ResponseEntity<ApiResponse<VoucherDTO>> validateVoucher(
            @AuthenticationPrincipal String userId,
            @RequestBody ValidateVoucherRequest request) throws ExecutionException, InterruptedException {
        
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }

        VoucherDTO voucher = voucherService.validateVoucher(request.getCode(), userId);

        return ResponseEntity.ok(
                ApiResponse.<VoucherDTO>builder()
                        .success(true)
                        .message("Voucher is valid")
                        .data(voucher)
                        .build()
        );
    }
}
