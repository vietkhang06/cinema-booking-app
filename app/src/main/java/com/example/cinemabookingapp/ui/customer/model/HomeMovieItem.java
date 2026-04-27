package com.example.cinemabookingapp.ui.customer.model;

public class HomeMovieItem {

    public static final String NOW_SHOWING = "NOW_SHOWING";
    public static final String COMING_SOON = "COMING_SOON";

    private String movieId;
    private String title;
    private String imageUrl;
    private String rating;
    private String ageRating;
    private String status;

    public HomeMovieItem() {
    }

    public HomeMovieItem(String movieId, String title, String imageUrl, String rating, String ageRating, String status) {
        this.movieId = movieId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.ageRating = ageRating;
        this.status = status;
    }

    public HomeMovieItem(String title, String imageUrl, String rating, String ageRating, String status) {
        this(null, title, imageUrl, rating, ageRating, status);
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRating() {
        return rating;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public String getStatus() {
        return status;
    }
}