package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.domain.model.Cinema;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CinemaApiService {
    @GET("cinemas")
    Call<ApiResponse<List<Cinema>>> getAllCinemas();

    @GET("cinemas/{id}")
    Call<ApiResponse<Cinema>> getCinemaById(@Path("id") String id);
}
