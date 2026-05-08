package com.example.cinemabookingapp.ui.customer.profile;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;

public class ProfileTransactionActivity extends AuthActivity {

    RecyclerView bookingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_activity_transaction);

        initViews();
        bindActions();

        loadBookingHistory();
    }

    void initViews(){
        bookingContainer = findViewById(R.id.booking_container);
    }

    void bindActions(){

    }

    void loadBookingHistory(){

    }
}
