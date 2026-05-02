package com.example.cinemabookingapp.domain.usecase.movie;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.repository.MovieRepository;

public class GetMovieByIdUseCase {

    private final MovieRepository movieRepository;

    public GetMovieByIdUseCase(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public void execute(String movieId, ResultCallback<Movie> callback) {
        movieRepository.getMovieById(movieId, callback);
    }
}