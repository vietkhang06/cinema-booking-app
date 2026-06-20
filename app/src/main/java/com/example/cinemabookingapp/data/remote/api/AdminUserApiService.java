package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.UserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminUserApiService {

    // Request wrapper for user creation
    class CreateUserRequest {
        public UserDTO user;
        public String password;

        public CreateUserRequest(UserDTO user, String password) {
            this.user = user;
            this.password = password;
        }
    }

    @POST("admin/users")
    Call<ApiResponse<UserDTO>> createUser(@Body CreateUserRequest request);

    @GET("admin/users")
    Call<ApiResponse<List<UserDTO>>> getAllUsers(
            @Query("search") String search,
            @Query("status") String status,
            @Query("cinemaId") String cinemaId
    );

    @GET("admin/users/{uid}")
    Call<ApiResponse<UserDTO>> getUserDetail(@Path("uid") String uid);

    @PUT("admin/users/{uid}")
    Call<ApiResponse<UserDTO>> updateUser(@Path("uid") String uid, @Body UserDTO user);

    @DELETE("admin/users/{uid}")
    Call<ApiResponse<Void>> deleteUser(@Path("uid") String uid);

    @POST("admin/users/{uid}/reset-password")
    Call<ApiResponse<Void>> resetPassword(
            @Path("uid") String uid,
            @Query("newPassword") String newPassword
    );
}
