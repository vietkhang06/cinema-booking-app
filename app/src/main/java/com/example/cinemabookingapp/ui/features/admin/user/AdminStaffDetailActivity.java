package com.example.cinemabookingapp.ui.features.admin.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.AttendanceDTO;
import com.example.cinemabookingapp.data.dto.UserDTO;
import com.example.cinemabookingapp.data.dto.ViolationDTO;
import com.example.cinemabookingapp.data.mapper.AttendanceMapper;
import com.example.cinemabookingapp.data.mapper.ViolationMapper;
import com.example.cinemabookingapp.data.remote.api.AdminStaffApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.domain.model.Attendance;
import com.example.cinemabookingapp.domain.model.Violation;
import com.example.cinemabookingapp.ui.features.admin.user.adapter.AdminAttendanceAdapter;
import com.example.cinemabookingapp.ui.features.admin.user.adapter.AdminViolationAdapter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStaffDetailActivity extends BaseActivity {

    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvPhone, tvCinema, tvStatus;
    private TextView tvStatLate, tvStatViolations, tvStatOnTime, tvStatPenalty;
    private View btnEdit, btnResetPassword, btnAddViolation, btnLock;
    private ImageView ivLockIcon;
    private TextView tvLockLabel;

    private MaterialButtonToggleGroup toggleGroup;
    private RecyclerView rvAttendance, rvViolations;
    private TextView tvLogsEmpty;

    private View gridLayoutStats;
    private View cardAdminDetails;
    private TextView tvAdminEmail, tvAdminCreatedDate, tvAdminLoginCount, tvAdminStatus;
    private TextView tvViolationWarning;

    private AdminStaffApiService adminStaffApi;
    private String staffId;
    private UserDTO staffUser;

    private AdminAttendanceAdapter attendanceAdapter;
    private AdminViolationAdapter violationAdapter;

    private final List<Attendance> attendanceList = new ArrayList<>();
    private final List<Violation> violationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_staff_detail);

        adminStaffApi = RetrofitClient.getInstance().create(AdminStaffApiService.class);
        staffId = getIntent().getStringExtra("staff_id");

        if (staffId == null) {
            showToast("Không tìm thấy mã nhân viên!");
            finish();
            return;
        }

        initViews();
        setupRecyclerViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStaffProfile();
        loadAttendanceHistory();
        loadViolationHistory();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivDetailAvatar);
        tvName = findViewById(R.id.tvDetailName);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvPhone = findViewById(R.id.tvDetailPhone);
        tvCinema = findViewById(R.id.tvDetailCinema);
        tvStatus = findViewById(R.id.tvDetailStatus);

        tvStatLate = findViewById(R.id.tvStatLate);
        tvStatViolations = findViewById(R.id.tvStatViolations);
        tvStatOnTime = findViewById(R.id.tvStatOnTime);
        tvStatPenalty = findViewById(R.id.tvStatPenalty);

        btnEdit = findViewById(R.id.btnDetailEdit);
        btnResetPassword = findViewById(R.id.btnDetailResetPassword);
        btnAddViolation = findViewById(R.id.btnDetailAddViolation);
        btnLock = findViewById(R.id.btnDetailLock);
        ivLockIcon = findViewById(R.id.ivDetailLockIcon);
        tvLockLabel = findViewById(R.id.tvDetailLockLabel);

        toggleGroup = findViewById(R.id.toggleLogsGroup);
        rvAttendance = findViewById(R.id.rvDetailAttendance);
        rvViolations = findViewById(R.id.rvDetailViolations);
        tvLogsEmpty = findViewById(R.id.tvLogsEmpty);

        gridLayoutStats = findViewById(R.id.gridLayoutStats);
        cardAdminDetails = findViewById(R.id.cardAdminDetails);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvAdminCreatedDate = findViewById(R.id.tvAdminCreatedDate);
        tvAdminLoginCount = findViewById(R.id.tvAdminLoginCount);
        tvAdminStatus = findViewById(R.id.tvAdminStatus);
        tvViolationWarning = findViewById(R.id.tvViolationWarning);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerViews() {
        attendanceAdapter = new AdminAttendanceAdapter();
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        rvAttendance.setAdapter(attendanceAdapter);

        violationAdapter = new AdminViolationAdapter();
        violationAdapter.setListener(v -> {
            // Edit violation dialog
            showEditViolationDialog(v);
        });
        rvViolations.setLayoutManager(new LinearLayoutManager(this));
        rvViolations.setAdapter(violationAdapter);
    }

    private void bindActions() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminStaffFormActivity.class);
            intent.putExtra("staff_id", staffId);
            startActivity(intent);
        });

        btnResetPassword.setOnClickListener(v -> showResetPasswordDialog());

        btnAddViolation.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminViolationFormActivity.class);
            intent.putExtra("staff_id", staffId);
            startActivity(intent);
        });

        btnLock.setOnClickListener(v -> toggleStaffLockStatus());

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabAttendance) {
                    rvAttendance.setVisibility(View.VISIBLE);
                    rvViolations.setVisibility(View.GONE);
                    updateEmptyLogsState(attendanceList.isEmpty());
                } else if (checkedId == R.id.btnTabViolations) {
                    rvAttendance.setVisibility(View.GONE);
                    rvViolations.setVisibility(View.VISIBLE);
                    updateEmptyLogsState(violationList.isEmpty());
                }
            }
        });
    }

    private void updateEmptyLogsState(boolean isEmpty) {
        tvLogsEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void loadStaffProfile() {
        adminStaffApi.getStaffDetail(staffId).enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    staffUser = response.body().getData();
                    bindProfile(staffUser);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                showToast("Lỗi tải thông tin nhân viên");
            }
        });
    }

    private void bindProfile(UserDTO user) {
        tvName.setText(user.name != null ? user.name : "N/A");
        tvEmail.setText(user.email != null ? user.email : "N/A");
        tvPhone.setText("SĐT: " + (user.phone != null ? user.phone : "Chưa cập nhật"));
        tvCinema.setText("Chi nhánh: " + (user.cinemaName != null ? user.cinemaName : "Chưa phân công"));

        if ("active".equalsIgnoreCase(user.status)) {
            tvStatus.setText("HOẠT ĐỘNG");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
            tvStatus.setBackgroundResource(R.drawable.bg_status_active_pill);
            tvLockLabel.setText("Vô hiệu hóa");
            tvLockLabel.setTextColor(android.graphics.Color.parseColor("#E53935"));
            ivLockIcon.setColorFilter(android.graphics.Color.parseColor("#E53935"));
        } else {
            tvStatus.setText("TẠM KHÓA");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#E53935"));
            tvStatus.setBackgroundResource(R.drawable.bg_status_inactive_pill);
            tvLockLabel.setText("Kích hoạt");
            tvLockLabel.setTextColor(android.graphics.Color.parseColor("#10B981"));
            ivLockIcon.setColorFilter(android.graphics.Color.parseColor("#10B981"));
        }

        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.user_solid_full)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.user_solid_full);
        }

        // Reset warning and button by default
        tvViolationWarning.setVisibility(View.GONE);
        btnAddViolation.setVisibility(View.VISIBLE);
        btnAddViolation.setEnabled(true);
        btnAddViolation.setAlpha(1.0f);

        String currentUserId = sessionManager.getUserId();

        if ("admin".equalsIgnoreCase(user.role)) {
            // Target is ADMIN: hide stats grid, show admin details
            gridLayoutStats.setVisibility(View.GONE);
            toggleGroup.setVisibility(View.GONE);
            rvAttendance.setVisibility(View.GONE);
            rvViolations.setVisibility(View.GONE);
            tvLogsEmpty.setVisibility(View.GONE);
            cardAdminDetails.setVisibility(View.VISIBLE);

            tvAdminEmail.setText("Email: " + (user.email != null ? user.email : "N/A"));
            tvAdminCreatedDate.setText("Ngày tạo tài khoản: " + (user.createdAt > 0 
                    ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(user.createdAt)) 
                    : "N/A"));
            tvAdminLoginCount.setText("Số lần đăng nhập: " + (user.loginCount != null ? user.loginCount : 0));
            tvAdminStatus.setText("Trạng thái: " + ("active".equalsIgnoreCase(user.status) ? "Hoạt động" : "Tạm khóa"));


        } else {
            // Target is STAFF: show stats grid, hide admin details
            gridLayoutStats.setVisibility(View.VISIBLE);
            toggleGroup.setVisibility(View.VISIBLE);
            cardAdminDetails.setVisibility(View.GONE);

            // Re-apply correct list visibility based on active tab
            if (toggleGroup.getCheckedButtonId() == R.id.btnTabAttendance) {
                rvAttendance.setVisibility(View.VISIBLE);
                rvViolations.setVisibility(View.GONE);
                updateEmptyLogsState(attendanceList.isEmpty());
            } else {
                rvAttendance.setVisibility(View.GONE);
                rvViolations.setVisibility(View.VISIBLE);
                updateEmptyLogsState(violationList.isEmpty());
            }
        }
    }

    private void loadAttendanceHistory() {
        adminStaffApi.getAllAttendances(staffId, null, null).enqueue(new Callback<ApiResponse<List<AttendanceDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceDTO>>> call, Response<ApiResponse<List<AttendanceDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    attendanceList.clear();
                    List<AttendanceDTO> dtoList = response.body().getData();
                    if (dtoList != null) {
                        for (AttendanceDTO dto : dtoList) {
                            attendanceList.add(AttendanceMapper.toDomain(dto));
                        }
                    }
                    attendanceAdapter.submitList(attendanceList);
                    if (toggleGroup.getCheckedButtonId() == R.id.btnTabAttendance) {
                        updateEmptyLogsState(attendanceList.isEmpty());
                    }
                    computeStats();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceDTO>>> call, Throwable t) {
                showToast("Lỗi kết nối lịch sử điểm danh");
            }
        });
    }

    private void loadViolationHistory() {
        adminStaffApi.getAllViolations(staffId, null, null).enqueue(new Callback<ApiResponse<List<ViolationDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ViolationDTO>>> call, Response<ApiResponse<List<ViolationDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    violationList.clear();
                    List<ViolationDTO> dtoList = response.body().getData();
                    if (dtoList != null) {
                        for (ViolationDTO dto : dtoList) {
                            violationList.add(ViolationMapper.toDomain(dto));
                        }
                    }
                    violationAdapter.submitList(violationList);
                    if (toggleGroup.getCheckedButtonId() == R.id.btnTabViolations) {
                        updateEmptyLogsState(violationList.isEmpty());
                    }
                    computeStats();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ViolationDTO>>> call, Throwable t) {
                showToast("Lỗi kết nối nhật ký vi phạm");
            }
        });
    }

    private void computeStats() {
        if (staffUser != null && "admin".equalsIgnoreCase(staffUser.role)) {
            return;
        }
        int lateCount = 0;
        int onTimeCount = 0;
        int activeViolations = 0;
        double totalFine = 0;

        for (Attendance a : attendanceList) {
            String s = a.status != null ? a.status.toLowerCase() : "";
            if (s.contains("late")) {
                lateCount++;
            } else if ("completed".equals(s) || "present".equals(s)) {
                onTimeCount++;
            }
        }

        for (Violation v : violationList) {
            if ("PENDING".equals(v.status)) {
                activeViolations++;
                totalFine += v.penaltyAmount;
            }
        }

        tvStatLate.setText(String.valueOf(lateCount));
        tvStatOnTime.setText(String.valueOf(onTimeCount));
        tvStatViolations.setText(String.valueOf(activeViolations));

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvStatPenalty.setText(nf.format(totalFine) + "đ");
    }

    private void showResetPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        View etOld = dialogView.findViewById(R.id.etOldPassword);
        if (etOld != null && etOld.getParent() instanceof View) {
            ((View) etOld.getParent()).setVisibility(View.GONE); // Hide old password input
        }

        new AlertDialog.Builder(this)
                .setTitle("Cấp lại mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newPass = etNewPassword.getText().toString().trim();
                    String confirmPass = etConfirmPassword.getText().toString().trim();

                    if (newPass.length() < 6) {
                        showToast("Mật khẩu mới phải từ 6 ký tự");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        showToast("Xác nhận mật khẩu không khớp");
                        return;
                    }

                    adminStaffApi.resetPassword(staffId, newPass).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                showToast("Reset mật khẩu thành công!");
                            } else {
                                showToast("Reset mật khẩu thất bại: " + getErrorMessage(response));
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            showToast("Lỗi kết nối: " + t.getMessage());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void toggleStaffLockStatus() {
        if (staffUser == null) return;
        boolean isActive = "active".equalsIgnoreCase(staffUser.status);
        String actionStr = isActive ? "Khóa" : "Kích hoạt";

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Xác nhận")
                .setMessage("Bạn có chắc muốn " + actionStr.toLowerCase() + " tài khoản nhân viên này không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    staffUser.status = isActive ? "inactive" : "active";
                    adminStaffApi.updateStaff(staffId, staffUser).enqueue(new Callback<ApiResponse<UserDTO>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                showToast(isActive ? "Đã khóa tài khoản nhân viên" : "Kích hoạt tài khoản nhân viên thành công!");
                                loadStaffProfile();
                            } else {
                                showToast("Thao tác thất bại: " + getErrorMessage(response));
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                            showToast("Lỗi kết nối: " + t.getMessage());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditViolationDialog(Violation v) {
        String[] options = {"Đánh dấu Đã xử lý (RESOLVED)", "Đánh dấu Hủy bỏ (CANCELLED)", "Xóa vi phạm (Soft Delete)"};
        new AlertDialog.Builder(this)
                .setTitle("Xử lý vi phạm")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        updateViolationStatus(v, "RESOLVED");
                    } else if (which == 1) {
                        updateViolationStatus(v, "CANCELLED");
                    } else {
                        deleteViolationLog(v);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateViolationStatus(Violation v, String status) {
        ViolationDTO dto = ViolationMapper.toDTO(v);
        dto.status = status;
        adminStaffApi.updateViolation(v.id, dto).enqueue(new Callback<ApiResponse<ViolationDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ViolationDTO>> call, Response<ApiResponse<ViolationDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToast("Cập nhật trạng thái vi phạm thành công!");
                    loadViolationHistory();
                } else {
                    showToast("Lỗi cập nhật: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ViolationDTO>> call, Throwable t) {
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void deleteViolationLog(Violation v) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa vi phạm này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    adminStaffApi.deleteViolation(v.id).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                showToast("Đã xóa vi phạm");
                                loadViolationHistory();
                            } else {
                                showToast("Lỗi xóa vi phạm: " + getErrorMessage(response));
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            showToast("Lỗi kết nối: " + t.getMessage());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String getErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errStr = response.errorBody().string();
                if (errStr.contains("\"message\":\"")) {
                    int start = errStr.indexOf("\"message\":\"") + 11;
                    int end = errStr.indexOf("\"", start);
                    return errStr.substring(start, end);
                }
                return errStr;
            }
        } catch (Exception ignored) {}
        if (response.body() != null && response.body() instanceof ApiResponse) {
            ApiResponse<?> apiResp = (ApiResponse<?>) response.body();
            if (apiResp.getMessage() != null) {
                return apiResp.getMessage();
            }
        }
        return "Lỗi hệ thống (Code: " + response.code() + ")";
    }
}
