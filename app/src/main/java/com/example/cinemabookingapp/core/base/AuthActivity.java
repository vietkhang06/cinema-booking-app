package com.example.cinemabookingapp.core.base;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


// auto redirect to login when user logout
public class AuthActivity extends BaseActivity{
    FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onStart() {

        //auto redirect when not auth
        super.onStart();
        authStateListener = authState -> {
            FirebaseUser user = authState.getCurrentUser();
            if (user == null) {
                // User signed out, redirect to Login
                AppNavigator.goToLogin(this);
            }
        };

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
}
