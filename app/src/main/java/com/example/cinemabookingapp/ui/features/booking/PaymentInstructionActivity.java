package com.example.cinemabookingapp.ui.features.booking;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.remote.api.BookingApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.ui.features.transaction.TicketDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class PaymentInstructionActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "bookingId";
    public static final String EXTRA_PAYMENT_ID = "paymentId";
    public static final String EXTRA_PAYMENT_CODE = "paymentCode";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_PAYMENT_METHOD = "paymentMethod";

    private String bookingId;
    private String paymentId;
    private String paymentCode;
    private double amount;
    private String paymentMethod;

    private TextView tvStatusText;
    private TextView tvAmount;
    private TextView tvPaymentCode;
    private TextView tvAccountNumber;
    private ImageView imgQrCode;
    private ProgressBar pbQrLoading;
    private MaterialButton btnOpenApp;
    private View layoutTestButtons;
    private MaterialButton btnTestSuccess;
    private MaterialButton btnTestFailed;

    private FirebaseFirestore db;
    private ListenerRegistration paymentListener;
    private ListenerRegistration bookingListener;

    // Guard: Ä‘áº£m báº£o payment chá»‰ Ä‘Æ°á»£c xá»­ lÃ½ Má»˜T Láº¦N dÃ¹ listener fire nhiá»u láº§n
    private volatile boolean paymentHandled = false;
    private BookingTimerManager.TimerListener timerListener;
    private boolean hasShownWarning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_instruction);

        // Retrieve extras
        bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        paymentId = getIntent().getStringExtra(EXTRA_PAYMENT_ID);
        paymentCode = getIntent().getStringExtra(EXTRA_PAYMENT_CODE);
        amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0.0);
        paymentMethod = getIntent().getStringExtra(EXTRA_PAYMENT_METHOD);

        if (bookingId == null || paymentCode == null) {
            Toast.makeText(this, "KhÃ´ng cÃ³ thÃ´ng tin Ä‘áº·t vÃ© há»£p lá»‡", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        loadQrCode();
        startPaymentListener();
        startBookingListener(); // Listener thá»© 2: láº¯ng nghe bookings doc Ä‘á»ƒ báº¯t paymentStatus

        long createdAt = getIntent().getLongExtra("createdAt", 0);
        if (createdAt > 0) {
            startCountdownTimer(createdAt + 300000);
        }
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvStatusText = findViewById(R.id.tvStatusText);
        tvAmount = findViewById(R.id.tvAmount);
        tvPaymentCode = findViewById(R.id.tvPaymentCode);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        imgQrCode = findViewById(R.id.imgQrCode);
        pbQrLoading = findViewById(R.id.pbQrLoading);
        btnOpenApp = findViewById(R.id.btnOpenApp);
        layoutTestButtons = findViewById(R.id.layoutTestButtons);
        btnTestSuccess = findViewById(R.id.btnTestSuccess);
        btnTestFailed = findViewById(R.id.btnTestFailed);

        tvAmount.setText(String.format(Locale.getDefault(), "%,.0f Ä‘", amount));
        tvPaymentCode.setText(paymentCode);

        TextView tvBankName = findViewById(R.id.tvBankName);
        TextView tvAccountName = findViewById(R.id.tvAccountName);

        if ("momo".equals(paymentMethod)) {
            btnOpenApp.setText("Má»Ÿ á»©ng dá»¥ng MoMo");
            if (layoutTestButtons != null) {
                layoutTestButtons.setVisibility(View.VISIBLE);
            }
            if (tvBankName != null) tvBankName.setText("VÃ­ Ä‘iá»‡n tá»­ MOMO");
            if (tvAccountName != null) tvAccountName.setText("ÄoÃ n Viá»‡t Khang");
            if (tvAccountNumber != null) tvAccountNumber.setText("0762654245");
        } else {
            btnOpenApp.setText("Má»Ÿ á»©ng dá»¥ng NgÃ¢n hÃ ng");
            if (layoutTestButtons != null) {
                layoutTestButtons.setVisibility(View.GONE);
            }
            if (tvBankName != null) tvBankName.setText("MB BANK");
            if (tvAccountName != null) tvAccountName.setText("PHAM NGOC GIA KHANG");
            if (tvAccountNumber != null) tvAccountNumber.setText("0869612460");
        }
    }

    private void setupListeners() {
        ImageButton btnCopyAccount = findViewById(R.id.btnCopyAccount);
        ImageButton btnCopyAmount = findViewById(R.id.btnCopyAmount);
        ImageButton btnCopyCode = findViewById(R.id.btnCopyCode);

        if (btnCopyAccount != null) {
            btnCopyAccount.setOnClickListener(v -> copyToClipboard("Sá»‘ tÃ i khoáº£n", tvAccountNumber.getText().toString()));
        }

        if (btnCopyAmount != null) {
            btnCopyAmount.setOnClickListener(v -> {
                String rawAmount = String.format(Locale.getDefault(), "%.0f", amount);
                copyToClipboard("Sá»‘ tiá»n", rawAmount);
            });
        }

        if (btnCopyCode != null) {
            btnCopyCode.setOnClickListener(v -> copyToClipboard("Ná»™i dung chuyá»ƒn khoáº£n", paymentCode));
        }

        if (btnOpenApp != null) {
            btnOpenApp.setOnClickListener(v -> openPaymentApp());
        }

        if (btnTestSuccess != null) {
            btnTestSuccess.setOnClickListener(v -> handleTestPaymentSuccess());
        }

        if (btnTestFailed != null) {
            btnTestFailed.setOnClickListener(v -> handleTestPaymentFailed());
        }
    }

    private void loadQrCode() {
        String encodedAccountName = Uri.encode("PHAM NGOC GIA KHANG");
        String qrUrl = String.format(Locale.US,
                "https://img.vietqr.io/image/MB-0869612460-compact.png?amount=%.0f&addInfo=%s&accountName=%s",
                amount, paymentCode, encodedAccountName);

        pbQrLoading.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(qrUrl)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        pbQrLoading.setVisibility(View.GONE);
                        Toast.makeText(PaymentInstructionActivity.this, "KhÃ´ng thá»ƒ táº£i mÃ£ QR", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                   com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource,
                                                   boolean isFirstResource) {
                        pbQrLoading.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imgQrCode);
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "ÄÃ£ sao chÃ©p " + label + " vÃ o bá»™ nhá»› táº¡m", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPaymentApp() {
        if ("momo".equals(paymentMethod)) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.mservice.momotransfer");
            if (intent != null) {
                startActivity(intent);
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mservice.momotransfer")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.mservice.momotransfer")));
                }
            }
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dl.vietqr.co/pay"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Vui lÃ²ng má»Ÿ á»©ng dá»¥ng ngÃ¢n hÃ ng cá»§a báº¡n Ä‘á»ƒ chuyá»ƒn khoáº£n.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Listener 1: Láº¯ng nghe collection "payments" â€” báº¯t khi payment doc Ä‘Æ°á»£c táº¡o/cáº­p nháº­t.
     */
    private void startPaymentListener() {
        if (paymentId != null && !paymentId.isEmpty()) {
            paymentListener = db.collection("payments").document(paymentId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            android.util.Log.e("PAYMENT_FLOW", "payments listener failed", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            checkPaymentStatus(snapshot.getString("status"));
                        }
                    });
        } else {
            // Fallback: query by bookingId
            paymentListener = db.collection("payments")
                    .whereEqualTo("bookingId", bookingId)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            android.util.Log.e("PAYMENT_FLOW", "payments query listener failed", e);
                            return;
                        }
                        if (snapshots != null && !snapshots.isEmpty()) {
                            DocumentSnapshot doc = snapshots.getDocuments().get(0);
                            checkPaymentStatus(doc.getString("status"));
                        }
                    });
        }
    }

    /**
     * Listener 2: Láº¯ng nghe document "bookings/{bookingId}" â€” báº¯t khi backend
     * update paymentStatus trá»±c tiáº¿p trÃªn booking document (khÃ´ng qua payments collection).
     * ÄÃ¢y lÃ  fallback quan trá»ng khi payments doc chÆ°a tá»“n táº¡i.
     */
    private void startBookingListener() {
        if (bookingId == null) return;
        bookingListener = db.collection("bookings").document(bookingId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        android.util.Log.e("PAYMENT_FLOW", "bookings listener failed", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Long createdAtVal = snapshot.getLong("createdAt");
                        if (createdAtVal != null && createdAtVal > 0) {
                            startCountdownTimer(createdAtVal + 300000);
                        }

                        // Kiá»ƒm tra cáº£ paymentStatus láº«n bookingStatus
                        String paymentStatus = snapshot.getString("paymentStatus");
                        String bookingStatus = snapshot.getString("bookingStatus");
                        android.util.Log.d("PAYMENT_FLOW", "Booking doc update â€” paymentStatus=" + paymentStatus + " bookingStatus=" + bookingStatus);
                        // Æ¯u tiÃªn paymentStatus, fallback sang bookingStatus
                        String effectiveStatus = paymentStatus != null ? paymentStatus : bookingStatus;
                        checkPaymentStatus(effectiveStatus);
                    }
                });
    }

    /**
     * ZELIOUS: Kiá»ƒm tra vÃ  xá»­ lÃ½ tráº¡ng thÃ¡i payment. Guard paymentHandled Ä‘áº£m báº£o
     * chá»‰ xá»­ lÃ½ 1 láº§n dÃ¹ 2 listeners cÃ¹ng fire.
     */
    private void checkPaymentStatus(String status) {
        if (status == null) return;
        // Guard: Ä‘Ã£ xá»­ lÃ½ rá»“i thÃ¬ bá» qua
        if (paymentHandled) return;

        android.util.Log.d("PAYMENT_FLOW", "checkPaymentStatus: " + status);

        if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)
                || "CONFIRMED".equalsIgnoreCase(status)) {
            // ÄÃ¡nh dáº¥u Ä‘Ã£ xá»­ lÃ½ TRÆ¯á»šC â€” trÃ¡nh race condition tá»« 2 listeners
            paymentHandled = true;
            stopAllListeners();
            BookingTimerManager.getInstance().stopTimer(this);

            createNotification("Thanh toÃ¡n thÃ nh cÃ´ng", "Giao dá»‹ch thanh toÃ¡n vÃ© xem phim cá»§a báº¡n Ä‘Ã£ thÃ nh cÃ´ng. ChÃºc báº¡n xem phim vui váº»!", "BOOKING_SUCCESS");
            Toast.makeText(this, "Thanh toÃ¡n thÃ nh cÃ´ng! Äang xuáº¥t vÃ©...", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ID, bookingId);
            intent.putExtra("EXTRA_FROM_BOOKING_SUCCESS", true);
            startActivity(intent);
            finish();

        } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
            if (!paymentHandled) {
                paymentHandled = true;
                stopAllListeners();
                BookingTimerManager.getInstance().stopTimer(this);
                createNotification("Thanh toÃ¡n tháº¥t báº¡i", "Giao dá»‹ch thanh toÃ¡n cá»§a báº¡n Ä‘Ã£ tháº¥t báº¡i hoáº·c bá»‹ há»§y.", "BOOKING_FAILED");
                tvStatusText.setText("Tráº¡ng thÃ¡i: Giao dá»‹ch tháº¥t báº¡i hoáº·c Ä‘Ã£ bá»‹ huá»·");
                View banner = findViewById(R.id.layoutStatusBanner);
                if (banner != null) {
                    banner.setBackgroundColor(0xFFD32F2F);
                }
            }
        } else if ("WAITING_CONFIRMATION".equalsIgnoreCase(status)) {
            paymentHandled = true;
            stopAllListeners();
            BookingTimerManager.getInstance().stopTimer(this);
            tvStatusText.setText("Tráº¡ng thÃ¡i: Chá» Admin xÃ¡c nháº­n...");
            View banner = findViewById(R.id.layoutStatusBanner);
            if (banner != null) {
                banner.setBackgroundColor(0xFF1976D2);
            }
        } else if ("PENDING".equalsIgnoreCase(status)) {
            View banner = findViewById(R.id.layoutStatusBanner);
            if (banner != null) {
                banner.setBackgroundColor(0xFFF57C00); // Keep orange for active countdown
            }
        }
    }

    private void handleTestPaymentSuccess() {
        // Disable button immediately to prevent double-click spam
        if (btnTestSuccess != null) btnTestSuccess.setEnabled(false);

        android.util.Log.d("BOOKING_FLOW", "TEST_BUTTON_CLICKED: SUCCESS. Calling PUT /api/v1/bookings/payment/" + bookingId + "/confirmed");

        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        bookingApi.confirmPayment(bookingId).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("BOOKING_FLOW", "confirmPayment API succeeded â€” waiting for Firestore listener to fire");
                    // KHÃ”NG Toast á»Ÿ Ä‘Ã¢y â€” listener sáº½ tá»± xá»­ lÃ½ khi Firestore update
                } else {
                    if (btnTestSuccess != null) btnTestSuccess.setEnabled(true);
                    Toast.makeText(PaymentInstructionActivity.this, "Lá»—i cáº­p nháº­t test success: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                if (btnTestSuccess != null) btnTestSuccess.setEnabled(true);
                Toast.makeText(PaymentInstructionActivity.this, "Lá»—i káº¿t ná»‘i máº¡ng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleTestPaymentFailed() {
        performBookingCancellation(false);
    }

    private void performBookingCancellation(boolean isAuto) {
        if (btnTestFailed != null) btnTestFailed.setEnabled(false);

        android.util.Log.d("BOOKING_FLOW", "Cancellation requested. Auto=" + isAuto + ". Calling PUT /api/v1/bookings/payment/" + bookingId + "/failed");

        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        bookingApi.cancelBooking(bookingId).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("BOOKING_FLOW", "cancelBooking API succeeded");
                    if (isAuto) {
                        Toast.makeText(PaymentInstructionActivity.this, "Giao dá»‹ch Ä‘Ã£ bá»‹ huá»· do háº¿t háº¡n giá»¯ gháº¿!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PaymentInstructionActivity.this, "Giáº£ láº­p: Thanh toÃ¡n tháº¥t báº¡i!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (btnTestFailed != null) btnTestFailed.setEnabled(true);
                    Toast.makeText(PaymentInstructionActivity.this, "Lá»—i huá»· giao dá»‹ch: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                if (btnTestFailed != null) btnTestFailed.setEnabled(true);
                Toast.makeText(PaymentInstructionActivity.this, "Lá»—i káº¿t ná»‘i máº¡ng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopAllListeners() {
        if (paymentListener != null) {
            paymentListener.remove();
            paymentListener = null;
        }
        if (bookingListener != null) {
            bookingListener.remove();
            bookingListener = null;
        }
    }

    // ZELIOUS TASK: Logic gá»­i thÃ´ng bÃ¡o khi thanh toÃ¡n thÃ nh cÃ´ng/tháº¥t báº¡i.
    // Láº¥y userId hiá»‡n táº¡i, táº¡o object Notification vá»›i type 'BOOKING_SUCCESS' hoáº·c 'BOOKING_FAILED'
    // Sau Ä‘Ã³ gá»i NotificationRepositoryImpl Ä‘á»ƒ Ä‘áº©y Document nÃ y xuá»‘ng Firestore.
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

    @Override
    protected void onResume() {
        super.onResume();
        if (timerListener != null) {
            BookingTimerManager.getInstance().registerListener(timerListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timerListener != null) {
            BookingTimerManager.getInstance().unregisterListener(timerListener);
        }
    }

    private void startCountdownTimer(long targetEndTime) {
        if (paymentHandled) return;

        long currentRemaining = BookingTimerManager.getInstance().getRemainingTimeMillis();
        long currentEndTime = System.currentTimeMillis() + currentRemaining;
        long diff = Math.abs(currentEndTime - targetEndTime);

        if (BookingTimerManager.getInstance().isTimerActive(this) && diff < 2000) {
            if (timerListener == null) {
                setupTimerListener();
                BookingTimerManager.getInstance().registerListener(timerListener);
            }
            return;
        }

        if (timerListener == null) {
            setupTimerListener();
        }

        BookingTimerManager.getInstance().startTimerWithEndTime(this, targetEndTime);
        BookingTimerManager.getInstance().registerListener(timerListener);
    }

    private void setupTimerListener() {
        timerListener = new BookingTimerManager.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                if (paymentHandled) return;
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                if (tvStatusText != null) {
                    tvStatusText.setText(String.format(Locale.getDefault(), "Tráº¡ng thÃ¡i: Chá» thanh toÃ¡n (%02d:%02d)", minutes, seconds));
                }
                if (millisUntilFinished <= 60000 && !hasShownWarning) {
                    hasShownWarning = true;
                    if (!isFinishing() && !isDestroyed()) {
                        new androidx.appcompat.app.AlertDialog.Builder(PaymentInstructionActivity.this)
                                .setTitle("ThÃ´ng bÃ¡o")
                                .setMessage("ChÃº Ã½ thá»i gian thanh toÃ¡n cÃ²n 1 phÃºt, xin vui lÃ²ng thanh toÃ¡n")
                                .setPositiveButton("ÄÃ³ng", (dialog, which) -> dialog.dismiss())
                                .setCancelable(false)
                                .show();
                    }
                }
            }

            @Override
            public void onFinish() {
                if (paymentHandled) return;
                paymentHandled = true;
                stopAllListeners();
                BookingTimerManager.getInstance().stopTimer(PaymentInstructionActivity.this);
                if (tvStatusText != null) {
                    tvStatusText.setText("Tráº¡ng thÃ¡i: Háº¿t háº¡n giá»¯ gháº¿!");
                }
                View banner = findViewById(R.id.layoutStatusBanner);
                if (banner != null) {
                    banner.setBackgroundColor(0xFFD32F2F);
                }
                Toast.makeText(PaymentInstructionActivity.this, "Thá»i gian giá»¯ gháº¿ Ä‘Ã£ háº¿t!", Toast.LENGTH_LONG).show();
                performBookingCancellation(true);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllListeners();
    }
}
