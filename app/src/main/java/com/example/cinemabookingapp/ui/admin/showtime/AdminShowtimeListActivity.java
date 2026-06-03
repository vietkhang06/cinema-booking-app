package com.example.cinemabookingapp.ui.admin.showtime;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.data.repository.RoomRepositoryImpl;
import com.example.cinemabookingapp.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.model.Room;
import com.example.cinemabookingapp.domain.model.Showtime;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminShowtimeListActivity extends AppCompatActivity implements AdminShowtimeAdapter.OnShowtimeActionListener {

    private static final String TAG = "AdminShowtimeList";
    private static final int REQUEST_CODE_ADD_EDIT = 1001;

    private MaterialAutoCompleteTextView actvCinemaChooser;
    private RecyclerView rvShowtimes;
    private TextView tvEmptyShowtimes;
    private com.google.android.material.button.MaterialButton btnActiveShowtimes;
    private com.google.android.material.button.MaterialButton btnScheduledShowtimes;
    private com.google.android.material.button.MaterialButton btnExpiredShowtimes;
    private int currentTab = 0; // 0: Active, 1: Scheduled, 2: Expired

    private ShowtimeRepositoryImpl showtimeRepository;
    private CinemaRepositoryImpl cinemaRepository;
    private MovieRepositoryImpl movieRepository;
    private RoomRepositoryImpl roomRepository;

    private final List<Showtime> allShowtimes = new ArrayList<>();
    private final List<Showtime> allSchedules = new ArrayList<>();
    private final List<Showtime> displayedShowtimes = new ArrayList<>();
    private final List<Cinema> cinemaList = new ArrayList<>();
    
    private final Map<String, Movie> movieMap = new HashMap<>();
    private final Map<String, Cinema> cinemaMap = new HashMap<>();
    private final Map<String, Room> roomMap = new HashMap<>();

    private AdminShowtimeAdapter adapter;
    private String selectedCinemaIdFilter = null; // null means "All"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_showtime_list);

        showtimeRepository = new ShowtimeRepositoryImpl();
        cinemaRepository = new CinemaRepositoryImpl();
        movieRepository = new MovieRepositoryImpl(new com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource());
        roomRepository = new RoomRepositoryImpl();

        initViews();
        loadMetaDataAndShowtimes();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        actvCinemaChooser = findViewById(R.id.actvCinemaChooser);
        rvShowtimes = findViewById(R.id.rvShowtimes);
        tvEmptyShowtimes = findViewById(R.id.tvEmptyShowtimes);

        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminShowtimeAdapter(displayedShowtimes, movieMap, cinemaMap, roomMap, this);
        rvShowtimes.setAdapter(adapter);

        findViewById(R.id.btnAddShowtime).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminShowtimeAddEditActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT);
        });

        findViewById(R.id.btnScheduleShowtime).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminShowtimeScheduleActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT);
        });

        btnActiveShowtimes = findViewById(R.id.btnActiveShowtimes);
        btnScheduledShowtimes = findViewById(R.id.btnScheduledShowtimes);
        btnExpiredShowtimes = findViewById(R.id.btnExpiredShowtimes);

        btnActiveShowtimes.setOnClickListener(v -> switchTab(0));
        btnScheduledShowtimes.setOnClickListener(v -> switchTab(1));
        btnExpiredShowtimes.setOnClickListener(v -> switchTab(2));

        actvCinemaChooser.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedCinemaIdFilter = null;
            } else {
                selectedCinemaIdFilter = cinemaList.get(position - 1).cinemaId;
            }
            filterShowtimes();
        });
    }

    private void loadMetaDataAndShowtimes() {
        // Load movies, cinemas, and rooms first to ensure we can display names correctly
        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                for (Movie m : movies) {
                    movieMap.put(m.movieId, m);
                }
                loadCinemas();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải thông tin phim: " + message);
            }
        });
    }

    private void loadCinemas() {
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> cinemas) {
                cinemaList.clear();
                cinemaList.addAll(cinemas);
                for (Cinema c : cinemas) {
                    cinemaMap.put(c.cinemaId, c);
                }

                // Populate filter dropdown
                List<String> cinemaNames = new ArrayList<>();
                cinemaNames.add("Tất cả các rạp");
                for (Cinema c : cinemas) {
                    cinemaNames.add(c.name);
                }
                ArrayAdapter<String> adapterFilter = new ArrayAdapter<>(
                        AdminShowtimeListActivity.this,
                        android.R.layout.simple_list_item_1,
                        cinemaNames
                );
                actvCinemaChooser.setAdapter(adapterFilter);

                loadRooms();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách rạp: " + message);
            }
        });
    }

    private void loadRooms() {
        roomRepository.getAllRooms(new ResultCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                for (Room r : rooms) {
                    roomMap.put(r.roomId, r);
                }
                loadShowtimes();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách phòng: " + message);
            }
        });
    }

    private void loadShowtimes() {
        showtimeRepository.getAllShowtimes(new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(List<Showtime> showtimes) {
                allShowtimes.clear();
                allShowtimes.addAll(showtimes);
                
                showtimeRepository.getAllShowtimeSchedules(new ResultCallback<List<Showtime>>() {
                    @Override
                    public void onSuccess(List<Showtime> schedules) {
                        allSchedules.clear();
                        allSchedules.addAll(schedules);
                        filterShowtimes();
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Lỗi tải lịch trình suất chiếu: " + message);
                        filterShowtimes();
                    }
                });
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách suất chiếu: " + message);
            }
        });
    }

    private void switchTab(int tabIndex) {
        currentTab = tabIndex;
        
        // Active Tab
        if (currentTab == 0) {
            btnActiveShowtimes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E1A23")));
            btnActiveShowtimes.setTextColor(Color.WHITE);
        } else {
            btnActiveShowtimes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            btnActiveShowtimes.setTextColor(Color.parseColor("#1E1A23"));
        }

        // Scheduled Tab
        if (currentTab == 1) {
            btnScheduledShowtimes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E1A23")));
            btnScheduledShowtimes.setTextColor(Color.WHITE);
        } else {
            btnScheduledShowtimes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            btnScheduledShowtimes.setTextColor(Color.parseColor("#1E1A23"));
        }

        // Expired Tab
        if (currentTab == 2) {
            btnExpiredShowtimes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E1A23")));
            btnExpiredShowtimes.setTextColor(Color.WHITE);
        } else {
            btnExpiredShowtimes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            btnExpiredShowtimes.setTextColor(Color.parseColor("#1E1A23"));
        }

        filterShowtimes();
    }

    private void filterShowtimes() {
        long currentTime = System.currentTimeMillis();
        int activeCount = 0;
        int scheduledCount = 0;
        int expiredCount = 0;

        // Calculate active and expired counts from allShowtimes
        for (Showtime s : allShowtimes) {
            if (selectedCinemaIdFilter == null || selectedCinemaIdFilter.equals(s.cinemaId)) {
                if (s.endAt < currentTime) {
                    expiredCount++;
                } else {
                    activeCount++;
                }
            }
        }

        // Calculate scheduled counts from allSchedules (where executed = false)
        for (Showtime s : allSchedules) {
            if (selectedCinemaIdFilter == null || selectedCinemaIdFilter.equals(s.cinemaId)) {
                if (!s.executed) {
                    scheduledCount++;
                }
            }
        }

        btnActiveShowtimes.setText("Hoạt động (" + activeCount + ")");
        btnScheduledShowtimes.setText("Lên lịch (" + scheduledCount + ")");
        btnExpiredShowtimes.setText("Hết hạn (" + expiredCount + ")");

        displayedShowtimes.clear();
        if (currentTab == 0) { // Active
            for (Showtime s : allShowtimes) {
                if (selectedCinemaIdFilter == null || selectedCinemaIdFilter.equals(s.cinemaId)) {
                    if (s.endAt >= currentTime) {
                        displayedShowtimes.add(s);
                    }
                }
            }
        } else if (currentTab == 1) { // Scheduled
            for (Showtime s : allSchedules) {
                if (selectedCinemaIdFilter == null || selectedCinemaIdFilter.equals(s.cinemaId)) {
                    if (!s.executed) {
                        displayedShowtimes.add(s);
                    }
                }
            }
        } else if (currentTab == 2) { // Expired
            for (Showtime s : allShowtimes) {
                if (selectedCinemaIdFilter == null || selectedCinemaIdFilter.equals(s.cinemaId)) {
                    if (s.endAt < currentTime) {
                        displayedShowtimes.add(s);
                    }
                }
            }
        }
        
        adapter.notifyDataSetChanged();

        if (displayedShowtimes.isEmpty()) {
            tvEmptyShowtimes.setVisibility(View.VISIBLE);
        } else {
            tvEmptyShowtimes.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(Showtime showtime) {
        Intent intent = new Intent(this, AdminShowtimeAddEditActivity.class);
        intent.putExtra("extra_showtime_id", showtime.showtimeId);
        startActivityForResult(intent, REQUEST_CODE_ADD_EDIT);
    }

    @Override
    public void onDelete(Showtime showtime) {
        // Business Rule validation: Check if showtime has active bookings
        FirebaseFirestore.getInstance().collection(FirestoreCollections.BOOKINGS)
                .whereEqualTo("showtimeId", showtime.showtimeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Không thể xóa")
                                .setMessage("Suất chiếu này đã có vé được đặt. Vui lòng không xóa để tránh lỗi dữ liệu.")
                                .setPositiveButton("Đã hiểu", null)
                                .show();
                    } else {
                        // Confirm deletion
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc chắn muốn xóa suất chiếu này không?")
                                .setPositiveButton("Xóa", (dialog, which) -> deleteShowtime(showtime))
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Lỗi kiểm tra vé đặt: " + e.getMessage());
                });
    }

    private void deleteShowtime(Showtime showtime) {
        if (showtime.isScheduled && !showtime.executed) {
            showtimeRepository.deleteShowtimeSchedule(showtime.showtimeId, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    showToast("Đã xóa lịch trình suất chiếu thành công");
                    loadShowtimes();
                }

                @Override
                public void onError(String message) {
                    showToast("Xóa lịch trình thất bại: " + message);
                }
            });
        } else {
            showtimeRepository.softDeleteShowtime(showtime.showtimeId, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    showToast("Đã xóa suất chiếu thành công");
                    loadShowtimes();
                }

                @Override
                public void onError(String message) {
                    showToast("Xóa suất chiếu thất bại: " + message);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EDIT && resultCode == RESULT_OK) {
            loadShowtimes();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}