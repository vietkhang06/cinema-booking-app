package com.example.cinemabookingapp.ui.features.admin.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.UserDTO;
import com.example.cinemabookingapp.data.remote.api.AdminStaffApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStaffFormActivity extends BaseActivity {

    private TextInputLayout tilName, tilEmail, tilPhone, tilPassword, tilNotes;
    private EditText edtName, edtEmail, edtPhone, edtPassword, edtNotes;
    private Spinner spinnerRole, spinnerCinema, spinnerStatus;
    private MaterialButton btnSave;
    private TextView tvTitle;

    private AdminStaffApiService adminStaffApi;
    private CinemaRepositoryImpl cinemaRepository;

    private boolean isEditMode = false;
    private String staffId;
    private UserDTO currentStaff;
    private final List<Cinema> cinemaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_staff_form);

        adminStaffApi = RetrofitClient.getInstance().create(AdminStaffApiService.class);
        cinemaRepository = new CinemaRepositoryImpl();

        staffId = getIntent().getStringExtra("staff_id");
        isEditMode = !TextUtils.isEmpty(staffId);

        initViews();
        setupSpinners();
        bindActions();
        loadCinemas();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilStaffName);
        tilEmail = findViewById(R.id.tilStaffEmail);
        tilPhone = findViewById(R.id.tilStaffPhone);
        tilPassword = findViewById(R.id.tilStaffPassword);
        tilNotes = findViewById(R.id.tilStaffNotes);

        edtName = findViewById(R.id.edtStaffName);
        edtEmail = findViewById(R.id.edtStaffEmail);
        edtPhone = findViewById(R.id.edtStaffPhone);
        edtPassword = findViewById(R.id.edtStaffPassword);
        edtNotes = findViewById(R.id.edtStaffNotes);

        spinnerRole = findViewById(R.id.spinnerStaffRole);
        spinnerCinema = findViewById(R.id.spinnerStaffCinema);
        spinnerStatus = findViewById(R.id.spinnerStaffStatus);

        btnSave = findViewById(R.id.btnSaveStaff);
        tvTitle = findViewById(R.id.tvFormTitle);

        if (isEditMode) {
            tvTitle.setText("Cập nhật nhân viên");
            tilPassword.setVisibility(View.GONE);
            edtEmail.setEnabled(false); // Do not allow email change
        } else {
            tvTitle.setText("Thêm nhân viên mới");
            tilPassword.setVisibility(View.VISIBLE);
        }

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupSpinners() {
        // Roles spinner
        String[] roles = {"staff", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Status spinner
        String[] statuses = {"active", "inactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statuses
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Cinema spinner (initial empty)
        List<String> cinemaNames = new ArrayList<>();
        cinemaNames.add("Chưa gán rạp");
        ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cinemaNames
        );
        cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCinema.setAdapter(cinemaAdapter);
    }

    private void bindActions() {
        btnSave.setOnClickListener(v -> saveStaff());
    }

    private void loadCinemas() {
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> data) {
                if (data != null) {
                    cinemaList.clear();
                    cinemaList.addAll(data);

                    List<String> cinemaNames = new ArrayList<>();
                    cinemaNames.add("Chưa gán rạp");
                    for (Cinema c : data) {
                        cinemaNames.add(c.name);
                    }

                    ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(
                            AdminStaffFormActivity.this,
                            android.R.layout.simple_spinner_item,
                            cinemaNames
                    );
                    cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCinema.setAdapter(cinemaAdapter);

                    // If in edit mode, load staff details now after cinema spinner is ready
                    if (isEditMode) {
                        loadStaffDetails();
                    }
                }
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải rạp: " + message);
            }
        });
    }

    private void loadStaffDetails() {
        adminStaffApi.getStaffDetail(staffId).enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentStaff = response.body().getData();
                    bindStaffData(currentStaff);
                } else {
                    showToast("Không tìm thấy thông tin nhân viên");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                showToast("Lỗi kết nối khi tải chi tiết nhân viên");
                finish();
            }
        });
    }

    private void bindStaffData(UserDTO staff) {
        edtName.setText(staff.name);
        edtEmail.setText(staff.email);
        edtPhone.setText(staff.phone);
        edtNotes.setText(staff.internalNotes);

        // Set Role
        if ("admin".equalsIgnoreCase(staff.role)) {
            spinnerRole.setSelection(1);
        } else {
            spinnerRole.setSelection(0);
        }

        // Set Status
        if ("inactive".equalsIgnoreCase(staff.status)) {
            spinnerStatus.setSelection(1);
        } else {
            spinnerStatus.setSelection(0);
        }

        // Set Cinema selection
        if (staff.cinemaId != null) {
            for (int i = 0; i < cinemaList.size(); i++) {
                if (staff.cinemaId.equals(cinemaList.get(i).cinemaId)) {
                    spinnerCinema.setSelection(i + 1); // 0 is "Chưa gán rạp"
                    break;
                }
            }
        }
    }

    private void saveStaff() {
        clearErrors();

        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        String role = spinnerRole.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

        String cinemaId = null;
        String cinemaName = null;
        int cinemaPos = spinnerCinema.getSelectedItemPosition();
        if (cinemaPos > 0) {
            Cinema c = cinemaList.get(cinemaPos - 1);
            cinemaId = c.cinemaId;
            cinemaName = c.name;
        }

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Vui lòng nhập tên");
            return;
        }

        if (!TextUtils.isEmpty(phone)) {
            String phonePattern = "^(0|\\+84)[35789][0-9]{8}$";
            if (!phone.matches(phonePattern)) {
                tilPhone.setError("Số điện thoại không hợp lệ (Ví dụ: 0987654321 hoặc +84987654321)");
                return;
            }
        }

        if (!isEditMode) {
            if (TextUtils.isEmpty(email)) {
                tilEmail.setError("Vui lòng nhập email");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError("Email không hợp lệ");
                return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                tilPassword.setError("Mật khẩu khởi tạo >= 6 ký tự");
                return;
            }
        }

        btnSave.setEnabled(false);

        UserDTO staff = new UserDTO();
        staff.name = name;
        staff.email = email;
        staff.phone = phone;
        staff.role = role;
        staff.status = status;
        staff.cinemaId = cinemaId;
        staff.cinemaName = cinemaName;
        staff.internalNotes = notes;

        if (isEditMode) {
            adminStaffApi.updateStaff(staffId, staff).enqueue(new Callback<ApiResponse<UserDTO>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                    btnSave.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        showToast("Cập nhật thông tin thành công!");
                        finish();
                    } else {
                        showToast("Lỗi cập nhật: " + getErrorMessage(response));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                    btnSave.setEnabled(true);
                    showToast("Lỗi kết nối máy chủ: " + t.getMessage());
                }
            });
        } else {
            AdminStaffApiService.CreateStaffRequest request = new AdminStaffApiService.CreateStaffRequest(staff, password);
            adminStaffApi.createStaff(request).enqueue(new Callback<ApiResponse<UserDTO>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                    btnSave.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        showToast("Đã thêm tài khoản nhân viên thành công!");
                        finish();
                    } else {
                        showToast("Lỗi thêm tài khoản: " + getErrorMessage(response));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                    btnSave.setEnabled(true);
                    showToast("Lỗi kết nối máy chủ: " + t.getMessage());
                }
            });
        }
    }

    private String getErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errStr = response.errorBody().string();
                if (errStr.contains("\"message\":\"")) {
                    int start = errStr.indexOf("\"message\":\"") + 11;
                    int end = errStr.indexOf("\"", start);
                    return errStr.substring(start, end);
                }
                return errStr;
            }
        } catch (Exception ignored) {}
        if (response.body() != null && response.body() instanceof ApiResponse) {
            ApiResponse<?> apiResp = (ApiResponse<?>) response.body();
            if (apiResp.getMessage() != null) {
                return apiResp.getMessage();
            }
        }
        return "Lỗi hệ thống (Code: " + response.code() + ")";
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilNotes.setError(null);
    }
}
