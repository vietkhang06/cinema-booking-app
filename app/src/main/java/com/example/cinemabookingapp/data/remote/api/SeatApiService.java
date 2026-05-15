package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.SeatActionRequest;
import com.example.cinemabookingapp.data.dto.SeatDTO;

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
    Call<ApiResponse<Void>> lockSeats(@Body SeatActionRequest request);

    @POST("seats/unlock")
    Call<ApiResponse<Void>> unlockSeats(@Body SeatActionRequest request);
}
