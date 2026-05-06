package com.example.cinemabookingapp.ui.customer.cinema;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.example.cinemabookingapp.domain.repository.CinemaRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CinemaDetailActivity extends BaseActivity {

    public static final String EXTRA_CINEMA_ID = "extra_cinema_id";
    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_CINEMA_CITY = "extra_cinema_city";
    public static final String EXTRA_CINEMA_DISTRICT = "extra_cinema_district";
    public static final String EXTRA_CINEMA_PHONE = "extra_cinema_phone";
    public static final String EXTRA_CINEMA_STATUS = "extra_cinema_status";
    public static final String EXTRA_CINEMA_LATITUDE = "extra_cinema_latitude";
    public static final String EXTRA_CINEMA_LONGITUDE = "extra_cinema_longitude";

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView imgHeroBackdrop;
    private ImageView imgCinemaLogo;
    private TextView tvCinemaTitle;
    private TextView tvCinemaTagline;
    private TextView tvStatus;
    private TextView tvCityDistrict;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvAbout;
    private TextView tvMapInfo;
    private MaterialButton btnOpenMap;
    private MaterialButton btnCall;
    private LinearLayout layoutNowShowing;
    private LinearLayout layoutComingSoon;

    private CinemaRepository cinemaRepository;
    private String cinemaId = "";
    private String cinemaName = "Rap phim";
    private String address = "";
    private String city = "";
    private String district = "";
    private String phone = "";
    private String status = "";
    private double latitude = 0;
    private double longitude = 0;

    public static Intent createIntent(Context context, Cinema cinema) {
        Intent intent = new Intent(context, CinemaDetailActivity.class);
        if (cinema == null) {
            return intent;
        }
        intent.putExtra(EXTRA_CINEMA_ID, cinema.cinemaId);
        intent.putExtra(EXTRA_CINEMA_NAME, cinema.name);
        intent.putExtra(EXTRA_CINEMA_ADDRESS, cinema.address);
        intent.putExtra(EXTRA_CINEMA_CITY, cinema.city);
        intent.putExtra(EXTRA_CINEMA_DISTRICT, cinema.district);
        intent.putExtra(EXTRA_CINEMA_PHONE, cinema.phone);
        intent.putExtra(EXTRA_CINEMA_STATUS, cinema.status);
        intent.putExtra(EXTRA_CINEMA_LATITUDE, cinema.latitude);
        intent.putExtra(EXTRA_CINEMA_LONGITUDE, cinema.longitude);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_cinema);

        cinemaRepository = new CinemaRepositoryImpl();
        initViews();
        bindFallbackExtras();
        setupActions();
        renderMockShowtimes();
        loadCinemaFromFirestore();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        imgHeroBackdrop = findViewById(R.id.imgHeroBackdrop);
        imgCinemaLogo = findViewById(R.id.imgCinemaLogo);
        tvCinemaTitle = findViewById(R.id.tvCinemaTitle);
        tvCinemaTagline = findViewById(R.id.tvCinemaTagline);
        tvStatus = findViewById(R.id.tvStatus);
        tvCityDistrict = findViewById(R.id.tvCityDistrict);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvCinemaFullAddress);
        tvAbout = findViewById(R.id.tvAbout);
        tvMapInfo = findViewById(R.id.tvMapInfo);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnCall = findViewById(R.id.btnCall);
        layoutNowShowing = findViewById(R.id.layoutNowShowing);
        layoutComingSoon = findViewById(R.id.layoutComingSoon);
    }

    private void bindFallbackExtras() {
        Intent intent = getIntent();
        cinemaId = safe(intent.getStringExtra(EXTRA_CINEMA_ID), cinemaId);
        cinemaName = safe(intent.getStringExtra(EXTRA_CINEMA_NAME), cinemaName);
        address = safe(intent.getStringExtra(EXTRA_CINEMA_ADDRESS), address);
        city = safe(intent.getStringExtra(EXTRA_CINEMA_CITY), city);
        district = safe(intent.getStringExtra(EXTRA_CINEMA_DISTRICT), district);
        phone = safe(intent.getStringExtra(EXTRA_CINEMA_PHONE), phone);
        status = safe(intent.getStringExtra(EXTRA_CINEMA_STATUS), status);
        latitude = intent.getDoubleExtra(EXTRA_CINEMA_LATITUDE, latitude);
        longitude = intent.getDoubleExtra(EXTRA_CINEMA_LONGITUDE, longitude);

        bindCinemaInfo();
        loadCinemaImage();
    }

    private void loadCinemaFromFirestore() {
        if (TextUtils.isEmpty(cinemaId)) {
            return;
        }

        cinemaRepository.getCinemaById(cinemaId, new ResultCallback<Cinema>() {
            @Override
            public void onSuccess(Cinema cinema) {
                if (cinema == null) {
                    return;
                }

                cinemaName = safe(cinema.name, cinemaName);
                address = safe(cinema.address, address);
                city = safe(cinema.city, city);
                district = safe(cinema.district, district);
                phone = safe(cinema.phone, phone);
                status = safe(cinema.status, status);
                latitude = cinema.latitude;
                longitude = cinema.longitude;
                bindCinemaInfo();
            }

            @Override
            public void onError(String message) {
                showToast(message == null ? "Khong the tai thong tin rap" : message);
            }
        });
    }

    private void bindCinemaInfo() {
        tvCinemaTitle.setText(safe(cinemaName, "Rap phim"));
        tvCinemaTagline.setText(buildLocationText());
        tvStatus.setText(TextUtils.isEmpty(status) ? "Dang hoat dong" : status);
        tvCityDistrict.setText(buildLocationText());
        tvPhone.setText(TextUtils.isEmpty(phone) ? "Chua cap nhat hotline" : phone);
        tvAddress.setText(TextUtils.isEmpty(address) ? "Chua cap nhat dia chi" : address);
        tvAbout.setText("Khong gian rap duoc thiet ke cho trai nghiem xem phim thoai mai, am thanh ro net va khu vuc ghe ngoi hien dai.");
        tvMapInfo.setText(hasCoordinate()
                ? String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude)
                : "Chua cap nhat toa do");
    }

    private void loadCinemaImage() {
        Glide.with(this)
                .load(R.drawable.ic_cinemax_logo)
                .into(imgHeroBackdrop);
        Glide.with(this)
                .load(R.drawable.ic_cinemax_logo)
                .into(imgCinemaLogo);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> shareCinema());
        btnOpenMap.setOnClickListener(v -> openMap());
        btnCall.setOnClickListener(v -> callCinema());
    }

    private void shareCinema() {
        String shareText = cinemaName + "\n" + tvAddress.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText.trim());
        startActivity(Intent.createChooser(intent, "Chia se rap phim"));
    }

    private void openMap() {
        Uri uri;
        if (hasCoordinate()) {
            uri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + Uri.encode(cinemaName) + ")");
        } else if (!TextUtils.isEmpty(address)) {
            uri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        } else {
            showToast("Chua co dia chi de mo ban do");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (Exception e) {
            showToast("Khong the mo ban do");
        }
    }

    private void callCinema() {
        if (TextUtils.isEmpty(phone)) {
            showToast("Chua co so dien thoai");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void renderMockShowtimes() {
        layoutNowShowing.removeAllViews();
        layoutComingSoon.removeAllViews();

        List<CinemaMovieSchedule> nowShowing = Arrays.asList(
                new CinemaMovieSchedule("Lat Mat 8", "Hanh dong - Hai", "T13", "2D Phu de",
                        Arrays.asList("09:30", "12:10", "15:20", "18:40", "21:30")),
                new CinemaMovieSchedule("Dia Dao", "Chien tranh - Lich su", "T16", "2D",
                        Arrays.asList("10:15", "13:25", "16:35", "20:00")),
                new CinemaMovieSchedule("Thunderbolts", "Hanh dong - Sieu anh hung", "T13", "IMAX 2D",
                        Arrays.asList("11:00", "14:10", "17:20", "19:45", "22:15"))
        );

        List<CinemaMovieSchedule> comingSoon = Arrays.asList(
                new CinemaMovieSchedule("Mission: Impossible", "Khoi chieu 23/05", "T13", "Dat ve som",
                        Arrays.asList("Sap cap nhat")),
                new CinemaMovieSchedule("Elio", "Khoi chieu 13/06", "P", "Hoat hinh",
                        Arrays.asList("Sap cap nhat")),
                new CinemaMovieSchedule("How to Train Your Dragon", "Khoi chieu 20/06", "P", "2D - 3D",
                        Arrays.asList("Sap cap nhat"))
        );

        for (CinemaMovieSchedule schedule : nowShowing) {
            layoutNowShowing.addView(buildScheduleCard(schedule, true));
        }

        for (CinemaMovieSchedule schedule : comingSoon) {
            layoutComingSoon.addView(buildScheduleCard(schedule, false));
        }
    }

    private View buildScheduleCard(CinemaMovieSchedule schedule, boolean bookable) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E6E6E6")));
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleGroup = new LinearLayout(this);
        titleGroup.setOrientation(LinearLayout.VERTICAL);
        titleGroup.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(schedule.title);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);

        TextView meta = new TextView(this);
        meta.setText(schedule.meta);
        meta.setTextColor(Color.parseColor("#666666"));
        meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metaParams.setMargins(0, dp(4), 0, 0);
        meta.setLayoutParams(metaParams);

        titleGroup.addView(title);
        titleGroup.addView(meta);

        TextView age = new TextView(this);
        age.setText(schedule.ageRating);
        age.setGravity(Gravity.CENTER);
        age.setTextColor(Color.parseColor("#B56B00"));
        age.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        age.setTypeface(age.getTypeface(), Typeface.BOLD);
        age.setBackgroundColor(Color.parseColor("#FFF4D8"));
        age.setPadding(dp(10), 0, dp(10), 0);
        age.setMinHeight(dp(28));

        header.addView(titleGroup);
        header.addView(age);
        body.addView(header);

        TextView format = new TextView(this);
        LinearLayout.LayoutParams formatParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(28)
        );
        formatParams.setMargins(0, dp(12), 0, 0);
        format.setLayoutParams(formatParams);
        format.setGravity(Gravity.CENTER);
        format.setPadding(dp(10), 0, dp(10), 0);
        format.setText(schedule.format);
        format.setTextColor(Color.parseColor("#1E1A23"));
        format.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        format.setTypeface(format.getTypeface(), Typeface.BOLD);
        format.setBackgroundResource(R.drawable.bg_cinema_status);
        body.addView(format);

        LinearLayout timesWrap = new LinearLayout(this);
        LinearLayout.LayoutParams timesWrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timesWrapParams.setMargins(0, dp(12), 0, 0);
        timesWrap.setLayoutParams(timesWrapParams);
        timesWrap.setOrientation(LinearLayout.VERTICAL);

        for (int start = 0; start < schedule.times.size(); start += 3) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            int end = Math.min(start + 3, schedule.times.size());
            for (int index = start; index < end; index++) {
                MaterialButton timeButton = buildTimeButton(schedule, schedule.times.get(index), bookable);
                row.addView(timeButton);
            }
            timesWrap.addView(row);
        }

        body.addView(timesWrap);
        card.addView(body);
        return card;
    }

    private MaterialButton buildTimeButton(CinemaMovieSchedule schedule, String time, boolean bookable) {
        MaterialButton button = new MaterialButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(40), 1f);
        params.setMargins(0, 0, dp(8), dp(8));
        button.setLayoutParams(params);
        button.setText(time);
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setCornerRadius(dp(12));
        button.setStrokeWidth(dp(1));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor(bookable ? "#D8D8D8" : "#E5E7EB")));
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(bookable ? "#FFFFFF" : "#F8FAFC")));
        button.setTextColor(Color.parseColor(bookable ? "#111111" : "#9CA3AF"));
        button.setEnabled(bookable);
        button.setOnClickListener(v -> showToast(schedule.title + " - " + time));
        return button;
    }

    private boolean hasCoordinate() {
        return latitude != 0 || longitude != 0;
    }

    private String buildLocationText() {
        if (!TextUtils.isEmpty(district) && !TextUtils.isEmpty(city)) {
            return district + ", " + city;
        }
        if (!TextUtils.isEmpty(city)) {
            return city;
        }
        if (!TextUtils.isEmpty(district)) {
            return district;
        }
        return "Khu vuc dang cap nhat";
    }

    private String safe(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class CinemaMovieSchedule {
        final String title;
        final String meta;
        final String ageRating;
        final String format;
        final List<String> times;

        CinemaMovieSchedule(String title, String meta, String ageRating, String format, List<String> times) {
            this.title = title;
            this.meta = meta;
            this.ageRating = ageRating;
            this.format = format;
            this.times = times;
        }
    }
}
