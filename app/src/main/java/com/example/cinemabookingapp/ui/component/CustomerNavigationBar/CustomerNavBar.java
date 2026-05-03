package com.example.cinemabookingapp.ui.component.CustomerNavigationBar;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.customer.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class CustomerNavBar extends BottomNavigationView {

    private LinearLayout bottomNavContainer;

    private MaterialCardView navHomeCard, navShowtimeCard, navCartCard, navMovieCard, navProfileCard;
    private TextView navHomeLabel, navShowtimeLabel, navCartLabel, navMovieLabel, navProfileLabel;
    private ImageView navHomeIcon, navShowtimeIcon, navCartIcon, navMovieIcon, navProfileIcon;

    private final int activeColor = Color.parseColor("#1E1A23");
    private final int inactiveTint = Color.parseColor("#4A4650");
    private final int activeTint = Color.WHITE;

    public static int navIndex = 0;


    public CustomerNavBar(Context context, AttributeSet attrs){
        super(context, attrs);
        inflate(context, R.layout.view_bottom_nav, this);

        initViews();
        bindActions();
        applyBottomNavState(navIndex);
    }

    private void initViews() {
        bottomNavContainer = findViewById(R.id.bottomNavContainer);

        navHomeCard = findViewById(R.id.navHomeCard);
        navShowtimeCard = findViewById(R.id.navShowtimeCard);
        navCartCard = findViewById(R.id.navCartCard);
        navMovieCard = findViewById(R.id.navMovieCard);
        navProfileCard = findViewById(R.id.navProfileCard);

        navHomeLabel = findViewById(R.id.navHomeLabel);
        navShowtimeLabel = findViewById(R.id.navShowtimeLabel);
        navCartLabel = findViewById(R.id.navCartLabel);
        navMovieLabel = findViewById(R.id.navMovieLabel);
        navProfileLabel = findViewById(R.id.navProfileLabel);

        navHomeIcon = findViewById(R.id.navHomeIcon);
        navShowtimeIcon = findViewById(R.id.navShowtimeIcon);
        navCartIcon = findViewById(R.id.navCartIcon);
        navMovieIcon = findViewById(R.id.navMovieIcon);
        navProfileIcon = findViewById(R.id.navProfileIcon);
    }

    private void bindActions() {
        navHomeCard.setOnClickListener(v -> {
            AppNavigator.goToCustomerHome(getActivity());
            applyBottomNavState(0);
        });
        navShowtimeCard.setOnClickListener(v -> applyBottomNavState(1));
        navCartCard.setOnClickListener(v -> applyBottomNavState(2));
        navMovieCard.setOnClickListener(v -> applyBottomNavState(3));
        navProfileCard.setOnClickListener(v -> {
            AppNavigator.navigateWithData(getActivity(), ProfileActivity.class, null);
            applyBottomNavState(4);
        });
    }
    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void applyBottomNavState(int index) {
        navIndex = index;
        applyBottomState(navHomeCard, navHomeLabel, navHomeIcon, index == 0, "Trang chủ");
        applyBottomState(navShowtimeCard, navShowtimeLabel, navShowtimeIcon, index == 1, "Rạp Phim");
        applyBottomState(navCartCard, navCartLabel, navCartIcon, index == 2, "Cine Shop");
        applyBottomState(navMovieCard, navMovieLabel, navMovieIcon, index == 3, "Điện Ảnh");
        applyBottomState(navProfileCard, navProfileLabel, navProfileIcon, index == 4, "Tài Khoản");
        bottomNavContainer.requestLayout();
    }

    private void applyBottomState(MaterialCardView card, TextView label, ImageView icon, boolean selected, String text) {
        LinearLayout.LayoutParams params;
        if (selected) {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(activeColor);
            card.setStrokeWidth(0);
            label.setText(text);
            label.setVisibility(View.VISIBLE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(activeTint));
            label.setTextColor(activeTint);
            card.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start();
        } else {
            params = new LinearLayout.LayoutParams(0, dp(48), 0.8f);
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(Color.WHITE);
            card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D3CAD7")));
            card.setStrokeWidth(dp(1));
            label.setVisibility(View.GONE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(inactiveTint));
            card.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
        }
        card.setLayoutParams(params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
