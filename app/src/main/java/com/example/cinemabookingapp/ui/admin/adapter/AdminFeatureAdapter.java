package com.example.cinemabookingapp.ui.admin.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.ui.admin.model.AdminFeatureItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AdminFeatureAdapter extends RecyclerView.Adapter<AdminFeatureAdapter.FeatureVH> {

    private final Context context;
    private final List<AdminFeatureItem> items;

    public AdminFeatureAdapter(Context context, List<AdminFeatureItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public FeatureVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_feature, parent, false);
        return new FeatureVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureVH holder, int position) {
        AdminFeatureItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);
        holder.ivIcon.setImageResource(item.iconRes);

        holder.card.setOnClickListener(v -> {
            Intent intent = new Intent(context, item.targetActivity);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FeatureVH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;

        public FeatureVH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAdminFeature);
            ivIcon = itemView.findViewById(R.id.ivAdminFeatureIcon);
            tvTitle = itemView.findViewById(R.id.tvAdminFeatureTitle);
            tvSubtitle = itemView.findViewById(R.id.tvAdminFeatureSubtitle);
        }
    }
}