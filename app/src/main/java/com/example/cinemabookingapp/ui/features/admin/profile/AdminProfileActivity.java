package com.example.cinemabookingapp.ui.features.admin.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.remote.api.ProfileApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.ui.features.admin.chat.AdminCustomerChatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileActivity extends AuthActivity {

    private View backBtn;
    private ImageView ivAvatar;
    private TextView tvAvatarInitials;
    private TextView tvName, tvRoleBadge, tvUid, tvEmail, tvPhone, tvGender, tvBirthdate, tvStatus;
    private MaterialButton btnCustomerSupport, btnChangePassword, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void initViews() {
        backBtn = findViewById(R.id.back_btn);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvAvatarInitials = findViewById(R.id.tv_avatar_initials);
        tvName = findViewById(R.id.tv_name);
        tvRoleBadge = findViewById(R.id.tv_role_badge);
        tvUid = findViewById(R.id.tv_uid);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvGender = findViewById(R.id.tv_gender);
        tvBirthdate = findViewById(R.id.tv_birthdate);
        tvStatus = findViewById(R.id.tv_status);
        btnCustomerSupport = findViewById(R.id.btn_customer_support);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void bindActions() {
        if (backBtn != null) {
            backBtn.setVisibility(View.GONE); // Admin profile has bottom nav, no back button needed
        }
        if (btnCustomerSupport != null) {
            btnCustomerSupport.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminCustomerChatActivity.class);
                startActivity(intent);
            });
        }
        btnLogout.setOnClickListener(v -> performLogout());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        View btnCopyUid = findViewById(R.id.btn_copy_uid);
        if (btnCopyUid != null) {
            btnCopyUid.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("UserID", tvUid.getText().toString());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Đã sao chép mã quản trị", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View btnEditProfile = findViewById(R.id.btn_edit_profile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.cinemabookingapp.ui.features.profile.EditProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadProfileData() {
        showLoading(true);
        ProfileApiService profileApi = RetrofitClient.getInstance().create(ProfileApiService.class);
        profileApi.getMyProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    User user = response.body().getData();
                    displayProfile(user);
                } else {
                    showToast("Không thể tải hồ sơ quản trị");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayProfile(User user) {
        tvUid.setText(user.getFormattedCode());
        String name = user.name != null && !user.name.isEmpty() ? user.name : "Chưa cập nhật";
        tvName.setText(name);
        tvEmail.setText(user.email);
        tvPhone.setText(user.phone != null && !user.phone.isEmpty() ? user.phone : "Chưa cập nhật");
        tvGender.setText(user.gender != null && !user.gender.isEmpty() ? user.gender : "Chưa cập nhật");
        tvBirthdate.setText(user.birthDate != null && !user.birthDate.isEmpty() ? user.birthDate : "Chưa cập nhật");
        tvStatus.setText(user.status != null && !user.status.isEmpty() ? user.status : "Active");

        tvRoleBadge.setText("Quản trị viên");

        // Calculate initials for Avatar
        String initials = "";
        if (name != null && !name.equals("Chưa cập nhật") && !name.isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length > 0) {
                StringBuilder sb = new StringBuilder();
                int count = 0;
                for (String part : parts) {
                    if (!part.isEmpty() && count < 3) {
                        sb.append(part.charAt(0));
                        count++;
                    }
                }
                initials = sb.toString().toUpperCase();
            }
        }
        if (initials.isEmpty()) {
            initials = "AD";
        }
        if (tvAvatarInitials != null) {
            tvAvatarInitials.setText(initials);
        }

        View adminBottomNav = findViewById(R.id.adminBottomNav);
        if (adminBottomNav != null) {
            adminBottomNav.setVisibility(View.VISIBLE);
            com.example.cinemabookingapp.ui.features.admin.dashboard.AdminBottomNavHelper.setupAdminBottomNavigation(this, 4);
        }

        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            if (ivAvatar != null) {
                ivAvatar.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.user_solid_full)
                        .into(ivAvatar);
            }
        } else {
            if (ivAvatar != null) {
                ivAvatar.setVisibility(View.GONE);
            }
        }
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    ServiceProvider.getInstance().getAuthenticationService().logOut();
                    showToast("Đã đăng xuất");
                    AppNavigator.goToLogin(this);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String oldPass = etOldPassword.getText().toString();
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmPassword.getText().toString();

                    if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                        showToast("Mật khẩu mới phải ít nhất 6 ký tự");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        showToast("Mật khẩu xác nhận không khớp");
                        return;
                    }

                    showLoading(true);
                    ServiceProvider.getInstance().getAuthenticationService().updatePassword(newPass)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                showToast("Đổi mật khẩu thành công");
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                showToast("Lỗi: " + e.getMessage());
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
