package com.example.cinemabookingapp.ui.features.booking;

import com.example.cinemabookingapp.domain.model.Booking;
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
    private double appliedVoucherDiscountAmount = 0;
    private double appliedVoucherDiscountPercent = 0;
    private double appliedPromoMaxDiscount = 0;
    private String appliedVoucherId = "";

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
                    tvTimer.setText(String.format(Locale.getDefault(), "Thời gian giữ ghế: %02d:%02d", minutes, seconds));
                }
                if (millisUntilFinished <= 60000 && !hasShownWarning) {
                    hasShownWarning = true;
                    if (!isFinishing() && !isDestroyed()) {
                        new androidx.appcompat.app.AlertDialog.Builder(BookingConfirmActivity.this)
                                .setTitle("Thông báo")
                                .setMessage("Chú ý thời gian giữ ghế còn 1 phút, xin vui lòng thanh toán")
                                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                                .setCancelable(false)
                                .show();
                    }
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
                if (!isBookingConfirmed) {
                    releaseLockedSeats();
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
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
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
                            checkAndApplyVouchers();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(BookingConfirmActivity.this, "Không thể tải thông tin thành viên. Ưu đãi thẻ có thể không được áp dụng.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void checkAndApplyVouchers() {
        if (currentUser == null) return;
        
        FirebaseFirestore.getInstance()
                .collection("vouchers")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("isUsed", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && !snapshot.isEmpty()) {
                        double maxDiscount = 0;
                        com.google.firebase.firestore.DocumentSnapshot bestVoucher = null;
                        
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                            double discount = 0;
                            Object discountObj = doc.get("discountPercent");
                            if (discountObj instanceof Number) {
                                discount = ((Number) discountObj).doubleValue();
                            }
                            if (discount <= 0) {
                                Object valObj = doc.get("discountValue");
                                if (valObj instanceof Number) {
                                    discount = ((Number) valObj).doubleValue();
                                }
                            }

                            if (discount <= 0) {
                                doc.getReference().delete();
                                continue;
                            }

                            if (discount > maxDiscount) {
                                maxDiscount = discount;
                                bestVoucher = doc;
                            }
                        }
                        
                        if (bestVoucher != null) {
                            appliedVoucherId = bestVoucher.getId();
                            String vCode = bestVoucher.getString("code");
                            appliedPromoCode = vCode != null ? vCode : "";
                            String type = bestVoucher.getString("voucherType");
                            
                            // Phân biệt Voucher giảm thẳng (200k) và giảm % (10%)
                            if (maxDiscount > 100) {
                                appliedVoucherDiscountAmount = maxDiscount;
                                appliedVoucherDiscountPercent = 0;
                            } else {
                                appliedVoucherDiscountAmount = 0;
                                appliedVoucherDiscountPercent = maxDiscount;
                            }
                            
                            updateTotalPrice();
                            if (tvAppliedPromo != null) {
                                tvAppliedPromo.setText(String.format(Locale.getDefault(), "Voucher ví: -%,.0f đ", discountVoucher));
                                tvAppliedPromo.setVisibility(android.view.View.VISIBLE);
                            }
                            Toast.makeText(this, "Hệ thống tự động áp dụng Voucher từ ví của bạn!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void applyTierDiscount() {
        if (currentUser == null || currentUser.memberLevel == null) return;
        String level = currentUser.memberLevel.toLowerCase();
        double factor = 0;
        String levelName = "Thành viên";
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
                tvAppliedPromo.setText("Đã áp dụng ưu đãi hạng " + levelName + " (-" + (int) (factor * 100) + "%)");
            }
        }
        updateTotalPrice();
    }

    private void updateStarsUI() {
        if (currentUser == null) return;
        int points = (currentUser.points != null) ? currentUser.points : 0;
        if (tvStarsLabel != null) {
            tvStarsLabel.setText(String.format(Locale.getDefault(), "Áp dụng điểm Stars (%d Stars có sẵn)", points));
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
        double subtotal = total + totalSnacksPrice;
        if (appliedVoucherDiscountPercent > 0) {
            discountVoucher = subtotal * (appliedVoucherDiscountPercent / 100.0);
            if (appliedPromoMaxDiscount > 0) {
                discountVoucher = Math.min(discountVoucher, appliedPromoMaxDiscount);
            }
        } else if (appliedVoucherDiscountAmount > 0) {
            discountVoucher = appliedVoucherDiscountAmount;
        } else {
            discountVoucher = 0;
        }
        double finalTotal = subtotal - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

        if (discountVoucher > 0 || discountRank > 0 || discountStars > 0) {
            if (tvOriginalPrice != null) {
                tvOriginalPrice.setVisibility(android.view.View.VISIBLE);
                tvOriginalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total + totalSnacksPrice));
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
        androidx.recyclerview.widget.RecyclerView rvVouchersList = view.findViewById(R.id.rvVouchersList);
        android.view.View btnExpandVouchers = view.findViewById(R.id.btnExpandVouchers);
        TextView tvExpandLabel = view.findViewById(R.id.tvExpandLabel);
        android.view.View btnVoucherConditions = view.findViewById(R.id.btnVoucherConditions);

        if (!appliedPromoCode.isEmpty() && edtPromoCode != null) {
            edtPromoCode.setText(appliedPromoCode);
        }

        if (btnVoucherConditions != null) {
            btnVoucherConditions.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Điều kiện Voucher")
                        .setMessage("- Mỗi đơn hàng chỉ áp dụng tối đa 1 voucher.\n- Không thể áp dụng đồng thời voucher ví và mã khuyến mãi nhập tay.\n- Voucher không có giá trị quy đổi thành tiền mặt.")
                        .setPositiveButton("Đã hiểu", null)
                        .show();
            });
        }

        final java.util.List<DocumentSnapshot> allVouchers = new ArrayList<>();
        final java.util.List<DocumentSnapshot> displayedVouchers = new ArrayList<>();
        final boolean[] isExpanded = {false};

        if (rvVouchersList != null && currentUser != null) {
            rvVouchersList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            FirebaseFirestore.getInstance().collection("vouchers")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereEqualTo("isUsed", false)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        allVouchers.clear();
                        displayedVouchers.clear();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                double discount = 0;
                                Object discountObj = doc.get("discountPercent");
                                if (discountObj instanceof Number) {
                                    discount = ((Number) discountObj).doubleValue();
                                }
                                if (discount <= 0) {
                                    Object valObj = doc.get("discountValue");
                                    if (valObj instanceof Number) {
                                        discount = ((Number) valObj).doubleValue();
                                    }
                                }

                                if (discount <= 0) {
                                    doc.getReference().delete();
                                    continue;
                                }
                                allVouchers.add(doc);
                            }
                        }

                        if (allVouchers.size() > 3) {
                            if (btnExpandVouchers != null) {
                                btnExpandVouchers.setVisibility(android.view.View.VISIBLE);
                            }
                            displayedVouchers.addAll(allVouchers.subList(0, 3));
                        } else {
                            if (btnExpandVouchers != null) {
                                btnExpandVouchers.setVisibility(android.view.View.GONE);
                            }
                            displayedVouchers.addAll(allVouchers);
                        }

                        DialogVoucherAdapter adapter = new DialogVoucherAdapter(displayedVouchers, appliedVoucherId, (voucher, isCurrentlySelected) -> {
                            if (isCurrentlySelected) {
                                appliedVoucherId = "";
                                appliedPromoCode = "";
                                appliedVoucherDiscountAmount = 0;
                                appliedVoucherDiscountPercent = 0;
                                if (tvAppliedPromo != null) {
                                    tvAppliedPromo.setText("");
                                    tvAppliedPromo.setVisibility(android.view.View.GONE);
                                }
                                updateTotalPrice();
                                dialog.dismiss();
                                Toast.makeText(this, "Đã bỏ áp dụng Voucher!", Toast.LENGTH_SHORT).show();
                            } else {
                                appliedVoucherId = voucher.getId();
                                String vCode = voucher.getString("code");
                                appliedPromoCode = vCode != null ? vCode : "";
                                double discount = 0;
                                Object discountObj = voucher.get("discountPercent");
                                if (discountObj instanceof Number) {
                                    discount = ((Number) discountObj).doubleValue();
                                }
                                if (discount <= 0) {
                                    Object valObj = voucher.get("discountValue");
                                    if (valObj instanceof Number) {
                                        discount = ((Number) valObj).doubleValue();
                                    }
                                }

                                if (discount > 100) {
                                    appliedVoucherDiscountAmount = discount;
                                    appliedVoucherDiscountPercent = 0;
                                } else {
                                    appliedVoucherDiscountAmount = 0;
                                    appliedVoucherDiscountPercent = discount;
                                }

                                updateTotalPrice();
                                if (tvAppliedPromo != null) {
                                    String name = voucher.getString("title");
                                    if (name == null) name = voucher.getString("name");
                                    if (name == null) {
                                        String type = voucher.getString("voucherType");
                                        if ("WELCOME_VOUCHER".equals(type)) name = "Quà Tân Binh";
                                        else name = "Voucher hệ thống";
                                    }
                                    tvAppliedPromo.setText(String.format(Locale.getDefault(), "Đã áp dụng: %s (-%,.0f đ)", name, discountVoucher));
                                    tvAppliedPromo.setVisibility(android.view.View.VISIBLE);
                                    tvAppliedPromo.setTextColor(0xFF4CAF50);
                                }
                                dialog.dismiss();
                                Toast.makeText(this, "Áp dụng Voucher cá nhân thành công!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        rvVouchersList.setAdapter(adapter);

                        if (btnExpandVouchers != null) {
                            btnExpandVouchers.setOnClickListener(v -> {
                                isExpanded[0] = !isExpanded[0];
                                displayedVouchers.clear();
                                if (isExpanded[0]) {
                                    displayedVouchers.addAll(allVouchers);
                                    if (tvExpandLabel != null) {
                                        tvExpandLabel.setText("Thu gọn ▲");
                                    }
                                } else {
                                    displayedVouchers.addAll(allVouchers.subList(0, 3));
                                    if (tvExpandLabel != null) {
                                        tvExpandLabel.setText(String.format(Locale.getDefault(), "Xem thêm voucher (%d) ▼", allVouchers.size() - 3));
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            });

                            if (allVouchers.size() > 3 && tvExpandLabel != null) {
                                tvExpandLabel.setText(String.format(Locale.getDefault(), "Xem thêm voucher (%d) ▼", allVouchers.size() - 3));
                            }
                        }
                    });
        }

        if (btnApplyPromo != null) {
            btnApplyPromo.setOnClickListener(v -> {
                if (edtPromoCode == null) return;

                String code = edtPromoCode.getText().toString().trim().toUpperCase(Locale.getDefault());
                if (code.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mã khuyến mãi!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double subtotal = total + totalSnacksPrice;

                btnApplyPromo.setEnabled(false);
                if (tvPromoStatus != null) {
                    tvPromoStatus.setVisibility(android.view.View.VISIBLE);
                    tvPromoStatus.setText("Đang kiểm tra mã khuyến mãi...");
                }

                FirebaseFirestore.getInstance()
                        .collection("promotions") // nÃ¡ÂºÂ¿u collection cÃ¡Â»Â§a bÃ¡ÂºÂ¡n tÃƒÂªn khÃƒÂ¡c, Ã„â€˜Ã¡Â»â€¢i Ã¡Â»Å¸ Ã„â€˜ÃƒÂ¢y
                        .whereEqualTo("code", code)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            btnApplyPromo.setEnabled(true);

                            if (snapshot == null || snapshot.isEmpty()) {
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi không hợp lệ hoặc đã hết hạn!");
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
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi không còn hoạt động!");
                                return;
                            }

                            if (Boolean.TRUE.equals(deleted)) {
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi đã bị xoá!");
                                return;
                            }

                            if (validFrom != null && now < validFrom) {
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi chưa đến thời gian áp dụng!");
                                return;
                            }

                            if (validTo != null && now > validTo) {
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi đã hết hạn!");
                                return;
                            }

                            if (usageLimit != null && usedCount != null && usedCount >= usageLimit) {
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi đã hết lượt sử dụng!");
                                return;
                            }

                            if (minAmount != null && subtotal < minAmount) {
                                showPromoInvalid(tvPromoStatus,
                                        String.format(Locale.getDefault(),
                                                "Đơn hàng phải tối thiểu %,.0f đ để áp dụng mã này!", minAmount));
                                return;
                            }

                            if (!isPromoTargetMatch(targetRole)) {
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi không áp dụng cho tài khoản của bạn!");
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
                                showPromoInvalid(tvPromoStatus, "Mã khuyến mãi không hợp lệ!");
                                return;
                            }

                            if ("percentage".equalsIgnoreCase(discountType)) {
                                appliedVoucherDiscountPercent = discountValue != null ? discountValue : 0;
                                appliedVoucherDiscountAmount = 0;
                                appliedPromoMaxDiscount = maxDiscountAmount != null ? maxDiscountAmount : 0;
                            } else {
                                appliedVoucherDiscountPercent = 0;
                                appliedVoucherDiscountAmount = discountValue != null ? discountValue : 0;
                                appliedPromoMaxDiscount = 0;
                            }
                            appliedPromoCode = code;
                            appliedVoucherId = ""; // Clear personal voucher

                            updateTotalPrice();

                            if (tvAppliedPromo != null) {
                                String promoLabel = (title != null && !title.trim().isEmpty())
                                        ? title
                                        : code;

                                tvAppliedPromo.setText(
                                        String.format(Locale.getDefault(),
                                                "Đã áp dụng: %s (-%,.0f đ)", promoLabel, discountVoucher)
                                );
                                tvAppliedPromo.setTextColor(0xFF4CAF50);
                            }
                            dialog.dismiss();
                            Toast.makeText(this, "Áp dụng mã khuyến mãi thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            btnApplyPromo.setEnabled(true);
                            showPromoInvalid(tvPromoStatus, "Không thể kiểm tra khuyến mãi: " + e.getMessage());
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

    // tao booking voi payment status la pending
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
        request.discountVoucher = discountVoucher + discountRank + discountStars;
        request.useStars = isStarsApplied;

        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);

        bookingApi.createBooking(request).enqueue(new retrofit2.Callback<ApiResponse<BookingDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<BookingDTO>> call, retrofit2.Response<ApiResponse<BookingDTO>> response) {
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    BookingDTO booking = response.body().getData();
                    isBookingConfirmed = true;
                    BookingTimerManager.getInstance().stopTimer(BookingConfirmActivity.this);
                    
                    if (appliedVoucherId != null && !appliedVoucherId.isEmpty()) {
                        FirebaseFirestore.getInstance().collection("vouchers")
                                .document(appliedVoucherId)
                                .update("isUsed", true, "usedAt", System.currentTimeMillis());
                    }
//
                    if ("momo".equals(paymentMethod)) {
                        Toast.makeText(BookingConfirmActivity.this, "Thanh toán qua Ví MoMo thành công!", Toast.LENGTH_LONG).show();
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
                    }
                } else {
                    String msg = "Lỗi tạo vé. Vui lòng thử lại.";
                    if (response.code() == 409) {
                        msg = "Xung đột: Ghế đã được đặt hoặc đang có người khác giữ!";
                    } else if (response.code() == 403) {
                        msg = "Lỗi: Bạn không giữ ghế này hoặc đã hết hạn giữ ghế!";
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    createNotification("Đặt vé thất bại", msg, "BOOKING_FAILED", null);
                    Toast.makeText(BookingConfirmActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<BookingDTO>> call, Throwable t) {
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                Toast.makeText(BookingConfirmActivity.this, "Kết nối mạng không ổn định. Vui lòng kiểm tra lại Wifi/4G.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // xac nhan thanh toan thanh cong
    private void confirmPayment(BookingDTO booking){
        BookingApiService bookingApi = RetrofitClient.getInstance()
                .create(BookingApiService.class);

        bookingApi.confirmPayment(booking.bookingId).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    createNotification("Thanh toán thành công", "Bạn đã thanh toán thành công. Vui lòng kiểm tra vé của bạn.", "BOOKING_SUCCESS", booking.bookingId);
                    if ("momo".equals(booking.paymentMethod)) {
                        Toast.makeText(BookingConfirmActivity.this, "Thanh toán qua Ví MoMo thành công!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(BookingConfirmActivity.this, TicketDetailActivity.class);
                        intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ID, booking.bookingId);
                        intent.putExtra("EXTRA_FROM_BOOKING_SUCCESS", true);
                        startActivity(intent);
                        finish();
                    } else if ("bank".equals(booking.paymentMethod)) {
                        Intent intent = new Intent(BookingConfirmActivity.this, PaymentInstructionActivity.class);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_BOOKING_ID, booking.bookingId);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_ID, (String) null);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_CODE, booking.paymentCode);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_AMOUNT, booking.total);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_METHOD, booking.paymentMethod);
                        intent.putExtra("createdAt", booking.createdAt);
                        intent.putExtra("serverTime", booking.serverTime);
                        startActivity(intent);
                        finish();
                    } else {
                        createNotification("Đặt vé thành công", "Bạn đã đặt vé thành công. Vui lòng thanh toán tại quầy trước khi suất chiếu bắt đầu 15 phút.", "BOOKING_SUCCESS", booking.bookingId);
                        Toast.makeText(BookingConfirmActivity.this, "Đặt vé thành công (Chờ thanh toán tại quầy)!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    String msg = "Lỗi xác nhận thanh toán. Vui lòng thử lại.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    createNotification("Thanh toán thất bại", msg, "BOOKING_FAILED", booking.bookingId);
                    Toast.makeText(BookingConfirmActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(BookingConfirmActivity.this, "Kết nối mạng không ổn định. Vui lòng kiểm tra lại Wifi/4G.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // ZELIOUS: Logic gÃ¡Â»Â­i thÃƒÂ´ng bÃƒÂ¡o khi cÃƒÂ³ kÃ¡ÂºÂ¿t quÃ¡ÂºÂ£ API trÃ¡ÂºÂ£ vÃ¡Â»Â.
    // LÃ¡ÂºÂ¥y userId hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i, tÃ¡ÂºÂ¡o object Notification vÃ¡Â»â€ºi type 'BOOKING_SUCCESS' hoÃ¡ÂºÂ·c 'BOOKING_FAILED'
    // Sau Ã„â€˜ÃƒÂ³ gÃ¡Â»Âi NotificationRepositoryImpl Ã„â€˜Ã¡Â»Æ’ Ã„â€˜Ã¡ÂºÂ©y Document nÃƒÂ y xuÃ¡Â»â€˜ng Firestore.
    private void createNotification(String title, String message, String type, String refId) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ?
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        com.example.cinemabookingapp.domain.model.Notification notification = new com.example.cinemabookingapp.domain.model.Notification();
        notification.userId = userId;
        notification.title = title;
        notification.message = message;
        notification.type = type;
        notification.refId = refId;
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

        double finalTotal = (total + totalSnacksPrice) - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

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
        tvLoading.setText("Đang tải danh sách bắp nước...");
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
                            tvEmpty.setText("Không có combo bắp nước khả dụng.");
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
                        tvError.setText("Không thể tải danh sách bắp nước.");
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
            tvSnackPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", snack.price));
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

    private class DialogVoucherAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<DialogVoucherAdapter.ViewHolder> {
        private final List<DocumentSnapshot> items;
        private final String currentSelectedId;
        private final OnVoucherClickListener listener;

        public DialogVoucherAdapter(List<DocumentSnapshot> items, String currentSelectedId, OnVoucherClickListener listener) {
            this.items = items;
            this.currentSelectedId = currentSelectedId;
            this.listener = listener;
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = getLayoutInflater().inflate(R.layout.item_dialog_voucher, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            DocumentSnapshot doc = items.get(position);
            
            // 1. Get title/name
            String name = doc.getString("title");
            if (name == null) name = doc.getString("name");
            if (name == null) {
                String type = doc.getString("voucherType");
                if ("WELCOME_VOUCHER".equals(type)) name = "Quà Tân Binh";
                else name = "Voucher hệ thống";
            }

            // 2. Get discount
            double discount = 0;
            Object discountObj = doc.get("discountPercent");
            if (discountObj instanceof Number) {
                discount = ((Number) discountObj).doubleValue();
            }
            if (discount <= 0) {
                Object valObj = doc.get("discountValue");
                if (valObj instanceof Number) {
                    discount = ((Number) valObj).doubleValue();
                }
            }

            String valueTag = "";
            String titleText = "";
            if (discount > 100) {
                valueTag = String.format(Locale.getDefault(), "%,.0fK", discount / 1000.0);
                if (valueTag.endsWith(",0K") || valueTag.endsWith(".0K")) {
                    valueTag = valueTag.replace(",0K", "K").replace(".0K", "K");
                }
                titleText = String.format(Locale.getDefault(), "Giảm %,.0f đ", discount);
            } else {
                valueTag = String.format(Locale.getDefault(), "%,.0f%%", discount);
                if (valueTag.endsWith(",0%") || valueTag.endsWith(".0%")) {
                    valueTag = valueTag.replace(",0%", "%").replace(".0%", "%");
                }
                titleText = String.format(Locale.getDefault(), "Giảm %,.0f%%", discount);
            }
            holder.tvVoucherValueText.setText(valueTag);
            holder.tvVoucherTitle.setText(titleText);

            // 3. Conditions (minAmount)
            Double minAmount = doc.getDouble("minAmount");
            String condStr = "Áp dụng cho đơn vé";
            if (minAmount != null && minAmount > 0) {
                condStr += String.format(Locale.getDefault(), " từ %,.0f đ", minAmount);
            } else {
                condStr += " mọi giá trị";
            }
            holder.tvVoucherConditions.setText(condStr);

            // 4. Expiry Date
            long expiredTime = 0;
            if (doc.contains("expiredAt")) {
                Long exp = doc.getLong("expiredAt");
                if (exp != null) expiredTime = exp;
            }
            String expiryStr = "HSD: ";
            if (expiredTime > 0) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                expiryStr += sdf.format(new java.util.Date(expiredTime));
            } else {
                expiryStr += "Không giới hạn";
            }
            holder.tvVoucherExpiry.setText(expiryStr);

            // 5. Radio selection state
            boolean isSelected = doc.getId().equals(currentSelectedId);
            holder.imgVoucherRadio.setImageResource(isSelected ? R.drawable.ic_radio_selected : R.drawable.ic_radio_unselected);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoucherClick(doc, isSelected);
                }
            });

            final String finalName = name;
            final String finalCond = condStr;
            final String finalExpiry = expiryStr;
            holder.tvVoucherDetailLink.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(BookingConfirmActivity.this)
                        .setTitle("Chi tiết Voucher")
                        .setMessage(String.format(Locale.getDefault(), 
                                "Mã voucher: %s\nNội dung: %s\nĐiều kiện: %s\n%s", 
                                doc.getString("code") != null ? doc.getString("code") : doc.getId(),
                                finalName, 
                                finalCond, 
                                finalExpiry))
                        .setPositiveButton("Đóng", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            android.widget.TextView tvVoucherValueText;
            android.widget.TextView tvVoucherTitle;
            android.widget.TextView tvVoucherConditions;
            android.widget.TextView tvVoucherExpiry;
            android.widget.TextView tvVoucherDetailLink;
            android.widget.ImageView imgVoucherRadio;

            public ViewHolder(@androidx.annotation.NonNull android.view.View itemView) {
                super(itemView);
                tvVoucherValueText = itemView.findViewById(R.id.tvVoucherValueText);
                tvVoucherTitle = itemView.findViewById(R.id.tvVoucherTitle);
                tvVoucherConditions = itemView.findViewById(R.id.tvVoucherConditions);
                tvVoucherExpiry = itemView.findViewById(R.id.tvVoucherExpiry);
                tvVoucherDetailLink = itemView.findViewById(R.id.tvVoucherDetailLink);
                imgVoucherRadio = itemView.findViewById(R.id.imgVoucherRadio);
            }
        }
    }

    private interface OnVoucherClickListener {
        void onVoucherClick(DocumentSnapshot voucher, boolean isCurrentlySelected);
    }
}