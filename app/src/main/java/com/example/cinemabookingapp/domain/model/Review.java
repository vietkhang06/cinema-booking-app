package com.example.cinemabookingapp.domain.model;

public class Review {
    public String reviewId;
    public String userId;
    public String movieId;
    public String bookingId;
    public String movieTitleSnapshot;
    public int rating;
    public String content;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
    public String parentId;
    public int replyCount;
    public java.util.List<Review> replies;
    public int likes;
    public int dislikes;
    public java.util.List<String> likedBy = new java.util.ArrayList<>();
    public java.util.List<String> dislikedBy = new java.util.ArrayList<>();

    public Review() {
    }
}