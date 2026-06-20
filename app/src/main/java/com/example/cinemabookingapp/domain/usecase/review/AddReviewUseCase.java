package com.example.cinemabookingapp.domain.usecase.review;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Review;
import com.example.cinemabookingapp.domain.repository.ReviewRepository;

public class AddReviewUseCase {
    private final ReviewRepository repository;

    public AddReviewUseCase(ReviewRepository repository) {
        this.repository = repository;
    }

    public void execute(Review review, ResultCallback<Review> callback) {
        if ((review.content == null || review.content.trim().isEmpty()) && (review.rating == null || review.rating == 0)) {
            callback.onError("Ná»™i dung bÃ¬nh luáº­n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            return;
        }

        repository.createReview(review, callback);
    }
}