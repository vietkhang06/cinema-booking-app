package com.example.cinemabookingapp.ui.features.booking;

import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import android.content.Intent;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import android.os.Bundle;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import android.widget.ImageButton;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import android.widget.TextView;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import android.widget.Toast;

import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import com.example.cinemabookingapp.data.dto.SeatDTO;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import com.google.android.material.button.MaterialButton;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import java.text.SimpleDateFormat;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import java.util.ArrayList;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import java.util.Date;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import java.util.List;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import java.util.Locale;
import com.example.cinemabookingapp.ui.features.booking.SeatAdapter;
import android.widget.LinearLayout;

public class SeatSelectionActivity extends BaseActivity {



    public static final String EXTRA_SHOWTIME_ID  = "showtimeId";
    public static final String EXTRA_MOVIE_TITLE  = "movieTitle";
    public static final String EXTRA_MOVIE_ID     = "movieId";
    public static final String EXTRA_POSTER_URL   = "posterUrl";
    public static final String EXTRA_BASE_PRICE   = "basePrice";
    public static final String EXTRA_SHOWTIME_START = "showtimeStart";
    public static final String EXTRA_CINEMA_NAME  = "cinemaName";

    private SeatAdapter adapter;
    private android.widget.LinearLayout llSelectedSeatChips;
    private android.widget.HorizontalScrollView scrollSelectedSeats;
    private android.view.View dividerBottom;
    private final List<SeatDTO> seatList = new ArrayList<>();

    private TextView tvMovieTitle, tvTotalPrice, tvSeatCount, tvShowtimeDate;
    private MaterialButton btnContinue;
    private android.widget.FrameLayout layoutLoading;

    private String showtimeId, movieTitle, movieId, posterUrl, cinemaName;
    private double basePrice = 85000;
    private long showtimeStart;

    private com.google.firebase.firestore.ListenerRegistration seatListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        showtimeId   = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieTitle   = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        movieId      = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        posterUrl    = getIntent().getStringExtra(EXTRA_POSTER_URL);
        cinemaName   = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        basePrice    = getIntent().getDoubleExtra(EXTRA_BASE_PRICE, 85000);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);

        initViews();
        checkPendingBooking();
    }

    private void initViews() {
        tvMovieTitle   = findViewById(R.id.tvMovieTitle);
        tvTotalPrice   = findViewById(R.id.tvTotalPrice);
        tvSeatCount    = findViewById(R.id.tvSeatCount);
        tvShowtimeDate = findViewById(R.id.tvShowtimeDate);
        btnContinue    = findViewById(R.id.btnContinue);

        llSelectedSeatChips = findViewById(R.id.llSelectedSeatChips);
        scrollSelectedSeats = findViewById(R.id.scrollSelectedSeats);
        dividerBottom = findViewById(R.id.dividerBottom);
        layoutLoading = findViewById(R.id.layoutLoading);

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (movieTitle != null) tvMovieTitle.setText(movieTitle);

        // HiÃƒÂ¡Ã‚Â»Ã†â€™n thÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ ngÃƒÆ’Ã‚Â y + giÃƒÂ¡Ã‚Â»Ã‚Â chiÃƒÂ¡Ã‚ÂºÃ‚Â¿u
        if (showtimeStart > 0) {
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd 'Th'M", new Locale("vi"));
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvShowtimeDate.setText(dateFmt.format(new Date(showtimeStart))
                    + " ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ " + timeFmt.format(new Date(showtimeStart)));
        }

        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);
        rvSeatMap.setLayoutManager(new GridLayoutManager(this, 9));

        adapter = new SeatAdapter(seatList, (seat, position) -> {
            long now = System.currentTimeMillis();
            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                    ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "";

            boolean isBooked = "booked".equalsIgnoreCase(seat.status);
            boolean isHeldByOther = "held".equalsIgnoreCase(seat.status)
                    && (seat.heldUntil > now)
                    && !currentUserId.equals(seat.heldBy);
            boolean isLocked = "LOCKED".equalsIgnoreCase(seat.status)
                    || "LOCKED".equalsIgnoreCase(seat.seatType);

            if (isLocked) {
                Toast.makeText(this, "GhÃƒÂ¡Ã‚ÂºÃ‚Â¿ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ khÃƒÆ’Ã‚Â³a!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isBooked) {
                Toast.makeText(this, "GhÃƒÂ¡Ã‚ÂºÃ‚Â¿ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isHeldByOther) {
                Toast.makeText(this, "GhÃƒÂ¡Ã‚ÂºÃ‚Â¿ Ãƒâ€žÃ¢â‚¬Ëœang Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi khÃƒÆ’Ã‚Â¡c giÃƒÂ¡Ã‚Â»Ã‚Â¯!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enforce maximum 5 seats selection
            if (!seat.isSelected) {
                if (getSelectedSeats().size() >= 5) {
                    Toast.makeText(this, "BÃƒÂ¡Ã‚ÂºÃ‚Â¡n chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â° cÃƒÆ’Ã‚Â³ thÃƒÂ¡Ã‚Â»Ã†â€™ chÃƒÂ¡Ã‚Â»Ã‚Ân tÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi Ãƒâ€žÃ¢â‚¬Ëœa 5 ghÃƒÂ¡Ã‚ÂºÃ‚Â¿", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            seat.isSelected = !seat.isSelected;
            adapter.notifyItemChanged(position);
            updateBottomBar();
        });
        rvSeatMap.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            List<SeatDTO> selected = getSelectedSeats();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Vui lÃƒÆ’Ã‚Â²ng chÃƒÂ¡Ã‚Â»Ã‚Ân ÃƒÆ’Ã‚Â­t nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t 1 ghÃƒÂ¡Ã‚ÂºÃ‚Â¿!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasEmptySeatInBetween(selected)) {
                Toast.makeText(this, "KhÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t vÃƒÆ’Ã‚Â© nÃƒÂ¡Ã‚ÂºÃ‚Â¿u cÃƒÆ’Ã‚Â²n ghÃƒÂ¡Ã‚ÂºÃ‚Â¿ trÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng ÃƒÂ¡Ã‚Â»Ã…Â¸ giÃƒÂ¡Ã‚Â»Ã‚Â¯a trong cÃƒÆ’Ã‚Â¹ng mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t hÃƒÆ’Ã‚Â ng!", Toast.LENGTH_LONG).show();
                return;
            }

            btnContinue.setEnabled(false);
            if (layoutLoading != null) layoutLoading.setVisibility(android.view.View.VISIBLE);

            List<String> selectedSeatIds = new ArrayList<>();
            for (SeatDTO s : selected) {
                if (s.seatId != null) selectedSeatIds.add(s.seatId);
            }

            com.example.cinemabookingapp.data.dto.SeatLockRequestDTO lockRequest =
                    new com.example.cinemabookingapp.data.dto.SeatLockRequestDTO(showtimeId, selectedSeatIds);

            com.example.cinemabookingapp.data.remote.api.SeatApiService seatApi =
                    com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance()
                            .create(com.example.cinemabookingapp.data.remote.api.SeatApiService.class);

            seatApi.lockSeats(lockRequest).enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<Void>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> call, retrofit2.Response<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> response) {
                    btnContinue.setEnabled(true);
                    if (layoutLoading != null) layoutLoading.setVisibility(android.view.View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        goToBookingConfirm(selected);
                    } else {
                        String errMsg = "GhÃƒÂ¡Ã‚ÂºÃ‚Â¿ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ cÃƒÆ’Ã‚Â³ ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi khÃƒÆ’Ã‚Â¡c chÃƒÂ¡Ã‚Â»Ã‚Ân hoÃƒÂ¡Ã‚ÂºÃ‚Â·c hÃƒÂ¡Ã‚ÂºÃ‚Â¿t hÃƒÂ¡Ã‚ÂºÃ‚Â¡n khÃƒÆ’Ã‚Â³a ghÃƒÂ¡Ã‚ÂºÃ‚Â¿. Vui lÃƒÆ’Ã‚Â²ng chÃƒÂ¡Ã‚Â»Ã‚Ân ghÃƒÂ¡Ã‚ÂºÃ‚Â¿ khÃƒÆ’Ã‚Â¡c!";
                        try {
                            if (response.errorBody() != null) {
                                String errorJson = response.errorBody().string();
                                com.example.cinemabookingapp.data.dto.ApiResponse<?> apiError = 
                                        new com.google.gson.Gson().fromJson(errorJson, com.example.cinemabookingapp.data.dto.ApiResponse.class);
                                if (apiError != null && apiError.getMessage() != null) {
                                    errMsg = apiError.getMessage();
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        if (response.code() == 404) {
                            errMsg = "KhÃƒÆ’Ã‚Â´ng thÃƒÂ¡Ã‚Â»Ã†â€™ kÃƒÂ¡Ã‚ÂºÃ‚Â¿t nÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¿n hÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t vÃƒÆ’Ã‚Â©. Vui lÃƒÆ’Ã‚Â²ng thÃƒÂ¡Ã‚Â»Ã‚Â­ lÃƒÂ¡Ã‚ÂºÃ‚Â¡i sau.";
                        } else if (response.code() == 409) {
                            if (errMsg.equals("GhÃƒÂ¡Ã‚ÂºÃ‚Â¿ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ cÃƒÆ’Ã‚Â³ ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi khÃƒÆ’Ã‚Â¡c chÃƒÂ¡Ã‚Â»Ã‚Ân hoÃƒÂ¡Ã‚ÂºÃ‚Â·c hÃƒÂ¡Ã‚ÂºÃ‚Â¿t hÃƒÂ¡Ã‚ÂºÃ‚Â¡n khÃƒÆ’Ã‚Â³a ghÃƒÂ¡Ã‚ÂºÃ‚Â¿. Vui lÃƒÆ’Ã‚Â²ng chÃƒÂ¡Ã‚Â»Ã‚Ân ghÃƒÂ¡Ã‚ÂºÃ‚Â¿ khÃƒÆ’Ã‚Â¡c!")) {
                                errMsg = "RÃƒÂ¡Ã‚ÂºÃ‚Â¥t tiÃƒÂ¡Ã‚ÂºÃ‚Â¿c, ghÃƒÂ¡Ã‚ÂºÃ‚Â¿ bÃƒÂ¡Ã‚ÂºÃ‚Â¡n chÃƒÂ¡Ã‚Â»Ã‚Ân Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ cÃƒÆ’Ã‚Â³ ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi giÃƒÂ¡Ã‚Â»Ã‚Â¯. Vui lÃƒÆ’Ã‚Â²ng chÃƒÂ¡Ã‚Â»Ã‚Ân ghÃƒÂ¡Ã‚ÂºÃ‚Â¿ khÃƒÆ’Ã‚Â¡c!";
                            }
                        } else if (response.code() == 401 || response.code() == 403) {
                            errMsg = "PhiÃƒÆ’Ã‚Âªn Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ hÃƒÂ¡Ã‚ÂºÃ‚Â¿t hÃƒÂ¡Ã‚ÂºÃ‚Â¡n. Vui lÃƒÆ’Ã‚Â²ng Ãƒâ€žÃ¢â‚¬ËœÃƒâ€žÃ†â€™ng nhÃƒÂ¡Ã‚ÂºÃ‚Â­p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i.";
                        } else if (response.code() >= 500) {
                            errMsg = "HÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng Ãƒâ€žÃ¢â‚¬Ëœang bÃƒÂ¡Ã‚ÂºÃ‚Â­n. Vui lÃƒÆ’Ã‚Â²ng thÃƒÂ¡Ã‚Â»Ã‚Â­ lÃƒÂ¡Ã‚ÂºÃ‚Â¡i sau.";
                        }
                        Toast.makeText(SeatSelectionActivity.this, errMsg, Toast.LENGTH_LONG).show();
                        loadSeats(); // Refresh seat map
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> call, Throwable t) {
                    btnContinue.setEnabled(true);
                    if (layoutLoading != null) layoutLoading.setVisibility(android.view.View.GONE);
                    Toast.makeText(SeatSelectionActivity.this, "KÃƒÂ¡Ã‚ÂºÃ‚Â¿t nÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi mÃƒÂ¡Ã‚ÂºÃ‚Â¡ng khÃƒÆ’Ã‚Â´ng ÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢n Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh. Vui lÃƒÆ’Ã‚Â²ng kiÃƒÂ¡Ã‚Â»Ã†â€™m tra lÃƒÂ¡Ã‚ÂºÃ‚Â¡i Wifi/4G.", Toast.LENGTH_LONG).show();
                }
            });
        });


    }

    private void checkPendingBooking() {
        if (showtimeId == null) {
            loadSeats();
            return;
        }

        com.example.cinemabookingapp.data.remote.api.BookingApiService bookingApi =
                com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance()
                        .create(com.example.cinemabookingapp.data.remote.api.BookingApiService.class);

        bookingApi.getPendingBooking(showtimeId).enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<com.example.cinemabookingapp.data.dto.BookingDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<com.example.cinemabookingapp.data.dto.BookingDTO>> call, retrofit2.Response<com.example.cinemabookingapp.data.dto.ApiResponse<com.example.cinemabookingapp.data.dto.BookingDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    com.example.cinemabookingapp.data.dto.BookingDTO pendingBooking = response.body().getData();
                    Toast.makeText(SeatSelectionActivity.this, "BÃƒÂ¡Ã‚ÂºÃ‚Â¡n cÃƒÆ’Ã‚Â³ giao dÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t vÃƒÆ’Ã‚Â© chÃƒâ€ Ã‚Â°a hoÃƒÆ’Ã‚Â n tÃƒÂ¡Ã‚ÂºÃ‚Â¥t. Ãƒâ€žÃ‚Âang chuyÃƒÂ¡Ã‚Â»Ã†â€™n hÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºng...", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SeatSelectionActivity.this, PaymentInstructionActivity.class);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_BOOKING_ID, pendingBooking.bookingId);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_CODE, pendingBooking.paymentCode);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_AMOUNT, pendingBooking.total);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_METHOD, pendingBooking.paymentMethod);
                    intent.putExtra("createdAt", pendingBooking.createdAt);
                    startActivity(intent);
                    finish();
                } else {
                    loadSeats();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<com.example.cinemabookingapp.data.dto.BookingDTO>> call, Throwable t) {
                loadSeats();
            }
        });
    }

    private void loadSeats() {
        if (showtimeId == null) { loadDummySeats(); return; }

        com.example.cinemabookingapp.data.remote.api.SeatApiService seatApi =
                com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance()
                        .create(com.example.cinemabookingapp.data.remote.api.SeatApiService.class);

        seatApi.getSeatsByShowtimeId(showtimeId).enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>> call, retrofit2.Response<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Initial load successful. Now start realtime Firestore sync!
                    startRealtimeSeatSync();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "LÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i server (" + response.code() + ")";
                    Toast.makeText(SeatSelectionActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>> call, Throwable t) {
                Toast.makeText(SeatSelectionActivity.this, "LÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i kÃƒÂ¡Ã‚ÂºÃ‚Â¿t nÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startRealtimeSeatSync() {
        if (showtimeId == null) return;

        if (seatListenerRegistration != null) {
            seatListenerRegistration.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        seatListenerRegistration = db.collection("seats")
                .whereEqualTo("showtimeId", showtimeId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        android.util.Log.e("SEAT_SYNC", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        // 1. Keep track of currently selected seat IDs
                        java.util.Set<String> currentlySelectedIds = new java.util.HashSet<>();
                        for (SeatDTO s : seatList) {
                            if (s.isSelected) {
                                currentlySelectedIds.add(s.seatId);
                            }
                        }

                        // 2. Parse new seats from Firestore snapshot
                        List<SeatDTO> newSeats = new ArrayList<>();
                        int maxCol = 1;
                        boolean seatStolen = false;
                        String stolenSeatCode = "";

                        long now = System.currentTimeMillis();
                        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                                : "";

                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            SeatDTO seat = doc.toObject(SeatDTO.class);
                            if (seat != null) {
                                seat.seatId = doc.getId();

                                boolean isAvailable = "available".equalsIgnoreCase(seat.status)
                                        || ("held".equalsIgnoreCase(seat.status) && seat.heldUntil < now);

                                boolean isHeldByMe = "held".equalsIgnoreCase(seat.status)
                                        && (seat.heldUntil >= now)
                                        && currentUserId.equals(seat.heldBy);

                                // Check if this seat was selected by me previously
                                if (currentlySelectedIds.contains(seat.seatId)) {
                                    if (isAvailable || isHeldByMe) {
                                        seat.isSelected = true;
                                    } else {
                                        // Stolen! The status has changed to booked or held by someone else!
                                        seat.isSelected = false;
                                        seatStolen = true;
                                        stolenSeatCode = seat.seatCode;
                                    }
                                } else {
                                    seat.isSelected = false;
                                }
                                newSeats.add(seat);
                                if (seat.columnNo > maxCol) {
                                    maxCol = seat.columnNo;
                                }
                            }
                        }

                        if (seatStolen) {
                            Toast.makeText(SeatSelectionActivity.this,
                                    "GhÃƒÂ¡Ã‚ÂºÃ‚Â¿ " + stolenSeatCode + " Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi khÃƒÆ’Ã‚Â¡c giÃƒÂ¡Ã‚Â»Ã‚Â¯ hoÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc!",
                                    Toast.LENGTH_LONG).show();
                        }

                        // 3. Sort A1 -> F8
                        newSeats.sort((a, b) -> {
                            if (a.rowName == null || b.rowName == null) return 0;
                            int r = a.rowName.compareTo(b.rowName);
                            return r != 0 ? r : Integer.compare(a.columnNo, b.columnNo);
                        });

                        // 4. Update the seatList and UI
                        seatList.clear();
                        seatList.addAll(newSeats);

                        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);
                        if (rvSeatMap != null && rvSeatMap.getLayoutManager() instanceof GridLayoutManager) {
                            ((GridLayoutManager) rvSeatMap.getLayoutManager()).setSpanCount(maxCol + 1);
                        }

                        adapter.setSeats(seatList);
                        updateBottomBar();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (seatListenerRegistration != null) {
            seatListenerRegistration.remove();
            seatListenerRegistration = null;
        }
    }

    private void loadDummySeats() {
        String[] rows = {"A","B","C","D","E","F"};
        for (String row : rows) {
            for (int col = 1; col <= 8; col++) {
                SeatDTO s = new SeatDTO();
                s.seatCode = row + String.format(Locale.getDefault(), "%02d", col);
                s.rowName = row;
                s.columnNo = col;
                s.seatType = (row.equals("C") || row.equals("D")) ? "VIP" : "STANDARD";
                s.status = (col == 4) ? "booked" : "available";
                s.isSelected = false;
                seatList.add(s);
            }
        }
        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);
        if (rvSeatMap != null && rvSeatMap.getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) rvSeatMap.getLayoutManager()).setSpanCount(9);
        }
        adapter.setSeats(seatList);
    }

    private void updateBottomBar() {
        List<SeatDTO> selected = getSelectedSeats();
        double total = 0;

        for (SeatDTO s : selected) {
            double price = "VIP".equalsIgnoreCase(s.seatType) ? 75000 : 60000;
            total += price;
        }

        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f Ãƒâ€žÃ¢â‚¬Ëœ", total));
        tvSeatCount.setText(selected.size() + " GhÃƒÂ¡Ã‚ÂºÃ‚Â¿");

        // CÃƒÂ¡Ã‚ÂºÃ‚Â­p nhÃƒÂ¡Ã‚ÂºÃ‚Â­t chips ghÃƒÂ¡Ã‚ÂºÃ‚Â¿ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ chÃƒÂ¡Ã‚Â»Ã‚Ân
        llSelectedSeatChips.removeAllViews();

        if (selected.isEmpty()) {
            scrollSelectedSeats.setVisibility(android.view.View.GONE);
            dividerBottom.setVisibility(android.view.View.GONE);
        } else {
            scrollSelectedSeats.setVisibility(android.view.View.VISIBLE);
            dividerBottom.setVisibility(android.view.View.VISIBLE);

            for (SeatDTO s : selected) {
                TextView chip = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(8);
                chip.setLayoutParams(params);
                chip.setText(s.seatCode);
                chip.setTextColor(0xFFFFFFFF);
                chip.setTextSize(12f);
                chip.setPadding(24, 8, 24, 8);

                if ("VIP".equalsIgnoreCase(s.seatType)) {
                    chip.setBackgroundTintList(null);
                    chip.setBackground(getDrawable(R.drawable.bg_chip_vip));
                } else {
                    chip.setBackground(getDrawable(R.drawable.bg_chip_selected));
                }
                llSelectedSeatChips.addView(chip);
            }
        }
    }

    private void goToBookingConfirm(List<SeatDTO> selected) {
        ArrayList<String> seatCodes = new ArrayList<>();
        ArrayList<String> seatIds = new ArrayList<>();
        double total = 0;

        for (SeatDTO s : selected) {
            seatCodes.add(s.seatCode);
            if (s.seatId != null) seatIds.add(s.seatId);
            double price = "VIP".equalsIgnoreCase(s.seatType) ? 75000 : 60000;
            total += price;
        }

        Intent intent = new Intent(this, BookingConfirmActivity.class);
        intent.putExtra(BookingConfirmActivity.EXTRA_SHOWTIME_ID, showtimeId);
        intent.putExtra(BookingConfirmActivity.EXTRA_MOVIE_TITLE, movieTitle);
        intent.putExtra(BookingConfirmActivity.EXTRA_MOVIE_ID, movieId);
        intent.putExtra(BookingConfirmActivity.EXTRA_POSTER_URL, posterUrl);
        intent.putExtra(BookingConfirmActivity.EXTRA_CINEMA_NAME, cinemaName);
        intent.putExtra(BookingConfirmActivity.EXTRA_SHOWTIME_START, showtimeStart);
        intent.putExtra(BookingConfirmActivity.EXTRA_TOTAL, total);
        intent.putStringArrayListExtra(BookingConfirmActivity.EXTRA_SEAT_CODES, seatCodes);
        intent.putStringArrayListExtra(BookingConfirmActivity.EXTRA_SEAT_IDS, seatIds);
        startActivity(intent);
    }

    private List<SeatDTO> getSelectedSeats() {
        List<SeatDTO> result = new ArrayList<>();
        for (SeatDTO s : seatList) if (s.isSelected) result.add(s);
        return result;
    }

    private boolean hasEmptySeatInBetween(List<SeatDTO> selected) {
        java.util.Map<String, List<SeatDTO>> selectedByRow = new java.util.HashMap<>();
        for (SeatDTO s : selected) {
            if (s.rowName != null) {
                if (!selectedByRow.containsKey(s.rowName)) {
                    selectedByRow.put(s.rowName, new ArrayList<>());
                }
                selectedByRow.get(s.rowName).add(s);
            }
        }

        java.util.Map<String, List<SeatDTO>> allByRow = new java.util.HashMap<>();
        for (SeatDTO s : seatList) {
            if (s.rowName != null) {
                if (!allByRow.containsKey(s.rowName)) {
                    allByRow.put(s.rowName, new ArrayList<>());
                }
                allByRow.get(s.rowName).add(s);
            }
        }

        long now = System.currentTimeMillis();

        for (java.util.Map.Entry<String, List<SeatDTO>> entry : selectedByRow.entrySet()) {
            String rowName = entry.getKey();
            List<SeatDTO> rowSelected = entry.getValue();
            List<SeatDTO> rowAll = allByRow.get(rowName);
            if (rowAll == null) continue;

            int minCol = Integer.MAX_VALUE;
            int maxCol = Integer.MIN_VALUE;
            for (SeatDTO s : rowSelected) {
                if (true) {
                    if (s.columnNo < minCol) minCol = s.columnNo;
                    if (s.columnNo > maxCol) maxCol = s.columnNo;
                }
            }

            if (minCol != Integer.MAX_VALUE && maxCol != Integer.MIN_VALUE && maxCol > minCol) {
                for (SeatDTO seat : rowAll) {
                    if (true) {
                        int col = seat.columnNo;
                        if (col > minCol && col < maxCol) {
                            boolean isSel = false;
                            for (SeatDTO sel : rowSelected) {
                                if (sel.seatId != null && sel.seatId.equals(seat.seatId)) {
                                    isSel = true;
                                    break;
                                }
                            }
                            if (!isSel) {
                                boolean isAvailable = "available".equalsIgnoreCase(seat.status)
                                        || seat.status == null
                                        || seat.status.isEmpty()
                                        || ("held".equalsIgnoreCase(seat.status) && (seat.heldUntil < now));
                                if (isAvailable) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}