package com.example.cinemabookingapp.data.mapper;

import com.example.cinemabookingapp.data.dto.ReviewDTO;
import com.example.cinemabookingapp.domain.model.Review;

public final class ReviewMapper {
    private ReviewMapper() {
    }

    public static Review toDomain(ReviewDTO dto) {
        if (dto == null) return null;
        Review model = new Review();
        model.reviewId = dto.reviewId;
        model.userId = dto.userId;
        model.movieId = dto.movieId;
        model.bookingId = dto.bookingId;
        model.movieTitleSnapshot = dto.movieTitleSnapshot;
        model.rating = dto.rating;
        model.content = dto.content;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        model.parentId = dto.parentId;
        model.replyCount = dto.replyCount;
        model.likes = dto.likes;
        model.dislikes = dto.dislikes;
        if (dto.likedBy != null) {
            model.likedBy = new java.util.ArrayList<>(dto.likedBy);
        } else {
            model.likedBy = new java.util.ArrayList<>();
        }
        if (dto.dislikedBy != null) {
            model.dislikedBy = new java.util.ArrayList<>(dto.dislikedBy);
        } else {
            model.dislikedBy = new java.util.ArrayList<>();
        }
        if (dto.replies != null) {
            model.replies = new java.util.ArrayList<>();
            for (ReviewDTO replyDto : dto.replies) {
                model.replies.add(toDomain(replyDto));
            }
        }
        return model;
    }

    public static ReviewDTO toDTO(Review model) {
        if (model == null) return null;
        ReviewDTO dto = new ReviewDTO();
        dto.reviewId = model.reviewId;
        dto.userId = model.userId;
        dto.movieId = model.movieId;
        dto.bookingId = model.bookingId;
        dto.movieTitleSnapshot = model.movieTitleSnapshot;
        dto.rating = model.rating;
        dto.content = model.content;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        dto.parentId = model.parentId;
        dto.replyCount = model.replyCount;
        dto.likes = model.likes;
        dto.dislikes = model.dislikes;
        if (model.likedBy != null) {
            dto.likedBy = new java.util.ArrayList<>(model.likedBy);
        } else {
            dto.likedBy = new java.util.ArrayList<>();
        }
        if (model.dislikedBy != null) {
            dto.dislikedBy = new java.util.ArrayList<>(model.dislikedBy);
        } else {
            dto.dislikedBy = new java.util.ArrayList<>();
        }
        if (model.replies != null) {
            dto.replies = new java.util.ArrayList<>();
            for (Review replyModel : model.replies) {
                dto.replies.add(toDTO(replyModel));
            }
        }
        return dto;
    }
}