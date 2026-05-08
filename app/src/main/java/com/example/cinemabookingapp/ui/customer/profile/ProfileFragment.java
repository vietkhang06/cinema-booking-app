package com.example.cinemabookingapp.ui.customer.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
import com.example.cinemabookingapp.service.ProfileService;
import com.example.cinemabookingapp.ui.component.AchievementProgressBar;
import com.example.cinemabookingapp.utils.QRCodeGenerator;
import com.google.android.material.card.MaterialCardView;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        super(R.layout.customer_activity_profile);
    }

    TextView userNameTV, totalSpendingTV;
    ImageView profileAvatar, badgeImage, userProfileQRBitmap;
    MaterialCardView editProfileBtn, viewTransactionBtn, viewNotificationBtn;
    MaterialCardView logOutBtn;
    MaterialCardView hotlineViewBtn;
    AchievementProgressBar achievementBar;

    AuthenticationService authService;
    ProfileService profileService;

    int maxSpendingMilestone = 4100000;
    int totalSpending = 0;
    List<Integer> spendingMilestones = Arrays.asList(0, 2_000_000, 4_000_000);
    List<Integer> milestoneIcons = Arrays.asList(
        R.drawable.ic_small_star,
        R.drawable.apple_brands_solid_full,
        R.drawable.ic_scan_qr
    );

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authService = ServiceProvider.getInstance().getAuthenticationService();
        profileService = ServiceProvider.getInstance().getProfileService();

        initViews(view);
        bindActions();
    }

    @Override
    public void onStart() {
        super.onStart();

        loadUserProfile();
        loadUserSpendingMilestone();
    }

    private void loadUserProfile() {
        User profileData = profileService.getUserProfile();

        userNameTV.setText(profileData.name == null || profileData.name.isBlank() ? profileData.email : profileData.name);
        totalSpendingTV.setText(String.format("%s vnd", 0));
        Glide.with(this)
                .load(profileData.avatarUrl != null ? profileData.avatarUrl : R.drawable.user_solid_full)
                .into(profileAvatar);

//        try {
//            Glide.with(this)
//                  .load(QRCodeGenerator.generateQRCodeFromString(profileData.uid))
//                  .into();
//        } catch (WriterException ignored) { }

        totalSpending = (int) profileService.getUserTotalSpending();
    }

    void loadUserSpendingMilestone(){
        AtomicInteger index = new AtomicInteger(0);
        List<AchievementProgressBar.Milestone> milestones = spendingMilestones.stream()
            .map((value) ->
                new AchievementProgressBar.Milestone(
                        (float)value/ maxSpendingMilestone,
                        String.format("%,d", value).replace(',', '.'),
                        milestoneIcons.get(index.getAndIncrement()))
            ).collect(Collectors.toList());

        achievementBar.setMilestones(milestones);



        float progress = (float) totalSpending/maxSpendingMilestone;
        int badgeId = R.drawable.apple_brands_solid_full;
        for (AchievementProgressBar.Milestone m: milestones) {
            if(m.fraction <= progress){
                badgeId = m.iconResId;
            }
        }

        badgeImage.setImageResource(badgeId);
        achievementBar.setProgress((float) totalSpending/maxSpendingMilestone);
    }

    private void initViews(@NonNull View view) {
        achievementBar = view.findViewById(R.id.achievement_bar);

        userNameTV = view.findViewById(R.id.profile_user_name);
        totalSpendingTV = view.findViewById(R.id.profile_total_spending);
        profileAvatar = view.findViewById(R.id.profile_avatar);
        badgeImage = view.findViewById(R.id.profile_customer_badge);

        editProfileBtn = view.findViewById(R.id.profile_edit_btn);
        viewTransactionBtn = view.findViewById(R.id.profile_transaction_btn);
        viewNotificationBtn = view.findViewById(R.id.profile_notification_btn);

        logOutBtn = view.findViewById(R.id.profile_logout_btn);
    }

    private void bindActions() {
        editProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(this.getContext(), EditProfileActivity.class));
        });
        logOutBtn.setOnClickListener(v -> {
            authService.logOut();
        });
    }
}
