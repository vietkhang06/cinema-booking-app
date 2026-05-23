package com.example.cinemabookingapp.ui.customer;

import android.content.Intent;
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

    private String showtimeId, movieTitle, movieId, cinemaName, imageUrl;
    private long showtimeStart;
    private double total;
    private ArrayList<String> seatCodes, seatIds;

    private TextView tvTimer;
    private BookingTimerManager.TimerListener timerListener;

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

    // Phase 4 MoMo Payment Fields
    private android.widget.RadioGroup rgPayment;
    private String selectedPaymentMethod = "cash";
    private com.google.android.material.bottomsheet.BottomSheetDialog momoDialog;

    // Phase 5 Age Rating Fields
    private TextView tvAgeRatingBadge;
    private String movieAgeRating = "P";

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

        tvTimer = findViewById(R.id.tvTimer);

        // Start timer if not already active
        if (!BookingTimerManager.getInstance().isTimerActive(this)) {
            BookingTimerManager.getInstance().startTimer(this, 7 * 60 * 1000);
        } else {
            BookingTimerManager.getInstance().restoreTimer(this);
        }

        timerListener = new BookingTimerManager.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                if (tvTimer != null) {
                    tvTimer.setText(String.format(Locale.getDefault(), "Thời gian giữ ghế: %02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (tvTimer != null) {
                    tvTimer.setText("Thời gian giữ ghế: 00:00");
                }
                if (momoDialog != null && momoDialog.isShowing()) {
                    momoDialog.dismiss();
                }
                Toast.makeText(BookingConfirmActivity.this, "Thời gian giữ ghế đã hết! Vui lòng chọn lại.", Toast.LENGTH_LONG).show();
                finish();
            }
        };

        initViews();
        loadUserProfile();
        loadMovieData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bind data lên UI
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
            SimpleDateFormat fmt = new SimpleDateFormat(
                    "HH:mm - dd/MM/yyyy", Locale.getDefault());
            tvTime.setText(fmt.format(new Date(showtimeStart)));
        }

        if (tvSeats != null && seatCodes != null)
            tvSeats.setText(String.join(", ", seatCodes));

        if (tvTotal != null)
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", total));

        rgPayment = findViewById(R.id.rgPayment);
        if (rgPayment != null) {
            rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbPayCash) {
                    selectedPaymentMethod = "cash";
                } else if (checkedId == R.id.rbPayBank) {
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
            tvWarningTitle.setText("Xác nhận độ tuổi tối thiểu C" + minAge);
        }

        if (tvWarningMsg != null) {
            tvWarningMsg.setText("Phim này có phân loại độ tuổi là C" + minAge + " - CHỈ DÀNH CHO KHÁN GIẢ TỪ " + minAge + " TUỔI TRỞ LÊN. Vui lòng xác nhận bạn đủ tuổi trước khi tiếp tục thanh toán. Vé đã mua không được hoàn trả hoặc đổi trả nếu không đủ tuổi.");
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
                    }
                }

                @Override
                public void onError(String message) {
                    // Fail silently
                }
            }
        );
    }

    private void applyTierDiscount() {
        if (currentUser == null || currentUser.memberLevel == null) return;
        String level = currentUser.memberLevel.toLowerCase();
        double factor = 0;
        String levelName = "Thành viên";
        if (level.contains("vip")) {
            factor = 0.10; // 10%
            levelName = "VIP";
        } else if (level.contains("platinum")) {
            factor = 0.15; // 15%
            levelName = "Platinum";
        } else if (level.contains("gold")) {
            factor = 0.08; // 8%
            levelName = "Gold";
        }
        if (factor > 0) {
            discountRank = total * factor;
            if (tvAppliedPromo != null) {
                tvAppliedPromo.setText("Đã áp dụng ưu đãi hạng " + levelName + " (-" + (int)(factor * 100) + "%)");
            }
        }
        updateTotalPrice();
    }

    private void updateStarsUI() {
        if (currentUser == null) return;
        int points = currentUser.points;
        if (tvStarsLabel != null) {
            tvStarsLabel.setText(String.format(Locale.getDefault(), "Áp dụng điểm Stars (%d Stars có sẵn)", points));
        }
        if (switchStars != null) {
            switchStars.setEnabled(points > 0);
            switchStars.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isStarsApplied = isChecked;
                if (isChecked) {
                    // 1 Stars = 1,000 VND
                    discountStars = points * 1000.0;
                } else {
                    discountStars = 0;
                }
                updateTotalPrice();
            });
        }
    }

    private void updateTotalPrice() {
        double finalTotal = total - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

        if (discountVoucher > 0 || discountRank > 0 || discountStars > 0) {
            if (tvOriginalPrice != null) {
                tvOriginalPrice.setVisibility(android.view.View.VISIBLE);
                tvOriginalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }
        } else {
            if (tvOriginalPrice != null) {
                tvOriginalPrice.setVisibility(android.view.View.GONE);
            }
        }

        if (tvTotal != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", finalTotal));
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

        if (!appliedPromoCode.isEmpty() && edtPromoCode != null) {
            edtPromoCode.setText(appliedPromoCode);
        }

        if (btnApplyPromo != null) {
            btnApplyPromo.setOnClickListener(v -> {
                if (edtPromoCode == null) return;
                String code = edtPromoCode.getText().toString().trim().toUpperCase();
                if (code.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mã khuyến mãi!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double voucherValue = 0;
                String promoMsg = "";
                boolean isValid = false;

                if ("GALAXY50".equals(code)) {
                    voucherValue = 50000;
                    promoMsg = "Đã áp dụng mã GALAXY50 (-50k)";
                    isValid = true;
                } else if ("WELCOME10".equals(code)) {
                    voucherValue = total * 0.10;
                    promoMsg = "Đã áp dụng mã WELCOME10 (-10%)";
                    isValid = true;
                } else if ("FREESHOP".equals(code)) {
                    voucherValue = 20000;
                    promoMsg = "Đã áp dụng mã FREESHOP (-20k)";
                    isValid = true;
                }

                if (isValid) {
                    discountVoucher = voucherValue;
                    appliedPromoCode = code;
                    if (tvAppliedPromo != null) {
                        tvAppliedPromo.setText(promoMsg);
                        tvAppliedPromo.setTextColor(0xFF4CAF50);
                    }
                    updateTotalPrice();
                    dialog.dismiss();
                    Toast.makeText(this, "Áp dụng mã khuyến mãi thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    if (tvPromoStatus != null) {
                        tvPromoStatus.setText("Mã khuyến mãi không hợp lệ hoặc đã hết hạn!");
                        tvPromoStatus.setVisibility(android.view.View.VISIBLE);
                    }
                }
            });
        }

        dialog.show();
    }

    private void confirmBooking() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        String bookingId = "booking_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();

        double finalTotal = total - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

        if ("momo".equals(selectedPaymentMethod)) {
            showMomoCheckoutDialog(bookingId, userId, movieId, now, finalTotal);
        } else {
            saveBookingToFirestore(bookingId, userId, movieId, now, finalTotal, selectedPaymentMethod, "pending");
        }
    }

    private void saveBookingToFirestore(String bookingId, String userId, String movieId, long now, double finalTotal, String paymentMethod, String paymentStatus) {
        String suffix = bookingId.contains("_") ? bookingId.substring(bookingId.indexOf("_") + 1) : bookingId;
        if (suffix.length() > 8) {
            suffix = suffix.substring(0, 8);
        }
        String paymentCode = ("BK" + suffix).toUpperCase();
        String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);

        // Lưu cả Booking và Payment dưới trạng thái PENDING trước
        saveBookingAndPaymentToFirestore(bookingId, paymentId, userId, movieId, now, finalTotal, paymentMethod, "PENDING", "PENDING", "PENDING", new com.google.android.gms.tasks.OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if ("momo".equals(paymentMethod) || "bank".equals(paymentMethod)) {
                    BookingTimerManager.getInstance().stopTimer(BookingConfirmActivity.this);
                    Intent intent = new Intent(BookingConfirmActivity.this, PaymentInstructionActivity.class);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_BOOKING_ID, bookingId);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_ID, paymentId);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_CODE, paymentCode);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_AMOUNT, finalTotal);
                    intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_METHOD, paymentMethod);
                    startActivity(intent);
                    finish();
                } else {
                    BookingTimerManager.getInstance().stopTimer(BookingConfirmActivity.this);
                    Toast.makeText(BookingConfirmActivity.this, "Đặt vé thành công (Chờ thanh toán)!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void saveBookingAndPaymentToFirestore(
            String bookingId,
            String paymentId,
            String userId,
            String movieId,
            long now,
            double finalTotal,
            String paymentMethod,
            String bookingStatus,
            String paymentStatus,
            String paymentRecordStatus,
            com.google.android.gms.tasks.OnSuccessListener<Void> onSuccessListener
    ) {
        String suffix = bookingId.contains("_") ? bookingId.substring(bookingId.indexOf("_") + 1) : bookingId;
        if (suffix.length() > 8) {
            suffix = suffix.substring(0, 8);
        }
        String paymentCode = ("BK" + suffix).toUpperCase();

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
        booking.put("total", finalTotal);
        booking.put("discountVoucher", discountVoucher);
        booking.put("discountRank", discountRank);
        booking.put("discountStars", discountStars);
        booking.put("appliedPromoCode", appliedPromoCode);
        booking.put("paymentMethod", paymentMethod);
        booking.put("paymentStatus", paymentStatus);
        booking.put("bookingStatus", bookingStatus);
        booking.put("paymentCode", paymentCode);
        booking.put("createdAt", now);
        booking.put("updatedAt", now);
        booking.put("deleted", false);

        Map<String, Object> payment = new HashMap<>();
        payment.put("paymentId", paymentId);
        payment.put("bookingId", bookingId);
        payment.put("paymentCode", paymentCode);
        payment.put("userId", userId);
        payment.put("provider", paymentMethod);
        payment.put("amount", finalTotal);
        payment.put("status", paymentRecordStatus);
        payment.put("transactionId", "TXN_" + UUID.randomUUID().toString().substring(0, 8));
        payment.put("payUrl", "http://fake-payurl.com/pay/" + paymentId);
        payment.put("createdAt", now);
        payment.put("updatedAt", now);

        android.util.Log.d("BOOKING_FLOW", "Creating/updating booking record: bookingId=" + bookingId + ", paymentCode=" + paymentCode + ", status=" + bookingStatus + ", paymentStatus=" + paymentStatus);
        android.util.Log.d("PAYMENT_FLOW", "Creating/updating payment record: paymentId=" + paymentId + ", bookingId=" + bookingId + ", paymentCode=" + paymentCode + ", amount=" + finalTotal + ", status=" + paymentRecordStatus);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        batch.set(db.collection("bookings").document(bookingId), booking);
        batch.set(db.collection("payments").document(paymentId), payment);

        batch.commit()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showMomoSimulationDialog(String bookingId, String userId, String movieId, long now, double finalTotal) {
        String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Mô phỏng thanh toán MoMo")
                .setMessage("Chọn kết quả giao dịch thanh toán để mô phỏng (local fake):")
                .setPositiveButton("Thành công (SUCCESS)", (dialog, which) -> {
                    saveBookingAndPaymentToFirestore(bookingId, paymentId, userId, movieId, now, finalTotal, "momo", "confirmed", "paid", "SUCCESS", aVoid -> {
                        BookingTimerManager.getInstance().stopTimer(this);
                        Toast.makeText(this, "Thanh toán MoMo thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton("Thất bại (FAILED)", (dialog, which) -> {
                    saveBookingAndPaymentToFirestore(bookingId, paymentId, userId, movieId, now, finalTotal, "momo", "cancelled", "failed", "FAILED", aVoid -> {
                        BookingTimerManager.getInstance().stopTimer(this);
                        Toast.makeText(this, "Thanh toán MoMo thất bại!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setCancelable(false)
                .show();
    }

    private void showMomoCheckoutDialog(String bookingId, String userId, String movieId, long now, double finalTotal) {
        momoDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_momo_checkout, null);
        momoDialog.setContentView(view);

        TextView tvMomoAmount = view.findViewById(R.id.tvMomoAmount);
        android.widget.Button btnCancelMomo = view.findViewById(R.id.btnCancelMomo);
        android.widget.Button btnConfirmMomo = view.findViewById(R.id.btnConfirmMomo);

        if (tvMomoAmount != null) {
            tvMomoAmount.setText(String.format(Locale.getDefault(), "Số tiền: %,.0f đ", finalTotal));
        }

        if (btnCancelMomo != null) {
            btnCancelMomo.setOnClickListener(v -> {
                momoDialog.dismiss();
                Toast.makeText(this, "Hủy thanh toán MoMo", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnConfirmMomo != null) {
            btnConfirmMomo.setOnClickListener(v -> {
                momoDialog.dismiss();
                Toast.makeText(this, "Đang xử lý giao dịch MoMo...", Toast.LENGTH_SHORT).show();
                // Thực hiện tạo PENDING trước
                saveBookingToFirestore(bookingId, userId, movieId, now, finalTotal, "momo", "pending");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            BookingTimerManager.getInstance().stopTimer(this);
        }
    }
}