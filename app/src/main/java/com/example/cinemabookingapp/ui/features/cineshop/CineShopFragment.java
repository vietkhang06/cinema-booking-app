package com.example.cinemabookingapp.ui.features.cineshop;

import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.graphics.Color;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.os.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.os.Looper;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.view.LayoutInflater;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.widget.LinearLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.model.Snack;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cineshop.adapter.CineShopAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cineshop.adapter.CineShopBannerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.ui.features.cineshop.CineCartActivity;

//Zikenic was here
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.dto.CineShopBannerDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.dto.CineShopItemDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;

import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.util.ArrayList;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.util.Arrays;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import java.util.List;

/**
 * CineShopFragment Ã¢â‚¬â€ mÃƒÂ n hÃƒÂ¬nh "Trang sản phẩm" (Star Shop / Cine Shop).
 *
 * Giao diÃ¡Â»â€¡n theo phong cÃƒÂ¡ch Galaxy Cinema:
 *   Ã¢â‚¬Â¢ Banner carousel tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng chÃ¡ÂºÂ¡y mÃ¡Â»â€”i 3 giÃƒÂ¢y
 *   Ã¢â‚¬Â¢ 2 tab: SEASONAL / MOVIE
 *   Ã¢â‚¬Â¢ Danh sÃƒÂ¡ch sÃ¡ÂºÂ£n phÃ¡ÂºÂ©m dÃ¡ÂºÂ¡ng 1 cÃ¡Â»â„¢t (full-width)
 *   Ã¢â‚¬Â¢ MÃ¡Â»â€”i item cÃƒÂ³ 2 nÃƒÂºt: "MUA NGAY" vÃƒÂ  "THÊM VÀO GIỎ HÀNG"
 *
 * DÃ¡Â»Â¯ liÃ¡Â»â€¡u hiÃ¡Â»â€¡n dÃƒÂ¹ng mock. Khi Firebase sÃ¡ÂºÂµn sÃƒÂ ng, gÃ¡Â»Âi SnackRepository qua UseCase.
 */
public class CineShopFragment extends Fragment {

    // Ã¢â€â‚¬Ã¢â€â‚¬ Views Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private ViewPager2 bannerPager;
    private LinearLayout bannerDots;
    private TextView tabSeasonal, tabMovie;
    private RecyclerView rvProducts;
    private ImageView btnCart;
    private TextView tvCartBadge;
    private android.widget.ProgressBar loadingProgress;
    private TextView tvEmptyState;
    private LinearLayout layoutLoginRequired;
    private com.google.android.material.button.MaterialButton btnLoginRequired;
    private androidx.core.widget.NestedScrollView scrollContent;

    // Ã¢â€â‚¬Ã¢â€â‚¬ Adapters Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private CineShopAdapter productAdapter;
    private CineShopBannerAdapter bannerAdapter;

    // Ã¢â€â‚¬Ã¢â€â‚¬ Data Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private final List<Snack> allProducts = new ArrayList<>();
    private final List<View> dotViews     = new ArrayList<>();

    // Ã¢â€â‚¬Ã¢â€â‚¬ Banner auto-scroll Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    // Ã¢â€â‚¬Ã¢â€â‚¬ Tab state Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    /** SEASONAL Ã¢â€ â€™ {"CAT_COMBO", "CAT_POPCORN"}; MOVIE Ã¢â€ â€™ {"CAT_DRINK", "CAT_SNACK"} */
    private static final String TAB_SEASONAL = "seasonal";
    private static final String TAB_MOVIE    = "movie";
    private String currentTab = TAB_SEASONAL;

    // Ã¢â€â‚¬Ã¢â€â‚¬ Cart total (hiÃ¡Â»Æ’n thÃ¡Â»â€¹ icon giÃ¡Â»Â hÃƒÂ ng) Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private int cartItemCount = 0;

    // Ã¢â€â‚¬Ã¢â€â‚¬ Lifecycle Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cine_shop, container, false);

        // KÃƒÂ­ch hoÃ¡ÂºÂ¡t Seeder khÃ¡Â»Å¸i tÃ¡ÂºÂ¡o dÃ¡Â»Â¯ liÃ¡Â»â€¡u mÃ¡ÂºÂ«u nÃ¡ÂºÂ¿u Firestore trÃ¡Â»â€˜ng
        CineShopSeeder.seedIfNeeded();

        bindViews(view);
        setupBanner();
        setupTabs();
        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginState();
    }

    // ZELIOUS: Logic kiÃ¡Â»Æ’m tra trÃ¡ÂºÂ¡ng thÃƒÂ¡i Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p Ã„â€˜Ã¡Â»Æ’ bÃ¡ÂºÂ£o vÃ¡Â»â€¡ tab khÃ¡Â»Âi Guest.
    // HÃƒÂ m Ã„â€˜Ã†Â°Ã¡Â»Â£c gÃ¡Â»Âi mÃ¡Â»â€”i khi Fragment Ã„â€˜Ã†Â°Ã¡Â»Â£c mÃ¡Â»Å¸ lÃƒÂªn (onResume). DÃƒÂ¹ng FirebaseAuth.getInstance().getCurrentUser() != null Ã„â€˜Ã¡Â»Æ’ kiÃ¡Â»Æ’m tra.
    // NÃ¡ÂºÂ¿u chÃ†Â°a Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p -> Ã¡ÂºÂ©n giao diÃ¡Â»â€¡n (scrollContent.setVisibility(View.GONE)), hiÃ¡Â»â€¡n layoutLoginRequired (bao gÃ¡Â»â€œm nÃƒÂºt YÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p).
    private void checkLoginState() {
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (isLoggedIn) {
            layoutLoginRequired.setVisibility(View.GONE);
            scrollContent.setVisibility(View.VISIBLE);
            
            // Load data if empty
            if (bannerAdapter != null && bannerAdapter.getItemCount() == 0) {
                fetchBannersFromDatabase();
            }
            if (allProducts.isEmpty()) {
                loadCineShopItems();
            }
            startBannerAutoScroll();
            // Update cart count
            updateCartBadge(CineCartManager.getInstance().getTotalCount());
        } else {
            layoutLoginRequired.setVisibility(View.VISIBLE);
            scrollContent.setVisibility(View.GONE);
            stopBannerAutoScroll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Bind views Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    private void bindViews(View view) {
        bannerPager  = view.findViewById(R.id.bannerPager);
        bannerDots   = view.findViewById(R.id.bannerDots);
        tabSeasonal  = view.findViewById(R.id.tabSeasonal);
        tabMovie     = view.findViewById(R.id.tabMovie);
        rvProducts   = view.findViewById(R.id.rvProducts);
        btnCart      = view.findViewById(R.id.btnCart);
        tvCartBadge  = view.findViewById(R.id.tvCartBadge);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        tvEmptyState    = view.findViewById(R.id.tvEmptyState);
        layoutLoginRequired = view.findViewById(R.id.layoutLoginRequired);
        btnLoginRequired = view.findViewById(R.id.btnLoginRequired);
        scrollContent   = view.findViewById(R.id.scrollContent);

        View layoutCart = view.findViewById(R.id.layoutCart);
        if (layoutCart != null) {
            layoutCart.setOnClickListener(v -> {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    startActivity(new Intent(requireContext(), CineCartActivity.class));
                } else {
                    AppNavigator.goToLoginForBooking(requireActivity());
                }
            });
        }

        if (btnLoginRequired != null) {
            btnLoginRequired.setOnClickListener(v -> 
                    AppNavigator.goToLoginForBooking(requireActivity())
            );
        }
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Banner Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    //Zikenic was here
    private void setupBanner() {
        bannerAdapter = new CineShopBannerAdapter();
        bannerPager.setAdapter(bannerAdapter);

        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void fetchBannersFromDatabase() {
        FirebaseFirestore.getInstance().collection("cine_shop_banners")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CineShopBannerDTO> activeBanners = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CineShopBannerDTO banner = doc.toObject(CineShopBannerDTO.class);
                        if (banner.isActive) {
                            activeBanners.add(banner);
                        }
                    }

                    // SÃ¡ÂºÂ¯p xÃ¡ÂºÂ¿p banner theo sortOrder tÃ„Æ’ng dÃ¡ÂºÂ§n trÃƒÂªn client
                    activeBanners.sort((b1, b2) -> Integer.compare(b1.sortOrder, b2.sortOrder));

                    List<String> bannerUrls = new ArrayList<>();
                    for (CineShopBannerDTO banner : activeBanners) {
                        if (banner.imageUrl != null) {
                            bannerUrls.add(banner.imageUrl);
                        }
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            bannerAdapter.setBanners(bannerUrls);
                            setupBannerDots(bannerUrls.size());

                            // Restart auto-scroll with new data
                            stopBannerAutoScroll();
                            startBannerAutoScroll();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CineShopFragment", "Failed to fetch banners from Firestore: " + e.getMessage());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            bannerAdapter.setBanners(new ArrayList<>());
                            setupBannerDots(0);
                            stopBannerAutoScroll();
                        });
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

    // Ã¢â€â‚¬Ã¢â€â‚¬ Tabs Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

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

    // Ã¢â€â‚¬Ã¢â€â‚¬ RecyclerView Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    private void setupRecyclerView() {
        productAdapter = new CineShopAdapter();
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productAdapter);
        rvProducts.setNestedScrollingEnabled(false);

        productAdapter.setOnCartChangedListener(
                totalCount -> {
                    cartItemCount = totalCount;
                    updateCartBadge(totalCount);
                    // TODO: hiÃ¡Â»Æ’n thÃ¡Â»â€¹ badge sÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng trÃƒÂªn icon giÃ¡Â»Â hÃƒÂ ng nÃ¡ÂºÂ¿u cÃ¡ÂºÂ§n
                });
    }

    private void updateCartBadge(int count) {
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Data Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    private void loadCineShopItems() {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.VISIBLE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }

        FirebaseFirestore.getInstance().collection("cine_shop_items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (loadingProgress != null) {
                        loadingProgress.setVisibility(View.GONE);
                    }

                    List<CineShopItemDTO> activeItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CineShopItemDTO dto = doc.toObject(CineShopItemDTO.class);
                        if (dto.isActive) {
                            activeItems.add(dto);
                        }
                    }

                    // SÃ¡ÂºÂ¯p xÃ¡ÂºÂ¿p sÃ¡ÂºÂ£n phÃ¡ÂºÂ©m theo sortOrder tÃ„Æ’ng dÃ¡ÂºÂ§n trÃƒÂªn client
                    activeItems.sort((i1, i2) -> Integer.compare(i1.sortOrder, i2.sortOrder));

                    allProducts.clear();
                    android.util.Log.d("CineShopFragment", "Successfully fetched and client-sorted " + activeItems.size() + " active CineShop products.");
                    for (CineShopItemDTO dto : activeItems) {
                        Snack product = new Snack();
                        product.snackId = dto.itemId;
                        product.categoryId = dto.categoryId;
                        product.name = dto.name;
                        product.description = dto.description;
                        product.price = dto.price;
                        product.imageUrl = dto.imageUrl;
                        product.isAvailable = dto.isActive && "available".equals(dto.status);
                        product.status = dto.status;
                        product.createdAt = System.currentTimeMillis();
                        product.updatedAt = System.currentTimeMillis();
                        product.deleted = false;
                        allProducts.add(product);
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> filterProducts(currentTab));
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CineShopFragment", "Failed to fetch CineShop items: " + e.getMessage());
                    if (loadingProgress != null) {
                        loadingProgress.setVisibility(View.GONE);
                    }
                    if (tvEmptyState != null) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Lỗi tải sản phẩm: " + e.getMessage());
                    }
                });
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

        if (tvEmptyState != null) {
            if (filtered.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Không có sản phẩm nào khả dụng.");
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        }
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Helpers Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
