package com.example.cinemabookingapp.ui.features.profile;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.data.repository.PromotionRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Promotion;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.ProfileService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyPromotionListActivity extends AppCompatActivity {

    private RecyclerView rvMyPromotions;
    private View layoutPromoEmptyState;

    private PromotionRepositoryImpl promotionRepository;
    private ProfileService profileService;
    private final List<Promotion> activePromoList = new ArrayList<>();
    private MyPromoAdapter adapter;
    private String userLevel = "standard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_promotion_list);

        promotionRepository = new PromotionRepositoryImpl();
        profileService = ServiceProvider.getInstance().getProfileService();

        initViews();
        loadUserProfileAndPromotions();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvMyPromotions = findViewById(R.id.rvMyPromotions);
        layoutPromoEmptyState = findViewById(R.id.layoutPromoEmptyState);

        rvMyPromotions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyPromoAdapter(activePromoList);
        rvMyPromotions.setAdapter(adapter);
    }

    private void loadUserProfileAndPromotions() {
        // Fetch user profile first to get the member level
        profileService.getUserProfile(new ResultCallback<User>() {
            @Override
            public void onSuccess(User profileData) {
                if (profileData != null && profileData.memberLevel != null) {
                    userLevel = profileData.memberLevel.trim().toLowerCase();
                } else {
                    userLevel = "standard";
                }
                loadActivePromotions();
            }

            @Override
            public void onError(String message) {
                userLevel = "standard";
                loadActivePromotions();
            }
        });
    }

    private void loadActivePromotions() {
        long now = System.currentTimeMillis();
        promotionRepository.getAllPromotions(new ResultCallback<List<Promotion>>() {
            @Override
            public void onSuccess(List<Promotion> promotions) {
                activePromoList.clear();
                if (promotions != null) {
                    for (Promotion p : promotions) {
                        if (!p.deleted) {
                            // Check active status, time validity, and usage limit
                            boolean isActive = "active".equalsIgnoreCase(p.status)
                                    && p.validFrom <= now
                                    && p.validTo >= now
                                    && (p.usageLimit <= 0 || p.usedCount < p.usageLimit);

                            if (isActive) {
                                // Check role applicability
                                boolean isRoleMatch = false;
                                if (p.targetRole == null || p.targetRole.trim().isEmpty() || p.targetRole.equalsIgnoreCase("all")) {
                                    isRoleMatch = true;
                                } else if (userLevel.contains(p.targetRole.trim().toLowerCase())) {
                                    isRoleMatch = true;
                                } else if (p.targetRole.trim().toLowerCase().contains(userLevel)) {
                                    isRoleMatch = true;
                                }

                                if (isRoleMatch) {
                                    activePromoList.add(p);
                                }
                            }
                        }
                    }
                }
                
                // Fetch user's personal vouchers from Firestore and add them
                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                if (uid != null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("vouchers")
                            .whereEqualTo("userId", uid)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult().getDocuments()) {
                                        boolean isUsed = doc.contains("isUsed") && Boolean.TRUE.equals(doc.getBoolean("isUsed"));
                                        String status = doc.getString("status");
                                        boolean isActive = "ACTIVE".equalsIgnoreCase(status) 
                                                || (!isUsed && (status == null || (!"USED".equalsIgnoreCase(status) && !"EXPIRED".equalsIgnoreCase(status))));
                                        long expiredAt = 0;
                                        if (doc.contains("expiredAt")) {
                                            Long exp = doc.getLong("expiredAt");
                                            if (exp != null) expiredAt = exp;
                                        }
                                        boolean isNotExpired = expiredAt <= 0 || expiredAt >= now;

                                        if (isActive && isNotExpired && !isUsed) {
                                            Promotion p = new Promotion();
                                            p.promoId = doc.getId();
                                            p.code = doc.getString("code");
                                            if (p.code == null) p.code = doc.getId();

                                            Double discount = doc.getDouble("discountValue");
                                            if (discount == null) {
                                                Long dp = doc.getLong("discountPercent");
                                                discount = (dp != null) ? dp.doubleValue() : 0.0;
                                            }

                                            String titleText;
                                            if (discount > 100) {
                                                titleText = String.format(Locale.getDefault(), "Giảm %,.0f đ", discount);
                                            } else {
                                                titleText = String.format(Locale.getDefault(), "Giảm %,.0f%%", discount);
                                            }
                                            p.title = titleText;

                                            String name = doc.getString("title");
                                            if (name == null) name = doc.getString("name");
                                            if (name == null) {
                                                String type = doc.getString("voucherType");
                                                if ("WELCOME_VOUCHER".equals(type)) name = "Quà Tân Binh";
                                                else name = "Voucher hệ thống";
                                            }
                                            p.description = name;
                                            p.validTo = expiredAt;

                                            Double minAmt = doc.getDouble("minAmount");
                                            p.minAmount = (minAmt != null) ? minAmt : 0.0;

                                            p.status = "active";
                                            p.deleted = false;

                                            activePromoList.add(p);
                                        }
                                    }
                                }
                                updateUI();
                            });
                } else {
                    updateUI();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyPromotionListActivity.this, "Lỗi tải khuyến mãi: " + message, Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        if (activePromoList.isEmpty()) {
            layoutPromoEmptyState.setVisibility(View.VISIBLE);
            rvMyPromotions.setVisibility(View.GONE);
        } else {
            layoutPromoEmptyState.setVisibility(View.GONE);
            rvMyPromotions.setVisibility(View.VISIBLE);
        }
    }

    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("PromoCode", code);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép mã: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyPromoAdapter extends RecyclerView.Adapter<MyPromoAdapter.ViewHolder> {
        private final List<Promotion> items;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public MyPromoAdapter(List<Promotion> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_promotion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Promotion p = items.get(position);

            holder.tvCode.setText(p.code != null ? p.code.toUpperCase(Locale.getDefault()) : "");
            holder.tvTitle.setText(p.title != null ? p.title : "");
            holder.tvDesc.setText(p.description != null ? p.description : "");

            // Expiry Date
            String dateEnd = p.validTo > 0 ? dateFormat.format(new Date(p.validTo)) : "";
            holder.tvValidity.setText("HSD: " + dateEnd);

            // Target Role badge
            if (p.targetRole != null && !p.targetRole.trim().isEmpty() && !p.targetRole.equalsIgnoreCase("all")) {
                holder.tvTargetRole.setText(p.targetRole.toUpperCase(Locale.getDefault()));
                holder.tvTargetRole.setVisibility(View.VISIBLE);
            } else {
                holder.tvTargetRole.setVisibility(View.GONE);
            }

            // Copy button click listener
            holder.btnCopyCode.setOnClickListener(v -> {
                if (p.code != null) {
                    copyToClipboard(p.code.toUpperCase(Locale.getDefault()));
                }
            });

            // Card item click listener to show details dialog
            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                android.app.Dialog dialog = new android.app.Dialog(context);
                dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_my_voucher_detail);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                    android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                    dialog.getWindow().setAttributes(lp);
                }

                TextView tvDialogCode = dialog.findViewById(R.id.tvDialogCode);
                TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
                TextView tvDialogDesc = dialog.findViewById(R.id.tvDialogDesc);
                TextView tvDialogCond = dialog.findViewById(R.id.tvDialogCond);
                TextView tvDialogExpiry = dialog.findViewById(R.id.tvDialogExpiry);
                android.widget.Button btnDialogClose = dialog.findViewById(R.id.btnDialogClose);

                if (tvDialogCode != null) {
                    tvDialogCode.setText(p.code != null ? p.code.toUpperCase(Locale.getDefault()) : "");
                }
                if (tvDialogTitle != null) {
                    tvDialogTitle.setText(p.title != null ? p.title : "");
                }
                if (tvDialogDesc != null) {
                    tvDialogDesc.setText(p.description != null ? p.description : "Voucher hệ thống dành cho bạn.");
                }

                if (tvDialogCond != null) {
                    String condStr = "Áp dụng cho đơn vé mọi giá trị.";
                    if (p.minAmount > 0) {
                        condStr = String.format(Locale.getDefault(), "Áp dụng cho đơn vé từ %,.0f đ.", p.minAmount);
                    }
                    tvDialogCond.setText(condStr);
                }

                if (tvDialogExpiry != null) {
                    String expiryStr = "HSD: Không giới hạn";
                    if (p.validTo > 0) {
                        expiryStr = "HSD: " + dateFormat.format(new Date(p.validTo));
                    }
                    tvDialogExpiry.setText(expiryStr);
                }

                if (btnDialogClose != null) {
                    btnDialogClose.setOnClickListener(v2 -> dialog.dismiss());
                }

                dialog.show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCode, tvTitle, tvDesc, tvValidity, tvTargetRole;
            View btnCopyCode;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCode = itemView.findViewById(R.id.tvPromoCode);
                tvTitle = itemView.findViewById(R.id.tvPromoTitle);
                tvDesc = itemView.findViewById(R.id.tvPromoDesc);
                tvValidity = itemView.findViewById(R.id.tvPromoValidity);
                tvTargetRole = itemView.findViewById(R.id.tvPromoTargetRole);
                btnCopyCode = itemView.findViewById(R.id.btnCopyCode);
            }
        }
    }
}
