package com.example.cinemabookingapp.ui.features.admin.user;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.UserDTO;
import com.example.cinemabookingapp.data.mapper.UserMapper;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.AdminUserApiService;
import com.example.cinemabookingapp.data.remote.api.VoucherApiService;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.ui.features.admin.user.adapter.AdminUserAdapter;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private VoucherApiService voucherApi;

    private String currentSearch = "";
    private int currentFilterId = R.id.chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        adminUserApi = RetrofitClient.getInstance().create(AdminUserApiService.class);
        voucherApi = RetrofitClient.getInstance().create(VoucherApiService.class);

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

        adapter.setListener(this::showCustomerDetailsBottomSheet);
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
                matchesFilter = "locked".equals(status) || "inactive".equals(status);
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

    private void showCustomerDetailsBottomSheet(User user) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_customer_details, null);
        dialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvDialogName);
        TextView tvEmail = view.findViewById(R.id.tvDialogEmail);
        TextView tvPhone = view.findViewById(R.id.tvDialogPhone);
        TextView tvGender = view.findViewById(R.id.tvDialogGender);
        TextView tvBirthDate = view.findViewById(R.id.tvDialogBirthDate);
        TextView tvLevel = view.findViewById(R.id.tvDialogLevel);
        TextView tvPoints = view.findViewById(R.id.tvDialogPoints);
        TextView tvJoined = view.findViewById(R.id.tvDialogJoinedDate);
        TextView tvStatus = view.findViewById(R.id.tvDialogStatus);

        Button btnToggleStatus = view.findViewById(R.id.btnDialogToggleStatus);
        Button btnChangeLevel = view.findViewById(R.id.btnDialogChangeLevel);
        Button btnAdjustPoints = view.findViewById(R.id.btnDialogAdjustPoints);
        Button btnDeleteUser = view.findViewById(R.id.btnDialogDeleteUser);
        Button btnGiveVoucher = view.findViewById(R.id.btnDialogGiveVoucher);

        // Populate fields
        tvName.setText(user.name != null ? user.name : "Chưa cập nhật");
        tvEmail.setText(user.email != null ? user.email : "Không có");
        tvPhone.setText(user.phone != null ? user.phone : "Không có");
        tvGender.setText(user.gender != null ? user.gender : "Chưa chọn");
        tvBirthDate.setText(user.birthDate != null ? user.birthDate : "Chưa chọn");
        
        String levelUpper = user.memberLevel != null ? user.memberLevel.toUpperCase(Locale.getDefault()) : "STANDARD";
        if ("BASIC".equals(levelUpper)) {
            levelUpper = "STANDARD";
        }
        tvLevel.setText(levelUpper);
        tvPoints.setText(((user.points != null) ? user.points : 0) + " điểm");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvJoined.setText(user.createdAt > 0 ? sdf.format(new Date(user.createdAt)) : "Chưa rõ");

        boolean isLocked = "locked".equalsIgnoreCase(user.status) || "inactive".equalsIgnoreCase(user.status);
        if (isLocked) {
            tvStatus.setText("ĐÃ KHÓA");
            tvStatus.setTextColor(Color.RED);
            btnToggleStatus.setText("Mở khóa tài khoản");
            btnToggleStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981"))); // Green
        } else {
            tvStatus.setText("ĐANG HOẠT ĐỘNG");
            tvStatus.setTextColor(Color.parseColor("#10B981"));
            btnToggleStatus.setText("Khóa tài khoản");
            btnToggleStatus.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }

        // Action: Lock/Unlock
        btnToggleStatus.setOnClickListener(v -> {
            String newStatus = isLocked ? "active" : "locked";
            user.status = newStatus;
            showLoading(true);
            adminUserApi.updateUser(user.uid, UserMapper.toDTO(user)).enqueue(new Callback<ApiResponse<UserDTO>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        dialog.dismiss();
                        showToast("Cập nhật trạng thái thành công");
                        loadUsers();
                    } else {
                        showToast("Lỗi: " + getErrorMessage(response));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                    showLoading(false);
                    showToast("Lỗi kết nối: " + t.getMessage());
                }
            });
        });

        // Action: Change Level
        btnChangeLevel.setOnClickListener(v -> {
            dialog.dismiss();
            showChangeLevelDialog(user);
        });

        // Action: Adjust Points
        btnAdjustPoints.setOnClickListener(v -> {
            dialog.dismiss();
            showAdjustPointsDialog(user);
        });

        // Action: Delete
        btnDeleteUser.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmDialog(user);
        });

        // Action: Give Voucher
        btnGiveVoucher.setOnClickListener(v -> {
            dialog.dismiss();
            showGiveVoucherDialog(user);
        });

        dialog.show();
    }

    private void showChangeLevelDialog(User user) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_change_level);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        Spinner spinnerLevel = dialog.findViewById(R.id.spinnerMemberLevel);
        Button btnSave = dialog.findViewById(R.id.btnSaveLevel);
        Button btnCancel = dialog.findViewById(R.id.btnCancelLevel);

        String[] levels = {"standard", "vip", "gold", "platinum"};
        ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(adapterLevel);

        // Select current level
        String currentLevel = user.memberLevel != null ? user.memberLevel.toLowerCase() : "standard";
        if ("basic".equals(currentLevel)) {
            currentLevel = "standard";
        }
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(currentLevel)) {
                spinnerLevel.setSelection(i);
                break;
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String selectedLevel = spinnerLevel.getSelectedItem().toString();
            user.memberLevel = selectedLevel;
            showLoading(true);
            adminUserApi.updateUser(user.uid, UserMapper.toDTO(user)).enqueue(new Callback<ApiResponse<UserDTO>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        dialog.dismiss();
                        showToast("Cập nhật hạng thành công");
                        loadUsers();
                    } else {
                        showToast("Lỗi: " + getErrorMessage(response));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                    showLoading(false);
                    showToast("Lỗi kết nối: " + t.getMessage());
                }
            });
        });

        dialog.show();
    }

    private void showAdjustPointsDialog(User user) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_adjust_points);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvCurrentPoints = dialog.findViewById(R.id.tvCurrentPointsText);
        EditText etAmount = dialog.findViewById(R.id.etPointsAmount);
        Button btnAdd = dialog.findViewById(R.id.btnPointsAdd);
        Button btnSubtract = dialog.findViewById(R.id.btnPointsSubtract);
        Button btnCancel = dialog.findViewById(R.id.btnCancelPoints);

        tvCurrentPoints.setText("Điểm hiện tại: " + ((user.points != null) ? user.points : 0));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> adjustPoints(user, dialog, etAmount, true));
        btnSubtract.setOnClickListener(v -> adjustPoints(user, dialog, etAmount, false));

        dialog.show();
    }

    private void adjustPoints(User user, android.app.Dialog dialog, EditText etAmount, boolean isAdd) {
        String inputStr = etAmount.getText().toString().trim();
        if (inputStr.isEmpty()) {
            etAmount.setError("Vui lòng nhập số điểm");
            return;
        }

        int amountVal;
        try {
            amountVal = Integer.parseInt(inputStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Số điểm không hợp lệ");
            return;
        }

        if (amountVal <= 0) {
            etAmount.setError("Số điểm phải lớn hơn 0");
            return;
        }

        int diff = isAdd ? amountVal : -amountVal;
        int currentPoints = (user.points != null) ? user.points : 0;
        int finalPoints = currentPoints + diff;
        if (finalPoints < 0) {
            etAmount.setError("Khách hàng không đủ điểm để trừ");
            return;
        }

        user.points = finalPoints;
        showLoading(true);
        adminUserApi.updateUser(user.uid, UserMapper.toDTO(user)).enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    dialog.dismiss();
                    showToast("Cập nhật điểm thành công");
                    loadUsers();
                } else {
                    showToast("Lỗi: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmDialog(User user) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_delete_confirm);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDeleteTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMsg);
        Button btnDelete = dialog.findViewById(R.id.btnConfirmDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancelDelete);

        tvTitle.setText("Xóa khách hàng");
        tvMessage.setText("Bạn có chắc chắn muốn xóa khách hàng " + (user.name != null ? user.name : "") + "? Hành động này sẽ lưu trữ ẩn tài khoản khỏi hệ thống.");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            showLoading(true);
            adminUserApi.deleteUser(user.uid).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        dialog.dismiss();
                        showToast("Đã xóa khách hàng thành công");
                        loadUsers();
                    } else {
                        showToast("Lỗi: " + getErrorMessage(response));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showLoading(false);
                    showToast("Lỗi kết nối: " + t.getMessage());
                }
            });
        });

        dialog.show();
    }

    private void showGiveVoucherDialog(User user) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_give_voucher);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvSubtitle = dialog.findViewById(R.id.tvGiftVoucherSubtitle);
        EditText etDiscount = dialog.findViewById(R.id.etVoucherDiscount);
        EditText etMessage = dialog.findViewById(R.id.etVoucherMessage);
        Button btnSend = dialog.findViewById(R.id.btnSendVoucher);
        Button btnCancel = dialog.findViewById(R.id.btnCancelVoucher);

        tvSubtitle.setText("Khách hàng: " + (user.name != null ? user.name : "Khách hàng"));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSend.setOnClickListener(v -> {
            String discountStr = etDiscount.getText().toString().trim();
            if (discountStr.isEmpty()) {
                etDiscount.setError("Vui lòng nhập phần trăm giảm giá");
                return;
            }

            int discountVal;
            try {
                double doubleVal = Double.parseDouble(discountStr);
                discountVal = (int) doubleVal;
                if (discountVal <= 0 || discountVal > 100) {
                    etDiscount.setError("Mức giảm giá không hợp lệ (1-100%)");
                    return;
                }
            } catch (NumberFormatException e) {
                etDiscount.setError("Mức giảm giá không hợp lệ");
                return;
            }

            String message = etMessage.getText().toString().trim();
            if (message.isEmpty()) {
                message = "Bạn được tặng 1 voucher giảm giá " + discountStr + "% từ Admin. Chúc bạn xem phim vui vẻ!";
            }

            sendVoucherToFirebase(user, dialog, discountVal, message);
        });

        dialog.show();
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

    // ZELIOUS TASK: Logic gửi Voucher (Gom 3 thao tác vào 1 WriteBatch để đảm bảo tính nguyên vẹn).
    // 1. Tạo Voucher. 2. Tạo Notification báo cho khách. 3. Tạo AuditLog lưu lịch sử Admin.
    private void sendVoucherToFirebase(com.example.cinemabookingapp.domain.model.User user, android.app.Dialog dialog, int discount, String message) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        long currentTime = System.currentTimeMillis();

        // 1. Create Voucher
        com.google.firebase.firestore.DocumentReference voucherRef = db.collection("vouchers").document();
        com.example.cinemabookingapp.domain.model.Voucher voucher = new com.example.cinemabookingapp.domain.model.Voucher();
        voucher.voucherId = voucherRef.getId();
        voucher.userId = user.uid;
        // THE VOUCHER RULE
        voucher.code = "GIFT_" + voucherRef.getId().substring(0, Math.min(voucherRef.getId().length(), 6)).toUpperCase();
        voucher.discountPercent = discount;
        voucher.status = "ACTIVE";
        voucher.expiredAt = currentTime + 30L * 24 * 60 * 60 * 1000;
        voucher.createdAt = currentTime;
        batch.set(voucherRef, voucher);

        // 2. Create Notification
        com.google.firebase.firestore.DocumentReference notifRef = db.collection("notifications").document();
        com.example.cinemabookingapp.domain.model.Notification notif = new com.example.cinemabookingapp.domain.model.Notification();
        notif.notificationId = notifRef.getId();
        notif.userId = user.uid;
        notif.title = "Nhận Voucher từ Admin";
        notif.message = message;
        notif.type = "VOUCHER_RECEIVED";
        notif.isRead = false;
        notif.createdAt = currentTime;
        notif.updatedAt = currentTime;
        batch.set(notifRef, notif);

        // 3. Create AuditLog
        com.google.firebase.firestore.DocumentReference auditRef = db.collection("audit_logs").document();
        com.example.cinemabookingapp.domain.model.AuditLog auditLog = new com.example.cinemabookingapp.domain.model.AuditLog();
        auditLog.logId = auditRef.getId();
        auditLog.adminId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null 
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "ADMIN";
        auditLog.action = "GIVE_VOUCHER";
        auditLog.createdAt = currentTime;
        auditLog.actorId = auditLog.adminId;
        auditLog.actorRole = "ADMIN";
        auditLog.targetId = user.uid;
        auditLog.targetType = "USER";
        auditLog.note = "Tặng voucher " + discount + "% cho " + (user.name != null ? user.name : "Khách hàng");
        batch.set(auditRef, auditLog);

        showLoading(true);
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    dialog.dismiss();
                    showToast("Đã tặng Voucher thành công!");
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showToast("Lỗi khi tặng Voucher: " + e.getMessage());
                });
    }
}