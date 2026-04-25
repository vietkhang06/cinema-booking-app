package com.example.cinemabookingapp.ui.customer.model;

public class HomeMovieItem {

    public static final String NOW_SHOWING = "NOW_SHOWING";
    public static final String COMING_SOON = "COMING_SOON";

    private final String title;
    private final int posterResId;
    private final String rating;
    private final String ageRating;
    private final String status;

    public HomeMovieItem(String title, int posterResId, String rating, String ageRating, String status) {
        this.title = title;
        this.posterResId = posterResId;
        this.rating = rating;
        this.ageRating = ageRating;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public int getPosterResId() {
        return posterResId;
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