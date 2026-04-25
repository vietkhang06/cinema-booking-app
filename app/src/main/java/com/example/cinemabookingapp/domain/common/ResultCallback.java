package com.example.cinemabookingapp.domain.common;

public interface ResultCallback<T> {
    void onSuccess(T data);
    void onError(String message);

    void softDeleteMovie(String movieId, ResultCallback<Void> callback);
}