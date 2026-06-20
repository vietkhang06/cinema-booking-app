package com.example.cinemabookingapp.ui.features.admin.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.UserDTO;
import com.example.cinemabookingapp.data.mapper.UserMapper;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.AdminUserApiService;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.ui.features.admin.user.adapter.AdminUserAdapter;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserManagementActivity extends BaseActivity {

    private EditText etSearchUser;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvCustomers;
    private View layoutEmptyState;

    private AdminUserAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private AdminUserApiService adminUserApi;

    private String currentSearch = "";
    private int currentFilterId = R.id.chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        adminUserApi = RetrofitClient.getInstance().create(AdminUserApiService.class);

        initViews();
        setupListeners();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void initViews() {
        etSearchUser = findViewById(R.id.etSearchUser);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        rvCustomers = findViewById(R.id.rvCustomers);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupListeners() {
        etSearchUser.addTextChangedListener(new TextWatcher() {
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

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                chipGroupFilters.check(R.id.chipAll);
                currentFilterId = R.id.chipAll;
            } else {
                currentFilterId = checkedId;
            }
            applyLocalFilters();
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter();
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvCustomers.setAdapter(adapter);

        adapter.setListener(user -> {
            Intent intent = new Intent(this, AdminUserDetailActivity.class);
            intent.putExtra("user_id", user.uid);
            startActivity(intent);
        });
    }

    private void loadUsers() {
        showLoading(true);
        adminUserApi.getAllUsers(null, null, null).enqueue(new Callback<ApiResponse<List<UserDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserDTO>>> call, Response<ApiResponse<List<UserDTO>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userList.clear();
                    List<UserDTO> dtoList = response.body().getData();
                    if (dtoList != null) {
                        for (UserDTO dto : dtoList) {
                            userList.add(UserMapper.toDomain(dto));
                        }
                    }
                    applyLocalFilters();
                } else {
                    showToast("Lỗi tải danh sách người dùng: " + getErrorMessage(response));
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
        for (User u : userList) {
            // Search filter
            if (!currentSearch.isEmpty()) {
                String keyword = currentSearch.toLowerCase();
                boolean match = (u.name != null && u.name.toLowerCase().contains(keyword))
                        || (u.email != null && u.email.toLowerCase().contains(keyword))
                        || (u.phone != null && u.phone.toLowerCase().contains(keyword));
                if (!match) continue;
            }

            // Status & Level filter
            boolean matchesFilter = true;
            String level = u.memberLevel != null ? u.memberLevel.toLowerCase() : "standard";
            if ("basic".equals(level)) {
                level = "standard";
            }
            String status = u.status != null ? u.status.toLowerCase() : "active";

            if (currentFilterId == R.id.chipStandard) {
                matchesFilter = "standard".equals(level);
            } else if (currentFilterId == R.id.chipVip) {
                matchesFilter = "vip".equals(level);
            } else if (currentFilterId == R.id.chipGold) {
                matchesFilter = "gold".equals(level);
            } else if (currentFilterId == R.id.chipPlatinum) {
                matchesFilter = "platinum".equals(level);
            } else if (currentFilterId == R.id.chipLocked) {
                matchesFilter = "locked".equals(status);
            }

            if (matchesFilter) {
                filtered.add(u);
            }
        }
        adapter.submitList(filtered);

        if (filtered.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvCustomers.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvCustomers.setVisibility(View.VISIBLE);
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
}