package com.example.cinemabookingapp.di;

import android.content.Context;

import com.example.cinemabookingapp.service.AuthenticationService;

// service locator pattern
public class ServiceProvider {

    private static ServiceProvider instance;

    private final AuthenticationService authService;
    public ServiceProvider(Context context){
        this.authService = new AuthenticationService(context);
    }

    public static ServiceProvider getInstance(Context context){
        if(instance == null){
            instance = new ServiceProvider(context);
        }
        return instance;
    }

    public static ServiceProvider getInstance(){
        if(instance == null){
            instance = new ServiceProvider(null);
        }
        return instance;
    }

    public AuthenticationService getAuthenticationService(){
        return authService;
    }


}
