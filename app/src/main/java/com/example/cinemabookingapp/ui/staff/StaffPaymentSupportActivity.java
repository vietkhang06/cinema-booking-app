package com.example.cinemabookingapp.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.BookingDTO;
import com.example.cinemabookingapp.data.remote.api.BookingApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.ui.staff.adapter.BookingAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffPaymentSupportActivity extends AuthActivity {

    private RecyclerView pendingPaymentsRv;
    private TextView tvNoResults;
    private View backBtn;
    private BookingAdapter adapter;
    private List<BookingDTO> pendingList = new ArrayList<>();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_payment_support);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
        loadPendingPayments();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        loadPendingPayments();
    }

    private void initViews() {
        pendingPaymentsRv = findViewById(R.id.pending_payments_rv);
        tvNoResults = findViewById(R.id.tv_no_results);
        backBtn = findViewById(R.id.back_btn);

        pendingPaymentsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(pendingList, booking -> {
            Intent intent = new Intent(StaffPaymentSupportActivity.this, StaffInvoiceActivity.class);
            intent.putExtra("invoiceId", booking.bookingId);
            startActivity(intent);
        });
        pendingPaymentsRv.setAdapter(adapter);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadPendingPayments() {
        showLoading(true);
        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        // query = "" gets all bookings, then filter client-side
        bookingApi.searchBookings("").enqueue(new Callback<ApiResponse<List<BookingDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDTO>>> call, Response<ApiResponse<List<BookingDTO>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    pendingList.clear();
                    for (BookingDTO b : response.body().getData()) {
                        if ("PENDING".equalsIgnoreCase(b.paymentStatus)) {
                            pendingList.add(b);
                        }
                    }
                    addListenerWithBookings(pendingList.stream().map(bookingDTO -> bookingDTO.bookingId).collect(Collectors.toList()));

                    if (pendingList.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        pendingPaymentsRv.setVisibility(View.GONE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        pendingPaymentsRv.setVisibility(View.VISIBLE);
                    }
                } else {
                    showToast("Không thể tải danh sách thanh toán");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDTO>>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }
    ListenerRegistration listener;
    void addListenerWithBookings(List<String> bookingIds){
        if(bookingIds == null || bookingIds.isEmpty()){
            return;
        }

        // lay booking theo query bookingId va cac booking moi
        listener = firestore.collection("bookings")
            .where(Filter.or(
                    Filter.equalTo("paymentStatus", "PENDING"),
                    Filter.inArray("bookingId", bookingIds))
            ).addSnapshotListener((value, error) -> {
                if(error != null) {
                    return;
                }
                if (value != null && !value.isEmpty()) {
                    value.toObjects(BookingDTO.class);
                    pendingList.clear();
                    pendingList.addAll(value.toObjects(BookingDTO.class).stream()
                            .filter(b -> "PENDING".equalsIgnoreCase(b.paymentStatus))
                            .collect(Collectors.toList()));

                    adapter.notifyDataSetChanged();
                }
            });
    }

    private void removeRealtimeBookingListener() {
        if(listener != null){
            listener.remove();
            listener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRealtimeBookingListener();
    }

}
