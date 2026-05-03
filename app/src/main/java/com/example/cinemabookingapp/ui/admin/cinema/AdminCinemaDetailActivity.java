package com.example.cinemabookingapp.ui.admin.cinema;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.example.cinemabookingapp.domain.repository.CinemaRepository;
import com.google.android.material.button.MaterialButton;

public class AdminCinemaDetailActivity extends BaseActivity {

    public static final String EXTRA_CINEMA_ID = "extra_cinema_id";

    private TextView tvName, tvAddress, tvCity, tvDistrict, tvPhone, tvLat, tvLng, tvStatus;
    private MaterialButton btnEdit, btnDelete;

    private CinemaRepository cinemaRepository;
    private String cinemaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cinema_detail);

        cinemaRepository = new CinemaRepositoryImpl();

        cinemaId = getIntent().getStringExtra(EXTRA_CINEMA_ID);

        if (cinemaId == null) {
            showToast("Thiếu cinemaId");
            finish();
            return;
        }

        initViews();
        loadData();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvCity = findViewById(R.id.tvCity);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvPhone = findViewById(R.id.tvPhone);
        tvLat = findViewById(R.id.tvLat);
        tvLng = findViewById(R.id.tvLng);
        tvStatus = findViewById(R.id.tvStatus);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadData() {
        cinemaRepository.getCinemaById(cinemaId, new ResultCallback<Cinema>() {
            @Override
            public void onSuccess(Cinema c) {
                if (c == null) return;

                tvName.setText(c.name);
                tvAddress.setText(c.address);
                tvCity.setText(c.city);
                tvDistrict.setText(c.district);
                tvPhone.setText(c.phone);
                tvLat.setText(String.valueOf(c.latitude));
                tvLng.setText(String.valueOf(c.longitude));
                tvStatus.setText(c.status);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }
}