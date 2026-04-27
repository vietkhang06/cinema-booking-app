package com.example.cinemabookingapp.domain.common;

public interface AuthCallback {
    void onSuccess(com.google.firebase.auth.FirebaseUser user);
    void onFailure(String errorMessage);
}
