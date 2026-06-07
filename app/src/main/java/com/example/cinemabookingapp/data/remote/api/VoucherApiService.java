package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.domain.model.Voucher;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VoucherApiService {
    @GET("vouchers/me")
    Call<ApiResponse<List<Voucher>>> getMyVouchers();

    @retrofit2.http.POST("vouchers/admin/grant")
    Call<ApiResponse<Voucher>> grantVoucher(
            @retrofit2.http.Query("targetUserId") String targetUserId,
            @retrofit2.http.Query("discountPercent") int discountPercent,
            @retrofit2.http.Query("validDays") int validDays
    );

    @retrofit2.http.POST("vouchers/test-grant")
    Call<ApiResponse<Voucher>> testGrantVoucher();

    @retrofit2.http.POST("vouchers/validate")
    Call<ApiResponse<Voucher>> validateVoucher(
            @retrofit2.http.Body com.example.cinemabookingapp.data.dto.ValidateVoucherRequest request
    );
}
