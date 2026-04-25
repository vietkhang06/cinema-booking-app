package com.example.cinemabookingapp.domain.usecase.movie;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.repository.MovieRepository;

import java.util.List;

public class GetMoviesUseCase {

    private final MovieRepository movieRepository;

    public GetMoviesUseCase(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public void execute(ResultCallback<List<Movie>> callback) {
        movieRepository.getAllMovies(callback);
    }
}