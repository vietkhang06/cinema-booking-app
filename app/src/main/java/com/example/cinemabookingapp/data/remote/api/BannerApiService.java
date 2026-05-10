package com.example.cinemabookingapp.data.remote.api;

import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.domain.model.Banner;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface BannerApiService {
    @GET("banners")
    Call<ApiResponse<List<Banner>>> getAllBanners();
}
