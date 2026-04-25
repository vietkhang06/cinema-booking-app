package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Review;

import java.util.List;

public interface ReviewRepository {
    void createReview(Review review, ResultCallback<Review> callback);
    void getReviewById(String reviewId, ResultCallback<Review> callback);
    void getReviewsByMovieId(String movieId, ResultCallback<List<Review>> callback);
    void getReviewsByUserId(String userId, ResultCallback<List<Review>> callback);
    void updateReview(Review review, ResultCallback<Review> callback);
    void hideReview(String reviewId, ResultCallback<Review> callback);
    void deleteReview(String reviewId, ResultCallback<Void> callback);
}