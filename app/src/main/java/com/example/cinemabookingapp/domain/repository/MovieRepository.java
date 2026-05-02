package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Movie;

import java.util.List;

public interface MovieRepository {
    void createMovie(Movie movie, ResultCallback<Movie> callback);
    void getMovieById(String movieId, ResultCallback<Movie> callback);
    void getAllMovies(ResultCallback<List<Movie>> callback);
    void getMoviesByStatus(String status, ResultCallback<List<Movie>> callback);
    void searchMovies(String keyword, ResultCallback<List<Movie>> callback);
    void updateMovie(Movie movie, ResultCallback<Movie> callback);
    void softDeleteMovie(String movieId, ResultCallback<Void> callback);
}