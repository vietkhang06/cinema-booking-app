package com.example.cinemabookingapp.ui.customer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.domain.model.Snack;
import com.example.cinemabookingapp.ui.customer.adapter.SnackAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SnackShopActivity extends BaseActivity {

    private RecyclerView rvSnacks;
    private TabLayout tabLayoutSnacks;
    private TextView tvTotalPrice;
    private ImageView btnBack;
    private MaterialCardView btnNextToPayment; // Nút thanh toán

    private SnackAdapter snackAdapter;

    // Danh sách lưu MỌI MÓN ĂN (Không bao giờ bị xóa, chỉ dùng để lọc)
    private final List<Snack> allSnacks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack_shop);

        initViews();
        setupRecyclerView();
        loadMockData();
        setupTabs(); // Kích hoạt sự kiện bấm Tab
    }

    private void initViews() {
        rvSnacks = findViewById(R.id.rvSnacks);
        tabLayoutSnacks = findViewById(R.id.tabLayoutSnacks);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnBack = findViewById(R.id.btnBack);
        btnNextToPayment = findViewById(R.id.btnNextToPayment);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút thanh toán (Tạm thời hiển thị thông báo)
        btnNextToPayment.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển đến màn hình thanh toán...", Toast.LENGTH_SHORT).show();
            // Xử lý tạo SnackOrder ở các bước tiếp theo...
        });
    }

    private void setupRecyclerView() {
        snackAdapter = new SnackAdapter();
        rvSnacks.setLayoutManager(new LinearLayoutManager(this));
        rvSnacks.setAdapter(snackAdapter);

        // Lắng nghe sự kiện thay đổi giỏ hàng từ Adapter
        snackAdapter.setOnCartChangedListener((cartQuantities, snacks) -> {
            double total = 0;
            // Tính tổng tiền dựa trên GIỎ HÀNG và DANH SÁCH GỐC (allSnacks)
            // Để dù bạn chuyển tab, giá trị món ăn ở tab khác vẫn được cộng dồn
            for (Snack snack : allSnacks) {
                if (cartQuantities.containsKey(snack.snackId)) {
                    int qty = cartQuantities.get(snack.snackId);
                    total += snack.price * qty;
                }
            }

            // Format tiền tệ
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvTotalPrice.setText(formatter.format(total) + " ₫");
        });
    }

    private void setupTabs() {
        tabLayoutSnacks.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Vị trí tab trùng khớp với file XML: 0=Combo, 1=Drink, 2=Popcorn, 3=Snack
                switch (tab.getPosition()) {
                    case 0:
                        filterSnacks("CAT_COMBO");
                        break;
                    case 1:
                        filterSnacks("CAT_DRINK");
                        break;
                    case 2:
                        filterSnacks("CAT_POPCORN");
                        break;
                    case 3:
                        filterSnacks("CAT_SNACK");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    // Hàm lọc dữ liệu theo Category
    private void filterSnacks(String categoryId) {
        List<Snack> filteredList = new ArrayList<>();
        for (Snack snack : allSnacks) {
            if (snack.categoryId != null && snack.categoryId.equals(categoryId)) {
                filteredList.add(snack);
            }
        }
        // Đẩy danh sách đã lọc vào Adapter
        snackAdapter.setSnacks(filteredList);
    }

    private void loadMockData() {
        allSnacks.clear(); // Xóa sạch trước khi nạp để tránh trùng lặp nếu gọi lại

        // --- 1. COMBO ---
        Snack c1 = new Snack(); c1.snackId = "CB01"; c1.categoryId = "CAT_COMBO"; c1.name = "Combo Solo (1 Bắp + 1 Nước)"; c1.price = 85000;
        Snack c2 = new Snack(); c2.snackId = "CB02"; c2.categoryId = "CAT_COMBO"; c2.name = "Combo Couple (1 Bắp + 2 Nước)"; c2.price = 115000;
        Snack c3 = new Snack(); c3.snackId = "CB03"; c3.categoryId = "CAT_COMBO"; c3.name = "Combo Family (2 Bắp + 4 Nước)"; c3.price = 210000;

        // --- 2. POPCORN ---
        Snack p1 = new Snack(); p1.snackId = "P01"; p1.categoryId = "CAT_POPCORN"; p1.name = "Bắp Phô Mai (Lớn)"; p1.price = 65000;
        Snack p2 = new Snack(); p2.snackId = "P02"; p2.categoryId = "CAT_POPCORN"; p2.name = "Bắp Caramel (Lớn)"; p2.price = 65000;
        Snack p3 = new Snack(); p3.snackId = "P03"; p3.categoryId = "CAT_POPCORN"; p3.name = "Bắp Ngọt (Lớn)"; p3.price = 55000;

        // --- 3. DRINK ---
        Snack d1 = new Snack(); d1.snackId = "D01"; d1.categoryId = "CAT_DRINK"; d1.name = "Pepsi Tươi (Lớn)"; d1.price = 35000;
        Snack d2 = new Snack(); d2.snackId = "D02"; d2.categoryId = "CAT_DRINK"; d2.name = "7UP (Lớn)"; d2.price = 35000;
        Snack d3 = new Snack(); d3.snackId = "D03"; d3.categoryId = "CAT_DRINK"; d3.name = "Milo Lúa Mạch"; d3.price = 40000;

        // --- 4. SNACK ---
        Snack s1 = new Snack(); s1.snackId = "S01"; s1.categoryId = "CAT_SNACK"; s1.name = "Xúc Xích Nướng Hàn Quốc"; s1.price = 30000;
        Snack s2 = new Snack(); s2.snackId = "S02"; s2.categoryId = "CAT_SNACK"; s2.name = "Khoai Tây Chiên Lắc Phô Mai"; s2.price = 45000;

        allSnacks.add(c1); allSnacks.add(c2); allSnacks.add(c3);
        allSnacks.add(p1); allSnacks.add(p2); allSnacks.add(p3);
        allSnacks.add(d1); allSnacks.add(d2); allSnacks.add(d3);
        allSnacks.add(s1); allSnacks.add(s2);

        // Mặc định hiển thị Tab "Combo" (Tab đầu tiên) khi vừa vào màn hình
        filterSnacks("CAT_COMBO");
    }
}