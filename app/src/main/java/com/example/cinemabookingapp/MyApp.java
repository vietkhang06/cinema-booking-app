package com.example.cinemabookingapp;

import android.app.Application;

import com.example.cinemabookingapp.di.AppContainer;
import com.google.firebase.FirebaseApp;

public class MyApp extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
        FirebaseApp.initializeApp(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}