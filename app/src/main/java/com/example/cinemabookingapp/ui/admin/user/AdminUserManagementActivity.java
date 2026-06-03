package com.example.cinemabookingapp.ui.admin.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.UserDTO;
import com.example.cinemabookingapp.data.mapper.UserMapper;
import com.example.cinemabookingapp.data.remote.api.AdminStaffApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.ui.admin.user.adapter.AdminStaffAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserManagementActivity extends BaseActivity {

    private EditText etSearch;
    private Spinner spinnerStatus, spinnerCinema;
    private MaterialButton btnAddStaff;
    private RecyclerView rvStaff;
    private TextView tvEmpty;

    private AdminStaffAdapter adapter;
    private final List<User> staffList = new ArrayList<>();
    private final List<Cinema> cinemaList = new ArrayList<>();

    private AdminStaffApiService adminStaffApi;
    private CinemaRepositoryImpl cinemaRepository;

    private String currentSearch = "";
    private String currentStatusFilter = "all";
    private String currentCinemaFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        adminStaffApi = RetrofitClient.getInstance().create(AdminStaffApiService.class);
        cinemaRepository = new CinemaRepositoryImpl();

        initViews();
        setupSpinners();
        setupRecyclerView();
        bindActions();
        loadCinemas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStaffs();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerCinema = findViewById(R.id.spinnerCinema);
        btnAddStaff = findViewById(R.id.btnAddStaff);
        rvStaff = findViewById(R.id.rvStaff);
        tvEmpty = findViewById(R.id.tvEmpty);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupSpinners() {
        // Status Spinner
        String[] statuses = {"Tất cả trạng thái", "Hoạt động", "Tạm khóa"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statuses
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Cinema Spinner (initial)
        List<String> cinemaNames = new ArrayList<>();
        cinemaNames.add("Tất cả chi nhánh");
        ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cinemaNames
        );
        cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCinema.setAdapter(cinemaAdapter);
    }

    private void setupRecyclerView() {
        adapter = new AdminStaffAdapter();
        rvStaff.setLayoutManager(new LinearLayoutManager(this));
        rvStaff.setAdapter(adapter);

        adapter.setListener(user -> {
            Intent intent = new Intent(this, AdminStaffDetailActivity.class);
            intent.putExtra("staff_id", user.uid);
            startActivity(intent);
        });
    }

    private void bindActions() {
        btnAddStaff.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminStaffFormActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                applyLocalFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentStatusFilter = "all";
                } else if (position == 1) {
                    currentStatusFilter = "active";
                } else {
                    currentStatusFilter = "inactive";
                }
                applyLocalFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCinema.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentCinemaFilter = "all";
                } else {
                    currentCinemaFilter = cinemaList.get(position - 1).cinemaId;
                }
                applyLocalFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCinemas() {
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> data) {
                if (data != null) {
                    cinemaList.clear();
                    cinemaList.addAll(data);

                    List<String> cinemaNames = new ArrayList<>();
                    cinemaNames.add("Tất cả chi nhánh");
                    for (Cinema c : data) {
                        cinemaNames.add(c.name);
                    }

                    ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(
                            AdminUserManagementActivity.this,
                            android.R.layout.simple_spinner_item,
                            cinemaNames
                    );
                    cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCinema.setAdapter(cinemaAdapter);
                }
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải rạp: " + message);
            }
        });
    }

    private void loadStaffs() {
        showLoading(true);
        adminStaffApi.getAllStaffs(null, null, null).enqueue(new Callback<ApiResponse<List<UserDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserDTO>>> call, Response<ApiResponse<List<UserDTO>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    staffList.clear();
                    List<UserDTO> dtoList = response.body().getData();
                    if (dtoList != null) {
                        for (UserDTO dto : dtoList) {
                            staffList.add(UserMapper.toDomain(dto));
                        }
                    }
                    applyLocalFilters();
                } else {
                    showToast("Lỗi tải danh sách nhân viên: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserDTO>>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi kết nối máy chủ: " + t.getMessage());
            }
        });
    }

    private void applyLocalFilters() {
        List<User> filtered = new ArrayList<>();
        for (User u : staffList) {
            // Search
            if (!currentSearch.isEmpty()) {
                String keyword = currentSearch.toLowerCase();
                boolean match = (u.name != null && u.name.toLowerCase().contains(keyword))
                        || (u.email != null && u.email.toLowerCase().contains(keyword))
                        || (u.phone != null && u.phone.toLowerCase().contains(keyword));
                if (!match) continue;
            }
            // Status
            if (!"all".equalsIgnoreCase(currentStatusFilter)) {
                if (!currentStatusFilter.equalsIgnoreCase(u.status)) {
                    continue;
                }
            }
            // Cinema
            if (!"all".equalsIgnoreCase(currentCinemaFilter)) {
                if (u.cinemaId == null || !u.cinemaId.equals(currentCinemaFilter)) {
                    continue;
                }
            }
            filtered.add(u);
        }
        adapter.submitList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
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
}