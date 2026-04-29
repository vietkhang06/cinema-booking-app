package com.example.cinemabookingapp.ui.admin;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
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

import com.example.cinemabookingapp.ui.admin.room.AdminSeatTemplateActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvMoviesCount;
    private TextView tvShowtimesCount;
    private TextView tvUsersCount;
    private TextView tvRevenueCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        ImageButton btnLogout = findViewById(R.id.btnAdminLogout);
        tvMoviesCount = findViewById(R.id.tvMoviesCount);
        tvShowtimesCount = findViewById(R.id.tvShowtimesCount);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvRevenueCount = findViewById(R.id.tvRevenueCount);

        RecyclerView rvFeatures = findViewById(R.id.rvAdminFeatures);
        rvFeatures.setLayoutManager(new GridLayoutManager(this, 2));
        rvFeatures.setAdapter(new AdminFeatureAdapter(this, createFeatures()));

        bindMockStats();

        btnLogout.setOnClickListener(v -> AppNavigator.goToLogin(this));
    }

    private void bindMockStats() {
        tvMoviesCount.setText("0");
        tvShowtimesCount.setText("0");
        tvUsersCount.setText("0");
        tvRevenueCount.setText("0");
    }

    private List<AdminFeatureItem> createFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Phim", "Quản lý danh sách phim", R.drawable.clapperboard_solid_full, AdminMovieListActivity.class));
        items.add(new AdminFeatureItem("Rạp", "Quản lý hệ thống rạp", R.drawable.house_user_solid_full, AdminCinemaListActivity.class));
        items.add(new AdminFeatureItem("Phòng", "Quản lý phòng chiếu", R.drawable.glasses_solid_full, AdminRoomListActivity.class));
        items.add(new AdminFeatureItem(
                "Ghế mẫu",
                "Cấu hình sơ đồ ghế",
                R.drawable.glasses_solid_full,   // hoặc icon có sẵn khác
                AdminSeatTemplateActivity.class
        ));
        items.add(new AdminFeatureItem("Suất chiếu", "Tạo và cập nhật lịch chiếu", R.drawable.calendar_days_solid_full, AdminShowtimeListActivity.class));
        items.add(new AdminFeatureItem("Người dùng", "Quản lý customer/staff/admin", R.drawable.user_solid_full, AdminUserManagementActivity.class));
        items.add(new AdminFeatureItem("Khuyến mãi", "Quản lý promotion", R.drawable.tag_solid_full, AdminPromotionListActivity.class));
        items.add(new AdminFeatureItem("Báo cáo", "Thống kê doanh thu", R.drawable.chart_line_solid_full, AdminReportActivity.class));
        items.add(new AdminFeatureItem("Nhật ký", "Audit log hệ thống", R.drawable.clipboard_solid_full, AdminAuditLogActivity.class));
        return items;
    }
}