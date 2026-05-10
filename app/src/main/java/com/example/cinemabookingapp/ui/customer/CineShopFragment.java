package com.example.cinemabookingapp.ui.customer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Snack;
import com.example.cinemabookingapp.ui.customer.adapter.CineShopAdapter;
import com.example.cinemabookingapp.ui.customer.adapter.CineShopBannerAdapter;
import com.example.cinemabookingapp.ui.customer.cine_shop.CineCartActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CineShopFragment — màn hình "Trang sản phẩm" (Star Shop / Cine Shop).
 *
 * Giao diện theo phong cách Galaxy Cinema:
 *   • Banner carousel tự động chạy mỗi 3 giây
 *   • 2 tab: SEASONAL / MOVIE
 *   • Danh sách sản phẩm dạng 1 cột (full-width)
 *   • Mỗi item có 2 nút: "MUA NGAY" và "THÊM VÀO GIỎ HÀNG"
 *
 * Dữ liệu hiện dùng mock. Khi Firebase sẵn sàng, gọi SnackRepository qua UseCase.
 */
public class CineShopFragment extends Fragment {

    // ── Views ────────────────────────────────────────────────────────────────
    private ViewPager2 bannerPager;
    private LinearLayout bannerDots;
    private TextView tabSeasonal, tabMovie;
    private RecyclerView rvProducts;
    private ImageView btnCart;

    // ── Adapters ─────────────────────────────────────────────────────────────
    private CineShopAdapter productAdapter;
    private CineShopBannerAdapter bannerAdapter;

    // ── Data ─────────────────────────────────────────────────────────────────
    private final List<Snack> allProducts = new ArrayList<>();
    private final List<View> dotViews     = new ArrayList<>();

    // ── Banner auto-scroll ────────────────────────────────────────────────────
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    // ── Tab state ─────────────────────────────────────────────────────────────
    /** SEASONAL → {"CAT_COMBO", "CAT_POPCORN"}; MOVIE → {"CAT_DRINK", "CAT_SNACK"} */
    private static final String TAB_SEASONAL = "seasonal";
    private static final String TAB_MOVIE    = "movie";
    private String currentTab = TAB_SEASONAL;

    // ── Cart total (hiển thị icon giỏ hàng) ──────────────────────────────────
    private int cartItemCount = 0;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cine_shop, container, false);

        bindViews(view);
        setupBanner();
        setupTabs();
        setupRecyclerView();
        loadMockData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startBannerAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    // ── Bind views ───────────────────────────────────────────────────────────

    private void bindViews(View view) {
        bannerPager  = view.findViewById(R.id.bannerPager);
        bannerDots   = view.findViewById(R.id.bannerDots);
        tabSeasonal  = view.findViewById(R.id.tabSeasonal);
        tabMovie     = view.findViewById(R.id.tabMovie);
        rvProducts   = view.findViewById(R.id.rvProducts);
        btnCart      = view.findViewById(R.id.btnCart);

        btnCart.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CineCartActivity.class)));
    }

    // ── Banner ───────────────────────────────────────────────────────────────

    private void setupBanner() {
        bannerAdapter = new CineShopBannerAdapter();

        // Mock banner data — URL placeholder (sẽ load thật từ Firestore sau)
        List<String> mockBannerUrls = Arrays.asList(
                "placeholder_1",
                "placeholder_2",
                "placeholder_3"
        );
        bannerAdapter.setBanners(mockBannerUrls);
        bannerPager.setAdapter(bannerAdapter);

        setupBannerDots(mockBannerUrls.size());

        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void setupBannerDots(int count) {
        bannerDots.removeAllViews();
        dotViews.clear();

        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(6), dpToPx(6));
            params.setMarginStart(dpToPx(3));
            params.setMarginEnd(dpToPx(3));
            dot.setLayoutParams(params);
            dot.setBackgroundColor(i == 0
                    ? Color.parseColor("#1E1A23")
                    : Color.parseColor("#CCCCCC"));
            bannerDots.addView(dot);
            dotViews.add(dot);
        }
    }

    private void updateDots(int selected) {
        for (int i = 0; i < dotViews.size(); i++) {
            dotViews.get(i).setBackgroundColor(
                    i == selected
                            ? Color.parseColor("#1E1A23")
                            : Color.parseColor("#CCCCCC"));
        }
    }

    private void startBannerAutoScroll() {
        int count = bannerAdapter.getItemCount();
        if (count <= 1) return;

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int next = (bannerPager.getCurrentItem() + 1) % count;
                bannerPager.setCurrentItem(next, true);
                bannerHandler.postDelayed(this, 3000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private void stopBannerAutoScroll() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private void setupTabs() {
        tabSeasonal.setOnClickListener(v -> selectTab(TAB_SEASONAL));
        tabMovie.setOnClickListener(v    -> selectTab(TAB_MOVIE));
        applyTabStyle(TAB_SEASONAL); // default
    }

    private void selectTab(String tab) {
        currentTab = tab;
        applyTabStyle(tab);
        filterProducts(tab);
    }

    private void applyTabStyle(String tab) {
        boolean isSeasonal = TAB_SEASONAL.equals(tab);

        // SEASONAL tab
        tabSeasonal.setBackgroundResource(isSeasonal
                ? R.drawable.bg_tab_cine_selected
                : android.R.color.transparent);
        tabSeasonal.setTextColor(isSeasonal
                ? Color.WHITE
                : Color.parseColor("#555555"));

        // MOVIE tab
        tabMovie.setBackgroundResource(isSeasonal
                ? android.R.color.transparent
                : R.drawable.bg_tab_cine_selected);
        tabMovie.setTextColor(isSeasonal
                ? Color.parseColor("#555555")
                : Color.WHITE);
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        productAdapter = new CineShopAdapter();
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productAdapter);
        rvProducts.setNestedScrollingEnabled(false);

        productAdapter.setOnCartChangedListener(
                totalCount -> {
                    cartItemCount = totalCount;
                    // TODO: hiển thị badge số lượng trên icon giỏ hàng nếu cần
                });
    }

    // cart badge count

    // ── Data ─────────────────────────────────────────────────────────────────

    private void loadMockData() {
        allProducts.clear();

        // SEASONAL products
        Snack s1 = new Snack(); s1.snackId = "SE01"; s1.categoryId = "CAT_SEASONAL";
        s1.name = "Ly nước Capybara"; s1.price = 350000;

        Snack s2 = new Snack(); s2.snackId = "SE02"; s2.categoryId = "CAT_SEASONAL";
        s2.name = "Combo Yummy Capybara"; s2.price = 500000;

        Snack s3 = new Snack(); s3.snackId = "SE03"; s3.categoryId = "CAT_SEASONAL";
        s3.name = "Set quà tặng Galaxy"; s3.price = 250000;

        // MOVIE products
        Snack m1 = new Snack(); m1.snackId = "MV01"; m1.categoryId = "CAT_MOVIE";
        m1.name = "Combo Bắp + Nước (1 người)"; m1.price = 85000;

        Snack m2 = new Snack(); m2.snackId = "MV02"; m2.categoryId = "CAT_MOVIE";
        m2.name = "Combo Couple (2 Bắp + 2 Nước)"; m2.price = 145000;

        Snack m3 = new Snack(); m3.snackId = "MV03"; m3.categoryId = "CAT_MOVIE";
        m3.name = "Pepsi Lớn"; m3.price = 35000;

        allProducts.add(s1); allProducts.add(s2); allProducts.add(s3);
        allProducts.add(m1); allProducts.add(m2); allProducts.add(m3);

        filterProducts(currentTab);
    }

    private void filterProducts(String tab) {
        List<Snack> filtered = new ArrayList<>();
        String targetCategory = TAB_SEASONAL.equals(tab) ? "CAT_SEASONAL" : "CAT_MOVIE";

        for (Snack product : allProducts) {
            if (targetCategory.equals(product.categoryId)) {
                filtered.add(product);
            }
        }
        productAdapter.setProducts(filtered);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
