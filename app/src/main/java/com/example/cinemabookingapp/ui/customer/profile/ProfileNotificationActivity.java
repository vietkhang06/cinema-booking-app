package com.example.cinemabookingapp.ui.customer.profile;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.di.ServiceProvider;

public class ProfileNotificationActivity extends AuthActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.customer_activity_notification);

        }
}
