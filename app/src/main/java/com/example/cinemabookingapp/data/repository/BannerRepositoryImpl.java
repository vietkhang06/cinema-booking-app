package com.example.cinemabookingapp.data.repository;

import com.example.cinemabookingapp.data.remote.datasource.BannerRemoteDataSource;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Banner;
import com.example.cinemabookingapp.domain.repository.BannerRepository;

import java.util.List;

public class BannerRepositoryImpl implements BannerRepository {

    private final BannerRemoteDataSource remote;

    public BannerRepositoryImpl(BannerRemoteDataSource remote) {
        this.remote = remote;
    }

    @Override
    public void getAllBanners(ResultCallback<List<Banner>> callback) {
        remote.getAllBanners(callback);
    }
}