package com.example.cinemabookingapp.ui.features.cinema_contents.model;

import com.example.cinemabookingapp.domain.model.cinema.CinemaContentType;

public class CinemaFeedItem {
    public String id;
    public CinemaContentType type;
    public String tag;
    public String title;
    public String excerpt;
    public String content;
    public String author;
    public String meta;
    public String imageUrl;

    public CinemaFeedItem() {
    }
}