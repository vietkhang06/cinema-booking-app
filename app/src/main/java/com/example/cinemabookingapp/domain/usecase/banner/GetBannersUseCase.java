package com.example.cinemabookingapp.domain.usecase.banner;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Banner;
import com.example.cinemabookingapp.domain.repository.BannerRepository;

import java.util.List;

public class GetBannersUseCase {

    private final BannerRepository repository;

    public GetBannersUseCase(BannerRepository repository) {
        this.repository = repository;
    }

    public void execute(ResultCallback<List<Banner>> callback) {
        repository.getAllBanners(callback);
    }
}