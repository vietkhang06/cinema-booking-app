package com.example.cinemabookingapp.di;

import android.content.Context;

import com.example.cinemabookingapp.service.AuthenticationService;

public class ServiceProvider {

    private static ServiceProvider instance;
    private final Context appContext;

    private AuthenticationService authenticationService;

    private ServiceProvider(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static synchronized ServiceProvider getInstance(Context context) {
        if (instance == null) {
            instance = new ServiceProvider(context);
        }
        return instance;
    }

    public static synchronized ServiceProvider getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServiceProvider chưa được init!");
        }
        return instance;
    }

    public AuthenticationService getAuthenticationService() {
        if (authenticationService == null) {
            authenticationService = new AuthenticationService(appContext);
        }
        return authenticationService;
    }
}