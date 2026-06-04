package com.example.cinemabookingapp.ui.customer.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.DataNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.model.CineShopOrder;
import com.example.cinemabookingapp.service.BookingService;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    public static class Observable {
        public interface OnBookingRealtimeListener {
            void onBookingUpdate(List<Booking> booking);
        }

        List<OnBookingRealtimeListener> listeners = new ArrayList<>();
        public void addListener(OnBookingRealtimeListener listener) {
            listeners.add(listener);
        }
        public void removeListener(OnBookingRealtimeListener listener) {
            listeners.remove(listener);
        }
        public void notify(List<Booking> booking) {
            for (OnBookingRealtimeListener listener : listeners) {
                listener.onBookingUpdate(booking);
            }
        }

    }
    Observable realtimeObservable;


    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private BookingService bookingService;

    private com.google.firebase.firestore.ListenerRegistration bookingsListener;
    private com.google.firebase.firestore.ListenerRegistration shopOrdersListener;
    private final List<Booking> movieBookings = new ArrayList<>();
    private final List<Booking> cineShopBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bookingService = ServiceProvider.getInstance().getBookingService();

        initViews();
        loadData();

        realtimeObservable = new Observable();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvTransactions = findViewById(R.id.rv_transactions);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);

        adapter = new TransactionAdapter(booking -> {
            String pStatus = booking.paymentStatus != null ? booking.paymentStatus.toUpperCase() : "PENDING";
            String bStatus = booking.bookingStatus != null ? booking.bookingStatus.toUpperCase() : "PENDING";
            boolean isPaid = "SUCCESS".equals(pStatus) || "PAID".equals(pStatus) ||
                             "CONFIRMED".equals(bStatus) || "SUCCESS".equals(bStatus);

            boolean isCancalled = "CANCELLED".equalsIgnoreCase(booking.bookingStatus);
            if (isCancalled) {
                Toast.makeText(this, "Chỉ vé đã thanh toán mới xem được chi tiết và mã QR!", Toast.LENGTH_LONG).show();
                return;
            }

            if (isPaid) {
//                AppNavigator.goToTicketDetail(this, booking.bookingId);
                Intent intent = new Intent(this, TicketDetailActivity.class);
                intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ID, booking.bookingId);
                intent.putExtra("observerResourceId", DataNavigator.getInstance().pushData(realtimeObservable));
                startActivity(intent);

                combineAndDisplay();
            } else {
                Toast.makeText(this, "Chỉ vé đã thanh toán mới xem được chi tiết và mã QR!", Toast.LENGTH_LONG).show();
            }
        });

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void loadData() {
        showLoading(true);
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUid == null) {
            showLoading(false);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        if (bookingsListener != null) {
            bookingsListener.remove();
        }
        if (shopOrdersListener != null) {
            shopOrdersListener.remove();
        }

        movieBookings.clear();
        cineShopBookings.clear();

        // 1. Listen to movie bookings collection in real-time
        bookingsListener = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("userId", currentUid)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("TransactionHistory", "Bookings listen error: " + error.getMessage());
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Booking> list = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Booking booking = doc.toObject(Booking.class);
                            if (booking != null && !booking.deleted) {
                                booking.bookingId = doc.getId();
                                list.add(booking);
                            }
                        }
                        movieBookings.clear();
                        movieBookings.addAll(list);
                        combineAndDisplay();

                    }
                });

        // 2. Listen to CineShop orders collection in real-time
        shopOrdersListener = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("cine_shop_orders")
                .whereEqualTo("userId", currentUid)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("TransactionHistory", "CineShop listen error: " + error.getMessage());
                        return;
                    }
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<CineShopOrder> cineShopOrders = querySnapshot.toObjects(CineShopOrder.class);
                        List<Booking> list = new ArrayList<>();

                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Booking orderBooking = new Booking();
                            orderBooking.bookingId = doc.getId();
                            orderBooking.userId = doc.getString("userId");
                            orderBooking.showtimeId = null; // Mark as CineShop order
                            orderBooking.movieTitleSnapshot = doc.getString("itemName");
                            orderBooking.movieImageUrlSnapshot = doc.getString("itemImageUrl");
                            
                            String paymentMethod = doc.getString("paymentMethod");
                            orderBooking.cinemaNameSnapshot = "CineShop - Nhận tại rạp (" + (paymentMethod != null ? paymentMethod : "ZALOPAY") + ")";
                            
                            Long qtyObj = doc.getLong("quantity");
                            int qty = qtyObj != null ? qtyObj.intValue() : 1;
                            orderBooking.roomNameSnapshot = "Số lượng: " + qty;
                            orderBooking.showtimeStartAtSnapshot = 0;
                            
                            Double priceObj = doc.getDouble("totalPrice");
                            double price = priceObj != null ? priceObj : 0.0;
                            orderBooking.total = price;
                            orderBooking.subtotal = price;
                            
                            orderBooking.bookingStatus = doc.getString("status");
                            orderBooking.paymentStatus = doc.getString("status");
                            
                            Long createdAtObj = doc.getLong("createdAt");
                            orderBooking.createdAt = createdAtObj != null ? createdAtObj : 0;

                            list.add(orderBooking);
                        }
                        cineShopBookings.clear();
                        cineShopBookings.addAll(list);
                        combineAndDisplay();
                    }
                });
    }

    private void combineAndDisplay() {
        List<Booking> allTransactions = new ArrayList<>();
        allTransactions.addAll(movieBookings);
        allTransactions.addAll(cineShopBookings);

        // Sort combined list by createdAt descending
        allTransactions.sort((t1, t2) -> Long.compare(t2.createdAt, t1.createdAt));

        if(realtimeObservable != null)
            realtimeObservable.notify(allTransactions);

        showLoading(false);
        if (allTransactions.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
            adapter.setBookings(allTransactions);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingsListener != null) {
            bookingsListener.remove();
        }
        if (shopOrdersListener != null) {
            shopOrdersListener.remove();
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            layoutEmpty.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.GONE);
        }
    }
}
