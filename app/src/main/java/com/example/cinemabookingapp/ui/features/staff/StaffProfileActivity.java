package com.example.cinemabookingapp.ui.features.staff;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffProfileActivity extends AuthActivity {

    private View backBtn;
    private ImageView ivAvatar;
    private TextView tvName, tvRoleBadge, tvUid, tvEmail, tvPhone, tvGender, tvBirthdate, tvStatus;
    private MaterialButton btnChangePassword, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
        loadProfileData();
    }

    private void initViews() {
        backBtn = findViewById(R.id.back_btn);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvName = findViewById(R.id.tv_name);
        tvRoleBadge = findViewById(R.id.tv_role_badge);
        tvUid = findViewById(R.id.tv_uid);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvGender = findViewById(R.id.tv_gender);
        tvBirthdate = findViewById(R.id.tv_birthdate);
        tvStatus = findViewById(R.id.tv_status);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> performLogout());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
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
                    showToast("KhÃ´ng thá»ƒ táº£i há»“ sÆ¡ nhÃ¢n viÃªn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                showToast("Lá»—i káº¿t ná»‘i: " + t.getMessage());
            }
        });
    }

    private void displayProfile(User user) {
        tvUid.setText(user.uid);
        tvName.setText(user.name != null && !user.name.isEmpty() ? user.name : "ChÆ°a cáº­p nháº­t");
        tvEmail.setText(user.email);
        tvPhone.setText(user.phone != null && !user.phone.isEmpty() ? user.phone : "ChÆ°a cáº­p nháº­t");
        tvGender.setText(user.gender != null && !user.gender.isEmpty() ? user.gender : "ChÆ°a cáº­p nháº­t");
        tvBirthdate.setText(user.birthDate != null && !user.birthDate.isEmpty() ? user.birthDate : "ChÆ°a cáº­p nháº­t");
        tvStatus.setText(user.status != null && !user.status.isEmpty() ? user.status : "Hoáº¡t Ä‘á»™ng");

        if ("admin".equalsIgnoreCase(user.role)) {
            tvRoleBadge.setText("Quáº£n trá»‹ viÃªn");
            View adminBottomNav = findViewById(R.id.adminBottomNav);
            if (adminBottomNav != null) {
                adminBottomNav.setVisibility(View.VISIBLE);
                com.example.cinemabookingapp.ui.features.admin.dashboard.AdminBottomNavHelper.setupAdminBottomNavigation(this, 4);
            }
            if (backBtn != null) {
                backBtn.setVisibility(View.GONE);
            }
        } else if ("staff".equalsIgnoreCase(user.role)) {
            tvRoleBadge.setText("NhÃ¢n viÃªn");
        } else {
            tvRoleBadge.setText(user.role);
        }

        if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.user_solid_full)
                    .into(ivAvatar);
        }
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("ÄÄƒng xuáº¥t")
                .setMessage("Báº¡n cÃ³ cháº¯c cháº¯n muá»‘n Ä‘Äƒng xuáº¥t khÃ´ng?")
                .setPositiveButton("ÄÄƒng xuáº¥t", (dialog, which) -> {
                    ServiceProvider.getInstance().getAuthenticationService().logOut();
                    showToast("ÄÃ£ Ä‘Äƒng xuáº¥t");
                    AppNavigator.goToLogin(this);
                })
                .setNegativeButton("Há»§y", null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Äá»•i máº­t kháº©u")
                .setView(dialogView)
                .setPositiveButton("Cáº­p nháº­t", (dialog, which) -> {
                    String oldPass = etOldPassword.getText().toString();
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmPassword.getText().toString();

                    if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                        showToast("Máº­t kháº©u má»›i pháº£i Ã­t nháº¥t 6 kÃ½ tá»±");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        showToast("Máº­t kháº©u xÃ¡c nháº­n khÃ´ng khá»›p");
                        return;
                    }

                    showLoading(true);
                    ServiceProvider.getInstance().getAuthenticationService().updatePassword(newPass)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                showToast("Äá»•i máº­t kháº©u thÃ nh cÃ´ng");
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                showToast("Lá»—i: " + e.getMessage());
                            });
                })
                .setNegativeButton("Há»§y", null)
                .show();
    }
}
