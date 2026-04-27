package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Banner;

import java.util.List;

public interface BannerRepository {
    void getAllBanners(ResultCallback<List<Banner>> callback);
}