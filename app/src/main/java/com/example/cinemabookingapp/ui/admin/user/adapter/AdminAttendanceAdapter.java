package com.example.cinemabookingapp.ui.admin.user.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Attendance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminAttendanceAdapter extends RecyclerView.Adapter<AdminAttendanceAdapter.Holder> {

    private List<Attendance> list = new ArrayList<>();

    public void submitList(List<Attendance> newList) {
        list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new Holder(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_attendance_log, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        Attendance a = list.get(i);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        h.date.setText(a.date != null ? a.date : "N/A");
        h.shift.setText(a.shiftName != null ? a.shiftName : "N/A");

        h.checkIn.setText("Vào: " + (a.checkInTime > 0 ? timeFormat.format(new Date(a.checkInTime)) : "--:--"));
        h.checkOut.setText("Ra: " + (a.checkOutTime > 0 ? timeFormat.format(new Date(a.checkOutTime)) : "--:--"));
        h.duration.setText("Làm: " + a.durationMinutes + " phút");

        String status = a.status != null ? a.status.toLowerCase() : "";
        if (status.contains("late") && status.contains("early")) {
            h.statusPill.setText("TRỄ & VỀ SỚM");
            h.statusPill.setTextColor(Color.parseColor("#E53935"));
            h.statusPill.setBackgroundResource(R.drawable.bg_status_inactive_pill);
        } else if (status.contains("late")) {
            h.statusPill.setText("ĐI TRỄ");
            h.statusPill.setTextColor(Color.parseColor("#EF6C00"));
            h.statusPill.setBackgroundResource(R.drawable.bg_status_inactive_pill);
        } else if (status.contains("early")) {
            h.statusPill.setText("VỀ SỚM");
            h.statusPill.setTextColor(Color.parseColor("#EF6C00"));
            h.statusPill.setBackgroundResource(R.drawable.bg_status_inactive_pill);
        } else if ("completed".equals(status) || "present".equals(status)) {
            h.statusPill.setText("ĐÚNG GIỜ");
            h.statusPill.setTextColor(Color.parseColor("#10B981"));
            h.statusPill.setBackgroundResource(R.drawable.bg_status_active_pill);
        } else {
            h.statusPill.setText(a.status != null ? a.status.toUpperCase() : "CHƯA RA CA");
            h.statusPill.setTextColor(Color.parseColor("#7A757F"));
            h.statusPill.setBackgroundResource(R.drawable.bg_status_active_pill);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView date, shift, checkIn, checkOut, duration, statusPill;

        Holder(View v) {
            super(v);
            date = v.findViewById(R.id.tvLogDate);
            shift = v.findViewById(R.id.tvLogShift);
            checkIn = v.findViewById(R.id.tvLogCheckIn);
            checkOut = v.findViewById(R.id.tvLogCheckOut);
            duration = v.findViewById(R.id.tvLogDuration);
            statusPill = v.findViewById(R.id.tvLogStatus);
        }
    }
}
