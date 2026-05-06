package com.example.cinemabookingapp.ui.customer;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.repository.MovieRepository;
import com.example.cinemabookingapp.domain.usecase.movie.GetMoviesUseCase;
import com.example.cinemabookingapp.ui.customer.adapter.HomeBannerAdapter;
import com.example.cinemabookingapp.ui.customer.adapter.HomeMovieAdapter;
import com.example.cinemabookingapp.ui.customer.model.HomeBannerItem;
import com.example.cinemabookingapp.ui.customer.model.HomeMovieItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.cinemabookingapp.domain.usecase.banner.GetBannersUseCase;
import com.example.cinemabookingapp.domain.model.Banner;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import com.example.cinemabookingapp.ui.customer.cinema_contents.CinemaFragment;
import com.example.cinemabookingapp.ui.customer.profile.ProfileFragment;


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

    private final List<HomeBannerItem> bannerItems = new ArrayList<>();
    private final List<HomeMovieItem> allMovies = new ArrayList<>();
    private final List<HomeMovieItem> visibleMovies = new ArrayList<>();
    private final List<View> bannerDotViews = new ArrayList<>();

    private final HomeBannerAdapter bannerAdapter = new HomeBannerAdapter();
    private final HomeMovieAdapter movieAdapter = new HomeMovieAdapter();

    private GetMoviesUseCase getMoviesUseCase;
    private String currentMovieFilter = FILTER_NOW_SHOWING;
    private GetBannersUseCase getBannersUseCase;

    private View scrollContent;
    private View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initMovieUseCase();
        initBottomNav();
        showHomeScreen();
//        initBottomNav();

//        applyBottomNavState(0);
//        applyFilterStyle(currentMovieFilter);
        loadMoviesFromFirestore();

        initBannerUseCase();
        loadBannersFromFirestore();
    }

    private void initViews() {
        viewPagerBanner = findViewById(R.id.viewPagerBanner);
        bannerDots = findViewById(R.id.bannerDots);
        btnNowShowing = findViewById(R.id.btnNowShowing);
        btnComingSoon = findViewById(R.id.btnComingSoon);
        btnLocation = findViewById(R.id.btnLocation);
        rvMovies = findViewById(R.id.rvMovies);
        scrollContent = findViewById(R.id.scrollContent);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        rvMovies.setNestedScrollingEnabled(false);
        rvMovies.setAdapter(movieAdapter);
        movieAdapter.setOnMovieClickListener(item -> openMovieDetail(item));
        movieAdapter.setOnMovieClickListener(item -> {
            Intent intent = new Intent(this, MovieDetailActivity.class);

            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, item.getMovieId());
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE, item.getTitle());
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_URL, item.getImageUrl());
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_RATING, item.getRating());
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_AGE_RATING, item.getAgeRating());

            startActivity(intent);
        });

        viewPagerBanner.setAdapter(bannerAdapter);
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateBannerDots(position);
            }
        });

        btnNowShowing.setOnClickListener(v -> {
            currentMovieFilter = FILTER_NOW_SHOWING;
            showMovies(currentMovieFilter);
            applyFilterStyle(currentMovieFilter);
        });

        btnComingSoon.setOnClickListener(v -> {
            currentMovieFilter = FILTER_COMING_SOON;
            showMovies(currentMovieFilter);
            applyFilterStyle(currentMovieFilter);
        });

        btnLocation.setOnClickListener(v -> showToast("Chọn khu vực sau"));
    }

    private void initMovieUseCase() {
        MovieRemoteDataSource remoteDataSource = new MovieRemoteDataSource();
        MovieRepository movieRepository = new MovieRepositoryImpl(remoteDataSource);
        getMoviesUseCase = new GetMoviesUseCase(movieRepository);
    }

    private void initBannerUseCase() {
        getBannersUseCase = appContainer.getBannersUseCase();
    }

    private void loadMoviesFromFirestore() {
        if (getMoviesUseCase == null) {
            showToast("Chưa khởi tạo movie use case");
            return;
        }

        getMoviesUseCase.execute(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                allMovies.clear();

                if (movies != null) {
                    for (Movie movie : movies) {
                        HomeMovieItem item = mapMovieToHomeMovieItem(movie);
                        if (item != null) {
                            allMovies.add(item);
                        }
                    }
                }

                showMovies(currentMovieFilter);
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage != null ? errorMessage : "Không thể tải danh sách phim");
            }
        });
    }

    private HomeMovieItem mapMovieToHomeMovieItem(Movie movie) {
        if (movie == null) {
            return null;
        }

        String title = firstNonEmpty(readString(movie, "title"), "");
        String imageUrl = firstNonEmpty(
                readString(movie, "imageUrl", "posterUrl"),
                ""
        );
        String rating = firstNonEmpty(
                readString(movie, "rating", "ratingAvg"),
                ""
        );
        String ageRating = firstNonEmpty(
                readString(movie, "ageRating", "age"),
                ""
        );
        String status = normalizeStatus(firstNonEmpty(
                readString(movie, "status"),
                FILTER_NOW_SHOWING
        ));

        return new HomeMovieItem(
                movie.movieId,
                title,
                imageUrl,
                rating,
                ageRating,
                status
        );
    }

    private void loadBannersFromFirestore() {

        getBannersUseCase.execute(new ResultCallback<List<Banner>>() {
            @Override
            public void onSuccess(List<Banner> banners) {
                bannerItems.clear();

                if (banners != null) {
                    for (Banner banner : banners) {
                        bannerItems.add(new HomeBannerItem(banner.imageUrl));
                    }
                }

                bannerAdapter.setBanners(bannerItems);

                setupBannerDots(bannerItems.size());
                updateBannerDots(0);
                viewPagerBanner.setCurrentItem(0, false);
            }

            @Override
            public void onError(String errorMessage) {
                showToast("Lỗi load banner");
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

    private void showMovies(String filter) {
        visibleMovies.clear();
        for (HomeMovieItem item : allMovies) {
            if (item != null && filter.equals(item.getStatus())) {
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

        navHomeCard.setOnClickListener(v -> showHomeScreen());
        navShowtimeCard.setOnClickListener(v -> showRapPhimScreen());
        navCartCard.setOnClickListener(v -> applyBottomNavState(2));
        navMovieCard.setOnClickListener(v -> showCinemaScreen());
        navProfileCard.setOnClickListener(v -> showProfileScreen());
    }

    private void applyBottomNavState(int index) {
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

    private String readString(Object target, String... possibleNames) {
        Object value = readProperty(target, possibleNames);
        return value == null ? null : String.valueOf(value);
    }

    private Object readProperty(Object target, String... possibleNames) {
        if (target == null || possibleNames == null) {
            return null;
        }

        Class<?> clazz = target.getClass();

        for (String name : possibleNames) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            String capitalized = capitalize(name);

            try {
                Method getter = clazz.getMethod("get" + capitalized);
                return getter.invoke(target);
            } catch (Exception ignored) {
            }

            try {
                Method getter = clazz.getMethod("is" + capitalized);
                return getter.invoke(target);
            } catch (Exception ignored) {
            }

            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String firstNonEmpty(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        return second;
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return FILTER_NOW_SHOWING;
        }

        String normalized = status.trim().toUpperCase(Locale.getDefault());
        if ("NOW SHOWING".equals(normalized)) {
            return FILTER_NOW_SHOWING;
        }
        if ("COMING SOON".equals(normalized)) {
            return FILTER_COMING_SOON;
        }
        return normalized;
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void openMovieDetail(HomeMovieItem item) {
        if (item == null) {
            showToast("Không thể mở chi tiết phim");
            return;
        }

        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, item.getMovieId());
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE, item.getTitle());
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_URL, item.getImageUrl());
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_RATING, item.getRating());
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_AGE_RATING, item.getAgeRating());
        startActivity(intent);
    }

    private void showHomeScreen() {
        fragmentContainer.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
        applyBottomNavState(0);
    }

    private void showCinemaScreen() {
        scrollContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        Fragment fragment = new CinemaFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        applyBottomNavState(3);
    }

    private void showProfileScreen() {
        scrollContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        Fragment fragment = new ProfileFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        applyBottomNavState(4);
    }

    private void showRapPhimScreen() {
        scrollContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        Fragment fragment = new com.example.cinemabookingapp.ui.customer.cinema.CinemaFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        applyBottomNavState(1);
    }
}
