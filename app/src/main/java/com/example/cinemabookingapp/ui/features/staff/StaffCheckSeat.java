package com.example.cinemabookingapp.ui.features.staff;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.AuditLogDTO;
import com.example.cinemabookingapp.data.dto.SeatDTO;
import com.example.cinemabookingapp.data.dto.SeatLockRequestDTO;
import com.example.cinemabookingapp.data.remote.api.AuditLogApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.SeatApiService;
import com.example.cinemabookingapp.ui.features.staff.adapter.StaffSeatAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffCheckSeat extends AuthActivity {

    private RecyclerView rvSeatMap;
    private View backBtn;
    private StaffSeatAdapter adapter;
    private final List<SeatDTO> seatList = new ArrayList<>();
    private String showtimeId;
    private ListenerRegistration seatListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_check_seat);

        showtimeId = getIntent().getStringExtra("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            showToast("SuÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
            finish();
            return;
        }

        initViews();
        bindActions();
        loadSeats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (seatListenerRegistration != null) {
            seatListenerRegistration.remove();
        }
    }

    private void initViews() {
        backBtn = findViewById(R.id.staff_invoice_back_btn);
        rvSeatMap = findViewById(R.id.rvSeatMap);

        // KhÃ¡Â»Å¸i tÃ¡ÂºÂ¡o vÃ¡Â»â€ºi span count mÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¹nh 9; sÃ¡ÂºÂ½ Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t Ã„â€˜Ã¡Â»â„¢ng sau khi load data
        GridLayoutManager gridLayout = new GridLayoutManager(this, 9);
        gridLayout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Row label chiÃ¡ÂºÂ¿m full width (tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ cÃ¡Â»â„¢t); ghÃ¡ÂºÂ¿ chiÃ¡ÂºÂ¿m 1 cÃ¡Â»â„¢t
                return adapter.isLabel(position) ? gridLayout.getSpanCount() : 1;
            }
        });
        rvSeatMap.setLayoutManager(gridLayout);

        adapter = new StaffSeatAdapter(seatList, (seat, position) -> handleSeatClick(seat));
        rvSeatMap.setAdapter(adapter);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadSeats() {
        showLoading(true);
        SeatApiService seatApi = RetrofitClient.getInstance().create(SeatApiService.class);
        seatApi.getSeatsByShowtimeId(showtimeId).enqueue(new Callback<ApiResponse<List<SeatDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SeatDTO>>> call, Response<ApiResponse<List<SeatDTO>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    startRealtimeSeatSync();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "LÃ¡Â»â€”i server (" + response.code() + ")";
                    showToast(msg);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SeatDTO>>> call, Throwable t) {
                showLoading(false);
                showToast("LÃ¡Â»â€”i kÃ¡ÂºÂ¿t nÃ¡Â»â€˜i: " + t.getMessage());
                finish();
            }
        });
    }

    private void startRealtimeSeatSync() {
        if (seatListenerRegistration != null) {
            seatListenerRegistration.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        seatListenerRegistration = db.collection("seats")
                .whereEqualTo("showtimeId", showtimeId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("STAFF_SEAT_SYNC", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<SeatDTO> newSeats = new ArrayList<>();
                        int maxCol = 1;
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            SeatDTO seat = doc.toObject(SeatDTO.class);
                            if (seat != null) {
                                seat.seatId = doc.getId();
                                newSeats.add(seat);
                                if (seat.columnNo > maxCol) {
                                    maxCol = seat.columnNo;
                                }
                            }
                        }

                        // Sort seats consistently by rowName and columnNo
                        newSeats.sort((s1, s2) -> {
                            if (s1.rowName == null || s2.rowName == null) return 0;
                            int r = s1.rowName.compareTo(s2.rowName);
                            if (r != 0) return r;
                            return Integer.compare(s1.columnNo, s2.columnNo);
                        });

                        // CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t span count Ã„â€˜Ã¡Â»â„¢ng Ã„â€˜Ã¡Â»Æ’ khÃ¡Â»â€ºp vÃ¡Â»â€ºi sÃ¡Â»â€˜ cÃ¡Â»â„¢t thÃ¡Â»Â±c tÃ¡ÂºÂ¿
                        if (rvSeatMap.getLayoutManager() instanceof GridLayoutManager) {
                            ((GridLayoutManager) rvSeatMap.getLayoutManager()).setSpanCount(maxCol);
                        }

                        seatList.clear();
                        seatList.addAll(newSeats);
                        adapter.setSeats(seatList);
                    }
                });
    }

    private void handleSeatClick(SeatDTO seat) {
        long now = System.currentTimeMillis();
        boolean isBooked = "booked".equalsIgnoreCase(seat.status);
        boolean isHeld = "held".equalsIgnoreCase(seat.status) && (seat.heldUntil > now);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ThÃƒÂ´ng tin ghÃ¡ÂºÂ¿ " + seat.seatCode);

        if (isHeld) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String expiry = sdf.format(new Date(seat.heldUntil));
            builder.setMessage("TrÃ¡ÂºÂ¡ng thÃƒÂ¡i: Ã„Âang giÃ¡Â»Â¯ chÃ¡Â»â€”\n\nMÃƒÂ£ khÃƒÂ¡ch hÃƒÂ ng: " + seat.heldBy + "\nThÃ¡Â»Âi hÃ¡ÂºÂ¡n giÃ¡Â»Â¯: " + expiry);

            builder.setPositiveButton("GiÃ¡ÂºÂ£i phÃƒÂ³ng ghÃ¡ÂºÂ¿", (dialog, which) -> releaseSeat(seat));
            builder.setNegativeButton("Ã„ÂÃƒÂ³ng", null);
        } else if (isBooked) {
            builder.setMessage("TrÃ¡ÂºÂ¡ng thÃƒÂ¡i: Ã„ÂÃƒÂ£ Ã„â€˜Ã¡ÂºÂ·t mua (KhÃƒÂ´ng thÃ¡Â»Æ’ giÃ¡ÂºÂ£i phÃƒÂ³ng)");
            builder.setNegativeButton("Ã„ÂÃƒÂ³ng", null);
        } else {
            builder.setMessage("TrÃ¡ÂºÂ¡ng thÃƒÂ¡i: GhÃ¡ÂºÂ¿ trÃ¡Â»â€˜ng (" + (seat.seatType != null ? seat.seatType : "Standard") + ")");
            builder.setNegativeButton("Ã„ÂÃƒÂ³ng", null);
        }
        builder.show();
    }

    private void releaseSeat(SeatDTO seat) {
        showLoading(true);
        SeatApiService seatApi = RetrofitClient.getInstance().create(SeatApiService.class);
        SeatLockRequestDTO request = new SeatLockRequestDTO(showtimeId, Collections.singletonList(seat.seatId));

        seatApi.releaseSeatsByStaff(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    writeAuditLog("RELEASE_SEAT", seat.seatId, "Released held seat " + seat.seatCode + " held by " + seat.heldBy);
                    showToast("Ã„ÂÃƒÂ£ giÃ¡ÂºÂ£i phÃƒÂ³ng ghÃ¡ÂºÂ¿ " + seat.seatCode + " thÃƒÂ nh cÃƒÂ´ng!");
                } else {
                    showToast("LÃ¡Â»â€”i giÃ¡ÂºÂ£i phÃƒÂ³ng ghÃ¡ÂºÂ¿: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                showToast("LÃ¡Â»â€”i kÃ¡ÂºÂ¿t nÃ¡Â»â€˜i: " + t.getMessage());
            }
        });
    }

    private void writeAuditLog(String action, String targetId, String note) {
        AuditLogDTO log = new AuditLogDTO();
        log.actorId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "unknown_staff";
        log.actorRole = "staff";
        log.action = action;
        log.targetType = "seat";
        log.targetId = targetId;
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
}