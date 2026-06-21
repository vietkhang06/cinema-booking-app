package com.example.cinemabookingapp.ui.features.auth;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SPLASH_DEBUG";
    private static final long SPLASH_DELAY = 2500L;

    private View[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initDots();
        startDotAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isLoggedIn = sessionManager.isLoggedIn();
            String role = sessionManager.getRole();
            boolean isRememberMe = sessionManager.isRememberMe();

            Log.e(TAG, "========== SPLASH TRACE ==========");
            Log.e(TAG, "isLoggedIn = " + isLoggedIn);
            Log.e(TAG, "isRememberMe = " + isRememberMe);
            Log.e(TAG, "role = [" + role + "]");
            Log.e(TAG, "==================================");

            if (isLoggedIn && isRememberMe) {
                ServiceProvider.getInstance(getApplicationContext()).getAuthenticationService().getCurrentAuthUser(new ResultCallback<User>() {
                    @Override
                    public void onSuccess(User data) {
                        String finalRole = role;
                        if (data != null) {
                            finalRole = data.role;
                            sessionManager.saveLoginState(true, data.role, data.uid);
                        }
                        Log.e(TAG, "Dynamic role sync success. Routing to: " + finalRole);
                        AppNavigator.goToHomeByRole(SplashActivity.this, finalRole);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Dynamic role sync failed: " + message + ". Falling back to cached role: " + role);
                        AppNavigator.goToHomeByRole(SplashActivity.this, role);
                    }
                });
            } else {
                ServiceProvider.getInstance(getApplicationContext()).getAuthenticationService().logOut();
                AppNavigator.goToCustomerHome(this);
            }
        }, SPLASH_DELAY);
    }

    private void initDots() {
        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4),
                findViewById(R.id.dot5),
                findViewById(R.id.dot6),
                findViewById(R.id.dot7)
        };
    }

    private void startDotAnimation() {
        long delayStep = 120L;

        for (int i = 0; i < dots.length; i++) {
            View dot = dots[i];
            dot.setScaleX(1f);
            dot.setScaleY(1f);

            ObjectAnimator animator = ObjectAnimator.ofFloat(dot, "translationY", 0f, -10f, 0f);
            animator.setDuration(500L);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setStartDelay(i * delayStep);
            animator.start();
        }
    }
}