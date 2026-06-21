package com.example.cinemabookingapp.ui.features.admin.cinema;

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
import com.example.cinemabookingapp.ui.features.admin.dashboard.AdminBottomNavHelper;
import com.example.cinemabookingapp.ui.features.admin.cinema.adapter.AdminCinemaAdapter;
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

        AdminBottomNavHelper.setupAdminBottomNavigation(this, 1);

        repo = new CinemaRepositoryImpl();

        rv = findViewById(R.id.rvCinema);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAdd = findViewById(R.id.btnAddCinema);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        adapter = new AdminCinemaAdapter();
        adapter.setListener(new AdminCinemaAdapter.OnCinemaActionListener() {
            @Override
            public void onEditClick(Cinema cinema) {
                Intent intent = new Intent(AdminCinemaListActivity.this, AdminCinemaFormActivity.class);
                intent.putExtra(AdminCinemaFormActivity.EXTRA_CINEMA_ID, cinema.cinemaId);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Cinema cinema) {
                new androidx.appcompat.app.AlertDialog.Builder(AdminCinemaListActivity.this)
                        .setTitle("⚠️ Xác nhận xóa rạp")
                        .setMessage("Bạn có chắc chắn muốn xóa rạp '" + cinema.name + "' không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            repo.softDeleteCinema(cinema.cinemaId, new ResultCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    com.example.cinemabookingapp.ui.features.admin.log.AdminAuditLogger.log(
                                            "DELETE_CINEMA", "CINEMA", cinema.cinemaId, "Đã xóa rạp: " + cinema.name
                                    );
                                    showToast("Đã xóa rạp thành công");
                                    loadData();
                                }

                                @Override
                                public void onError(String message) {
                                    showToast("Lỗi: " + message);
                                }
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onViewDetailsClick(Cinema cinema) {
                Intent intent = new Intent(AdminCinemaListActivity.this, AdminCinemaDetailActivity.class);
                intent.putExtra(AdminCinemaDetailActivity.EXTRA_CINEMA_ID, cinema.cinemaId);
                startActivity(intent);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCinemaFormActivity.class))
        );
        
        android.widget.EditText etSearch = findViewById(R.id.etSearchCinema);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
    
    private void filter(String text) {
        if (text.isEmpty()) {
            adapter.submitList(new ArrayList<>(list));
            return;
        }
        String lowerText = text.toLowerCase();
        List<Cinema> filtered = new ArrayList<>();
        for (Cinema c : list) {
            if ((c.name != null && c.name.toLowerCase().contains(lowerText)) || 
                (c.city != null && c.city.toLowerCase().contains(lowerText))) {
                filtered.add(c);
            }
        }
        adapter.submitList(filtered);
    }

    private void loadData() {
        repo.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> data) {
                if (isFinishing() || isDestroyed()) return;

                list.clear();
                if (data != null) list.addAll(data);

                // Fetch all rooms from database to count how many belong to each cinema
                new com.example.cinemabookingapp.data.repository.RoomRepositoryImpl().getAllRooms(new ResultCallback<List<com.example.cinemabookingapp.domain.model.Room>>() {
                    @Override
                    public void onSuccess(List<com.example.cinemabookingapp.domain.model.Room> rooms) {
                        if (isFinishing() || isDestroyed()) return;

                        java.util.Map<String, Integer> roomCounts = new java.util.HashMap<>();
                        if (rooms != null) {
                            for (com.example.cinemabookingapp.domain.model.Room room : rooms) {
                                if (room.cinemaId != null && !room.deleted) {
                                    roomCounts.put(room.cinemaId, roomCounts.getOrDefault(room.cinemaId, 0) + 1);
                                }
                            }
                        }

                        // Map counts to the cinemas' room list size
                        for (Cinema c : list) {
                            int count = roomCounts.getOrDefault(c.cinemaId, 0);
                            c.roomIds = new ArrayList<>();
                            for (int i = 0; i < count; i++) {
                                c.roomIds.add("dummy");
                            }
                        }

                        adapter.submitList(new ArrayList<>(list));
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        if (isFinishing() || isDestroyed()) return;
                        adapter.submitList(new ArrayList<>(list));
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }
}