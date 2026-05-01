package com.example.cinemabookingapp.ui.customer;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabookingapp.R;
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

    private String showtimeId, movieTitle, movieId, cinemaName, posterUrl;
    private long showtimeStart;
    private double total;
    private ArrayList<String> seatCodes, seatIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirm);

        // Nhận data
        showtimeId   = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieTitle   = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        movieId      = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        cinemaName   = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        posterUrl    = getIntent().getStringExtra(EXTRA_POSTER_URL);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);
        total        = getIntent().getDoubleExtra(EXTRA_TOTAL, 0);
        seatCodes    = getIntent().getStringArrayListExtra(EXTRA_SEAT_CODES);
        seatIds      = getIntent().getStringArrayListExtra(EXTRA_SEAT_IDS);

        initViews();
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