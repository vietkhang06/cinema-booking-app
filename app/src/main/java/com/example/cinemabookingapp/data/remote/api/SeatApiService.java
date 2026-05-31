package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.SeatDTO;
import com.example.cinemabookingapp.data.dto.SeatLockRequestDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SeatApiService {

    @GET("seats/showtime/{showtimeId}")
    Call<ApiResponse<List<SeatDTO>>> getSeatsByShowtimeId(@Path("showtimeId") String showtimeId);

    @POST("seats/lock")
    Call<ApiResponse<Void>> lockSeats(@Body SeatLockRequestDTO request);

    @POST("seats/release")
    Call<ApiResponse<Void>> releaseSeats(@Body SeatLockRequestDTO request);

    @POST("seats/release-by-staff")
    Call<ApiResponse<Void>> releaseSeatsByStaff(@Body SeatLockRequestDTO request);
}
