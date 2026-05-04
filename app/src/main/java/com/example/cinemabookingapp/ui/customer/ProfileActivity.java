package com.example.cinemabookingapp.ui.customer;

import android.os.Bundle;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;

public class ProfileActivity extends AuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_activity_profile);

        initViews();
        bindActions();
    }

    private void initViews() {
    }

    private void bindActions() {
    }
}
