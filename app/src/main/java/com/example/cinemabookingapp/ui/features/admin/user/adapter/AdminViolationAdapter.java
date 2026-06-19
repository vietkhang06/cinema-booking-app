package com.example.cinemabookingapp.ui.features.admin.user.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Violation;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminViolationAdapter extends RecyclerView.Adapter<AdminViolationAdapter.Holder> {

    public interface OnViolationClickListener {
        void onViolationClick(Violation violation);
    }

    private List<Violation> list = new ArrayList<>();
    private OnViolationClickListener listener;

    public void setListener(OnViolationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Violation> newList) {
        list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new Holder(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_violation_log, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        Violation v = list.get(i);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        h.type.setText(v.violationType != null ? v.violationType.toUpperCase() : "VI PHẠM");
        h.severity.setText("Mức độ: " + (v.severity != null ? v.severity.toUpperCase() : "THẤP"));
        h.date.setText(v.createdAt > 0 ? sdf.format(new Date(v.createdAt)) : "");
        h.desc.setText(v.description != null ? v.description : "Không có mô tả");
        h.fine.setText("Tiền phạt: " + nf.format(v.penaltyAmount) + "đ");
        h.points.setText("Điểm trừ: " + v.penaltyPoints);

        String status = v.status != null ? v.status.toUpperCase() : "PENDING";
        h.status.setText(status);
        if ("RESOLVED".equals(status)) {
            h.status.setTextColor(Color.parseColor("#10B981"));
        } else if ("CANCELLED".equals(status)) {
            h.status.setTextColor(Color.parseColor("#7A757F"));
        } else {
            h.status.setTextColor(Color.parseColor("#EF6C00"));
        }

        h.itemView.setOnClickListener(view -> {
            if (listener != null) listener.onViolationClick(v);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView type, severity, date, desc, fine, points, status;

        Holder(View view) {
            super(view);
            type = view.findViewById(R.id.tvVioType);
            severity = view.findViewById(R.id.tvVioSeverity);
            date = view.findViewById(R.id.tvVioDate);
            desc = view.findViewById(R.id.tvVioDesc);
            fine = view.findViewById(R.id.tvVioFine);
            points = view.findViewById(R.id.tvVioPoints);
            status = view.findViewById(R.id.tvVioStatus);
        }
    }
}
