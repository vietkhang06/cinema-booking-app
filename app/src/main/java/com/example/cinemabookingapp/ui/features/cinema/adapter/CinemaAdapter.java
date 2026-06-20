package com.example.cinemabookingapp.ui.features.cinema.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Cinema;

import java.util.ArrayList;
import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.ViewHolder> {

    private final List<Cinema> cinemas = new ArrayList<>();
    private final OnCinemaClickListener listener;

    public interface OnCinemaClickListener {
        void onCinemaClick(Cinema cinema);
    }

    public CinemaAdapter(OnCinemaClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Cinema> items) {
        cinemas.clear();
        if (items != null) {
            cinemas.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        holder.tvName.setText(valueOrDefault(cinema.name, "Ráº¡p phim"));
        holder.tvAddress.setText(valueOrDefault(cinema.address, "ChÆ°a cáº­p nháº­t Ä‘á»‹a chá»‰"));
        holder.tvDistrict.setText(valueOrDefault(joinLocation(cinema.district, cinema.city), "ChÆ°a cáº­p nháº­t khu vá»±c"));
        holder.tvPhone.setText(valueOrDefault(cinema.phone, "ChÆ°a cáº­p nháº­t hotline"));
        
        // Status Badge styling
        String status = cinema.status;
        String statusText = "Äang hoáº¡t Ä‘á»™ng";
        int textColor = android.graphics.Color.parseColor("#10B981");
        int bgColor = android.graphics.Color.parseColor("#E6FBF3");

        if (status != null && !status.trim().isEmpty()) {
            String cleanStatus = status.trim().toLowerCase();
            if ("active".equals(cleanStatus) || "available".equals(cleanStatus) || "scheduled".equals(cleanStatus) || "Ä‘ang hoáº¡t Ä‘á»™ng".equals(cleanStatus)) {
                statusText = "Äang hoáº¡t Ä‘á»™ng";
                textColor = android.graphics.Color.parseColor("#10B981");
                bgColor = android.graphics.Color.parseColor("#E6FBF3");
            } else if ("inactive".equals(cleanStatus) || "táº¡m dá»«ng".equals(cleanStatus)) {
                statusText = "Táº¡m dá»«ng";
                textColor = android.graphics.Color.parseColor("#EF4444");
                bgColor = android.graphics.Color.parseColor("#FEE2E2");
            } else {
                statusText = status;
                textColor = android.graphics.Color.parseColor("#1E1A23");
                bgColor = android.graphics.Color.parseColor("#F3F4F6");
            }
        }
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(textColor);
        holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCinemaClick(cinema);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cinemas.size();
    }

    private String joinLocation(String district, String city) {
        String safeDistrict = district == null ? "" : district.trim();
        String safeCity = city == null ? "" : city.trim();
        if (safeDistrict.isEmpty()) {
            return safeCity;
        }
        if (safeCity.isEmpty()) {
            return safeDistrict;
        }
        return safeDistrict + ", " + safeCity;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvAddress;
        final TextView tvDistrict;
        final TextView tvPhone;
        final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCinemaName);
            tvAddress = itemView.findViewById(R.id.tvCinemaAddress);
            tvDistrict = itemView.findViewById(R.id.tvCinemaDistrict);
            tvPhone = itemView.findViewById(R.id.tvCinemaPhone);
            tvStatus = itemView.findViewById(R.id.tvCinemaStatus);
        }
    }
}
