package com.example.cinemabookingapp.ui.customer;

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
import com.example.cinemabookingapp.ui.customer.transaction.TicketDetailActivity;
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

    private FirebaseFirestore db;
    private ListenerRegistration paymentListener;

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
            Toast.makeText(this, "Không có thông tin đặt vé hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        loadQrCode();
        startPaymentListener();
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

        tvAmount.setText(String.format(Locale.getDefault(), "%,.0f đ", amount));
        tvPaymentCode.setText(paymentCode);

        if ("momo".equals(paymentMethod)) {
            btnOpenApp.setText("Mở ứng dụng MoMo");
        } else {
            btnOpenApp.setText("Mở ứng dụng Ngân hàng");
        }
    }

    private void setupListeners() {
        ImageButton btnCopyAccount = findViewById(R.id.btnCopyAccount);
        ImageButton btnCopyAmount = findViewById(R.id.btnCopyAmount);
        ImageButton btnCopyCode = findViewById(R.id.btnCopyCode);

        if (btnCopyAccount != null) {
            btnCopyAccount.setOnClickListener(v -> copyToClipboard("Số tài khoản", tvAccountNumber.getText().toString()));
        }

        if (btnCopyAmount != null) {
            btnCopyAmount.setOnClickListener(v -> {
                // Copy raw double value for easy bank input
                String rawAmount = String.format(Locale.getDefault(), "%.0f", amount);
                copyToClipboard("Số tiền", rawAmount);
            });
        }

        if (btnCopyCode != null) {
            btnCopyCode.setOnClickListener(v -> copyToClipboard("Nội dung chuyển khoản", paymentCode));
        }

        if (btnOpenApp != null) {
            btnOpenApp.setOnClickListener(v -> openPaymentApp());
        }
    }

    private void loadQrCode() {
        // MBBank code: MB (970422)
        // VietQR endpoint: https://img.vietqr.io/image/{BIN}-{AccountNumber}-{Template}.png?amount={Amount}&addInfo={AddInfo}&accountName={AccountName}
        String encodedAccountName = Uri.encode("CONG TY CP CINEMA VIETNAM");
        String qrUrl = String.format(Locale.US,
                "https://img.vietqr.io/image/MB-09012345678-compact.png?amount=%.0f&addInfo=%s&accountName=%s",
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
                        Toast.makeText(PaymentInstructionActivity.this, "Không thể tải mã QR", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Đã sao chép " + label + " vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
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
            // Universal deep link / website redirection for banking apps, or prompt user
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dl.vietqr.co/pay"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Vui lòng mở ứng dụng ngân hàng của bạn để chuyển khoản.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startPaymentListener() {
        if (paymentId == null) {
            // Fallback: If no direct payment ID, query payments by bookingId
            paymentListener = db.collection("payments")
                    .whereEqualTo("bookingId", bookingId)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            android.util.Log.e("PAYMENT_FLOW", "Listen failed", e);
                            return;
                        }
                        if (snapshots != null && !snapshots.isEmpty()) {
                            DocumentSnapshot doc = snapshots.getDocuments().get(0);
                            checkPaymentStatus(doc);
                        }
                    });
        } else {
            // Direct listen to the payment document
            paymentListener = db.collection("payments").document(paymentId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            android.util.Log.e("PAYMENT_FLOW", "Listen failed", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            checkPaymentStatus(snapshot);
                        }
                    });
        }
    }

    private void checkPaymentStatus(DocumentSnapshot document) {
        String status = document.getString("status");
        if (status == null) return;

        android.util.Log.d("PAYMENT_FLOW", "Payment status updated: " + status);

        if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
            // Stop listener
            stopPaymentListener();

            // Stop booking hold timer
            BookingTimerManager.getInstance().stopTimer(this);

            Toast.makeText(this, "Thanh toán thành công! Đang xuất vé...", Toast.LENGTH_LONG).show();

            // Open TicketDetailActivity
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ID, bookingId);
            startActivity(intent);
            finish();
        } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            tvStatusText.setText("Trạng thái: Giao dịch thất bại hoặc đã bị huỷ");
            View banner = findViewById(R.id.layoutStatusBanner);
            if (banner != null) {
                banner.setBackgroundColor(0xFFD32F2F); // Red
            }
        } else if ("WAITING_CONFIRMATION".equalsIgnoreCase(status)) {
            tvStatusText.setText("Trạng thái: Chờ Admin xác nhận...");
            View banner = findViewById(R.id.layoutStatusBanner);
            if (banner != null) {
                banner.setBackgroundColor(0xFF1976D2); // Blue
            }
        }
    }

    private void stopPaymentListener() {
        if (paymentListener != null) {
            paymentListener.remove();
            paymentListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPaymentListener();
    }
}
