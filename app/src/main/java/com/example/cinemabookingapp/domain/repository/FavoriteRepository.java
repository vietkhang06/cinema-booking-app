package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Favorite;

import java.util.List;

public interface FavoriteRepository {
    void addFavorite(Favorite favorite, ResultCallback<Favorite> callback);
    void removeFavorite(String favoriteId, ResultCallback<Void> callback);
    void getFavoritesByUserId(String userId, ResultCallback<List<Favorite>> callback);
    void isFavorite(String userId, String movieId, ResultCallback<Boolean> callback);
}