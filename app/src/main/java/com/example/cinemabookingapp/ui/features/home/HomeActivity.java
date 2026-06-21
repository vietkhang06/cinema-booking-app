package com.example.cinemabookingapp.ui.features.home;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.movie.MovieDetailActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cineshop.CineShopFragment;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cinema.CinemaFragment;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.content.res.ColorStateList;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.graphics.Color;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.os.Bundle;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.util.Log;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.view.View;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.view.ViewGroup;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.widget.ImageView;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.widget.LinearLayout;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.widget.TextView;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.core.widget.ImageViewCompat;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.repository.MovieRepository;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.usecase.movie.GetMoviesUseCase;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.service.BookingService;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.home.adapter.HomeBannerAdapter;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.home.adapter.HomeMovieAdapter;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.home.model.HomeBannerItem;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.home.model.HomeMovieItem;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.android.material.button.MaterialButton;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.android.material.card.MaterialCardView;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.lang.reflect.Field;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.lang.reflect.Method;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.util.ArrayList;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.util.List;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.util.Locale;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.usecase.banner.GetBannersUseCase;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.model.Banner;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.content.Intent;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.fragment.app.Fragment;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cinema_contents.CinemaContentFragment;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.profile.ProfileFragment;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cinema.LocationFilterAdapter;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cinema.LocationBottomSheetFragment;



import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.android.material.chip.Chip;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.android.material.chip.ChipGroup;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.util.LinkedHashSet;

import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.firebase.auth.FirebaseUser;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.chat.CustomerSupportActivity;


public class HomeActivity extends BaseActivity {

    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabSupportChat;

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
    
    private TextView navProfileBadge;
    private com.google.firebase.firestore.ListenerRegistration notificationListener;
    private com.google.firebase.firestore.ListenerRegistration moviesListener;

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
    private com.google.android.material.chip.ChipGroup chipGroupGenre;
    private String selectedGenre = "Tất cả";

    // ── Featured Movie Popup ──
    // Static: lives for the entire app process, resets only on full restart
    private static boolean sPopupShownThisSession = false;
    private MovieRepository movieRepository;


    @Override
    protected void onStart() {
        super.onStart();
        listenToNotifications();
        loadMoviesFromFirestore();
    }

    private void listenToNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        com.example.cinemabookingapp.domain.repository.NotificationRepository repo = new com.example.cinemabookingapp.data.repository.NotificationRepositoryImpl();
        notificationListener = repo.listenToUserNotifications(user.getUid(), new ResultCallback<List<com.example.cinemabookingapp.domain.model.Notification>>() {
            @Override
            public void onSuccess(List<com.example.cinemabookingapp.domain.model.Notification> result) {
                int unreadCount = 0;
                if (result != null) {
                    for (com.example.cinemabookingapp.domain.model.Notification notif : result) {
                        if (!notif.isRead) {
                            unreadCount++;
                        }
                    }
                }
                
                if (navProfileBadge != null) {
                    if (unreadCount > 0) {
                        navProfileBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
                        navProfileBadge.setVisibility(View.VISIBLE);
                    } else {
                        navProfileBadge.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
        }
        if (moviesListener != null) {
            moviesListener.remove();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.getIdToken(true)
                    .addOnSuccessListener(result -> {
                        String token = result.getToken();

                        Log.d("FIREBASE_TOKEN",
                                token != null ? token : "NULL");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE_TOKEN",
                                "Failed to get token", e);
                    });
        }

        setContentView(R.layout.fragment_home);

        BookingService bookingService =
                ServiceProvider.getInstance()
                        .getBookingService();

        bookingService.getMyBookings(new ResultCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> data) {
                Log.d("BOOKING_TEST",
                        "SIZE = " + data.size());
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("BOOKING_TEST", errorMessage);
            }
        });

        initViews();
        initMovieUseCase();
        initBottomNav();
        showHomeScreen();
//        initBottomNav();

//        applyBottomNavState(0);
        loadMoviesFromFirestore();
        initBannerUseCase();
        loadBannersFromFirestore();

        // Show featured popup once per session (not for Admin)
        maybeShowFeaturedPopup();
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

        btnLocation.setOnClickListener(v -> {
            LocationBottomSheetFragment sheet =
                    LocationBottomSheetFragment.newInstance("Toan quoc");

            sheet.setOnLocationSelectedListener(location -> {
                btnLocation.setText(location);
            });

            sheet.show(getSupportFragmentManager(), "location_picker");
        });
        chipGroupGenre = findViewById(R.id.chipGroupGenre);
        fabSupportChat = findViewById(R.id.fabSupportChat);
        fabSupportChat.setOnClickListener(v -> {
            com.example.cinemabookingapp.domain.model.User cachedProfile = ServiceProvider.getInstance().getProfileService().getCachedProfile();
            String currentUid = null;
            if (cachedProfile != null) {
                currentUid = cachedProfile.uid;
            } else {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    currentUid = firebaseUser.getUid();
                }
            }

            if (currentUid == null) {
                showToast("Bạn cần đăng nhập tài khoản để tiếp tục.");
                AppNavigator.goToLoginForBooking(HomeActivity.this);
            } else {
                Intent intent = new Intent(HomeActivity.this, CustomerSupportActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initMovieUseCase() {
        MovieRemoteDataSource remoteDataSource = new MovieRemoteDataSource();
        movieRepository = new MovieRepositoryImpl(remoteDataSource);
        getMoviesUseCase = new GetMoviesUseCase(movieRepository);
    }

    private void initBannerUseCase() {
        getBannersUseCase = appContainer.getBannersUseCase();
    }

    private void loadMoviesFromFirestore() {
        if (moviesListener != null) {
            moviesListener.remove();
        }
        
        moviesListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("movies")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("HomeActivity", "Failed to listen for movies", e);
                        return;
                    }
                    if (snapshot != null) {
                        allMovies.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                            Movie movie = doc.toObject(Movie.class);
                            if (!movie.deleted) {
                                HomeMovieItem item = mapMovieToHomeMovieItem(movie);
                                if (item != null) {
                                    allMovies.add(item);
                                }
                            }
                        }
                        
                        buildGenreChipsFromData();
                        showMovies(currentMovieFilter);
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
        String ratingVal = firstNonEmpty(
                readString(movie, "ratingAvg", "rating"),
                ""
        );
        String rating = "";
        if (!ratingVal.isEmpty()) {
            try {
                double r = Double.parseDouble(ratingVal);
                rating = String.format(java.util.Locale.getDefault(), "â˜… %.1f", r);
            } catch (Exception e) {
                rating = "â˜… " + ratingVal;
            }
        }
        String ageRating = firstNonEmpty(
                readString(movie, "ageRating", "age"),
                ""
        );
        String status = normalizeStatus(firstNonEmpty(
                readString(movie, "status"),
                FILTER_NOW_SHOWING
        ));
        List<String> genres = movie.genres != null ? movie.genres : new ArrayList<>();

        return new HomeMovieItem(
                movie.movieId,
                title,
                imageUrl,
                rating,
                ageRating,
                status,
                genres
        );
    }

    private void loadBannersFromFirestore() {

        getBannersUseCase.execute(new ResultCallback<List<Banner>>() {
            @Override
            public void onSuccess(List<Banner> banners) {
                bannerItems.clear();

                if (banners != null) {
                    for (Banner banner : banners) {
                        bannerItems.add(new HomeBannerItem(banner.bannerId, banner.imageUrl));
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
            if (item == null) continue;

            boolean matchStatus = filter.equals(item.getStatus());
            boolean matchGenre = "Tất cả".equals(selectedGenre)
                    || item.getGenres().contains(selectedGenre);

            if (matchStatus && matchGenre) {
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
        button.setBackgroundTintList(ColorStateList.valueOf(
                selected ? activeColor : Color.WHITE
        ));
        button.setTextColor(selected ? activeTint : activeColor);
        button.setStrokeWidth(0);
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
        navCartCard.setOnClickListener(v -> showCineShopScreen());
        navMovieCard.setOnClickListener(v -> showCinemaScreen());
        navProfileCard.setOnClickListener(v -> showProfileScreen());
    }

    private void applyBottomNavState(int index) {
        applyBottomState(navHomeCard, navHomeLabel, navHomeIcon, index == 0, "Trang Ch\u1ee7");
        applyBottomState(navShowtimeCard, navShowtimeLabel, navShowtimeIcon, index == 1, "Rạp Phim");
        applyBottomState(navCartCard, navCartLabel, navCartIcon, index == 2, "Cine Shop");
        applyBottomState(navMovieCard, navMovieLabel, navMovieIcon, index == 3, "Điện Ảnh");
        applyBottomState(navProfileCard, navProfileLabel, navProfileIcon, index == 4, "Tài Khoản");
        bottomNavContainer.requestLayout();
    }

    private void applyBottomState(MaterialCardView card, TextView label, ImageView icon, boolean selected, String text) {
        LinearLayout.LayoutParams params;
        if (selected) {
            params = new LinearLayout.LayoutParams(0, dp(48), 1.2f);
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(Color.parseColor("#121212")); // TÃ´ Ä‘en nhÆ° áº£nh
            card.setStrokeWidth(0);
            label.setText(text);
            label.setVisibility(View.VISIBLE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.WHITE));
            label.setTextColor(Color.WHITE);
            card.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start();
        } else {
            params = new LinearLayout.LayoutParams(dp(48), dp(48));
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

        currentMovieFilter = FILTER_NOW_SHOWING;
        applyFilterStyle(currentMovieFilter);
        showMovies(currentMovieFilter);
    }


    private void showCinemaScreen() {
        scrollContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        Fragment fragment = new CinemaContentFragment();
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

    private void showCineShopScreen() {
        scrollContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        Fragment fragment = new CineShopFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        applyBottomNavState(2);
    }

    private void showRapPhimScreen() {
        scrollContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        Fragment fragment = new com.example.cinemabookingapp.ui.features.cinema.CinemaFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        applyBottomNavState(1);
    }
    private void buildGenreChipsFromData() {
        LinkedHashSet<String> genreSet = new LinkedHashSet<>();
        genreSet.add("Tất cả");
        for (HomeMovieItem item : allMovies) {
            for (String g : item.getGenres()) {
                if (g != null && !g.trim().isEmpty()) {
                    genreSet.add(g.trim());
                }
            }
        }

        chipGroupGenre.removeAllViews();
        selectedGenre = "Tất cả";

        for (String genre : genreSet) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setChecked(genre.equals("Tất cả"));

            chip.setTextSize(10f);
            chip.setChipMinHeight(dp(36));
            chip.setChipCornerRadius(dp(18));
            chip.setChipStartPadding(dp(12));
            chip.setChipEndPadding(dp(12));
            chip.setTextStartPadding(0f);
            chip.setTextEndPadding(0f);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setCheckedIconVisible(false);

            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
            chip.setTextColor(ColorStateList.valueOf(Color.parseColor("#1E1A23")));
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#1E1A23")));
            chip.setChipStrokeWidth(dp(1));
            chip.setRippleColor(ColorStateList.valueOf(Color.parseColor("#33000000")));

            if (genre.equals("Tất cả")) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1E1A23")));
                chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
            }

            chip.setOnCheckedChangeListener((v, checked) -> {
                if (checked) {
                    selectedGenre = genre;
                    chip.setChipBackgroundColor(
                            ColorStateList.valueOf(Color.parseColor("#1E1A23")));
                    chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
                } else {
                    chip.setChipBackgroundColor(
                            ColorStateList.valueOf(Color.WHITE));
                    chip.setTextColor(
                            ColorStateList.valueOf(Color.parseColor("#1E1A23")));
                }
                showMovies(currentMovieFilter);
            });

            chipGroupGenre.addView(chip);
        }
    }

    // ─────────────────────────────────────────────
    // Featured Movie Popup
    // ─────────────────────────────────────────────

    private void maybeShowFeaturedPopup() {
        // Only show once per app session
        if (sPopupShownThisSession) return;

        // Skip for Admin accounts
        com.google.firebase.auth.FirebaseUser firebaseUser =
                FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            com.example.cinemabookingapp.domain.model.User cached =
                    ServiceProvider.getInstance().getProfileService().getCachedProfile();
            if (cached != null && com.example.cinemabookingapp.core.constants.UserRoles.ADMIN.equals(cached.role)) {
                return;
            }
            // Also check SharedPreferences role in case cache is not yet loaded
            com.example.cinemabookingapp.core.session.SessionManager sm =
                    new com.example.cinemabookingapp.core.session.SessionManager(this);
            if (com.example.cinemabookingapp.core.constants.UserRoles.ADMIN.equals(sm.getRole())) {
                return;
            }
        }

        // Delay slightly so Home screen is fully rendered before popup appears
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            movieRepository.getFeaturedPopupMovie(new ResultCallback<Movie>() {
                @Override
                public void onSuccess(Movie movie) {
                    if (movie != null && !isFinishing() && !isDestroyed()) {
                        showFeaturedMoviePopup(movie);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Silently ignore – popup is non-critical
                }
            });
        }, 600);
    }

    private void showFeaturedMoviePopup(Movie movie) {
        sPopupShownThisSession = true;

        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_featured_movie_popup);
        dialog.setCanceledOnTouchOutside(true);

        // Apply fade-in animation to the card
        com.google.android.material.card.MaterialCardView cardPopup =
                dialog.findViewById(R.id.cardPopup);
        android.view.animation.Animation anim =
                android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in_popup);
        if (cardPopup != null) {
            cardPopup.startAnimation(anim);
        }

        // Load poster
        android.widget.ImageView imgPoster = dialog.findViewById(R.id.imgFeaturedPoster);
        if (imgPoster != null && movie.posterUrl != null) {
            com.bumptech.glide.Glide.with(this)
                    .load(movie.posterUrl)
                    .centerCrop()
                    .placeholder(R.drawable.login_icon)
                    .into(imgPoster);
        }

        // Set title
        android.widget.TextView tvTitle = dialog.findViewById(R.id.tvFeaturedTitle);
        if (tvTitle != null && movie.title != null) {
            tvTitle.setText(movie.title);
        }

        // Poster click → open MovieDetailActivity
        if (imgPoster != null) {
            imgPoster.setClickable(true);
            imgPoster.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(HomeActivity.this, MovieDetailActivity.class);
                intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.movieId);
                intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE,
                        movie.title != null ? movie.title : "");
                intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_URL,
                        movie.posterUrl != null ? movie.posterUrl : "");
                startActivity(intent);
            });
        }

        // Card click also opens detail (same as poster)
        if (cardPopup != null) {
            cardPopup.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(HomeActivity.this, MovieDetailActivity.class);
                intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.movieId);
                intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE,
                        movie.title != null ? movie.title : "");
                intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_URL,
                        movie.posterUrl != null ? movie.posterUrl : "");
                startActivity(intent);
            });
        }

        // Close (X) button
        com.google.android.material.card.MaterialCardView btnClose =
                dialog.findViewById(R.id.btnClosePopup);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        // Click outside (dim overlay) also dismisses
        android.widget.FrameLayout rootOverlay = dialog.findViewById(R.id.rootOverlay);
        if (rootOverlay != null) {
            rootOverlay.setOnClickListener(v -> dialog.dismiss());
            // Prevent card clicks from propagating to overlay
            if (cardPopup != null) {
                cardPopup.setOnClickListener(v -> {
                    dialog.dismiss();
                    Intent intent = new Intent(HomeActivity.this, MovieDetailActivity.class);
                    intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.movieId);
                    intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE,
                            movie.title != null ? movie.title : "");
                    intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_URL,
                            movie.posterUrl != null ? movie.posterUrl : "");
                    startActivity(intent);
                });
            }
        }

        dialog.show();
    }
}
