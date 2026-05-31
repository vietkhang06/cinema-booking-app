package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.request.UpdateProfileRequest;
import com.example.cinemabookingapp.domain.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface ProfileApiService {
    @GET("profile")
    Call<ApiResponse<User>> getMyProfile();

    @PUT("profile")
    Call<ApiResponse<User>> updateProfile(@Body UpdateProfileRequest request);

}
