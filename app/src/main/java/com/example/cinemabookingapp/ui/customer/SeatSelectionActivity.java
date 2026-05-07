package com.example.cinemabookingapp.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.SeatDTO;
import com.example.cinemabookingapp.ui.customer.adapter.SeatAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.LinearLayout;

public class SeatSelectionActivity extends AppCompatActivity {

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

    private String showtimeId, movieTitle, movieId, posterUrl, cinemaName;
    private double basePrice = 85000;
    private long showtimeStart;

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
        loadSeats();
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

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (movieTitle != null) tvMovieTitle.setText(movieTitle);

        // Hiển thị ngày + giờ chiếu
        if (showtimeStart > 0) {
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd 'Th'M", new Locale("vi"));
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvShowtimeDate.setText(dateFmt.format(new Date(showtimeStart))
                    + " • " + timeFmt.format(new Date(showtimeStart)));
        }

        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);
        rvSeatMap.setLayoutManager(new GridLayoutManager(this, 9));

        adapter = new SeatAdapter(seatList, (seat, position) -> {
            if ("booked".equalsIgnoreCase(seat.status)) return;
            seat.isSelected = !seat.isSelected;
            adapter.notifyItemChanged(position);
            updateBottomBar();
        });
        rvSeatMap.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            List<SeatDTO> selected = getSelectedSeats();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế!", Toast.LENGTH_SHORT).show();
                return;
            }
            goToBookingConfirm(selected);
        });
    }

    private void loadSeats() {
        if (showtimeId == null) { loadDummySeats(); return; }

        FirebaseFirestore.getInstance()
                .collection("seats")
                .whereEqualTo("showtimeId", showtimeId)
                .get()
                .addOnSuccessListener(snap -> {
                    seatList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        SeatDTO seat = doc.toObject(SeatDTO.class);
                        seat.isSelected = false;
                        seatList.add(seat);
                    }
                    // Sort A1 → F8
                    seatList.sort((a, b) -> {
                        if (a.rowName == null || b.rowName == null) return 0;
                        int r = a.rowName.compareTo(b.rowName);
                        return r != 0 ? r : Integer.compare(a.columnNo, b.columnNo);
                    });
                    adapter.setSeats(seatList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadDummySeats();
                });
    }

    private void loadDummySeats() {
        String[] rows = {"A","B","C","D","E","F"};
        for (String row : rows) {
            for (int col = 1; col <= 8; col++) {
                SeatDTO s = new SeatDTO();
                s.seatCode = row + col;
                s.rowName = row;
                s.columnNo = col;
                s.seatType = (row.equals("C") || row.equals("D")) ? "VIP" : "STANDARD";
                s.status = (col == 4) ? "booked" : "available";
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
            total += (s.priceOverride > 0) ? s.priceOverride : basePrice;
        }

        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        tvSeatCount.setText(selected.size() + " Ghế");

        // Cập nhật chips ghế đã chọn
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
            total += (s.priceOverride > 0) ? s.priceOverride : basePrice;
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
}