package com.example.cinemabookingapp.ui.customer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.SeatActionRequest;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.SeatApiService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BookingConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_SHOWTIME_ID   = "showtimeId";
    public static final String EXTRA_MOVIE_TITLE   = "movieTitle";
    public static final String EXTRA_MOVIE_ID      = "movieId";
    public static final String EXTRA_POSTER_URL    = "posterUrl";
    public static final String EXTRA_CINEMA_NAME   = "cinemaName";
    public static final String EXTRA_SHOWTIME_START = "showtimeStart";
    public static final String EXTRA_TOTAL         = "total";
    public static final String EXTRA_SEAT_CODES    = "seatCodes";
    public static final String EXTRA_SEAT_IDS      = "seatIds";

    private String showtimeId, movieTitle, movieId, cinemaName, imageUrl;
    private long showtimeStart;
    private double total;
    private ArrayList<String> seatCodes, seatIds;
    
    // Timer related
    private TextView tvTimer;
    private CountDownTimer seatTimer;
    private static final long SEAT_HOLD_DURATION_MS = 7 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirm);

        // Nhận data
        showtimeId   = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieTitle   = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        movieId      = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        cinemaName   = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        imageUrl     = getIntent().getStringExtra(EXTRA_POSTER_URL);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);
        total        = getIntent().getDoubleExtra(EXTRA_TOTAL, 0);
        seatCodes    = getIntent().getStringArrayListExtra(EXTRA_SEAT_CODES);
        seatIds      = getIntent().getStringArrayListExtra(EXTRA_SEAT_IDS);

        initViews();
        lockSeats();
        startTimer();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bind data lên UI
        TextView tvMovie = findViewById(R.id.tvMovieName);
        TextView tvCinema = findViewById(R.id.tvCinemaName);
        TextView tvTime = findViewById(R.id.tvShowtime);
        TextView tvSeats = findViewById(R.id.tvSeats);
        TextView tvTotal = findViewById(R.id.tvTotal);

        if (tvMovie != null && movieTitle != null) tvMovie.setText(movieTitle);
        if (tvCinema != null && cinemaName != null) tvCinema.setText(cinemaName);

        if (tvTime != null && showtimeStart > 0) {
            SimpleDateFormat fmt = new SimpleDateFormat(
                    "HH:mm - dd/MM/yyyy", Locale.getDefault());
            tvTime.setText(fmt.format(new Date(showtimeStart)));
        }

        if (tvSeats != null && seatCodes != null)
            tvSeats.setText(String.join(", ", seatCodes));

        if (tvTotal != null)
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", total));

        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> confirmBooking());
        }
        
        tvTimer = findViewById(R.id.tvTimerBadge);
    }
    
    private void startTimer() {
        if (tvTimer == null) return;
        
        seatTimer = new CountDownTimer(SEAT_HOLD_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(formatTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                handleTimeout();
            }
        }.start();
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long min = seconds / 60;
        long sec = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
    }

    private void handleTimeout() {
        Toast.makeText(this, "Hết thời gian giữ ghế!", Toast.LENGTH_LONG).show();
        unlockSeats();
        finish();
    }

    private void lockSeats() {
        if (seatIds == null || seatIds.isEmpty()) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";
        
        SeatApiService api = RetrofitClient.getInstance().create(SeatApiService.class);
        api.lockSeats(new SeatActionRequest(seatIds, userId)).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(BookingConfirmActivity.this, "Không thể giữ ghế, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                // Ignore failure for now or handle accordingly
            }
        });
    }

    private void unlockSeats() {
        if (seatIds == null || seatIds.isEmpty()) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";
        
        SeatApiService api = RetrofitClient.getInstance().create(SeatApiService.class);
        api.unlockSeats(new SeatActionRequest(seatIds, userId)).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {}
            @Override public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {}
        });
    }

    @Override
    public void onBackPressed() {
        unlockSeats();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (seatTimer != null) {
            seatTimer.cancel();
        }
        // In case of non-timeout/non-back exit (e.g. killed) 
        // usually we'd rely on backend TTL, but let's try to release
        super.onDestroy();
    }

    private void confirmBooking() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        String bookingId = "booking_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();

        Map<String, Object> booking = new HashMap<>();
        booking.put("bookingId", bookingId);
        booking.put("userId", userId);
        booking.put("movieId", movieId);
        booking.put("showtimeId", showtimeId);
        booking.put("cinemaNameSnapshot", cinemaName);
        booking.put("movieTitleSnapshot", movieTitle);
        booking.put("movieImageUrlSnapshot", imageUrl != null ? imageUrl : "");
        booking.put("showtimeStartAtSnapshot", showtimeStart);
        booking.put("seatCodes", seatCodes);
        booking.put("seatIds", seatIds != null ? seatIds : new ArrayList<>());
        booking.put("total", total);
        booking.put("paymentMethod", "cash");
        booking.put("paymentStatus", "pending");
        booking.put("bookingStatus", "confirmed");
        booking.put("createdAt", now);
        booking.put("updatedAt", now);
        booking.put("deleted", false);

        android.util.Log.d("BookingConfirm", "Saving booking with poster: " + imageUrl);

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .document(bookingId)
                .set(booking)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate sang màn hình thành công
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}