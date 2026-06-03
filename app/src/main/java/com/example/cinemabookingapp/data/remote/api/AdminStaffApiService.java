package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.AttendanceDTO;
import com.example.cinemabookingapp.data.dto.UserDTO;
import com.example.cinemabookingapp.data.dto.ViolationDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminStaffApiService {

    // Request wrapper for staff creation
    class CreateStaffRequest {
        public UserDTO staff;
        public String password;

        public CreateStaffRequest(UserDTO staff, String password) {
            this.staff = staff;
            this.password = password;
        }
    }

    @POST("admin/staff")
    Call<ApiResponse<UserDTO>> createStaff(@Body CreateStaffRequest request);

    @GET("admin/staff")
    Call<ApiResponse<List<UserDTO>>> getAllStaffs(
            @Query("search") String search,
            @Query("status") String status,
            @Query("cinemaId") String cinemaId
    );

    @GET("admin/staff/{uid}")
    Call<ApiResponse<UserDTO>> getStaffDetail(@Path("uid") String uid);

    @PUT("admin/staff/{uid}")
    Call<ApiResponse<UserDTO>> updateStaff(@Path("uid") String uid, @Body UserDTO staff);

    @DELETE("admin/staff/{uid}")
    Call<ApiResponse<Void>> deleteStaff(@Path("uid") String uid);

    @POST("admin/staff/{uid}/reset-password")
    Call<ApiResponse<Void>> resetPassword(
            @Path("uid") String uid,
            @Query("newPassword") String newPassword
    );

    @GET("admin/attendance")
    Call<ApiResponse<List<AttendanceDTO>>> getAllAttendances(
            @Query("staffId") String staffId,
            @Query("date") String date,
            @Query("cinemaId") String cinemaId
    );

    @GET("admin/violations")
    Call<ApiResponse<List<ViolationDTO>>> getAllViolations(
            @Query("staffId") String staffId,
            @Query("status") String status,
            @Query("severity") String severity
    );

    @POST("admin/violations")
    Call<ApiResponse<ViolationDTO>> createViolation(@Body ViolationDTO violation);

    @PUT("admin/violations/{id}")
    Call<ApiResponse<ViolationDTO>> updateViolation(
            @Path("id") String id,
            @Body ViolationDTO violation
    );

    @DELETE("admin/violations/{id}")
    Call<ApiResponse<Void>> deleteViolation(@Path("id") String id);
}
