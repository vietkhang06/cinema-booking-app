package com.example.cinemabookingapp.ui.features.staff.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.AttendanceDTO;
import com.example.cinemabookingapp.data.dto.ViolationDTO;
import com.example.cinemabookingapp.data.mapper.AttendanceMapper;
import com.example.cinemabookingapp.data.mapper.ViolationMapper;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.StaffAttendanceApiService;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Attendance;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.domain.model.Violation;
import com.example.cinemabookingapp.ui.features.admin.user.adapter.AdminAttendanceAdapter;
import com.example.cinemabookingapp.ui.features.admin.user.adapter.AdminViolationAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffAttendanceActivity extends BaseActivity {

    private TextView tvStaffName, tvStaffCinema;
    private Spinner spinnerShift;
    private MaterialButton btnCheckInOut;
    private TextView tvTodayStatus;
    private View cardShiftSelector;

    private MaterialButtonToggleGroup toggleGroup;
    private RecyclerView rvLogs;
    private TextView tvLogsEmpty;

    private StaffAttendanceApiService attendanceApi;
    private User currentStaffUser;
    private AttendanceDTO todayLog;

    private AdminAttendanceAdapter attendanceAdapter;
    private AdminViolationAdapter violationAdapter;

    private final List<Attendance> attendanceHistory = new ArrayList<>();
    private final List<Violation> violationHistory = new ArrayList<>();

    private boolean isCheckedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_attendance);

        attendanceApi = RetrofitClient.getInstance().create(StaffAttendanceApiService.class);

        initViews();
        setupShiftsSpinner();
        setupRecyclerView();
        bindActions();
        loadStaffProfile();
    }

    private void initViews() {
        tvStaffName = findViewById(R.id.tvStaffProfileName);
        tvStaffCinema = findViewById(R.id.tvStaffProfileCinema);
        spinnerShift = findViewById(R.id.spinnerShift);
        cardShiftSelector = findViewById(R.id.cardShiftSelector);
        btnCheckInOut = findViewById(R.id.btnCheckInOut);
        tvTodayStatus = findViewById(R.id.tvTodayStatusText);

        toggleGroup = findViewById(R.id.toggleLogsGroup);
        rvLogs = findViewById(R.id.rvMyLogs);
        tvLogsEmpty = findViewById(R.id.tvMyLogsEmpty);

        View btnBack = findViewById(R.id.btnStaffBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupShiftsSpinner() {
        String[] shifts = {"Ca Sáng (08:00 - 12:00)", "Ca Chiều (12:00 - 17:00)", "Ca Tối (17:00 - 22:00)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                shifts
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShift.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        attendanceAdapter = new AdminAttendanceAdapter();
        violationAdapter = new AdminViolationAdapter();

        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        rvLogs.setAdapter(attendanceAdapter); // Default is attendance
    }

    private void bindActions() {
        btnCheckInOut.setOnClickListener(v -> {
            if (!isCheckedIn) {
                performCheckIn();
            } else {
                performCheckOut();
            }
        });

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabMyAttendance) {
                    rvLogs.setAdapter(attendanceAdapter);
                    attendanceAdapter.submitList(attendanceHistory);
                    updateEmptyLogsState(attendanceHistory.isEmpty());
                } else if (checkedId == R.id.btnTabMyViolations) {
                    rvLogs.setAdapter(violationAdapter);
                    violationAdapter.submitList(violationHistory);
                    updateEmptyLogsState(violationHistory.isEmpty());
                }
            }
        });
    }

    private void updateEmptyLogsState(boolean isEmpty) {
        tvLogsEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void loadStaffProfile() {
        ServiceProvider.getInstance().getAuthenticationService().getCurrentAuthUser(new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    showToast("Phiên đăng nhập hết hạn!");
                    finish();
                    return;
                }
                currentStaffUser = user;
                tvStaffName.setText("Nhân Viên: " + (user.name != null ? user.name : user.email));
                
                String cinema = user.cinemaName != null ? user.cinemaName : "Chưa gán rạp";
                tvStaffCinema.setText("Rạp phụ trách: " + cinema);

                if (user.cinemaId == null || user.cinemaId.isEmpty()) {
                    btnCheckInOut.setEnabled(false);
                    btnCheckInOut.setAlpha(0.5f);
                    tvTodayStatus.setText("Lỗi: Bạn chưa được phân công chi nhánh, không thể điểm danh!");
                } else {
                    loadTodayStatus();
                }

                loadMyHistory();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải thông tin tài khoản");
                finish();
            }
        });
    }

    private void loadTodayStatus() {
        attendanceApi.getTodayAttendance().enqueue(new Callback<ApiResponse<AttendanceDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceDTO>> call, Response<ApiResponse<AttendanceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todayLog = response.body().getData();
                    bindTodayLogState(todayLog);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AttendanceDTO>> call, Throwable t) {
                showToast("Lỗi kết nối điểm danh hôm nay");
            }
        });
    }

    private void bindTodayLogState(AttendanceDTO log) {
        if (log == null) {
            isCheckedIn = false;
            cardShiftSelector.setVisibility(View.VISIBLE);
            btnCheckInOut.setEnabled(true);
            btnCheckInOut.setText("CHECK-IN");
            btnCheckInOut.setBackgroundColor(android.graphics.Color.parseColor("#A13345"));
            tvTodayStatus.setText("Trạng thái: Chưa điểm danh vào ca");
        } else if (log.checkOutTime <= 0) {
            isCheckedIn = true;
            cardShiftSelector.setVisibility(View.GONE);
            btnCheckInOut.setEnabled(true);
            btnCheckInOut.setText("CHECK-OUT");
            btnCheckInOut.setBackgroundColor(android.graphics.Color.parseColor("#E65100"));

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String checkinTimeStr = sdf.format(new Date(log.checkInTime));
            tvTodayStatus.setText("Trạng thái: Đã Check-in vào lúc " + checkinTimeStr + " (" + log.shiftName + ")");
        } else {
            isCheckedIn = false;
            cardShiftSelector.setVisibility(View.GONE);
            btnCheckInOut.setEnabled(false);
            btnCheckInOut.setAlpha(0.5f);
            btnCheckInOut.setText("ĐÃ RA CA");
            btnCheckInOut.setBackgroundColor(android.graphics.Color.parseColor("#7A757F"));

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String checkoutTimeStr = sdf.format(new Date(log.checkOutTime));
            tvTodayStatus.setText("Trạng thái: Đã Check-out ra lúc " + checkoutTimeStr + "\n(Thời gian làm: " + log.durationMinutes + " phút)");
        }
    }

    private void performCheckIn() {
        if (currentStaffUser == null || currentStaffUser.cinemaId == null) return;

        String shiftOption = spinnerShift.getSelectedItem().toString();
        String shiftName = "Ca Sáng";
        if (shiftOption.contains("Chiều")) {
            shiftName = "Ca Chiều";
        } else if (shiftOption.contains("Tối")) {
            shiftName = "Ca Tối";
        }

        btnCheckInOut.setEnabled(false);

        attendanceApi.checkIn(shiftName, currentStaffUser.cinemaId, currentStaffUser.cinemaName).enqueue(new Callback<ApiResponse<AttendanceDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceDTO>> call, Response<ApiResponse<AttendanceDTO>> response) {
                btnCheckInOut.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToast("Check-in thành công!");
                    todayLog = response.body().getData();
                    bindTodayLogState(todayLog);
                    loadMyHistory();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi check-in";
                    showToast(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AttendanceDTO>> call, Throwable t) {
                btnCheckInOut.setEnabled(true);
                showToast("Lỗi kết nối");
            }
        });
    }

    private void performCheckOut() {
        btnCheckInOut.setEnabled(false);

        attendanceApi.checkOut().enqueue(new Callback<ApiResponse<AttendanceDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<AttendanceDTO>> call, Response<ApiResponse<AttendanceDTO>> response) {
                btnCheckInOut.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToast("Check-out thành công!");
                    todayLog = response.body().getData();
                    bindTodayLogState(todayLog);
                    loadMyHistory();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi check-out";
                    showToast(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AttendanceDTO>> call, Throwable t) {
                btnCheckInOut.setEnabled(true);
                showToast("Lỗi kết nối");
            }
        });
    }

    private void loadMyHistory() {
        // Attendance logs
        attendanceApi.getMyAttendanceHistory().enqueue(new Callback<ApiResponse<List<AttendanceDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceDTO>>> call, Response<ApiResponse<List<AttendanceDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    attendanceHistory.clear();
                    List<AttendanceDTO> dtoList = response.body().getData();
                    if (dtoList != null) {
                        for (AttendanceDTO dto : dtoList) {
                            attendanceHistory.add(AttendanceMapper.toDomain(dto));
                        }
                    }
                    if (toggleGroup.getCheckedButtonId() == R.id.btnTabMyAttendance) {
                        attendanceAdapter.submitList(attendanceHistory);
                        updateEmptyLogsState(attendanceHistory.isEmpty());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceDTO>>> call, Throwable t) {}
        });

        // Violations log
        attendanceApi.getMyViolationHistory().enqueue(new Callback<ApiResponse<List<ViolationDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ViolationDTO>>> call, Response<ApiResponse<List<ViolationDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    violationHistory.clear();
                    List<ViolationDTO> dtoList = response.body().getData();
                    if (dtoList != null) {
                        for (ViolationDTO dto : dtoList) {
                            violationHistory.add(ViolationMapper.toDomain(dto));
                        }
                    }
                    if (toggleGroup.getCheckedButtonId() == R.id.btnTabMyViolations) {
                        violationAdapter.submitList(violationHistory);
                        updateEmptyLogsState(violationHistory.isEmpty());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ViolationDTO>>> call, Throwable t) {}
        });
    }
}
