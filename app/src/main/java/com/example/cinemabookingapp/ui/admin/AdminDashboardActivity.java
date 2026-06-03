package com.example.cinemabookingapp.ui.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.data.repository.UserRepositoryImpl;
import com.example.cinemabookingapp.data.repository.BookingRepositoryImpl;
import com.example.cinemabookingapp.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.model.Showtime;
import com.example.cinemabookingapp.ui.admin.adapter.AdminFeatureAdapter;
import com.example.cinemabookingapp.ui.admin.model.AdminFeatureItem;
import com.example.cinemabookingapp.ui.admin.movie.AdminMovieListActivity;
import com.example.cinemabookingapp.ui.admin.cinema.AdminCinemaListActivity;
import com.example.cinemabookingapp.ui.admin.room.AdminRoomListActivity;
import com.example.cinemabookingapp.ui.admin.showtime.AdminShowtimeListActivity;
import com.example.cinemabookingapp.ui.admin.user.AdminUserManagementActivity;
import com.example.cinemabookingapp.ui.admin.promotion.AdminPromotionListActivity;
import com.example.cinemabookingapp.ui.admin.report.AdminReportActivity;
import com.example.cinemabookingapp.ui.admin.log.AdminAuditLogActivity;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.service.ProfileService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.example.cinemabookingapp.ui.admin.widget.AdminLineChartView;
import com.example.cinemabookingapp.ui.admin.widget.AdminHorizontalBarChartView;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";

    private TextView tvMoviesCount;
    private TextView tvShowtimesCount;
    private TextView tvUsersCount;
    private TextView tvRevenueCount;

    private MovieRepositoryImpl movieRepository;
    private UserRepositoryImpl userRepository;
    private BookingRepositoryImpl bookingRepository;
    private ShowtimeRepositoryImpl showtimeRepository;

    private AdminLineChartView lineChartUsers;
    private AdminHorizontalBarChartView barChartMovies;

    // View declarations for bottom navigation tab switching
    private TextView tvAdminGreeting;
    private ImageView adminProfileAvatar;
    private TextView adminProfileName;
    private TextView adminProfileEmail;

    private androidx.core.widget.NestedScrollView adminScrollDashboard;
    private androidx.core.widget.NestedScrollView adminScrollCinemaSchedule;
    private androidx.core.widget.NestedScrollView adminScrollOperations;
    private android.widget.ScrollView adminScrollProfile;

    private com.google.android.material.card.MaterialCardView adminNavHomeCard;
    private com.google.android.material.card.MaterialCardView adminNavCinemaCard;
    private com.google.android.material.card.MaterialCardView adminNavOpsCard;
    private com.google.android.material.card.MaterialCardView adminNavProfileCard;

    private TextView adminNavHomeLabel;
    private TextView adminNavCinemaLabel;
    private TextView adminNavOpsLabel;
    private TextView adminNavProfileLabel;

    private ImageView adminNavHomeIcon;
    private ImageView adminNavCinemaIcon;
    private ImageView adminNavOpsIcon;
    private ImageView adminNavProfileIcon;

    private final int activeColor = Color.parseColor("#1E1A23");
    private final int inactiveTint = Color.parseColor("#4A4650");
    private final int activeTint = Color.WHITE;

    private ProfileService profileService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Stats indicators
        tvMoviesCount = findViewById(R.id.tvMoviesCount);
        tvShowtimesCount = findViewById(R.id.tvShowtimesCount);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvRevenueCount = findViewById(R.id.tvRevenueCount);

        // Welcome / Greeting
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);

        // Profile components
        adminProfileAvatar = findViewById(R.id.admin_profile_avatar);
        adminProfileName = findViewById(R.id.admin_profile_name);
        adminProfileEmail = findViewById(R.id.admin_profile_email);

        // Scroll containers for each tab
        adminScrollDashboard = findViewById(R.id.adminScrollDashboard);
        adminScrollCinemaSchedule = findViewById(R.id.adminScrollCinemaSchedule);
        adminScrollOperations = findViewById(R.id.adminScrollOperations);
        adminScrollProfile = findViewById(R.id.adminScrollProfile);

        // Tab selection cards
        adminNavHomeCard = findViewById(R.id.adminNavHomeCard);
        adminNavCinemaCard = findViewById(R.id.adminNavCinemaCard);
        adminNavOpsCard = findViewById(R.id.adminNavOpsCard);
        adminNavProfileCard = findViewById(R.id.adminNavProfileCard);

        // Tab selection labels
        adminNavHomeLabel = findViewById(R.id.adminNavHomeLabel);
        adminNavCinemaLabel = findViewById(R.id.adminNavCinemaLabel);
        adminNavOpsLabel = findViewById(R.id.adminNavOpsLabel);
        adminNavProfileLabel = findViewById(R.id.adminNavProfileLabel);

        // Tab selection icons
        adminNavHomeIcon = findViewById(R.id.adminNavHomeIcon);
        adminNavCinemaIcon = findViewById(R.id.adminNavCinemaIcon);
        adminNavOpsIcon = findViewById(R.id.adminNavOpsIcon);
        adminNavProfileIcon = findViewById(R.id.adminNavProfileIcon);

        // Initialize Services & Repositories
        profileService = ServiceProvider.getInstance(getApplicationContext()).getProfileService();
        movieRepository = new MovieRepositoryImpl(new MovieRemoteDataSource());
        userRepository = new UserRepositoryImpl();
        bookingRepository = new BookingRepositoryImpl();
        showtimeRepository = new ShowtimeRepositoryImpl();

        lineChartUsers = findViewById(R.id.lineChartUsers);
        barChartMovies = findViewById(R.id.barChartMovies);

        // Group 1: Cinema & Schedule Recycler
        RecyclerView rvCinema = findViewById(R.id.rvAdminGroupCinema);
        rvCinema.setLayoutManager(new GridLayoutManager(this, 2));
        rvCinema.setAdapter(new AdminFeatureAdapter(this, createCinemaFeatures()));

        // Group 2: Movies & Commerce Recycler
        RecyclerView rvMovies = findViewById(R.id.rvAdminGroupMovies);
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        rvMovies.setAdapter(new AdminFeatureAdapter(this, createMovieFeatures()));

        // Group 3: Operations & Accounts Recycler
        RecyclerView rvOperations = findViewById(R.id.rvAdminGroupOperations);
        rvOperations.setLayoutManager(new GridLayoutManager(this, 2));
        rvOperations.setAdapter(new AdminFeatureAdapter(this, createOperationFeatures()));

        loadRealStats();

        // Bottom nav click handlers
        adminNavHomeCard.setOnClickListener(v -> showTab(0));
        adminNavCinemaCard.setOnClickListener(v -> showTab(1));
        adminNavOpsCard.setOnClickListener(v -> showTab(2));
        adminNavProfileCard.setOnClickListener(v -> showTab(3));

        // Profile Tab Option Clicks
        View menuEditProfile = findViewById(R.id.admin_menu_edit_profile);
        View menuChangePassword = findViewById(R.id.admin_menu_change_password);
        View menuSettings = findViewById(R.id.admin_menu_settings);
        View menuLogout = findViewById(R.id.admin_menu_logout);

        menuEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.cinemabookingapp.ui.customer.profile.EditProfileActivity.class);
            startActivity(intent);
        });

        menuChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        menuSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Cài đặt hệ thống đang được cập nhật", Toast.LENGTH_SHORT).show();
        });

        menuLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        ServiceProvider.getInstance(getApplicationContext()).getAuthenticationService().logOut();
                        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                        AppNavigator.goToLogin(this);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProfileData();
    }

    private void loadProfileData() {
        // Load immediate cached profile to avoid flickering
        User cached = profileService.getCachedProfile();
        if (cached != null) {
            displayProfile(cached);
        }

        profileService.getUserProfile(new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    displayProfile(user);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching user profile: " + message);
            }
        });
    }

    private void displayProfile(User user) {
        String displayName = (user.name == null || user.name.isBlank()) ? user.email : user.name;
        adminProfileName.setText(displayName);
        adminProfileEmail.setText(user.email);
        tvAdminGreeting.setText("Xin chào, " + (user.name != null && !user.name.isBlank() ? user.name : "Quản trị viên"));
        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(AdminDashboardActivity.this)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.user_solid_full)
                    .circleCrop()
                    .into(adminProfileAvatar);
        } else {
            adminProfileAvatar.setImageResource(R.drawable.user_solid_full);
        }
    }

    private void showTab(int index) {
        adminScrollDashboard.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        adminScrollCinemaSchedule.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        adminScrollOperations.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        adminScrollProfile.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        applyAdminBottomNavState(index);
    }

    private void applyAdminBottomNavState(int index) {
        applyAdminBottomState(adminNavHomeCard, adminNavHomeLabel, adminNavHomeIcon, index == 0, "Tổng quan");
        applyAdminBottomState(adminNavCinemaCard, adminNavCinemaLabel, adminNavCinemaIcon, index == 1, "Rạp & Lịch");
        applyAdminBottomState(adminNavOpsCard, adminNavOpsLabel, adminNavOpsIcon, index == 2, "Vận hành");
        applyAdminBottomState(adminNavProfileCard, adminNavProfileLabel, adminNavProfileIcon, index == 3, "Cá nhân");
        findViewById(R.id.adminBottomNavContainer).requestLayout();
    }

    private void applyAdminBottomState(com.google.android.material.card.MaterialCardView card, TextView label, ImageView icon, boolean selected, String text) {
        android.widget.LinearLayout.LayoutParams params;
        if (selected) {
            params = new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(activeColor);
            card.setStrokeWidth(0);
            label.setText(text);
            label.setVisibility(View.VISIBLE);
            androidx.core.widget.ImageViewCompat.setImageTintList(icon, android.content.res.ColorStateList.valueOf(activeTint));
            label.setTextColor(activeTint);
            card.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start();
        } else {
            params = new android.widget.LinearLayout.LayoutParams(0, dp(48), 0.8f);
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(Color.WHITE);
            card.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#D3CAD7")));
            card.setStrokeWidth(dp(1));
            label.setVisibility(View.GONE);
            androidx.core.widget.ImageViewCompat.setImageTintList(icon, android.content.res.ColorStateList.valueOf(inactiveTint));
            card.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
        }
        card.setLayoutParams(params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        com.google.android.material.textfield.TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        com.google.android.material.textfield.TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmPassword.getText().toString();

                    if (android.text.TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                        Toast.makeText(this, "Mật khẩu mới phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ServiceProvider.getInstance(getApplicationContext()).getAuthenticationService().updatePassword(newPass)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
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
        items.add(new AdminFeatureItem("CineShop", "Quản lý sản phẩm, combos", R.drawable.cart_shopping_solid_full, null));
        items.add(new AdminFeatureItem("Duyệt thanh toán", "Xử lý chuyển khoản & MoMo", R.drawable.clipboard_solid_full, AdminPaymentListActivity.class));
        return items;
    }

    private List<AdminFeatureItem> createOperationFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Người dùng", "Quản lý customer/staff/admin", R.drawable.user_solid_full, AdminUserManagementActivity.class));
        items.add(new AdminFeatureItem("Đơn hàng/Vé đặt", "Tra cứu đơn hàng, hóa đơn", R.drawable.clipboard_solid_full, com.example.cinemabookingapp.ui.staff.StaffSearchBookingActivity.class));
        items.add(new AdminFeatureItem("Khuyến mãi", "Quản lý promotion", R.drawable.tag_solid_full, AdminPromotionListActivity.class));
        items.add(new AdminFeatureItem("Báo cáo", "Thống kê doanh thu", R.drawable.chart_line_solid_full, AdminReportActivity.class));
        items.add(new AdminFeatureItem("Nhật ký", "Audit log hệ thống", R.drawable.clipboard_solid_full, AdminAuditLogActivity.class));
        return items;
    }
}