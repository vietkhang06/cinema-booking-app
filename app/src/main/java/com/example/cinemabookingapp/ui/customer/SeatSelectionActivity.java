package com.example.cinemabookingapp.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;

import com.example.cinemabookingapp.data.dto.SeatDTO;
import com.example.cinemabookingapp.ui.customer.adapter.SeatAdapter;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.SeatApiService;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SeatSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SHOWTIME_ID = "showtimeId";
    public static final String EXTRA_MOVIE_TITLE = "movieTitle";
    public static final String EXTRA_MOVIE_ID = "movieId";
    public static final String EXTRA_POSTER_URL = "posterUrl";
    public static final String EXTRA_BASE_PRICE = "basePrice";
    public static final String EXTRA_SHOWTIME_START = "showtimeStart";
    public static final String EXTRA_CINEMA_NAME = "cinemaName";

    private SeatAdapter adapter;

    private final List<SeatDTO> seatList = new ArrayList<>();

    private LinearLayout llSelectedSeatChips;
    private HorizontalScrollView scrollSelectedSeats;
    private View dividerBottom;

    private TextView tvMovieTitle;
    private TextView tvTotalPrice;
    private TextView tvSeatCount;
    private TextView tvShowtimeDate;

    private MaterialButton btnContinue;

    private String showtimeId;
    private String movieTitle;
    private String movieId;
    private String posterUrl;
    private String cinemaName;

    private double basePrice = 85000;
    private long showtimeStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        getIntentData();

        initViews();

        loadSeats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSeats();
    }

    private void getIntentData() {
        showtimeId = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        posterUrl = getIntent().getStringExtra(EXTRA_POSTER_URL);
        cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);

        basePrice = getIntent().getDoubleExtra(EXTRA_BASE_PRICE, 85000);

        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);
    }

    private void initViews() {

        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvSeatCount = findViewById(R.id.tvSeatCount);
        tvShowtimeDate = findViewById(R.id.tvShowtimeDate);

        btnContinue = findViewById(R.id.btnContinue);

        llSelectedSeatChips = findViewById(R.id.llSelectedSeatChips);
        scrollSelectedSeats = findViewById(R.id.scrollSelectedSeats);
        dividerBottom = findViewById(R.id.dividerBottom);

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (tvMovieTitle != null) {
            tvMovieTitle.setText(
                    movieTitle != null ? movieTitle : "Chọn ghế"
            );
        }

        if (showtimeStart > 0) {

            SimpleDateFormat dateFmt =
                    new SimpleDateFormat("dd 'Th'M", new Locale("vi"));

            SimpleDateFormat timeFmt =
                    new SimpleDateFormat("HH:mm", Locale.getDefault());

            tvShowtimeDate.setText(
                    dateFmt.format(new Date(showtimeStart))
                            + " • "
                            + timeFmt.format(new Date(showtimeStart))
            );
        }

        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);

        rvSeatMap.setLayoutManager(new GridLayoutManager(this, 9));

        adapter = new SeatAdapter(seatList, (seat, position) -> {

            if ("booked".equalsIgnoreCase(seat.status)) {
                return;
            }

            if (!seat.isSelected && getSelectedSeats().size() >= 5) {

                Toast.makeText(
                        this,
                        "Bạn chỉ có thể chọn tối đa 5 ghế",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            seat.isSelected = !seat.isSelected;

            adapter.notifyItemChanged(position);

            updateBottomBar();
        });

        rvSeatMap.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {

            List<SeatDTO> selected = getSelectedSeats();

            if (selected.isEmpty()) {

                Toast.makeText(
                        this,
                        "Vui lòng chọn ít nhất 1 ghế!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            goToBookingConfirm(selected);
        });

        updateBottomBar();
    }

    private void loadSeats() {
        if (showtimeId == null) {
            loadDummySeats();
            return;
        }

        SeatApiService api = RetrofitClient.getInstance().create(SeatApiService.class);
        api.getSeatsByShowtimeId(showtimeId).enqueue(new retrofit2.Callback<ApiResponse<List<SeatDTO>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<SeatDTO>>> call, retrofit2.Response<ApiResponse<List<SeatDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SeatDTO> apiSeats = response.body().getData();
                    if (apiSeats != null) {
                        seatList.clear();
                        for (SeatDTO s : apiSeats) {
                            s.isSelected = false;
                            seatList.add(s);
                        }
                        // Sort A1 -> F8
                        seatList.sort((a, b) -> {
                            if (a.rowName == null || b.rowName == null) return 0;
                            int r = a.rowName.compareTo(b.rowName);
                            return r != 0 ? r : Integer.compare(a.columnNo, b.columnNo);
                        });
                        adapter.setSeats(seatList);
                    }
                } else {
                    loadDummySeats();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<SeatDTO>>> call, Throwable t) {
                loadDummySeats();
            }
        });
    }

    private void loadDummySeats() {

        String[] rows = {"A", "B", "C", "D", "E", "F"};

        for (String row : rows) {

            for (int col = 1; col <= 8; col++) {

                SeatDTO s = new SeatDTO();

                s.seatCode = row + col;
                s.rowName = row;
                s.columnNo = col;

                s.seatType =
                        (row.equals("C") || row.equals("D"))
                                ? "VIP"
                                : "STANDARD";

                s.status =
                        (col == 4)
                                ? "booked"
                                : "available";

                s.isSelected = false;

                seatList.add(s);
            }
        }

        adapter.setSeats(seatList);
    }

    private void updateBottomBar() {

        List<SeatDTO> selected = getSelectedSeats();

        double total = 0;

        for (SeatDTO s : selected) {

            total +=
                    (s.priceOverride > 0)
                            ? s.priceOverride
                            : basePrice;
        }

        tvTotalPrice.setText(
                String.format(
                        Locale.getDefault(),
                        "%,.0f đ",
                        total
                )
        );

        tvSeatCount.setText(selected.size() + " Ghế");

        llSelectedSeatChips.removeAllViews();

        if (selected.isEmpty()) {

            scrollSelectedSeats.setVisibility(View.GONE);

            dividerBottom.setVisibility(View.GONE);

        } else {

            scrollSelectedSeats.setVisibility(View.VISIBLE);

            dividerBottom.setVisibility(View.VISIBLE);

            for (SeatDTO s : selected) {

                TextView chip = new TextView(this);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMarginEnd(8);

                chip.setLayoutParams(params);

                chip.setText(s.seatCode);

                chip.setTextColor(0xFFFFFFFF);

                chip.setTextSize(12f);

                chip.setPadding(24, 8, 24, 8);

                if ("VIP".equalsIgnoreCase(s.seatType)) {

                    chip.setBackground(
                            getDrawable(R.drawable.bg_chip_vip)
                    );

                } else {

                    chip.setBackground(
                            getDrawable(R.drawable.bg_chip_selected)
                    );
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

            if (s.seatId != null) {
                seatIds.add(s.seatId);
            }

            total +=
                    (s.priceOverride > 0)
                            ? s.priceOverride
                            : basePrice;
        }

        Intent intent =
                new Intent(
                        this,
                        BookingConfirmActivity.class
                );

        intent.putExtra(
                BookingConfirmActivity.EXTRA_SHOWTIME_ID,
                showtimeId
        );

        intent.putExtra(
                BookingConfirmActivity.EXTRA_MOVIE_TITLE,
                movieTitle
        );

        intent.putExtra(
                BookingConfirmActivity.EXTRA_MOVIE_ID,
                movieId
        );

        intent.putExtra(
                BookingConfirmActivity.EXTRA_POSTER_URL,
                posterUrl
        );

        intent.putExtra(
                BookingConfirmActivity.EXTRA_CINEMA_NAME,
                cinemaName
        );

        intent.putExtra(
                BookingConfirmActivity.EXTRA_SHOWTIME_START,
                showtimeStart
        );

        intent.putExtra(
                BookingConfirmActivity.EXTRA_TOTAL,
                total
        );

        intent.putStringArrayListExtra(
                BookingConfirmActivity.EXTRA_SEAT_CODES,
                seatCodes
        );

        intent.putStringArrayListExtra(
                BookingConfirmActivity.EXTRA_SEAT_IDS,
                seatIds
        );

        startActivity(intent);
    }

    private List<SeatDTO> getSelectedSeats() {

        List<SeatDTO> result = new ArrayList<>();

        for (SeatDTO s : seatList) {

            if (s.isSelected) {
                result.add(s);
            }
        }

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}