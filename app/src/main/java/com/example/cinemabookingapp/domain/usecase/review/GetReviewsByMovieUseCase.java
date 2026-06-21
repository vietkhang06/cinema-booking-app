package com.example.cinemabookingapp.domain.usecase.review;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Review;
import com.example.cinemabookingapp.domain.repository.ReviewRepository;

import java.util.List;

public class GetReviewsByMovieUseCase {
    private final ReviewRepository repository;

    public GetReviewsByMovieUseCase(ReviewRepository repository) {
        this.repository = repository;
    }

    public void execute(String movieId, ResultCallback<List<Review>> callback) {
        repository.getReviewsByMovieId(movieId, callback);
    }
}