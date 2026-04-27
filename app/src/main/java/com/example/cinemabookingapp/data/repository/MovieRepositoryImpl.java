package com.example.cinemabookingapp.data.repository;

import com.example.cinemabookingapp.data.dto.MovieDTO;
import com.example.cinemabookingapp.data.mapper.MovieMapper;
import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;

public class MovieRepositoryImpl implements MovieRepository {

    private final MovieRemoteDataSource remoteDataSource;

    public MovieRepositoryImpl(MovieRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void createMovie(Movie movie, ResultCallback<Movie> callback) {
        callback.onError("createMovie chưa được triển khai");
    }

    @Override
    public void getMovieById(String movieId, ResultCallback<Movie> callback) {
        remoteDataSource.getMovieById(movieId, new ResultCallback<MovieDTO>() {
            @Override
            public void onSuccess(MovieDTO data) {
                Movie movie = MovieMapper.toDomain(data);
                if (movie == null) {
                    callback.onError("Movie data is empty");
                    return;
                }
                callback.onSuccess(movie);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }

        });
    }

    @Override
    public void getAllMovies(ResultCallback<List<Movie>> callback) {
        remoteDataSource.getAllMovies(new ResultCallback<List<MovieDTO>>() {
            @Override
            public void onSuccess(List<MovieDTO> data) {
                List<Movie> list = new ArrayList<>();
                if (data != null) {
                    for (MovieDTO dto : data) {
                        Movie movie = MovieMapper.toDomain(dto);
                        if (movie != null) {
                            list.add(movie);
                        }
                    }
                }
                callback.onSuccess(list);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }

        });
    }

    @Override
    public void getMoviesByStatus(String status, ResultCallback<List<Movie>> callback) {
        callback.onError("getMoviesByStatus chưa được triển khai");
    }

    @Override
    public void searchMovies(String keyword, ResultCallback<List<Movie>> callback) {
        callback.onError("searchMovies chưa được triển khai");
    }

    @Override
    public void updateMovie(Movie movie, ResultCallback<Movie> callback) {
        callback.onError("updateMovie chưa được triển khai");
    }

    @Override
    public void softDeleteMovie(String movieId, ResultCallback<Void> callback) {
        callback.onError("softDeleteMovie chưa được triển khai");
    }
}