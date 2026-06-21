package com.example.cinemabookingapp.ui.features.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.data.repository.BookingRepositoryImpl;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabookingapp.data.repository.UserRepositoryImpl;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.model.Showtime;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.ui.features.admin.adapter.AdminFeatureAdapter;
import com.example.cinemabookingapp.ui.features.admin.cinema.AdminCinemaListActivity;
import com.example.cinemabookingapp.ui.features.admin.log.AdminAuditLogActivity;
import com.example.cinemabookingapp.ui.features.admin.model.AdminFeatureItem;
import com.example.cinemabookingapp.ui.features.admin.movie.AdminMovieListActivity;
import com.example.cinemabookingapp.ui.features.admin.payment.AdminPaymentListActivity;
import com.example.cinemabookingapp.ui.features.admin.promotion.AdminPromotionListActivity;
import com.example.cinemabookingapp.ui.features.admin.report.AdminReportActivity;
import com.example.cinemabookingapp.ui.features.admin.room.AdminRoomListActivity;
import com.example.cinemabookingapp.ui.features.admin.showtime.AdminShowtimeListActivity;
import com.example.cinemabookingapp.ui.features.admin.user.AdminUserManagementActivity;
import com.example.cinemabookingapp.ui.features.admin.widget.AdminHorizontalBarChartView;
import com.example.cinemabookingapp.ui.features.admin.widget.AdminLineChartView;
import com.example.cinemabookingapp.ui.features.admin.cineshop.AdminCineShopListActivity;
import com.example.cinemabookingapp.ui.features.admin.notification.AdminSendNotificationActivity;
import com.example.cinemabookingapp.ui.features.admin.chat.AdminCustomerChatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";

    private TextView tvMoviesCount;
    private TextView tvShowtimesCount;
    private TextView tvUsersCount;
    private TextView tvRevenueCount;

    private TextView tvAdminGreeting, tvAdminName;
    private android.widget.ImageView imgAdminAvatar;
    private TextView tvAdminAvatarInitials;

    private MovieRepositoryImpl movieRepository;
    private UserRepositoryImpl userRepository;
    private BookingRepositoryImpl bookingRepository;
    private ShowtimeRepositoryImpl showtimeRepository;

    private AdminLineChartView lineChartUsers;
    private AdminHorizontalBarChartView barChartMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvMoviesCount = findViewById(R.id.tvMoviesCount);
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);
        tvAdminName = findViewById(R.id.tvAdminName);
        imgAdminAvatar = findViewById(R.id.imgAdminAvatar);
        tvAdminAvatarInitials = findViewById(R.id.tvAdminAvatarInitials);
        tvShowtimesCount = findViewById(R.id.tvShowtimesCount);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvRevenueCount = findViewById(R.id.tvRevenueCount);

        // Initialize Repositories
        movieRepository = new MovieRepositoryImpl(new MovieRemoteDataSource());
        userRepository = new UserRepositoryImpl();
        bookingRepository = new BookingRepositoryImpl();
        showtimeRepository = new ShowtimeRepositoryImpl();

        lineChartUsers = findViewById(R.id.lineChartUsers);
        barChartMovies = findViewById(R.id.barChartMovies);

        // Group 1: Cinema & Schedule
        RecyclerView rvCinema = findViewById(R.id.rvAdminGroupCinema);
        rvCinema.setLayoutManager(new GridLayoutManager(this, 2));
        rvCinema.setAdapter(new AdminFeatureAdapter(this, createCinemaFeatures()));

        // Group 2: Movies & Commerce
        RecyclerView rvMovies = findViewById(R.id.rvAdminGroupMovies);
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        rvMovies.setAdapter(new AdminFeatureAdapter(this, createMovieFeatures()));

        // Group 3: Operations & Accounts
        RecyclerView rvOperations = findViewById(R.id.rvAdminGroupOperations);
        rvOperations.setLayoutManager(new GridLayoutManager(this, 2));
        rvOperations.setAdapter(new AdminFeatureAdapter(this, createOperationFeatures()));

        loadRealStats();
        runShowtimeMigration();

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminProfile();
    }

    private void loadAdminProfile() {
        com.example.cinemabookingapp.data.remote.api.ProfileApiService profileApi = 
                com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance().create(com.example.cinemabookingapp.data.remote.api.ProfileApiService.class);
        profileApi.getMyProfile().enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<User>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<User>> call, retrofit2.Response<com.example.cinemabookingapp.data.dto.ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    User user = response.body().getData();
                    bindAdminProfile(user);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "Error loading admin profile: " + t.getMessage());
            }
        });
    }

    private void bindAdminProfile(User user) {
        if (user == null) return;
        
        String roleStr = "Quản trị viên";
        if ("staff".equalsIgnoreCase(user.role)) {
            roleStr = "Nhân viên";
        }
        String name = user.name != null && !user.name.isEmpty() ? user.name : "";
        tvAdminGreeting.setText("Xin chào, " + roleStr);
        tvAdminName.setText(name);

        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            imgAdminAvatar.setVisibility(android.view.View.VISIBLE);
            tvAdminAvatarInitials.setVisibility(android.view.View.GONE);
            com.bumptech.glide.Glide.with(this)
                    .load(user.avatarUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .circleCrop()
                    .placeholder(R.drawable.user_solid_full)
                    .into(imgAdminAvatar);
        } else {
            imgAdminAvatar.setVisibility(android.view.View.GONE);
            tvAdminAvatarInitials.setVisibility(android.view.View.VISIBLE);
            
            String initials = "AD";
            if (user.name != null && !user.name.trim().isEmpty()) {
                String[] parts = user.name.trim().split("\\s+");
                if (parts.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(parts.length, 3); i++) {
                        if (!parts[i].isEmpty()) {
                            sb.append(parts[i].toUpperCase().charAt(0));
                        }
                    }
                    initials = sb.toString();
                }
            }
            tvAdminAvatarInitials.setText(initials);
        }
    }

    private void loadRealStats() {
        tvMoviesCount.setText("...");
        tvShowtimesCount.setText("...");
        tvUsersCount.setText("...");
        tvRevenueCount.setText("...");

        // Load Movies
        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (movies != null) {
                    int count = 0;
                    for (Movie m : movies) {
                        if (!m.deleted) count++;
                    }
                    tvMoviesCount.setText(String.valueOf(count));
                    Log.d(TAG, "Fetched " + count + " active movies successfully.");
                } else {
                    tvMoviesCount.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching movies: " + message);
                tvMoviesCount.setText("Lỗi");
            }
        });

        // Load Showtimes
        showtimeRepository.getAllShowtimes(new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(List<Showtime> showtimes) {
                if (showtimes != null) {
                    int count = 0;
                    for (Showtime s : showtimes) {
                        if (!s.deleted) count++;
                    }
                    tvShowtimesCount.setText(String.valueOf(count));
                    Log.d(TAG, "Fetched " + count + " active showtimes successfully.");
                } else {
                    tvShowtimesCount.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching showtimes: " + message);
                tvShowtimesCount.setText("Lỗi");
            }
        });

        // Load Users
        userRepository.getAllUsers(new ResultCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null) {
                    int count = 0;
                    long now = System.currentTimeMillis();
                    long oneDay = 24L * 60 * 60 * 1000;
                    float[] dayCounts = new float[7];

                    for (User u : users) {
                        if (!u.deleted) {
                            count++;
                            long diff = now - u.createdAt;
                            int daysAgo = (int) (diff / oneDay);
                            if (daysAgo >= 0 && daysAgo < 7) {
                                dayCounts[6 - daysAgo]++;
                            }
                        }
                    }
                    tvUsersCount.setText(String.valueOf(count));
                    Log.d(TAG, "Fetched " + count + " active users successfully.");

                    List<Float> lineData = new ArrayList<>();
                    List<String> lineLabels = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    for (int i = 0; i < 7; i++) {
                        lineData.add(dayCounts[i]);
                        long dayTime = now - ((6 - i) * oneDay);
                        lineLabels.add(sdf.format(new Date(dayTime)));
                    }
                    if (lineChartUsers != null) {
                        lineChartUsers.setData(lineData, lineLabels);
                    }
                } else {
                    tvUsersCount.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching users: " + message);
                tvUsersCount.setText("Lỗi");
            }
        });

        // Load Bookings
        bookingRepository.getAllBookings(new ResultCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (bookings != null) {
                    int activeBookings = 0;
                    double totalRevenue = 0;
                    Map<String, Integer> movieSales = new HashMap<>();

                    for (Booking b : bookings) {
                        if (!b.deleted) {
                            activeBookings++;
                            // Sum paid/confirmed bookings for revenue
                            if ("confirmed".equalsIgnoreCase(b.bookingStatus) || "paid".equalsIgnoreCase(b.paymentStatus) || "completed".equalsIgnoreCase(b.paymentStatus)) {
                                totalRevenue += b.total;
                                int tickets = (b.seatCodes != null) ? b.seatCodes.size() : 1;
                                String title = (b.movieTitleSnapshot != null && !b.movieTitleSnapshot.isEmpty()) ? b.movieTitleSnapshot : "Khác";
                                movieSales.put(title, movieSales.getOrDefault(title, 0) + tickets);
                            }
                        }
                    }
                    NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    String formattedRevenue = nf.format(totalRevenue) + " đ";
                    tvRevenueCount.setText(formattedRevenue);
                    Log.d(TAG, "Fetched " + activeBookings + " active bookings successfully. Total Revenue: " + totalRevenue);

                    List<Map.Entry<String, Integer>> list = new ArrayList<>(movieSales.entrySet());
                    list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                    List<String> barLabels = new ArrayList<>();
                    List<Float> barValues = new ArrayList<>();
                    for (int i = 0; i < Math.min(5, list.size()); i++) {
                        barLabels.add(list.get(i).getKey());
                        barValues.add((float) list.get(i).getValue());
                    }
                    if (barChartMovies != null) {
                        barChartMovies.setData(barLabels, barValues);
                    }
                } else {
                    tvRevenueCount.setText("0 đ");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching bookings: " + message);
                tvRevenueCount.setText("Lỗi");
            }
        });
    }

    private List<AdminFeatureItem> createCinemaFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Rạp chiếu", "Quản lý hệ thống rạp", R.drawable.house_user_solid_full, AdminCinemaListActivity.class));
        items.add(new AdminFeatureItem("Phòng chiếu", "Quản lý phòng chiếu", R.drawable.glasses_solid_full, AdminRoomListActivity.class));
        items.add(new AdminFeatureItem("Ghế mẫu", "Cấu hình sơ đồ ghế", R.drawable.glasses_solid_full, AdminRoomListActivity.class));
        items.add(new AdminFeatureItem("Suất chiếu", "Tạo và cập nhật lịch chiếu", R.drawable.calendar_days_solid_full, AdminShowtimeListActivity.class));
        return items;
    }

    private List<AdminFeatureItem> createMovieFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Phim", "Quản lý danh sách phim", R.drawable.clapperboard_solid_full, AdminMovieListActivity.class));
        items.add(new AdminFeatureItem("CineShop", "Quản lý sản phẩm, combos", R.drawable.cart_shopping_solid_full, AdminCineShopListActivity.class));
        items.add(new AdminFeatureItem("Duyệt thanh toán", "Xử lý chuyển khoản & MoMo", R.drawable.clipboard_solid_full, AdminPaymentListActivity.class));
        return items;
    }

    private List<AdminFeatureItem> createOperationFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Khách hàng", "Quản lý customer", R.drawable.user_solid_full, AdminUserManagementActivity.class));
        items.add(new AdminFeatureItem("Khuyến mãi", "Quản lý promotion", R.drawable.tag_solid_full, AdminPromotionListActivity.class));
        items.add(new AdminFeatureItem("Báo cáo", "Thống kê doanh thu", R.drawable.chart_line_solid_full, AdminReportActivity.class));
        items.add(new AdminFeatureItem("Nhật ký", "Audit log hệ thống", R.drawable.clipboard_solid_full, AdminAuditLogActivity.class));
        items.add(new AdminFeatureItem("Thông báo", "Gửi thông báo", R.drawable.ic_notification, AdminSendNotificationActivity.class));
        items.add(new AdminFeatureItem("CSKH", "Hỗ trợ chat khách hàng", R.drawable.ic_headset_support, AdminCustomerChatActivity.class));
        return items;
    }

    private void setupBottomNavigation() {
        AdminBottomNavHelper.setupAdminBottomNavigation(this, 0);
    }

    private void runShowtimeMigration() {
        android.content.SharedPreferences prefs = getSharedPreferences("app_migrations", MODE_PRIVATE);
        if (prefs.getBoolean("migrated_rooms_to_2d_v2", false)) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("rooms").get().addOnSuccessListener(roomSnapshots -> {
            List<com.example.cinemabookingapp.domain.model.Room> allRooms = new ArrayList<>();
            for (DocumentSnapshot doc : roomSnapshots.getDocuments()) {
                com.example.cinemabookingapp.domain.model.Room r = doc.toObject(com.example.cinemabookingapp.domain.model.Room.class);
                if (r != null) {
                    r.roomId = doc.getId();
                    allRooms.add(r);
                }
            }

            java.util.Map<String, String> oldToNewRoomIdMap = new HashMap<>();
            List<com.example.cinemabookingapp.domain.model.Room> activeRooms = new ArrayList<>();
            List<com.example.cinemabookingapp.domain.model.Room> deletedRooms = new ArrayList<>();
            for (com.example.cinemabookingapp.domain.model.Room r : allRooms) {
                if (r.deleted) {
                    deletedRooms.add(r);
                } else {
                    activeRooms.add(r);
                }
            }

            for (com.example.cinemabookingapp.domain.model.Room oldRoom : deletedRooms) {
                if (oldRoom.name == null) continue;
                String name = oldRoom.name.trim();
                for (com.example.cinemabookingapp.domain.model.Room newRoom : activeRooms) {
                    if (newRoom.name != null && newRoom.name.trim().equalsIgnoreCase(name)) {
                        oldToNewRoomIdMap.put(oldRoom.roomId, newRoom.roomId);
                        Log.d(TAG, "Migration mapping: Old " + oldRoom.name + " (" + oldRoom.roomId + ") -> New " + newRoom.name + " (" + newRoom.roomId + ")");
                        break;
                    }
                }
            }

            if (oldToNewRoomIdMap.isEmpty()) {
                Log.d(TAG, "No deleted rooms matched active rooms by name.");
                return;
            }

            db.collection("showtimes").get().addOnSuccessListener(showtimeSnapshots -> {
                int[] updateCount = {0};
                int[] totalChecked = {0};
                int totalShowtimes = showtimeSnapshots.size();

                if (totalShowtimes == 0) return;

                for (DocumentSnapshot doc : showtimeSnapshots.getDocuments()) {
                    String sId = doc.getId();
                    String currentRoomId = doc.getString("roomId");

                    if (currentRoomId != null && oldToNewRoomIdMap.containsKey(currentRoomId)) {
                        String newRoomId = oldToNewRoomIdMap.get(currentRoomId);

                        db.collection("showtimes").document(sId)
                            .update("roomId", newRoomId)
                            .addOnSuccessListener(aVoid -> {
                                updateCount[0]++;
                                totalChecked[0]++;
                                Log.d(TAG, "Successfully updated showtime " + sId + " to new roomId " + newRoomId);
                                if (totalChecked[0] == totalShowtimes || updateCount[0] > 0) {
                                    showMigrationResultToast(updateCount[0]);
                                }
                            })
                            .addOnFailureListener(e -> {
                                totalChecked[0]++;
                                Log.e(TAG, "Failed to update showtime " + sId + ": " + e.getMessage());
                            });
                    } else {
                        totalChecked[0]++;
                    }
                }

                prefs.edit().putBoolean("migrated_rooms_to_2d_v2", true).apply();
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Migration room fetch failed: " + e.getMessage());
        });
    }

    private void showMigrationResultToast(int count) {
        runOnUiThread(() -> {
            android.widget.Toast.makeText(this, "Đã tự động cập nhật " + count + " suất chiếu sang các phòng 2D mới!", android.widget.Toast.LENGTH_LONG).show();
        });
    }
}