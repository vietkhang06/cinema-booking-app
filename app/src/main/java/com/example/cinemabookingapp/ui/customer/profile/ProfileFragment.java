package com.example.cinemabookingapp.ui.customer.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
import com.example.cinemabookingapp.service.ProfileService;
import com.example.cinemabookingapp.ui.component.AchievementProgressBar;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        super(R.layout.customer_activity_profile);
    }

    // ── Views ─────────────────────────────────────────────────────────────────
    TextView userNameTV, totalSpendingTV, tvMemberLevel, tvStarCount;
    ImageView profileAvatar, badgeImage;
    MaterialCardView editProfileBtn, viewTransactionBtn, viewNotificationBtn;
    MaterialCardView logOutBtn;
    LinearLayout btnMemberCard;
    AchievementProgressBar achievementBar;

    // Menu items
    LinearLayout menuHotline, menuEmail, menuCompanyInfo,
            menuTerms, menuPaymentPolicy, menuPrivacyPolicy, menuFaq;
    LinearLayout btnDoiQua, btnMyRewards, btnTinhNangMoi;

    // ── Services ──────────────────────────────────────────────────────────────
    AuthenticationService authService;
    ProfileService profileService;

    // ── Spending milestones ───────────────────────────────────────────────────
    int maxSpendingMilestone = 4100000;
    int totalSpending = 0;
    List<Integer> spendingMilestones = Arrays.asList(0, 2_000_000, 4_000_000);
    List<Integer> milestoneIcons = Arrays.asList(
            R.drawable.ic_small_star,
            R.drawable.apple_brands_solid_full,
            R.drawable.ic_scan_qr
    );

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authService   = ServiceProvider.getInstance().getAuthenticationService();
        profileService = ServiceProvider.getInstance().getProfileService();

        initViews(view);
        bindActions();
        setMenuText(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUserProfile();
        loadUserSpendingMilestone();
    }

    // ── Load data ─────────────────────────────────────────────────────────────

    private void loadUserProfile() {
        User profileData = profileService.getUserProfile();
        if (profileData == null) return;

        String displayName = (profileData.name == null || profileData.name.isBlank())
                ? profileData.email : profileData.name;
        userNameTV.setText(displayName);
        totalSpendingTV.setText("0đ");

        Glide.with(this)
                .load(profileData.avatarUrl != null ? profileData.avatarUrl : R.drawable.user_solid_full)
                .circleCrop()
                .into(profileAvatar);

        totalSpending = (int) profileService.getUserTotalSpending();
    }

    private void loadUserSpendingMilestone() {
        AtomicInteger index = new AtomicInteger(0);
        List<AchievementProgressBar.Milestone> milestones = spendingMilestones.stream()
                .map(value -> new AchievementProgressBar.Milestone(
                        (float) value / maxSpendingMilestone,
                        String.format("%,d", value).replace(',', '.'),
                        milestoneIcons.get(index.getAndIncrement())))
                .collect(Collectors.toList());
        achievementBar.setMilestones(milestones);

        float progress = (float) totalSpending / maxSpendingMilestone;
        int badgeId = R.drawable.apple_brands_solid_full;
        for (AchievementProgressBar.Milestone m : milestones) {
            if (m.fraction < progress) badgeId = m.iconResId;
        }
        badgeImage.setImageResource(badgeId);
        achievementBar.setProgress(progress);

        totalSpendingTV.setText(String.format("%,dđ", totalSpending).replace(',', '.'));
    }

    // ── Init views ────────────────────────────────────────────────────────────

    private void initViews(@NonNull View view) {
        achievementBar      = view.findViewById(R.id.achievement_bar);
        userNameTV          = view.findViewById(R.id.profile_user_name);
        totalSpendingTV     = view.findViewById(R.id.profile_total_spending);
        profileAvatar       = view.findViewById(R.id.profile_avatar);
        badgeImage          = view.findViewById(R.id.profile_customer_badge);
        tvMemberLevel       = view.findViewById(R.id.tvMemberLevel);
        tvStarCount         = view.findViewById(R.id.tvStarCount);

        editProfileBtn      = view.findViewById(R.id.profile_edit_btn);
        viewTransactionBtn  = view.findViewById(R.id.profile_transaction_btn);
        viewNotificationBtn = view.findViewById(R.id.profile_notification_btn);
        logOutBtn           = view.findViewById(R.id.profile_logout_btn);
        btnMemberCard       = view.findViewById(R.id.btnMemberCard);

        btnDoiQua           = view.findViewById(R.id.btnDoiQua);
        btnMyRewards        = view.findViewById(R.id.btnMyRewards);
        btnTinhNangMoi      = view.findViewById(R.id.btnTinhNangMoi);

        menuHotline         = view.findViewById(R.id.menuHotline);
        menuEmail           = view.findViewById(R.id.menuEmail);
        menuCompanyInfo     = view.findViewById(R.id.menuCompanyInfo);
        menuTerms           = view.findViewById(R.id.menuTerms);
        menuPaymentPolicy   = view.findViewById(R.id.menuPaymentPolicy);
        menuPrivacyPolicy   = view.findViewById(R.id.menuPrivacyPolicy);
        menuFaq             = view.findViewById(R.id.menuFaq);
    }

    // ── Set rich-text menu labels ─────────────────────────────────────────────

    private void setMenuText(@NonNull View view) {
        setHtmlText(menuHotline, 0,
                "Gọi <b>ĐƯỜNG DÂY NÓNG</b>: <font color='#E8640C'>19002224</font>");
        setHtmlText(menuEmail, 0,
                "Email:  <font color='#E8640C'>hotro@galaxystudio.vn</font>");
    }

    /** Tìm TextView đầu tiên trong LinearLayout và set HTML text */
    private void setHtmlText(LinearLayout row, int childIndex, String html) {
        // TextView là con đầu tiên (index 0) của row
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
                break;
            }
        }
    }

    // ── Bind actions ──────────────────────────────────────────────────────────

    private void bindActions() {
        // Thông tin
        editProfileBtn.setOnClickListener(v ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));

        // Giao dịch (tạm thời toast)
        viewTransactionBtn.setOnClickListener(v ->
                Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show());

        // Thông báo (tạm thời toast)
        viewNotificationBtn.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chưa có thông báo mới", Toast.LENGTH_SHORT).show());

        // Mã thành viên
        if (btnMemberCard != null)
            btnMemberCard.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Mã thành viên đang được cập nhật", Toast.LENGTH_SHORT).show());

        // Feature cards (tạm thời)
        if (btnDoiQua != null)
            btnDoiQua.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Đổi quà sắp ra mắt!", Toast.LENGTH_SHORT).show());
        if (btnMyRewards != null)
            btnMyRewards.setOnClickListener(v ->
                    Toast.makeText(getContext(), "My Rewards sắp ra mắt!", Toast.LENGTH_SHORT).show());
        if (btnTinhNangMoi != null)
            btnTinhNangMoi.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Tính năng mới sắp ra mắt!", Toast.LENGTH_SHORT).show());

        // Menu items
        if (menuHotline != null)
            menuHotline.setOnClickListener(v -> {
                Intent call = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:19002224"));
                startActivity(call);
            });
        if (menuEmail != null)
            menuEmail.setOnClickListener(v -> {
                Intent mail = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:hotro@galaxystudio.vn"));
                startActivity(mail);
            });
        if (menuCompanyInfo != null)
            menuCompanyInfo.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Thông tin công ty", Toast.LENGTH_SHORT).show());
        if (menuTerms != null)
            menuTerms.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Điều khoản sử dụng", Toast.LENGTH_SHORT).show());
        if (menuPaymentPolicy != null)
            menuPaymentPolicy.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Chính sách thanh toán", Toast.LENGTH_SHORT).show());
        if (menuPrivacyPolicy != null)
            menuPrivacyPolicy.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Chính sách bảo mật", Toast.LENGTH_SHORT).show());
        if (menuFaq != null)
            menuFaq.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Câu hỏi thường gặp", Toast.LENGTH_SHORT).show());

        // ── ĐĂNG XUẤT — fix crash: navigate về LoginActivity sau khi logout ──
        logOutBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        authService.logOut();
                        // Navigate về LoginActivity và xoá back stack
                        AppNavigator.goToLogin(requireActivity());
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }
}
