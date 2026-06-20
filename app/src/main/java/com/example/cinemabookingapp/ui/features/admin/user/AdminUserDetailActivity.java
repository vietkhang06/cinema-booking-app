package com.example.cinemabookingapp.ui.features.admin.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.UserDTO;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.AdminUserApiService;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserDetailActivity extends BaseActivity {

    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvPhone, tvCinema, tvStatus;
    private TextView tvUserEmailDetail, tvUserRoleDetail, tvUserCreatedDate, tvUserLoginCount, tvUserStatusDetail;
    private View btnEdit, btnResetPassword, btnLock;
    private ImageView ivLockIcon;
    private TextView tvLockLabel;

    private AdminUserApiService adminUserApi;
    private String userId;
    private UserDTO targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        adminUserApi = RetrofitClient.getInstance().create(AdminUserApiService.class);
        userId = getIntent().getStringExtra("user_id");

        if (userId == null) {
            showToast("Không tìm thấy mã người dùng!");
            finish();
            return;
        }

        initViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivDetailAvatar);
        tvName = findViewById(R.id.tvDetailName);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvPhone = findViewById(R.id.tvDetailPhone);
        tvCinema = findViewById(R.id.tvDetailCinema);
        tvStatus = findViewById(R.id.tvDetailStatus);

        tvUserEmailDetail = findViewById(R.id.tvUserEmailDetail);
        tvUserRoleDetail = findViewById(R.id.tvUserRoleDetail);
        tvUserCreatedDate = findViewById(R.id.tvUserCreatedDate);
        tvUserLoginCount = findViewById(R.id.tvUserLoginCount);
        tvUserStatusDetail = findViewById(R.id.tvUserStatusDetail);

        btnEdit = findViewById(R.id.btnDetailEdit);
        btnResetPassword = findViewById(R.id.btnDetailResetPassword);
        btnLock = findViewById(R.id.btnDetailLock);
        ivLockIcon = findViewById(R.id.ivDetailLockIcon);
        tvLockLabel = findViewById(R.id.tvDetailLockLabel);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void bindActions() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserFormActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        btnResetPassword.setOnClickListener(v -> showResetPasswordDialog());
        btnLock.setOnClickListener(v -> toggleUserLockStatus());
    }

    private void loadUserProfile() {
        adminUserApi.getUserDetail(userId).enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    targetUser = response.body().getData();
                    bindProfile(targetUser);
                } else {
                    showToast("Tải thông tin người dùng thất bại: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                showToast("Lỗi tải thông tin người dùng: " + t.getMessage());
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

        tvUserEmailDetail.setText("Email: " + (user.email != null ? user.email : "N/A"));
        tvUserRoleDetail.setText("Vai trò: " + (user.role != null ? user.role.toUpperCase() : "CUSTOMER"));
        tvUserCreatedDate.setText("Ngày tạo tài khoản: " + (user.createdAt > 0
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(user.createdAt))
                : "N/A"));
        tvUserLoginCount.setText("Số lần đăng nhập: " + (user.loginCount != null ? user.loginCount : 0));
        tvUserStatusDetail.setText("Trạng thái: " + ("active".equalsIgnoreCase(user.status) ? "Hoạt động" : "Tạm khóa"));
    }

    private void toggleUserLockStatus() {
        if (targetUser == null) return;
        boolean isActive = "active".equalsIgnoreCase(targetUser.status);
        String actionStr = isActive ? "Khóa" : "Kích hoạt";

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Xác nhận")
                .setMessage("Bạn có chắc muốn " + actionStr.toLowerCase() + " tài khoản này không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    targetUser.status = isActive ? "inactive" : "active";
                    adminUserApi.updateUser(userId, targetUser).enqueue(new Callback<ApiResponse<UserDTO>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                showToast(isActive ? "Đã khóa tài khoản thành công" : "Kích hoạt tài khoản thành công!");
                                loadUserProfile();
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

    private void showResetPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        View etOld = dialogView.findViewById(R.id.etOldPassword);
        if (etOld != null && etOld.getParent() instanceof View) {
            ((View) etOld.getParent()).setVisibility(View.GONE);
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

                    adminUserApi.resetPassword(userId, newPass).enqueue(new Callback<ApiResponse<Void>>() {
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
