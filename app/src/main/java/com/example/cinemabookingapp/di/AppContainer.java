package com.example.cinemabookingapp.di;

import android.content.Context;

import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.core.session.SessionManager;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.domain.repository.MovieRepository;
import com.example.cinemabookingapp.domain.usecase.movie.GetMoviesUseCase;

import com.example.cinemabookingapp.data.remote.datasource.BannerRemoteDataSource;
import com.example.cinemabookingapp.data.repository.BannerRepositoryImpl;
import com.example.cinemabookingapp.domain.repository.BannerRepository;
import com.example.cinemabookingapp.domain.usecase.banner.GetBannersUseCase;

public class AppContainer {

    private final SessionManager sessionManager;
    private final MovieRemoteDataSource movieRemoteDataSource;
    private final MovieRepository movieRepository;
    private final GetMoviesUseCase getMoviesUseCase;
    private final BannerRemoteDataSource bannerRemoteDataSource;
    private final BannerRepository bannerRepository;
    private final GetBannersUseCase getBannersUseCase;

    public AppContainer(Context context) {
        sessionManager = new SessionManager(context);
        movieRemoteDataSource = new MovieRemoteDataSource();
        movieRepository = new MovieRepositoryImpl(movieRemoteDataSource);
        getMoviesUseCase = new GetMoviesUseCase(movieRepository);
        bannerRemoteDataSource = new BannerRemoteDataSource();
        bannerRepository = new BannerRepositoryImpl(bannerRemoteDataSource);
        getBannersUseCase = new GetBannersUseCase(bannerRepository);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public GetMoviesUseCase getMoviesUseCase() {
        return getMoviesUseCase;
    }

    public GetBannersUseCase getBannersUseCase() {
        return getBannersUseCase;
    }
}