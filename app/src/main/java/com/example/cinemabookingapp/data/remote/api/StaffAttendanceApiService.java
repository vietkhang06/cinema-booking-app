package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.AttendanceDTO;
import com.example.cinemabookingapp.data.dto.ViolationDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StaffAttendanceApiService {

    @GET("staff/attendance/today")
    Call<ApiResponse<AttendanceDTO>> getTodayAttendance();

    @POST("staff/attendance/checkin")
    Call<ApiResponse<AttendanceDTO>> checkIn(
            @Query("shiftName") String shiftName,
            @Query("cinemaId") String cinemaId,
            @Query("cinemaName") String cinemaName
    );

    @POST("staff/attendance/checkout")
    Call<ApiResponse<AttendanceDTO>> checkOut();

    @GET("staff/attendance/history")
    Call<ApiResponse<List<AttendanceDTO>>> getMyAttendanceHistory();

    @GET("staff/violations/history")
    Call<ApiResponse<List<ViolationDTO>>> getMyViolationHistory();
}
