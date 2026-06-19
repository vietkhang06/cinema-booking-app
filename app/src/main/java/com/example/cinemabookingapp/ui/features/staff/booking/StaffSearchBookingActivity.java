package com.example.cinemabookingapp.ui.features.staff.booking;

import com.example.cinemabookingapp.ui.features.staff.scanqr.StaffInvoiceActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.BookingDTO;
import com.example.cinemabookingapp.data.remote.api.BookingApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.ui.features.staff.booking.adapter.BookingAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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

public class StaffSearchBookingActivity extends AuthActivity {

    private TextInputEditText searchInput;
    private MaterialButton btnSearch;
    private RecyclerView resultsRv;
    private TextView tvNoResults;
    private View backBtn;
    private BookingAdapter adapter;
    private List<BookingDTO> bookingList = new ArrayList<>();

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_search_booking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        btnSearch = findViewById(R.id.btn_search);
        resultsRv = findViewById(R.id.search_results_rv);
        tvNoResults = findViewById(R.id.tv_no_results);
        backBtn = findViewById(R.id.back_btn);

        resultsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingList, booking -> {
            Intent intent = new Intent(StaffSearchBookingActivity.this, StaffInvoiceActivity.class);
            intent.putExtra("invoiceId", booking.bookingId);
            startActivity(intent);
        });
        resultsRv.setAdapter(adapter);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> doSearch());
    }

    private void doSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            showToast("Vui lòng nhập từ khóa tìm kiếm");
            return;
        }

        showLoading(true);
        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        bookingApi.searchBookings(query).enqueue(new Callback<ApiResponse<List<BookingDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDTO>>> call, Response<ApiResponse<List<BookingDTO>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bookingList.clear();
                    bookingList.addAll(response.body().getData());
//                    adapter.notifyDataSetChanged();

                    if (bookingList.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        resultsRv.setVisibility(View.GONE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        resultsRv.setVisibility(View.VISIBLE);
                    }

                    addRealtimeBookingListener(response.body().getData().stream().map(bookingDTO -> bookingDTO.bookingId).collect(Collectors.toList()));

                } else {
                    showToast("Không tìm thấy kết quả hoặc lỗi kết nối");
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
    private void addRealtimeBookingListener(List<String> bookingIds) {
        Log.i("StaffSearchBookingActivity", "Adding realtime listener for bookings: " + bookingIds);
        if(bookingIds == null || bookingIds.isEmpty()){
            return;
        }

        if(listener != null){
            removeRealtimeBookingListener();
        }
        Log.i("StaffSearchBookingActivity", "add realtime update listener for bookings: ");
        listener = firestore.collection("bookings")
                .whereIn("bookingId", bookingIds.stream().limit(29).collect(Collectors.toList())) // firebase 30 limit
                .addSnapshotListener((value, error) -> {
                    if(error != null) {
                        Log.e("StaffSearchBookingActivity", "Error listening for realtime booking updates: ", error);
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        Log.i("StaffSearchBookingActivity", "Realtime update received for bookings: " + value.size());
                        bookingList.clear();
                        bookingList.addAll(value.toObjects(BookingDTO.class));
                        adapter.notifyDataSetChanged();

                    }
                });
    }

    private void removeRealtimeBookingListener() {
        if(listener != null){
            listener.remove();
            listener = null;

            Log.i("StaffSearchBookingActivity", "remove realtime update listener for bookings: ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRealtimeBookingListener();
    }
}
