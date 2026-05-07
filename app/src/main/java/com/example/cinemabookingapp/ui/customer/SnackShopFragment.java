package com.example.cinemabookingapp.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Snack;
import com.example.cinemabookingapp.ui.customer.adapter.SnackAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;



public class SnackShopFragment extends Fragment {
    private com.example.cinemabookingapp.domain.repository.SnackRepository snackRepository;
    private RecyclerView rvSnacks;
    private TabLayout tabLayoutSnacks;
    private TextView tvTotalPrice;
    private ImageView btnBack;
    private MaterialCardView btnNextToPayment;

    private SnackAdapter snackAdapter;
    private final List<Snack> allSnacks = new ArrayList<>();

    private void initViews(View view) {
        rvSnacks = view.findViewById(R.id.rvSnacks);
        tabLayoutSnacks = view.findViewById(R.id.tabLayoutSnacks);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnBack = view.findViewById(R.id.btnBack);
        btnNextToPayment = view.findViewById(R.id.btnNextToPayment);

        // Nút back trong Fragment thường sẽ quay lại trang chủ
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).findViewById(R.id.navHomeCard).performClick();
            }
        });
    }

    private void setupRecyclerView() {
        snackAdapter = new SnackAdapter();
        rvSnacks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSnacks.setAdapter(snackAdapter);

        snackAdapter.setOnCartChangedListener((cartQuantities, snacks) -> {
            double total = 0;
            for (Snack snack : allSnacks) {
                if (cartQuantities.containsKey(snack.snackId)) {
                    Integer qty = cartQuantities.get(snack.snackId);
                    if (qty != null) {
                        total += snack.price * qty;
                    }
                }
            }
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvTotalPrice.setText(formatter.format(total) + " ₫");
        });
    }

    private void setupTabs() {
        tabLayoutSnacks.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: filterSnacks("CAT_COMBO"); break;
                    case 1: filterSnacks("CAT_DRINK"); break;
                    case 2: filterSnacks("CAT_POPCORN"); break;
                    case 3: filterSnacks("CAT_SNACK"); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterSnacks(String categoryId) {
        List<Snack> filteredList = new ArrayList<>();
        for (Snack snack : allSnacks) {
            if (snack.categoryId != null && snack.categoryId.equals(categoryId)) {
                filteredList.add(snack);
            }
        }
        snackAdapter.setSnacks(filteredList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_snack_shop, container, false);

        initViews(view);
        setupRecyclerView();

        // --- TẠM KHÓA FIREBASE ---
        // snackRepository = new com.example.cinemabookingapp.data.repository.SnackRepositoryImpl();
        // loadDataFromFirebase();

        // --- MỞ LẠI DỮ LIỆU GIẢ ĐỂ PUSH CODE ---
        loadMockData();

        setupTabs();

        return view;
    }

    private void loadMockData() {
        allSnacks.clear();

        // 1. COMBO
        Snack c1 = new Snack(); c1.snackId = "CB01"; c1.categoryId = "CAT_COMBO"; c1.name = "Combo Solo (1 Bắp + 1 Nước)"; c1.price = 85000;
        Snack c2 = new Snack(); c2.snackId = "CB02"; c2.categoryId = "CAT_COMBO"; c2.name = "Combo Couple (1 Bắp + 2 Nước)"; c2.price = 115000;

        // 2. DRINK
        Snack d1 = new Snack(); d1.snackId = "D01"; d1.categoryId = "CAT_DRINK"; d1.name = "Pepsi Tươi (Lớn)"; d1.price = 35000;
        Snack d2 = new Snack(); d2.snackId = "D02"; d2.categoryId = "CAT_DRINK"; d2.name = "Milo Lúa Mạch"; d2.price = 40000;

        // 3. POPCORN
        Snack p1 = new Snack(); p1.snackId = "P01"; p1.categoryId = "CAT_POPCORN"; p1.name = "Bắp Phô Mai (Lớn)"; p1.price = 65000;
        Snack p2 = new Snack(); p2.snackId = "P02"; p2.categoryId = "CAT_POPCORN"; p2.name = "Bắp Caramel (Lớn)"; p2.price = 65000;

        // 4. SNACK
        Snack s1 = new Snack(); s1.snackId = "S01"; s1.categoryId = "CAT_SNACK"; s1.name = "Xúc Xích Nướng Hàn Quốc"; s1.price = 30000;

        allSnacks.add(c1); allSnacks.add(c2);
        allSnacks.add(d1); allSnacks.add(d2);
        allSnacks.add(p1); allSnacks.add(p2);
        allSnacks.add(s1);

        filterSnacks("CAT_COMBO");
    }
}