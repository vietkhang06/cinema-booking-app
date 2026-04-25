package com.example.cinemabookingapp.ui.customer;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.ui.customer.adapter.HomeBannerAdapter;
import com.example.cinemabookingapp.ui.customer.adapter.HomeMovieAdapter;
import com.example.cinemabookingapp.ui.customer.model.HomeMovieItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private static final String FILTER_NOW_SHOWING = HomeMovieItem.NOW_SHOWING;
    private static final String FILTER_COMING_SOON = HomeMovieItem.COMING_SOON;

    private ViewPager2 viewPagerBanner;
    private LinearLayout bannerDots;
    private MaterialButton btnNowShowing;
    private MaterialButton btnComingSoon;
    private MaterialButton btnLocation;
    private RecyclerView rvMovies;

    private LinearLayout bottomNavContainer;

    private MaterialCardView navHomeCard;
    private MaterialCardView navShowtimeCard;
    private MaterialCardView navCartCard;
    private MaterialCardView navMovieCard;
    private MaterialCardView navProfileCard;

    private TextView navHomeLabel;
    private TextView navShowtimeLabel;
    private TextView navCartLabel;
    private TextView navMovieLabel;
    private TextView navProfileLabel;

    private ImageView navHomeIcon;
    private ImageView navShowtimeIcon;
    private ImageView navCartIcon;
    private ImageView navMovieIcon;
    private ImageView navProfileIcon;

    private final int activeColor = Color.parseColor("#1E1A23");
    private final int inactiveTint = Color.parseColor("#4A4650");
    private final int activeTint = Color.WHITE;

    private final List<Integer> bannerItems = Arrays.asList(
            R.drawable.login_icon,
            R.drawable.sign_up_pana,
            R.drawable.login_icon
    );

    private final List<HomeMovieItem> allMovies = new ArrayList<>();
    private final List<HomeMovieItem> visibleMovies = new ArrayList<>();

    private final List<View> bannerDotViews = new ArrayList<>();

    private final HomeBannerAdapter bannerAdapter = new HomeBannerAdapter();
    private final HomeMovieAdapter movieAdapter = new HomeMovieAdapter();

    private String currentFilter = FILTER_NOW_SHOWING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupBanner();
        setupMovies();
        initBottomNav();
        applyBottomNavState(0);
        showMovies(FILTER_NOW_SHOWING);
        applyFilterStyle(FILTER_NOW_SHOWING);
    }

    private void initViews() {
        viewPagerBanner = findViewById(R.id.viewPagerBanner);
        bannerDots = findViewById(R.id.bannerDots);
        btnNowShowing = findViewById(R.id.btnNowShowing);
        btnComingSoon = findViewById(R.id.btnComingSoon);
        btnLocation = findViewById(R.id.btnLocation);
        rvMovies = findViewById(R.id.rvMovies);

        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        rvMovies.setNestedScrollingEnabled(false);
        rvMovies.setAdapter(movieAdapter);

        btnNowShowing.setOnClickListener(v -> {
            currentFilter = FILTER_NOW_SHOWING;
            showMovies(FILTER_NOW_SHOWING);
            applyFilterStyle(FILTER_NOW_SHOWING);
        });

        btnComingSoon.setOnClickListener(v -> {
            currentFilter = FILTER_COMING_SOON;
            showMovies(FILTER_COMING_SOON);
            applyFilterStyle(FILTER_COMING_SOON);
        });

        btnLocation.setOnClickListener(v -> showToast("Chọn khu vực sau"));
    }

    private void setupBanner() {
        bannerAdapter.setBanners(bannerItems);
        viewPagerBanner.setAdapter(bannerAdapter);
        viewPagerBanner.setOffscreenPageLimit(1);

        setupBannerDots(bannerItems.size());
        updateBannerDots(0);

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateBannerDots(position);
            }
        });
    }

    private void setupBannerDots(int count) {
        bannerDots.removeAllViews();
        bannerDotViews.clear();

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(8), dp(8));
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            dot.setLayoutParams(params);
            dot.setBackgroundColor(Color.parseColor("#D0D0D0"));
            bannerDots.addView(dot);
            bannerDotViews.add(dot);
        }
    }

    private void updateBannerDots(int selectedPosition) {
        for (int i = 0; i < bannerDotViews.size(); i++) {
            View dot = bannerDotViews.get(i);
            if (i == selectedPosition) {
                dot.setBackgroundColor(Color.parseColor("#1E1A23"));
            } else {
                dot.setBackgroundColor(Color.parseColor("#D0D0D0"));
            }
        }
    }

    private void setupMovies() {
        allMovies.clear();

        allMovies.add(new HomeMovieItem("Heo năm móng", R.drawable.login_icon, "8.2", "T18", FILTER_NOW_SHOWING));
        allMovies.add(new HomeMovieItem("Hẹn em ngày mai", R.drawable.sign_up_pana, "8.0", "T16", FILTER_NOW_SHOWING));
        allMovies.add(new HomeMovieItem("Phi phong", R.drawable.login_icon, "7.9", "T18", FILTER_NOW_SHOWING));
        allMovies.add(new HomeMovieItem("Phim sắp chiếu 1", R.drawable.sign_up_pana, "8.5", "T16", FILTER_COMING_SOON));
        allMovies.add(new HomeMovieItem("Phim sắp chiếu 2", R.drawable.login_icon, "8.1", "T13", FILTER_COMING_SOON));
        allMovies.add(new HomeMovieItem("Phim sắp chiếu 3", R.drawable.sign_up_pana, "8.3", "T18", FILTER_COMING_SOON));
    }

    private void showMovies(String filter) {
        visibleMovies.clear();
        for (HomeMovieItem item : allMovies) {
            if (filter.equals(item.getStatus())) {
                visibleMovies.add(item);
            }
        }
        movieAdapter.setItems(visibleMovies);
    }

    private void applyFilterStyle(String activeFilter) {
        styleFilterButton(btnNowShowing, FILTER_NOW_SHOWING.equals(activeFilter));
        styleFilterButton(btnComingSoon, FILTER_COMING_SOON.equals(activeFilter));
    }

    private void styleFilterButton(MaterialButton button, boolean selected) {
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? activeColor : Color.WHITE));
        button.setTextColor(selected ? activeTint : activeColor);
        button.setStrokeColor(ColorStateList.valueOf(activeColor));
        button.setStrokeWidth(dp(1));
    }

    private void initBottomNav() {
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

        navHomeCard.setOnClickListener(v -> applyBottomNavState(0));
        navShowtimeCard.setOnClickListener(v -> applyBottomNavState(1));
        navCartCard.setOnClickListener(v -> applyBottomNavState(2));
        navMovieCard.setOnClickListener(v -> applyBottomNavState(3));
        navProfileCard.setOnClickListener(v -> applyBottomNavState(4));
    }

    private void applyBottomNavState(int index) {
        applyBottomState(navHomeCard, navHomeLabel, navHomeIcon, index == 0, "Trang chủ");
        applyBottomState(navShowtimeCard, navShowtimeLabel, navShowtimeIcon, index == 1, "Lịch chiếu");
        applyBottomState(navCartCard, navCartLabel, navCartIcon, index == 2, "Giỏ hàng");
        applyBottomState(navMovieCard, navMovieLabel, navMovieIcon, index == 3, "Phim");
        applyBottomState(navProfileCard, navProfileLabel, navProfileIcon, index == 4, "Tài khoản");
        bottomNavContainer.requestLayout();
    }

    private void applyBottomState(MaterialCardView card, TextView label, ImageView icon, boolean selected, String text) {
        LinearLayout.LayoutParams params;
        if (selected) {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(activeColor);
            label.setText(text);
            label.setVisibility(View.VISIBLE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(activeTint));
            label.setTextColor(activeTint);
        } else {
            params = new LinearLayout.LayoutParams(0, dp(48), 1f);
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(Color.TRANSPARENT);
            label.setVisibility(View.GONE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(inactiveTint));
        }
        card.setLayoutParams(params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}