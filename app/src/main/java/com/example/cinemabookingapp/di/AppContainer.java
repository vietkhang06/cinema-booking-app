package com.example.cinemabookingapp.di;

import android.content.Context;

import com.example.cinemabookingapp.core.session.SessionManager;
import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.domain.repository.MovieRepository;
import com.example.cinemabookingapp.domain.usecase.movie.GetMoviesUseCase;

public class AppContainer {

    private final SessionManager sessionManager;
    private final MovieRemoteDataSource movieRemoteDataSource;
    private final MovieRepository movieRepository;
    private final GetMoviesUseCase getMoviesUseCase;

    public AppContainer(Context context) {
        sessionManager = new SessionManager(context);
        movieRemoteDataSource = new MovieRemoteDataSource();
        movieRepository = new MovieRepositoryImpl(movieRemoteDataSource);
        getMoviesUseCase = new GetMoviesUseCase(movieRepository);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public GetMoviesUseCase getMoviesUseCase() {
        return getMoviesUseCase;
    }
}