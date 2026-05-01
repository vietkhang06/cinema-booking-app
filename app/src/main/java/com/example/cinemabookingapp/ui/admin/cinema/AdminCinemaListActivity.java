package com.example.cinemabookingapp.ui.admin.cinema;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.example.cinemabookingapp.ui.admin.cinema.adapter.AdminCinemaAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminCinemaListActivity extends BaseActivity {

    private RecyclerView rv;
    private TextView tvEmpty;
    private MaterialButton btnAdd;

    private final List<Cinema> list = new ArrayList<>();
    private AdminCinemaAdapter adapter;

    private CinemaRepositoryImpl repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cinema_list);

        repo = new CinemaRepositoryImpl();

        rv = findViewById(R.id.rvCinema);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAdd = findViewById(R.id.btnAddCinema);

        adapter = new AdminCinemaAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCinemaFormActivity.class))
        );

        loadData();
    }

    private void loadData() {
        repo.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> data) {

                if (isFinishing() || isDestroyed()) return;

                list.clear();
                if (data != null) list.addAll(data);

                adapter.submitList(new ArrayList<>(list));

                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }
}