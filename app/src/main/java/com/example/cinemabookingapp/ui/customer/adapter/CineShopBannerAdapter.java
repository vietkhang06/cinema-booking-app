package com.example.cinemabookingapp.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter banner cho ViewPager2 trong màn hình CineShop.
 * Hiện tại dùng placeholder. Khi tích hợp Glide/Coil thì load URL từ Firestore.
 */
public class CineShopBannerAdapter extends RecyclerView.Adapter<CineShopBannerAdapter.BannerVH> {

    private List<String> bannerUrls = new ArrayList<>();

    public void setBanners(List<String> urls) {
        this.bannerUrls = urls != null ? urls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cine_banner, parent, false);
        return new BannerVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerVH holder, int position) {
        // TODO: Glide.with(holder.imgBanner).load(bannerUrls.get(position)).into(holder.imgBanner);
        // Tạm thời giữ drawable placeholder
    }

    @Override
    public int getItemCount() {
        return bannerUrls.size();
    }

    static class BannerVH extends RecyclerView.ViewHolder {
        ImageView imgBanner;

        BannerVH(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}
