package com.example.cinemabookingapp.ui.features.profile;

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
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
import com.example.cinemabookingapp.service.ProfileService;
import com.example.cinemabookingapp.ui.component.AchievementProgressBar;
import com.example.cinemabookingapp.ui.features.chat.CustomerChatActivity;
import com.example.cinemabookingapp.ui.features.chat.CustomerSupportActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    // ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ Views ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬
    TextView userNameTV, totalSpendingTV, tvMemberLevel, tvStarCount;
    ImageView profileAvatar, badgeImage;
    MaterialCardView editProfileBtn, viewTransactionBtn, viewNotificationBtn;
    MaterialCardView logOutBtn;
    TextView tvLogoutBtnLabel, notificationBadge;
    LinearLayout btnMemberCard;
    ImageView btnSettings, btnProfileCart;
    AchievementProgressBar achievementBar;

    // Menu items
    LinearLayout menuHotline, menuEmail, menuCompanyInfo,
            menuTerms, menuPaymentPolicy, menuPrivacyPolicy, menuFaq, menuSupportCenter;
    LinearLayout btnMyTickets, btnMyRewards;

    private com.google.firebase.firestore.ListenerRegistration notificationListener;
    private User currentUserProfile;

    // ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ Services ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬
    AuthenticationService authService;
    ProfileService profileService;

    // ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ Spending milestones ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬
    int maxSpendingMilestone = 4100000;
    int totalSpending = 0;
    List<Integer> spendingMilestones = Arrays.asList(0, 2_000_000, 4_000_000);
    List<Integer> milestoneIcons = Arrays.asList(
            R.drawable.square_solid_full,
            R.drawable.pentagon_solid_full,
            R.drawable.hexagon_solid_full
    );

    // ── Lifecycle ──

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authService   = ServiceProvider.getInstance().getAuthenticationService();
        profileService = ServiceProvider.getInstance().getProfileService();

        initViews(view);
        
        if (achievementBar != null) {
            java.util.List<com.example.cinemabookingapp.ui.component.AchievementProgressBar.Milestone> list = new java.util.ArrayList<>();
            list.add(new com.example.cinemabookingapp.ui.component.AchievementProgressBar.Milestone(
                    0f,
                    "0đ",
                    milestoneIcons.get(0),
                    0xFF4A148C
            ));
            list.add(new com.example.cinemabookingapp.ui.component.AchievementProgressBar.Milestone(
                    (float) spendingMilestones.get(1) / maxSpendingMilestone,
                    "2.000.000đ",
                    milestoneIcons.get(1),
                    0xFF800000
            ));
            list.add(new com.example.cinemabookingapp.ui.component.AchievementProgressBar.Milestone(
                    (float) spendingMilestones.get(2) / maxSpendingMilestone,
                    "4.000.000đ",
                    milestoneIcons.get(2),
                    0xFFEAB308
            ));
            achievementBar.setMilestones(list);
        }

        bindActions();
        setMenuText(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateAuthButton();
        loadUserProfile();
        loadUserSpendingMilestone();
        listenToNotifications();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    private void listenToNotifications() {
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            if (notificationBadge != null) {
                notificationBadge.setVisibility(View.GONE);
            }
            return;
        }

        com.example.cinemabookingapp.domain.repository.NotificationRepository notificationRepo =
                new com.example.cinemabookingapp.data.repository.NotificationRepositoryImpl();

        notificationListener = notificationRepo.listenToUserNotifications(uid, new ResultCallback<List<com.example.cinemabookingapp.domain.model.Notification>>() {
            @Override
            public void onSuccess(List<com.example.cinemabookingapp.domain.model.Notification> notifications) {
                if (!isAdded() || notificationBadge == null) return;
                long unreadCount = 0;
                if (notifications != null) {
                    for (com.example.cinemabookingapp.domain.model.Notification notif : notifications) {
                        if (!notif.isRead) {
                            unreadCount++;
                        }
                    }
                }
                if (unreadCount > 0) {
                    notificationBadge.setText(String.valueOf(unreadCount));
                    notificationBadge.setVisibility(View.VISIBLE);
                } else {
                    notificationBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    // ── Load data ──

    private void loadUserProfile() {
        profileService.getUserProfile(new ResultCallback<User>() {
            @Override
            public void onSuccess(User profileData) {
                if (!isAdded()) return;

                if (profileData == null) {
                    userNameTV.setText("Guest");
                    if (tvMemberLevel != null) {
                        tvMemberLevel.setText("Basic");
                    }
                    if (tvStarCount != null) {
                        tvStarCount.setText("0 Stars");
                    }
                    Glide.with(ProfileFragment.this)
                            .load(R.drawable.user_solid_full)
                            .circleCrop()
                            .placeholder(R.drawable.user_solid_full)
                            .into(profileAvatar);
                    totalSpending = 0;
                    updateSpendingUI();
                    return;
                }

                currentUserProfile = profileData;

                String displayName = (profileData.name == null || profileData.name.isBlank())
                        ? profileData.email : profileData.name;
                userNameTV.setText(displayName);
                
                if (tvMemberLevel != null) {
                    tvMemberLevel.setText(profileData.memberLevel != null ? profileData.memberLevel : "Basic");
                }

                if (profileData.avatarUrl != null && profileData.avatarUrl.startsWith("data:image")) {
                    try {
                        String base64Content = profileData.avatarUrl.substring(profileData.avatarUrl.indexOf(",") + 1);
                        byte[] imageBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT);
                        Glide.with(ProfileFragment.this)
                                .load(imageBytes)
                                .circleCrop()
                                .placeholder(R.drawable.user_solid_full)
                                .into(profileAvatar);
                    } catch (Exception e) {
                        Glide.with(ProfileFragment.this)
                                .load(R.drawable.user_solid_full)
                                .circleCrop()
                                .placeholder(R.drawable.user_solid_full)
                                .into(profileAvatar);
                    }
                } else {
                    Glide.with(ProfileFragment.this)
                            .load(profileData.avatarUrl != null ? profileData.avatarUrl : R.drawable.user_solid_full)
                            .circleCrop()
                            .placeholder(R.drawable.user_solid_full)
                            .into(profileAvatar);
                }

                loadUserSpendingMilestone();
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUserSpendingMilestone() {
        profileService.getUserTotalSpending(new ResultCallback<Double>() {
            @Override
            public void onSuccess(Double total) {
                totalSpending = total.intValue();
                updateSpendingUI();
            }

            @Override
            public void onError(String message) {
                totalSpending = 0;
                updateSpendingUI();
            }
        });
    }

    private void updateSpendingUI() {
        if (!isAdded()) return;

        float progress = (float) totalSpending / maxSpendingMilestone;
        if (progress > 1.0f) progress = 1.0f;

        int badgeId;
        int badgeColor;
        if (progress >= (float) spendingMilestones.get(2) / maxSpendingMilestone) {
            badgeId = milestoneIcons.get(2);
            badgeColor = 0xFFEAB308;
        } else if (progress >= (float) spendingMilestones.get(1) / maxSpendingMilestone) {
            badgeId = milestoneIcons.get(1);
            badgeColor = 0xFF800000;
        } else {
            badgeId = milestoneIcons.get(0);
            badgeColor = 0xFF4A148C;
        }

        badgeImage.setImageResource(badgeId);
        badgeImage.setImageTintList(android.content.res.ColorStateList.valueOf(badgeColor));
        achievementBar.setProgress(progress);

        totalSpendingTV.setText(String.format("%,dđ", totalSpending).replace(',', '.'));
    }

    // ── Init views ──

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
        notificationBadge   = view.findViewById(R.id.profile_notification_badge);
        logOutBtn           = view.findViewById(R.id.profile_logout_btn);
        tvLogoutBtnLabel    = logOutBtn.findViewById(R.id.tvLogoutBtnLabel);
        btnMemberCard       = view.findViewById(R.id.btnMemberCard);
        btnSettings         = view.findViewById(R.id.btnProfileSettings);
        btnProfileCart      = view.findViewById(R.id.btnProfileCart);

        btnMyTickets        = view.findViewById(R.id.btnMyTickets);
        btnMyRewards        = view.findViewById(R.id.btnMyRewards);

        menuHotline         = view.findViewById(R.id.menuHotline);
        menuEmail           = view.findViewById(R.id.menuEmail);
        menuCompanyInfo     = view.findViewById(R.id.menuCompanyInfo);
        menuTerms           = view.findViewById(R.id.menuTerms);
        menuPaymentPolicy   = view.findViewById(R.id.menuPaymentPolicy);
        menuPrivacyPolicy   = view.findViewById(R.id.menuPrivacyPolicy);
        menuFaq             = view.findViewById(R.id.menuFaq);

        menuSupportCenter   = view.findViewById(R.id.menuSupportCenter);
    }

    // ── Set rich-text menu labels ──

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

    // ── Bind actions ──

    private void bindActions() {
        // Edit profile
        editProfileBtn.setOnClickListener(v ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));

        // Settings
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    startActivity(new Intent(getContext(), SettingsActivity.class)));
        }

        // Cart
        if (btnProfileCart != null) {
            btnProfileCart.setOnClickListener(v -> {
                boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
                if (isLoggedIn) {
                    startActivity(new Intent(getContext(), com.example.cinemabookingapp.ui.features.cineshop.CineCartActivity.class));
                } else {
                    AppNavigator.goToLogin(requireActivity());
                }
            });
        }

        // Transactions
        viewTransactionBtn.setOnClickListener(v -> {
                boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
                if (isLoggedIn) {
                    AppNavigator.goToTransactionHistory(requireActivity());
                } else {
                    AppNavigator.goToLogin(requireActivity());
                }
            });

        // Notifications
        viewNotificationBtn.setOnClickListener(v ->
                AppNavigator.goToNotification(requireActivity()));

        // Membership card
        if (btnMemberCard != null) {
            btnMemberCard.setOnClickListener(v -> {
                if (currentUserProfile != null) {
                    showMemberCardDialog(currentUserProfile);
                } else {
                    Toast.makeText(getContext(), "Không thể tải thông tin thành viên", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Feature cards
        if (btnMyTickets != null) {
            btnMyTickets.setOnClickListener(v -> {
                boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
                if (isLoggedIn) {
                    AppNavigator.goToTransactionHistory(requireActivity());
                } else {
                    AppNavigator.goToLogin(requireActivity());
                }
            });
        }
        if (btnMyRewards != null) {
            btnMyRewards.setOnClickListener(v -> {
                boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
                if (isLoggedIn) {
                    startActivity(new Intent(getContext(), com.example.cinemabookingapp.ui.features.profile.MyPromotionListActivity.class));
                } else {
                    AppNavigator.goToLogin(requireActivity());
                }
            });
        }

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

        // Logout
        logOutBtn.setOnClickListener(v -> {
            boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
            if (isLoggedIn) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            authService.logOut();
                            com.example.cinemabookingapp.ui.features.home.HomeActivity.resetPopupShownState();
                            AppNavigator.goToCustomerHome(requireActivity());
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            } else {
                AppNavigator.goToLogin(requireActivity());
            }
        });

        menuSupportCenter.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CustomerSupportActivity.class);
            startActivity(intent);
        });
    }

    // ── Cập nhật nút Login/Logout theo trạng thái đăng nhập ──

    private void updateAuthButton() {
        if (!isAdded() || tvLogoutBtnLabel == null) return;

        boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;

        if (isLoggedIn) {
            tvLogoutBtnLabel.setText("Đăng xuất");
            tvLogoutBtnLabel.setTextColor(android.graphics.Color.parseColor("#E8640C")); // cam
        } else {
            tvLogoutBtnLabel.setText("Đăng nhập");
            tvLogoutBtnLabel.setTextColor(android.graphics.Color.parseColor("#1E4F8F")); // xanh
        }
    }

    private void showMemberCardDialog(User user) {
        if (getContext() == null) return;
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_member_card, null);
        builder.setView(dialogView);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        com.google.android.material.card.MaterialCardView layoutCardBackground = dialogView.findViewById(R.id.layoutCardBackground);
        android.widget.ImageView ivQrCode = dialogView.findViewById(R.id.ivQrCode);
        android.widget.TextView tvMemberName = dialogView.findViewById(R.id.tvMemberName);
        android.widget.TextView tvMemberLevelBadge = dialogView.findViewById(R.id.tvMemberLevelBadge);
        android.widget.TextView tvMemberId = dialogView.findViewById(R.id.tvMemberId);
        android.view.View btnClose = dialogView.findViewById(R.id.btnClose);
        
        String name = (user.name != null && !user.name.trim().isEmpty()) ? user.name : user.email;
        if (tvMemberName != null) tvMemberName.setText(name);
        
        String level = (user.memberLevel != null) ? user.memberLevel.toLowerCase() : "standard";
        if (tvMemberLevelBadge != null) {
            tvMemberLevelBadge.setText((level.toUpperCase() + " MEMBER"));
        }
        
        if (tvMemberId != null) {
            tvMemberId.setText("ID: " + (user.uid != null ? user.uid : "—"));
        }
        
        int cardColor = 0xFF1A3A8C;
        int badgeTextColor = 0xFF1A3A8C;
        int badgeBgColor = 0xFFEBF0FF;
        
        if ("vip".equals(level)) {
            cardColor = 0xFFA13345;
            badgeTextColor = 0xFFA13345;
            badgeBgColor = 0xFFFFEBEB;
        } else if ("gold".equals(level)) {
            cardColor = 0xFFB8860B;
            badgeTextColor = 0xFFB8860B;
            badgeBgColor = 0xFFFFF8E7;
        } else if ("platinum".equals(level)) {
            cardColor = 0xFF4A4A4A;
            badgeTextColor = 0xFF4A4A4A;
            badgeBgColor = 0xFFF0F0F0;
        }
        
        if (layoutCardBackground != null) {
            layoutCardBackground.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(cardColor));
        }
        if (tvMemberLevelBadge != null) {
            tvMemberLevelBadge.setTextColor(badgeTextColor);
            tvMemberLevelBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(badgeBgColor));
        }
        
        String data = user.uid != null ? user.uid : "";
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=" + data;
        
        if (ivQrCode != null && getContext() != null) {
            com.bumptech.glide.Glide.with(this)
                .load(qrUrl)
                .placeholder(R.drawable.ic_scan_qr)
                .into(ivQrCode);
        }
        
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        
        dialog.show();
    }
}
