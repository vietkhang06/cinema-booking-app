package com.example.cinemabookingapp.ui.admin.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.ViolationDTO;
import com.example.cinemabookingapp.data.remote.api.AdminStaffApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminViolationFormActivity extends BaseActivity {

    private TextInputLayout tilDesc, tilFine, tilPoints;
    private EditText edtDesc, edtFine, edtPoints;
    private Spinner spinnerType, spinnerSeverity;
    private MaterialButton btnSave;

    private AdminStaffApiService adminStaffApi;
    private String staffId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_violation_form);

        adminStaffApi = RetrofitClient.getInstance().create(AdminStaffApiService.class);
        staffId = getIntent().getStringExtra("staff_id");

        if (staffId == null) {
            showToast("Không tìm thấy thông tin nhân viên!");
            finish();
            return;
        }

        initViews();
        setupSpinners();
        bindActions();
    }

    private void initViews() {
        tilDesc = findViewById(R.id.tilVioDesc);
        tilFine = findViewById(R.id.tilVioFine);
        tilPoints = findViewById(R.id.tilVioPoints);

        edtDesc = findViewById(R.id.edtVioDesc);
        edtFine = findViewById(R.id.edtVioFine);
        edtPoints = findViewById(R.id.edtVioPoints);

        spinnerType = findViewById(R.id.spinnerVioType);
        spinnerSeverity = findViewById(R.id.spinnerVioSeverity);
        btnSave = findViewById(R.id.btnSaveViolation);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupSpinners() {
        // Types spinner
        String[] types = {
                "Đi trễ",
                "Về sớm",
                "Không check-in",
                "Không check-out",
                "Không hoàn thành nhiệm vụ",
                "Xử lý sai vé / sai booking",
                "Vi phạm nội quy khác",
                "Vi phạm nghiệp vụ tại quầy"
        };
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Severity spinner
        String[] severities = {"LOW", "MEDIUM", "HIGH"};
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                severities
        );
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeverity.setAdapter(severityAdapter);
    }

    private void bindActions() {
        btnSave.setOnClickListener(v -> saveViolation());
    }

    private void saveViolation() {
        clearErrors();

        String desc = edtDesc.getText().toString().trim();
        String fineStr = edtFine.getText().toString().trim();
        String pointsStr = edtPoints.getText().toString().trim();

        String type = spinnerType.getSelectedItem().toString();
        String severity = spinnerSeverity.getSelectedItem().toString();

        if (TextUtils.isEmpty(desc)) {
            tilDesc.setError("Vui lòng nhập mô tả lỗi vi phạm");
            return;
        }

        double fine = 0;
        if (!TextUtils.isEmpty(fineStr)) {
            try {
                fine = Double.parseDouble(fineStr);
            } catch (NumberFormatException e) {
                tilFine.setError("Số tiền không hợp lệ");
                return;
            }
        }

        int points = 0;
        if (!TextUtils.isEmpty(pointsStr)) {
            try {
                points = Integer.parseInt(pointsStr);
            } catch (NumberFormatException e) {
                tilPoints.setError("Số điểm không hợp lệ");
                return;
            }
        }

        btnSave.setEnabled(false);

        ViolationDTO violation = new ViolationDTO();
        violation.staffId = staffId;
        violation.violationType = type;
        violation.description = desc;
        violation.severity = severity;
        violation.penaltyAmount = fine;
        violation.penaltyPoints = points;
        violation.status = "PENDING";

        adminStaffApi.createViolation(violation).enqueue(new Callback<ApiResponse<ViolationDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ViolationDTO>> call, Response<ApiResponse<ViolationDTO>> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToast("Ghi nhận vi phạm thành công!");
                    finish();
                } else {
                    showToast("Lỗi ghi nhận: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ViolationDTO>> call, Throwable t) {
                btnSave.setEnabled(true);
                showToast("Lỗi kết nối máy chủ: " + t.getMessage());
            }
        });
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
        tilDesc.setError(null);
        tilFine.setError(null);
        tilPoints.setError(null);
    }
}
