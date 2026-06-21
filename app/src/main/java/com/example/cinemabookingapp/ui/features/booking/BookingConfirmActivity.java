package com.example.cinemabookingapp.ui.features.booking;

import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import android.content.Intent;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import android.os.Bundle;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import android.widget.ImageButton;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import android.widget.TextView;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import android.widget.Toast;

import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.data.dto.BookingDTO;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.data.dto.SeatBookingRequestDTO;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.data.dto.ValidateVoucherRequest;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.data.remote.api.BookingApiService;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.cinemabookingapp.data.remote.api.VoucherApiService;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.material.button.MaterialButton;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Date;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Locale;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Map;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.UUID;

public class BookingConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_SHOWTIME_ID = "showtimeId";
    public static final String EXTRA_MOVIE_TITLE = "movieTitle";
    public static final String EXTRA_MOVIE_ID = "movieId";
    public static final String EXTRA_POSTER_URL = "posterUrl";
    public static final String EXTRA_CINEMA_NAME = "cinemaName";
    public static final String EXTRA_SHOWTIME_START = "showtimeStart";
    public static final String EXTRA_TOTAL = "total";
    public static final String EXTRA_SEAT_CODES = "seatCodes";
    public static final String EXTRA_SEAT_IDS = "seatIds";

    private String showtimeId, movieTitle, movieId, cinemaName, imageUrl;
    private long showtimeStart;
    private double total;
    private ArrayList<String> seatCodes, seatIds;

    private TextView tvTimer;
    private BookingTimerManager.TimerListener timerListener;
    // Bien cuc bo khai bao
    private boolean hasShownWarning = false;

    // Phase 3 Promotion Fields
    private com.example.cinemabookingapp.domain.model.User currentUser;
    private double discountVoucher = 0;
    private double discountRank = 0;
    private double discountStars = 0;
    private String appliedPromoCode = "";
    private boolean isStarsApplied = false;

    private TextView tvOriginalPrice, tvTotal, tvAppliedPromo, tvStarsLabel;
    private com.google.android.material.switchmaterial.SwitchMaterial switchStars;
    private TextView btnPromo;

    // Phase 4 Payment Fields
    private android.widget.RadioGroup rgPayment;
    private String selectedPaymentMethod = "bank";
    private com.google.android.material.bottomsheet.BottomSheetDialog momoDialog;

    // Phase 5 Age Rating Fields
    private TextView tvAgeRatingBadge;
    private String movieAgeRating = "P";

    private boolean isBookingConfirmed = false;

    // Snack order fields
    private android.widget.LinearLayout layoutSnackContainer;
    private final List<com.example.cinemabookingapp.domain.model.Snack> snackList = new ArrayList<>();
    private final java.util.Map<String, Integer> selectedSnacks = new java.util.HashMap<>();
    private double totalSnacksPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirm);

        // NhÃ¡ÂºÂ­n data
        showtimeId = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        imageUrl = getIntent().getStringExtra(EXTRA_POSTER_URL);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);
        total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0);
        seatCodes = getIntent().getStringArrayListExtra(EXTRA_SEAT_CODES);
        seatIds = getIntent().getStringArrayListExtra(EXTRA_SEAT_IDS);

        tvTimer = findViewById(R.id.tvTimer);

        if (!BookingTimerManager.getInstance().isTimerActive(this)) {
            BookingTimerManager.getInstance().startTimer(this, 5 * 60 * 1000);
        } else {
            BookingTimerManager.getInstance().restoreTimer(this);
        }
// Ham dem nguoc thoi gian
        timerListener = new BookingTimerManager.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                if (tvTimer != null) {
                    tvTimer.setText(String.format(Locale.getDefault(), "ThÃ¡Â»Âi gian giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿: %02d:%02d", minutes, seconds));
                }
                if (millisUntilFinished <= 60000 && !hasShownWarning) {
                    hasShownWarning = true;
                    if (!isFinishing() && !isDestroyed()) {
                        new androidx.appcompat.app.AlertDialog.Builder(BookingConfirmActivity.this)
                                .setTitle("ThÃƒÂ´ng bÃƒÂ¡o")
                                .setMessage("ChÃƒÂº ÃƒÂ½ thÃ¡Â»Âi gian giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿ cÃƒÂ²n 1 phÃƒÂºt, xin vui lÃƒÂ²ng thanh toÃƒÂ¡n")
                                .setPositiveButton("Ã„ÂÃƒÂ³ng", (dialog, which) -> dialog.dismiss())
                                .setCancelable(false)
                                .show();
                    }
                }
            }

            @Override
            public void onFinish() {
                if (tvTimer != null) {
                    tvTimer.setText("ThÃ¡Â»Âi gian giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿: 00:00");
                }
                if (momoDialog != null && momoDialog.isShowing()) {
                    momoDialog.dismiss();
                }
                if (!isBookingConfirmed) {
                    releaseLockedSeats();
                }
                Toast.makeText(BookingConfirmActivity.this, "ThÃ¡Â»Âi gian giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿ Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t! Vui lÃƒÂ²ng chÃ¡Â»Ân lÃ¡ÂºÂ¡i.", Toast.LENGTH_LONG).show();
                finish();
            }
        };

        initViews();
        loadUserProfile();
        loadMovieData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView tvMovie = findViewById(R.id.tvMovieName);
        TextView tvCinema = findViewById(R.id.tvCinemaName);
        TextView tvTime = findViewById(R.id.tvShowtime);
        TextView tvSeats = findViewById(R.id.tvSeats);

        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvTotal = findViewById(R.id.tvTotal);
        btnPromo = findViewById(R.id.btnPromo);
        tvAppliedPromo = findViewById(R.id.tvAppliedPromo);
        tvStarsLabel = findViewById(R.id.tvStarsLabel);
        switchStars = findViewById(R.id.switchStars);
        tvAgeRatingBadge = findViewById(R.id.tvAgeRatingBadge);

        if (tvMovie != null && movieTitle != null) tvMovie.setText(movieTitle);
        if (tvCinema != null && cinemaName != null) tvCinema.setText(cinemaName);

        if (tvTime != null && showtimeStart > 0) {
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
            tvTime.setText(fmt.format(new Date(showtimeStart)));
        }

        if (tvSeats != null && seatCodes != null) {
            tvSeats.setText(String.join(", ", seatCodes));
        }

        if (tvTotal != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f Ã„â€˜", total));
        }

        rgPayment = findViewById(R.id.rgPayment);
        if (rgPayment != null) {
            rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbPayBank) {
                    selectedPaymentMethod = "bank";
                } else if (checkedId == R.id.rbPayMomo) {
                    selectedPaymentMethod = "momo";
                }
            });
        }

        if (btnPromo != null) {
            btnPromo.setOnClickListener(v -> showPromoDialog());
        }

        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> checkAgeRatingAndProceed());
        }

        layoutSnackContainer = findViewById(R.id.layoutSnackContainer);
        loadSnacks();
    }

    private void loadMovieData() {
        if (movieId == null || movieId.isEmpty()) return;
        FirebaseFirestore.getInstance()
                .collection("movies")
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        movieAgeRating = documentSnapshot.getString("ageRating");
                        if (movieAgeRating == null) movieAgeRating = "P";
                        updateAgeRatingBadge();
                    }
                });
    }

    private void updateAgeRatingBadge() {
        if (tvAgeRatingBadge == null || movieAgeRating == null) return;
        tvAgeRatingBadge.setText(movieAgeRating);
        tvAgeRatingBadge.setVisibility(android.view.View.VISIBLE);

        int color = 0xFF888888;
        String rating = movieAgeRating.toUpperCase().trim();
        if (rating.contains("18")) {
            color = 0xFFD32F2F;
        } else if (rating.contains("16")) {
            color = 0xFFF57C00;
        } else if (rating.contains("13")) {
            color = 0xFFFBC02D;
        } else if (rating.contains("P")) {
            color = 0xFF388E3C;
        } else if (rating.contains("K")) {
            color = 0xFF1976D2;
        }

        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(10.0f);
        tvAgeRatingBadge.setBackground(gd);
        tvAgeRatingBadge.setTextColor(0xFFFFFFFF);
        tvAgeRatingBadge.setPadding(16, 4, 16, 4);
    }

    private void checkAgeRatingAndProceed() {
        String rating = movieAgeRating != null ? movieAgeRating.toUpperCase().trim() : "P";
        if (rating.contains("18") || rating.contains("16") || rating.contains("13")) {
            int minAge = 18;
            if (rating.contains("16")) minAge = 16;
            else if (rating.contains("13")) minAge = 13;

            showAgeWarningDialog(minAge, this::confirmBooking);
        } else {
            confirmBooking();
        }
    }

    private void showAgeWarningDialog(int minAge, Runnable onConfirm) {
        com.google.android.material.bottomsheet.BottomSheetDialog warnDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_age_warning, null);
        warnDialog.setContentView(view);

        TextView tvWarningTitle = view.findViewById(R.id.tvWarningTitle);
        TextView tvWarningMsg = view.findViewById(R.id.tvWarningMsg);
        android.widget.Button btnCancel = view.findViewById(R.id.btnCancelWarn);
        android.widget.Button btnAgree = view.findViewById(R.id.btnAgreeWarn);

        if (tvWarningTitle != null) {
            tvWarningTitle.setText("XÃƒÂ¡c nhÃ¡ÂºÂ­n Ã„â€˜Ã¡Â»â„¢ tuÃ¡Â»â€¢i tÃ¡Â»â€˜i thiÃ¡Â»Æ’u C" + minAge);
        }

        if (tvWarningMsg != null) {
            tvWarningMsg.setText("Phim nÃƒÂ y cÃƒÂ³ phÃƒÂ¢n loÃ¡ÂºÂ¡i Ã„â€˜Ã¡Â»â„¢ tuÃ¡Â»â€¢i lÃƒÂ  C" + minAge + " - CHÃ¡Â»Ë† DÃƒâ‚¬NH CHO KHÃƒÂN GIÃ¡ÂºÂ¢ TÃ¡Â»Âª " + minAge + " TUÃ¡Â»â€I TRÃ¡Â»Å¾ LÃƒÅ N. Vui lÃƒÂ²ng xÃƒÂ¡c nhÃ¡ÂºÂ­n bÃ¡ÂºÂ¡n Ã„â€˜Ã¡Â»Â§ tuÃ¡Â»â€¢i trÃ†Â°Ã¡Â»â€ºc khi tiÃ¡ÂºÂ¿p tÃ¡Â»Â¥c thanh toÃƒÂ¡n. VÃƒÂ© Ã„â€˜ÃƒÂ£ mua khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c hoÃƒÂ n trÃ¡ÂºÂ£ hoÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¢i trÃ¡ÂºÂ£ nÃ¡ÂºÂ¿u khÃƒÂ´ng Ã„â€˜Ã¡Â»Â§ tuÃ¡Â»â€¢i.");
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> warnDialog.dismiss());
        }

        if (btnAgree != null) {
            btnAgree.setOnClickListener(v -> {
                warnDialog.dismiss();
                onConfirm.run();
            });
        }

        warnDialog.show();
    }

    private void loadUserProfile() {
        new com.example.cinemabookingapp.service.AuthenticationService(this).getCurrentAuthUser(
                new com.example.cinemabookingapp.domain.common.ResultCallback<com.example.cinemabookingapp.domain.model.User>() {
                    @Override
                    public void onSuccess(com.example.cinemabookingapp.domain.model.User user) {
                        if (user != null) {
                            currentUser = user;
                            applyTierDiscount();
                            updateStarsUI();
                            checkAndApplyVouchers(); // TÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng lÃ¡ÂºÂ¥y voucher
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(BookingConfirmActivity.this, "KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ£i thÃƒÂ´ng tin thÃƒÂ nh viÃƒÂªn. Ã†Â¯u Ã„â€˜ÃƒÂ£i thÃ¡ÂºÂ» cÃƒÂ³ thÃ¡Â»Æ’ khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c ÃƒÂ¡p dÃ¡Â»Â¥ng.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private String appliedVoucherId = "";

    private void checkAndApplyVouchers() {
        if (currentUser == null) return;
        
        FirebaseFirestore.getInstance()
                .collection("vouchers")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("isUsed", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && !snapshot.isEmpty()) {
                        // TÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng chÃ¡Â»Ân voucher cÃƒÂ³ giÃƒÂ¡ trÃ¡Â»â€¹ cao nhÃ¡ÂºÂ¥t
                        double maxDiscount = 0;
                        com.google.firebase.firestore.DocumentSnapshot bestVoucher = null;
                        
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                            Double discount = doc.getDouble("discountValue");
                            if (discount != null && discount > maxDiscount) {
                                maxDiscount = discount;
                                bestVoucher = doc;
                            }
                        }
                        
                        if (bestVoucher != null) {
                            appliedVoucherId = bestVoucher.getId();
                            String type = bestVoucher.getString("voucherType");
                            
                            // PhÃƒÂ¢n biÃ¡Â»â€¡t Voucher giÃ¡ÂºÂ£m thÃ¡ÂºÂ³ng (200k) vÃƒÂ  giÃ¡ÂºÂ£m % (10%)
                            if (maxDiscount > 100) {
                                discountVoucher = maxDiscount;
                            } else {
                                discountVoucher = (total + totalSnacksPrice) * (maxDiscount / 100.0);
                            }
                            
                            if (tvAppliedPromo != null) {
                                tvAppliedPromo.setText(String.format(Locale.getDefault(), "Voucher vÃƒÂ­: -%,.0f Ã„â€˜", discountVoucher));
                                tvAppliedPromo.setVisibility(android.view.View.VISIBLE);
                            }
                            updateTotalPrice();
                            Toast.makeText(this, "HÃ¡Â»â€¡ thÃ¡Â»â€˜ng tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng ÃƒÂ¡p dÃ¡Â»Â¥ng Voucher tÃ¡Â»Â« vÃƒÂ­ cÃ¡Â»Â§a bÃ¡ÂºÂ¡n!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void applyTierDiscount() {
        if (currentUser == null || currentUser.memberLevel == null) return;
        String level = currentUser.memberLevel.toLowerCase();
        double factor = 0;
        String levelName = "ThÃƒÂ nh viÃƒÂªn";
        if (level.contains("vip")) {
            factor = 0.10;
            levelName = "VIP";
        } else if (level.contains("platinum")) {
            factor = 0.15;
            levelName = "Platinum";
        } else if (level.contains("gold")) {
            factor = 0.08;
            levelName = "Gold";
        }
        if (factor > 0) {
            discountRank = total * factor;
            if (tvAppliedPromo != null) {
                tvAppliedPromo.setText("Ã„ÂÃƒÂ£ ÃƒÂ¡p dÃ¡Â»Â¥ng Ã†Â°u Ã„â€˜ÃƒÂ£i hÃ¡ÂºÂ¡ng " + levelName + " (-" + (int) (factor * 100) + "%)");
            }
        }
        updateTotalPrice();
    }

    private void updateStarsUI() {
        if (currentUser == null) return;
        int points = (currentUser.points != null) ? currentUser.points : 0;
        if (tvStarsLabel != null) {
            tvStarsLabel.setText(String.format(Locale.getDefault(), "ÃƒÂp dÃ¡Â»Â¥ng Ã„â€˜iÃ¡Â»Æ’m Stars (%d Stars cÃƒÂ³ sÃ¡ÂºÂµn)", points));
        }
        if (switchStars != null) {
            switchStars.setEnabled(points > 0);
            switchStars.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isStarsApplied = isChecked;
                if (isChecked) {
                    discountStars = points * 1000.0;
                } else {
                    discountStars = 0;
                }
                updateTotalPrice();
            });
        }
    }

    private void updateTotalPrice() {
        double finalTotal = (total + totalSnacksPrice) - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

        if (discountVoucher > 0 || discountRank > 0 || discountStars > 0) {
            if (tvOriginalPrice != null) {
                tvOriginalPrice.setVisibility(android.view.View.VISIBLE);
                tvOriginalPrice.setText(String.format(Locale.getDefault(), "%,.0f Ã„â€˜", total + totalSnacksPrice));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }
        } else {
            if (tvOriginalPrice != null) {
                tvOriginalPrice.setVisibility(android.view.View.GONE);
            }
        }

        if (tvTotal != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f Ã„â€˜", finalTotal));
        }
    }

    private void showPromoDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_promo_input, null);
        dialog.setContentView(view);

        android.widget.EditText edtPromoCode = view.findViewById(R.id.edtPromoCode);
        android.widget.Button btnApplyPromo = view.findViewById(R.id.btnApplyPromo);
        TextView tvPromoStatus = view.findViewById(R.id.tvPromoStatus);
        android.widget.Spinner spinnerVouchers = view.findViewById(R.id.spinnerVouchers);

        if (!appliedPromoCode.isEmpty() && edtPromoCode != null) {
            edtPromoCode.setText(appliedPromoCode);
        }

        final java.util.List<DocumentSnapshot> myVouchers = new ArrayList<>();

        if (spinnerVouchers != null && currentUser != null) {
            FirebaseFirestore.getInstance().collection("vouchers")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereEqualTo("isUsed", false)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        myVouchers.clear();
                        List<String> voucherNames = new ArrayList<>();
                        voucherNames.add("--- ChÃ¡Â»Ân Voucher cÃƒÂ¡ nhÃƒÂ¢n ---");

                        if (snapshot != null && !snapshot.isEmpty()) {
                            myVouchers.addAll(snapshot.getDocuments());
                            for (DocumentSnapshot doc : myVouchers) {
                                String type = doc.getString("voucherType");
                                Double discount = doc.getDouble("discountValue");
                                if (discount == null) discount = 0.0;
                                
                                String name = "Voucher hÃ¡Â»â€¡ thÃ¡Â»â€˜ng";
                                if ("WELCOME_VOUCHER".equals(type)) name = "QuÃƒÂ  TÃƒÂ¢n Binh";
                                
                                voucherNames.add(String.format(Locale.getDefault(), "%s (-%,.0f Ã„â€˜)", name, discount));
                            }
                        }

                        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_dropdown_item, voucherNames);
                        spinnerVouchers.setAdapter(adapter);
                    });
        }

        if (btnApplyPromo != null) {
            btnApplyPromo.setOnClickListener(v -> {
                if (spinnerVouchers != null && spinnerVouchers.getSelectedItemPosition() > 0) {
                    // NgÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng Ã„â€˜ÃƒÂ£ chÃ¡Â»Ân Voucher cÃƒÂ¡ nhÃƒÂ¢n trong Dropdown
                    int selectedIndex = spinnerVouchers.getSelectedItemPosition() - 1;
                    DocumentSnapshot selectedVoucher = myVouchers.get(selectedIndex);
                    
                    appliedVoucherId = selectedVoucher.getId();
                    String type = selectedVoucher.getString("voucherType");
                    Double discount = selectedVoucher.getDouble("discountValue");
                    if (discount == null) discount = 0.0;

                    if (discount > 100) {
                        discountVoucher = discount;
                    } else {
                        discountVoucher = (total + totalSnacksPrice) * (discount / 100.0);
                    }

                    appliedPromoCode = ""; 
                    if (tvAppliedPromo != null) {
                        String name = "Voucher hÃ¡Â»â€¡ thÃ¡Â»â€˜ng";
                        if ("WELCOME_VOUCHER".equals(type)) name = "QuÃƒÂ  TÃƒÂ¢n Binh";
                        tvAppliedPromo.setText(String.format(Locale.getDefault(), "Ã„ÂÃƒÂ£ ÃƒÂ¡p dÃ¡Â»Â¥ng: %s (-%,.0f Ã„â€˜)", name, discountVoucher));
                        tvAppliedPromo.setVisibility(android.view.View.VISIBLE);
                        tvAppliedPromo.setTextColor(0xFF4CAF50);
                    }
                    
                    updateTotalPrice();
                    dialog.dismiss();
                    Toast.makeText(this, "ÃƒÂp dÃ¡Â»Â¥ng Voucher cÃƒÂ¡ nhÃƒÂ¢n thÃƒÂ nh cÃƒÂ´ng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (edtPromoCode == null) return;

                String code = edtPromoCode.getText().toString().trim().toUpperCase(Locale.getDefault());
                if (code.isEmpty()) {
                    Toast.makeText(this, "Vui lÃƒÂ²ng chÃ¡Â»Ân Voucher hoÃ¡ÂºÂ·c nhÃ¡ÂºÂ­p mÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double subtotal = total + totalSnacksPrice;

                btnApplyPromo.setEnabled(false);
                if (tvPromoStatus != null) {
                    tvPromoStatus.setVisibility(android.view.View.VISIBLE);
                    tvPromoStatus.setText("Ã„Âang kiÃ¡Â»Æ’m tra mÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i...");
                }

                FirebaseFirestore.getInstance()
                        .collection("promotions") // nÃ¡ÂºÂ¿u collection cÃ¡Â»Â§a bÃ¡ÂºÂ¡n tÃƒÂªn khÃƒÂ¡c, Ã„â€˜Ã¡Â»â€¢i Ã¡Â»Å¸ Ã„â€˜ÃƒÂ¢y
                        .whereEqualTo("code", code)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            btnApplyPromo.setEnabled(true);

                            if (snapshot == null || snapshot.isEmpty()) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡ hoÃ¡ÂºÂ·c Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n!");
                                return;
                            }

                            DocumentSnapshot doc = snapshot.getDocuments().get(0);

                            String status = doc.getString("status");
                            Boolean deleted = doc.getBoolean("deleted");
                            Long validFrom = doc.getLong("validFrom");
                            Long validTo = doc.getLong("validTo");
                            Long usageLimit = doc.getLong("usageLimit");
                            Long usedCount = doc.getLong("usedCount");
                            Double minAmount = doc.getDouble("minAmount");
                            String targetRole = doc.getString("targetRole");
                            String discountType = doc.getString("discountType");
                            Double discountValue = doc.getDouble("discountValue");
                            Double maxDiscountAmount = doc.getDouble("maxDiscountAmount");
                            String title = doc.getString("title");

                            long now = System.currentTimeMillis();

                            if (!"active".equalsIgnoreCase(status)) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i khÃƒÂ´ng cÃƒÂ²n hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng!");
                                return;
                            }

                            if (Boolean.TRUE.equals(deleted)) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ xoÃƒÂ¡!");
                                return;
                            }

                            if (validFrom != null && now < validFrom) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i chÃ†Â°a Ã„â€˜Ã¡ÂºÂ¿n thÃ¡Â»Âi gian ÃƒÂ¡p dÃ¡Â»Â¥ng!");
                                return;
                            }

                            if (validTo != null && now > validTo) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n!");
                                return;
                            }

                            if (usageLimit != null && usedCount != null && usedCount >= usageLimit) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t lÃ†Â°Ã¡Â»Â£t sÃ¡Â»Â­ dÃ¡Â»Â¥ng!");
                                return;
                            }

                            if (minAmount != null && subtotal < minAmount) {
                                showPromoInvalid(tvPromoStatus,
                                        String.format(Locale.getDefault(),
                                                "Ã„ÂÃ†Â¡n hÃƒÂ ng phÃ¡ÂºÂ£i tÃ¡Â»â€˜i thiÃ¡Â»Æ’u %,.0f Ã„â€˜ Ã„â€˜Ã¡Â»Æ’ ÃƒÂ¡p dÃ¡Â»Â¥ng mÃƒÂ£ nÃƒÂ y!", minAmount));
                                return;
                            }

                            if (!isPromoTargetMatch(targetRole)) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i khÃƒÂ´ng ÃƒÂ¡p dÃ¡Â»Â¥ng cho tÃƒÂ i khoÃ¡ÂºÂ£n cÃ¡Â»Â§a bÃ¡ÂºÂ¡n!");
                                return;
                            }

                            double voucherValue = 0;

                            if ("percentage".equalsIgnoreCase(discountType)) {
                                double percent = discountValue != null ? discountValue : 0;
                                voucherValue = subtotal * (percent / 100.0);

                                if (maxDiscountAmount != null && maxDiscountAmount > 0) {
                                    voucherValue = Math.min(voucherValue, maxDiscountAmount);
                                }
                            } else if ("fixed".equalsIgnoreCase(discountType)
                                    || "amount".equalsIgnoreCase(discountType)) {
                                voucherValue = discountValue != null ? discountValue : 0;
                            }

                            if (voucherValue <= 0) {
                                showPromoInvalid(tvPromoStatus, "MÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡!");
                                return;
                            }

                            discountVoucher = voucherValue;
                            appliedPromoCode = code;

                            if (tvAppliedPromo != null) {
                                String promoLabel = (title != null && !title.trim().isEmpty())
                                        ? title
                                        : code;

                                tvAppliedPromo.setText(
                                        String.format(Locale.getDefault(),
                                                "Ã„ÂÃƒÂ£ ÃƒÂ¡p dÃ¡Â»Â¥ng: %s (-%,.0f Ã„â€˜)", promoLabel, voucherValue)
                                );
                                tvAppliedPromo.setTextColor(0xFF4CAF50);
                            }

                            updateTotalPrice();
                            dialog.dismiss();
                            Toast.makeText(this, "ÃƒÂp dÃ¡Â»Â¥ng mÃƒÂ£ khuyÃ¡ÂºÂ¿n mÃƒÂ£i thÃƒÂ nh cÃƒÂ´ng!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            btnApplyPromo.setEnabled(true);
                            showPromoInvalid(tvPromoStatus, "KhÃƒÂ´ng thÃ¡Â»Æ’ kiÃ¡Â»Æ’m tra khuyÃ¡ÂºÂ¿n mÃƒÂ£i: " + e.getMessage());
                        });
            });
        }

        dialog.show();
    }

    private void showPromoInvalid(TextView tvPromoStatus, String message) {
        if (tvPromoStatus != null) {
            tvPromoStatus.setText(message);
            tvPromoStatus.setVisibility(android.view.View.VISIBLE);
        }
    }

    private boolean isPromoTargetMatch(String targetRole) {
        if (targetRole == null || targetRole.trim().isEmpty() || "all".equalsIgnoreCase(targetRole.trim())) {
            return true;
        }

        if (currentUser == null) {
            return false;
        }

        String userRole = currentUser.role != null ? currentUser.role : "";
        String memberLevel = currentUser.memberLevel != null ? currentUser.memberLevel : "";

        return targetRole.equalsIgnoreCase(userRole)
                || targetRole.equalsIgnoreCase(memberLevel);
    }

    private void confirmBooking() {
        if ("momo".equals(selectedPaymentMethod)) {
            showMomoCheckoutDialog(selectedPaymentMethod);
        } else {
            createBookingOnBackend(selectedPaymentMethod);
        }
    }

    private void createBookingOnBackend(String paymentMethod) {
        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);
        if (btnConfirm != null) btnConfirm.setEnabled(false);

        List<SeatBookingRequestDTO.SnackOrder> snackOrders = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : selectedSnacks.entrySet()) {
            snackOrders.add(new SeatBookingRequestDTO.SnackOrder(entry.getKey(), entry.getValue()));
        }

        SeatBookingRequestDTO request = new SeatBookingRequestDTO(
                showtimeId,
                seatIds != null ? seatIds : new ArrayList<>(),
                snackOrders,
                paymentMethod
        );
        request.promoCode = appliedPromoCode;
        request.discountVoucher = discountVoucher;
        request.useStars = isStarsApplied;

        BookingApiService bookingApi = RetrofitClient.getInstance()
                .create(BookingApiService.class);

        bookingApi.createBooking(request).enqueue(new retrofit2.Callback<ApiResponse<BookingDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<BookingDTO>> call, retrofit2.Response<ApiResponse<BookingDTO>> response) {
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    BookingDTO booking = response.body().getData();
                    isBookingConfirmed = true;
                    BookingTimerManager.getInstance().stopTimer(BookingConfirmActivity.this);
                    
                    // Ã„ÂÃƒÂ¡nh dÃ¡ÂºÂ¥u Voucher Ã„â€˜ÃƒÂ£ sÃ¡Â»Â­ dÃ¡Â»Â¥ng (Ã„â€˜Ã¡Â»Æ’ khÃƒÂ´ng xÃƒÂ i lÃ¡ÂºÂ¡i Ã„â€˜Ã†Â°Ã¡Â»Â£c nÃ¡Â»Â¯a)
                    if (appliedVoucherId != null && !appliedVoucherId.isEmpty()) {
                        FirebaseFirestore.getInstance().collection("vouchers")
                                .document(appliedVoucherId)
                                .update("isUsed", true, "usedAt", System.currentTimeMillis());
                    }

                    if ("momo".equals(paymentMethod)) {
                        Toast.makeText(BookingConfirmActivity.this, "Thanh toÃƒÂ¡n qua VÃƒÂ­ MoMo thÃƒÂ nh cÃƒÂ´ng!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(BookingConfirmActivity.this, TicketDetailActivity.class);
                        intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ID, booking.bookingId);
                        intent.putExtra("EXTRA_FROM_BOOKING_SUCCESS", true);
                        startActivity(intent);
                        finish();
                    } else if ("bank".equals(paymentMethod)) {
                        Intent intent = new Intent(BookingConfirmActivity.this, PaymentInstructionActivity.class);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_BOOKING_ID, booking.bookingId);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_ID, (String) null);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_CODE, booking.paymentCode);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_AMOUNT, booking.total);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_METHOD, paymentMethod);
                        intent.putExtra("createdAt", booking.createdAt);
                        startActivity(intent);
                        finish();
                    } else {
                        createNotification("Ã„ÂÃ¡ÂºÂ·t vÃƒÂ© thÃƒÂ nh cÃƒÂ´ng", "BÃ¡ÂºÂ¡n Ã„â€˜ÃƒÂ£ Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© thÃƒÂ nh cÃƒÂ´ng. Vui lÃƒÂ²ng thanh toÃƒÂ¡n tÃ¡ÂºÂ¡i quÃ¡ÂºÂ§y trÃ†Â°Ã¡Â»â€ºc khi suÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u 15 phÃƒÂºt.", "BOOKING_SUCCESS");
                        Toast.makeText(BookingConfirmActivity.this, "Ã„ÂÃ¡ÂºÂ·t vÃƒÂ© thÃƒÂ nh cÃƒÂ´ng (ChÃ¡Â»Â thanh toÃƒÂ¡n tÃ¡ÂºÂ¡i quÃ¡ÂºÂ§y)!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    String msg = "LÃ¡Â»â€”i tÃ¡ÂºÂ¡o vÃƒÂ©. Vui lÃƒÂ²ng thÃ¡Â»Â­ lÃ¡ÂºÂ¡i.";
                    if (response.code() == 409) {
                        msg = "Xung Ã„â€˜Ã¡Â»â„¢t: GhÃ¡ÂºÂ¿ Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡ÂºÂ·t hoÃ¡ÂºÂ·c Ã„â€˜ang cÃƒÂ³ ngÃ†Â°Ã¡Â»Âi khÃƒÂ¡c giÃ¡Â»Â¯!";
                    } else if (response.code() == 403) {
                        msg = "LÃ¡Â»â€”i: BÃ¡ÂºÂ¡n khÃƒÂ´ng giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿ nÃƒÂ y hoÃ¡ÂºÂ·c Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿!";
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    createNotification("Ã„ÂÃ¡ÂºÂ·t vÃƒÂ© thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i", msg, "BOOKING_FAILED");
                    Toast.makeText(BookingConfirmActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<BookingDTO>> call, Throwable t) {
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                Toast.makeText(BookingConfirmActivity.this, "KÃ¡ÂºÂ¿t nÃ¡Â»â€˜i mÃ¡ÂºÂ¡ng khÃƒÂ´ng Ã¡Â»â€¢n Ã„â€˜Ã¡Â»â€¹nh. Vui lÃƒÂ²ng kiÃ¡Â»Æ’m tra lÃ¡ÂºÂ¡i Wifi/4G.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // ZELIOUS: Logic gÃ¡Â»Â­i thÃƒÂ´ng bÃƒÂ¡o khi cÃƒÂ³ kÃ¡ÂºÂ¿t quÃ¡ÂºÂ£ API trÃ¡ÂºÂ£ vÃ¡Â»Â.
    // LÃ¡ÂºÂ¥y userId hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i, tÃ¡ÂºÂ¡o object Notification vÃ¡Â»â€ºi type 'BOOKING_SUCCESS' hoÃ¡ÂºÂ·c 'BOOKING_FAILED'
    // Sau Ã„â€˜ÃƒÂ³ gÃ¡Â»Âi NotificationRepositoryImpl Ã„â€˜Ã¡Â»Æ’ Ã„â€˜Ã¡ÂºÂ©y Document nÃƒÂ y xuÃ¡Â»â€˜ng Firestore.
    private void createNotification(String title, String message, String type) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ?
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        com.example.cinemabookingapp.domain.model.Notification notification = new com.example.cinemabookingapp.domain.model.Notification();
        notification.userId = userId;
        notification.title = title;
        notification.message = message;
        notification.type = type;
        notification.isRead = false;
        notification.createdAt = System.currentTimeMillis();
        notification.updatedAt = System.currentTimeMillis();

        new com.example.cinemabookingapp.data.repository.NotificationRepositoryImpl()
                .createNotification(notification, null);
    }

    private void showMomoCheckoutDialog(String paymentMethod) {
        momoDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_momo_checkout, null);
        momoDialog.setContentView(view);

        TextView tvMomoAmount = view.findViewById(R.id.tvMomoAmount);
        android.widget.Button btnCancelMomo = view.findViewById(R.id.btnCancelMomo);
        android.widget.Button btnConfirmMomo = view.findViewById(R.id.btnConfirmMomo);

        double finalTotal = total - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

        if (tvMomoAmount != null) {
            tvMomoAmount.setText(String.format(Locale.getDefault(), "SÃ¡Â»â€˜ tiÃ¡Â»Ân: %,.0f Ã„â€˜", finalTotal));
        }

        if (btnCancelMomo != null) {
            btnCancelMomo.setOnClickListener(v -> {
                momoDialog.dismiss();
                Toast.makeText(this, "HÃ¡Â»Â§y thanh toÃƒÂ¡n MoMo", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnConfirmMomo != null) {
            btnConfirmMomo.setOnClickListener(v -> {
                momoDialog.dismiss();
                Toast.makeText(this, "Ã„Âang xÃ¡Â»Â­ lÃƒÂ½ giao dÃ¡Â»â€¹ch MoMo...", Toast.LENGTH_SHORT).show();
                createBookingOnBackend(paymentMethod);
            });
        }

        momoDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BookingTimerManager.getInstance().registerListener(timerListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BookingTimerManager.getInstance().unregisterListener(timerListener);
    }

    private void releaseLockedSeats() {
        if (seatIds == null || seatIds.isEmpty() || showtimeId == null) return;

        com.example.cinemabookingapp.data.dto.SeatLockRequestDTO releaseReq =
                new com.example.cinemabookingapp.data.dto.SeatLockRequestDTO(showtimeId, seatIds);

        com.example.cinemabookingapp.data.remote.api.SeatApiService seatApi =
                com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance()
                        .create(com.example.cinemabookingapp.data.remote.api.SeatApiService.class);

        seatApi.releaseSeats(releaseReq).enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> call, retrofit2.Response<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> response) {
                android.util.Log.d("BOOKING_FLOW", "Seats released successfully in background");
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> call, Throwable t) {
                android.util.Log.e("BOOKING_FLOW", "Failed to release seats: " + t.getMessage());
            }
        });
    }

    private void loadSnacks() {
        if (layoutSnackContainer == null) return;
        layoutSnackContainer.removeAllViews();

        android.widget.TextView tvLoading = new android.widget.TextView(this);
        tvLoading.setText("Ã„Âang tÃ¡ÂºÂ£i danh sÃƒÂ¡ch bÃ¡ÂºÂ¯p nÃ†Â°Ã¡Â»â€ºc...");
        tvLoading.setTextColor(android.graphics.Color.GRAY);
        layoutSnackContainer.addView(tvLoading);

        new com.example.cinemabookingapp.data.repository.SnackRepositoryImpl().getAllSnacks(
                new com.example.cinemabookingapp.domain.common.ResultCallback<List<com.example.cinemabookingapp.domain.model.Snack>>() {
                    @Override
                    public void onSuccess(List<com.example.cinemabookingapp.domain.model.Snack> snacks) {
                        layoutSnackContainer.removeAllViews();
                        snackList.clear();
                        if (snacks == null || snacks.isEmpty()) {
                            android.widget.TextView tvEmpty = new android.widget.TextView(BookingConfirmActivity.this);
                            tvEmpty.setText("KhÃƒÂ´ng cÃƒÂ³ combo bÃ¡ÂºÂ¯p nÃ†Â°Ã¡Â»â€ºc khÃ¡ÂºÂ£ dÃ¡Â»Â¥ng.");
                            tvEmpty.setTextColor(android.graphics.Color.GRAY);
                            layoutSnackContainer.addView(tvEmpty);
                            return;
                        }
                        snackList.addAll(snacks);
                        for (com.example.cinemabookingapp.domain.model.Snack snack : snacks) {
                            addSnackItemToView(snack);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        layoutSnackContainer.removeAllViews();
                        android.widget.TextView tvError = new android.widget.TextView(BookingConfirmActivity.this);
                        tvError.setText("KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ£i danh sÃƒÂ¡ch bÃ¡ÂºÂ¯p nÃ†Â°Ã¡Â»â€ºc.");
                        tvError.setTextColor(android.graphics.Color.RED);
                        layoutSnackContainer.addView(tvError);
                    }
                }
        );
    }

    private void addSnackItemToView(com.example.cinemabookingapp.domain.model.Snack snack) {
        android.view.View snackView = getLayoutInflater().inflate(R.layout.item_booking_snack, layoutSnackContainer, false);

        android.widget.ImageView ivSnackImage = snackView.findViewById(R.id.ivSnackImage);
        android.widget.TextView tvSnackName = snackView.findViewById(R.id.tvSnackName);
        android.widget.TextView tvSnackDesc = snackView.findViewById(R.id.tvSnackDesc);
        android.widget.TextView tvSnackPrice = snackView.findViewById(R.id.tvSnackPrice);
        android.widget.TextView tvQuantity = snackView.findViewById(R.id.tvQuantity);
        android.view.View btnMinus = snackView.findViewById(R.id.btnMinus);
        android.view.View btnPlus = snackView.findViewById(R.id.btnPlus);

        if (tvSnackName != null) tvSnackName.setText(snack.name);
        if (tvSnackDesc != null) tvSnackDesc.setText(snack.description);
        if (tvSnackPrice != null) {
            tvSnackPrice.setText(String.format(Locale.getDefault(), "%,.0f Ã„â€˜", snack.price));
        }

        if (ivSnackImage != null) {
            if (snack.imageUrl != null && !snack.imageUrl.isEmpty()) {
                // Clear tint list so downloaded images are displayed in their original colors
                ivSnackImage.setImageTintList(null);
                com.bumptech.glide.Glide.with(this)
                        .load(snack.imageUrl)
                        .placeholder(R.drawable.gift_solid_full)
                        .into(ivSnackImage);
            } else {
                // Apply fallback tint color to the placeholder image
                ivSnackImage.setImageResource(R.drawable.gift_solid_full);
                ivSnackImage.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFA13345));
            }
        }

        if (tvQuantity != null) {
            tvQuantity.setText("0");
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                int currentQty = selectedSnacks.containsKey(snack.snackId) ? selectedSnacks.get(snack.snackId) : 0;
                currentQty++;
                selectedSnacks.put(snack.snackId, currentQty);
                if (tvQuantity != null) {
                    tvQuantity.setText(String.valueOf(currentQty));
                }
                recalculateSnacksTotal();
            });
        }

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                int currentQty = selectedSnacks.containsKey(snack.snackId) ? selectedSnacks.get(snack.snackId) : 0;
                if (currentQty > 0) {
                    currentQty--;
                    if (currentQty == 0) {
                        selectedSnacks.remove(snack.snackId);
                    } else {
                        selectedSnacks.put(snack.snackId, currentQty);
                    }
                    if (tvQuantity != null) {
                        tvQuantity.setText(String.valueOf(currentQty));
                    }
                    recalculateSnacksTotal();
                }
            });
        }

        layoutSnackContainer.addView(snackView);
    }

    private void recalculateSnacksTotal() {
        totalSnacksPrice = 0;
        for (com.example.cinemabookingapp.domain.model.Snack snack : snackList) {
            if (selectedSnacks.containsKey(snack.snackId)) {
                totalSnacksPrice += snack.price * selectedSnacks.get(snack.snackId);
            }
        }
        updateTotalPrice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            BookingTimerManager.getInstance().stopTimer(this);
            if (!isBookingConfirmed) {
                releaseLockedSeats();
            }
        }
    }
}