package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.domain.model.User;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ProfileApiService {
    @GET("profile")
    Call<ApiResponse<User>> getMyProfile();
}
