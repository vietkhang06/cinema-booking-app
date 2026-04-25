package com.example.cinemabookingapp.core.base;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabookingapp.MyApp;
import com.example.cinemabookingapp.core.session.SessionManager;
import com.example.cinemabookingapp.di.AppContainer;

public abstract class BaseActivity extends AppCompatActivity {

    protected AppContainer appContainer;
    protected SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContainer = ((MyApp) getApplication()).getAppContainer();
        sessionManager = appContainer.getSessionManager();
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}