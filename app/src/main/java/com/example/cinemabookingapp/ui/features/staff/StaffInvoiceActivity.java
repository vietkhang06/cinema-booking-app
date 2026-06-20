package com.example.cinemabookingapp.ui.features.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.AuditLogDTO;
import com.example.cinemabookingapp.data.remote.api.AuditLogApiService;
import com.example.cinemabookingapp.data.remote.api.BookingApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.service.InvoiceService;
import com.example.cinemabookingapp.ui.component.EInvoice.EInvoiceView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffInvoiceActivity extends AuthActivity {

    private InvoiceService invoiceService;
    private InvoiceService.InvoiceDetail invoiceDetail;

    private View backButton;
    private MaterialButton updatePaymentButton, checkinButton, checkSeatButton, checkOrderButton;
    private TextView paymentStatusTV, transactionDateTV, checkinStatusTV;
    private ImageView paymentStatusImg;
    private EInvoiceView eInvoiceView;
    private String invoiceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_invoice);

        invoiceService = ServiceProvider.getInstance().getInvoiceService();

        initViews();
        bindActions();
        retrieveDataFromNavigator();
    }

    private void initViews() {
        eInvoiceView = findViewById(R.id.staff_einvoice);
        backButton = findViewById(R.id.staff_invoice_back_btn);
        updatePaymentButton = findViewById(R.id.staff_invoice_update_payment);
        checkinButton = findViewById(R.id.staff_invoice_checkin_btn);
        paymentStatusTV = findViewById(R.id.staff_invoice_payment_status_tv);
        transactionDateTV = findViewById(R.id.staff_invoice_transaction_date);
        checkinStatusTV = findViewById(R.id.staff_invoice_checkin_status_tv);
        paymentStatusImg = findViewById(R.id.staff_invoice_payment_status_img);
        checkSeatButton = findViewById(R.id.staff_invoice_check_seats);
        checkOrderButton = findViewById(R.id.staff_invoice_check_order);
    }

    private void bindActions() {
        backButton.setOnClickListener(v -> finish());

        checkOrderButton.setOnClickListener(v -> {
            if (invoiceDetail != null && invoiceDetail.booking != null) {
                Intent intent = new Intent(this, StaffCheckOrder.class);
                intent.putExtra("invoiceId", invoiceDetail.booking.bookingId);
                startActivity(intent);
            }
        });

        checkSeatButton.setOnClickListener(v -> {
            if (invoiceDetail != null && invoiceDetail.booking != null) {
                Intent intent = new Intent(this, StaffCheckSeat.class);
                intent.putExtra("invoiceId", invoiceDetail.booking.bookingId);
                intent.putExtra("showtimeId", invoiceDetail.booking.showtimeId);
                startActivity(intent);
            }
        });

        updatePaymentButton.setOnClickListener(v -> {
            if (invoiceDetail == null || invoiceDetail.booking == null) return;
            showLoading(true);
            BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
            bookingApi.confirmPayment(invoiceDetail.booking.bookingId).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        writeAuditLog("CONFIRM_PAYMENT", "Confirmed payment at counter");
                        showToast("Ã„ÂÃƒÂ£ xÃƒÂ¡c nhÃ¡ÂºÂ­n thanh toÃƒÂ¡n");
                        retrieveDataFromNavigator();
                    } else {
                        showLoading(false);
                        showToast("KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thanh toÃƒÂ¡n: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showLoading(false);
                    showToast("LÃ¡Â»â€”i: " + t.getMessage());
                }
            });
        });

        checkinButton.setOnClickListener(v -> {
            if (invoiceDetail == null || invoiceDetail.booking == null) return;
            showLoading(true);
            BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
            bookingApi.checkInBooking(invoiceDetail.booking.bookingId).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        writeAuditLog("CHECKIN", "Successfully check-in customer");
                        showToast("Check-in thÃƒÂ nh cÃƒÂ´ng");
                        if (invoiceDetail != null && invoiceDetail.booking != null) {
                            createCheckinNotification(invoiceDetail.booking.userId, invoiceDetail.booking.movieTitleSnapshot);
                        }
                        retrieveDataFromNavigator();
                    } else {
                        showLoading(false);
                        showToast("Check-in thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showLoading(false);
                    showToast("LÃ¡Â»â€”i: " + t.getMessage());
                }
            });
        });
    }

    private void retrieveDataFromNavigator() {
        Intent intent = getIntent();
        invoiceId = intent.getStringExtra("invoiceId");

        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            showToast("HÃƒÂ³a Ã„â€˜Ã†Â¡n khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
            finish();
            return;
        }

        showLoading(true);
        invoiceService.getInvoiceFromId(invoiceId, new ResultCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                if (booking == null) {
                    showLoading(false);
                    showToast("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y hÃƒÂ³a Ã„â€˜Ã†Â¡n: " + invoiceId);
                    finish();
                    return;
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    invoiceDetail = invoiceService.getInvoiceDetail(invoiceId);
                    runOnUiThread(() -> {
                        showLoading(false);
                        if (invoiceDetail != null) {
                            bindDataView(invoiceDetail);
                        } else {
                            showToast("LÃ¡Â»â€”i tÃ¡ÂºÂ£i chi tiÃ¡ÂºÂ¿t hÃƒÂ³a Ã„â€˜Ã†Â¡n");
                        }
                    });
                });
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showToast("LÃ¡Â»â€”i: " + message);
            }
        });
    }

    private void bindDataView(InvoiceService.InvoiceDetail detail) {
        eInvoiceView.setInvoiceDetail(detail);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        transactionDateTV.setText("NgÃƒÂ y tÃ¡ÂºÂ¡o: " + sdf.format(new Date(detail.booking.createdAt)));

        String payStatus = detail.booking.paymentStatus;
        if ("confirmed".equalsIgnoreCase(payStatus) || "paid".equalsIgnoreCase(payStatus)) {
            paymentStatusTV.setText("Ã„ÂÃƒÆ’ THANH TOÃƒÂN");
            paymentStatusTV.setTextColor(0xFF4CAF50);
            updatePaymentButton.setVisibility(View.GONE);
            checkinButton.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.ic_confirm).into(paymentStatusImg);

            if (detail.booking.checkInAt > 0) {
                checkinButton.setEnabled(false);
                checkinButton.setText("Ã„ÂÃƒÂ£ Check-in");
                checkinStatusTV.setText("Ã„ÂÃƒÂ£ Check-in lÃƒÂºc: " + sdf.format(new Date(detail.booking.checkInAt)));
                checkinStatusTV.setTextColor(0xFF4CAF50);
            } else {
                checkinButton.setEnabled(true);
                checkinButton.setText("Check-in VÃƒÂ©");
                checkinStatusTV.setText("TrÃ¡ÂºÂ¡ng thÃƒÂ¡i: ChÃ†Â°a Check-in");
                checkinStatusTV.setTextColor(0xFFE53935);
            }
        } else {
            paymentStatusTV.setText("CHÃ¡Â»Å“ THANH TOÃƒÂN");
            paymentStatusTV.setTextColor(0xFFFF9800);
            updatePaymentButton.setVisibility(View.VISIBLE);
            checkinButton.setVisibility(View.GONE);
            Glide.with(this).load(R.drawable.ic_cross_circle).into(paymentStatusImg);
            checkinStatusTV.setText("VÃƒÂ© chÃ†Â°a thanh toÃƒÂ¡n, khÃƒÂ´ng thÃ¡Â»Æ’ check-in");
            checkinStatusTV.setTextColor(0xFFE53935);
        }
    }

    private void writeAuditLog(String action, String note) {
        AuditLogDTO log = new AuditLogDTO();
        log.actorId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "unknown_staff";
        log.actorRole = "staff";
        log.action = action;
        log.targetType = "booking";
        log.targetId = invoiceId;
        log.note = note;
        log.createdAt = System.currentTimeMillis();

        AuditLogApiService auditLogApi = RetrofitClient.getInstance().create(AuditLogApiService.class);
        auditLogApi.createAuditLog(log).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {}
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
        });
    }

    private void createCheckinNotification(String userId, String movieTitle) {
        if (userId == null) return;
        com.example.cinemabookingapp.domain.model.Notification notification = new com.example.cinemabookingapp.domain.model.Notification();
        notification.userId = userId;
        notification.title = "Check-in thÃƒÂ nh cÃƒÂ´ng";
        notification.message = "CÃ¡ÂºÂ£m Ã†Â¡n bÃ¡ÂºÂ¡n Ã„â€˜ÃƒÂ£ check-in xem phim " + (movieTitle != null ? movieTitle : "") + ". ChÃƒÂºc bÃ¡ÂºÂ¡n xem phim vui vÃ¡ÂºÂ»!";
        notification.type = "CHECKIN_SUCCESS";
        notification.isRead = false;
        notification.createdAt = System.currentTimeMillis();
        notification.updatedAt = System.currentTimeMillis();

        new com.example.cinemabookingapp.data.repository.NotificationRepositoryImpl()
            .createNotification(notification, null);
    }
}