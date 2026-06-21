package com.example.cinemabookingapp.ui.features.admin.user.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.Holder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> list = new ArrayList<>();
    private OnUserClickListener listener;

    public void setListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<User> newList) {
        list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new Holder(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_user, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        User u = list.get(i);

        h.name.setText(u.name != null ? u.name : "Khách hàng");
        
        String contact = "";
        if (u.phone != null && !u.phone.isEmpty()) {
            contact += u.phone;
        }
        if (u.email != null && !u.email.isEmpty()) {
            if (!contact.isEmpty()) contact += " • ";
            contact += u.email;
        }
        h.contact.setText(contact.isEmpty() ? "Không có thông tin liên hệ" : contact);

        // Level styling
        String level = u.memberLevel != null ? u.memberLevel.toUpperCase(Locale.getDefault()) : "STANDARD";
        if ("BASIC".equals(level)) {
            level = "STANDARD";
        }
        h.level.setText(level);
        if ("GOLD".equals(level)) {
            h.level.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3CD")));
            h.level.setTextColor(Color.parseColor("#856404"));
        } else if ("PLATINUM".equals(level)) {
            h.level.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
            h.level.setTextColor(Color.parseColor("#475569"));
        } else if ("VIP".equals(level)) {
            h.level.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8D7DA")));
            h.level.setTextColor(Color.parseColor("#721C24"));
        } else {
            h.level.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F6F4F8")));
            h.level.setTextColor(Color.parseColor("#4A4650"));
        }

        // Points
        h.points.setText(((u.points != null) ? u.points : 0) + " điểm");

        // Status light indicator
        boolean isActive = !"locked".equalsIgnoreCase(u.status);
        if (isActive) {
            h.viewStatus.setBackgroundResource(R.drawable.dot_active);
            h.viewStatus.setBackgroundTintList(null);
        } else {
            h.viewStatus.setBackgroundResource(0);
            h.viewStatus.setBackground(new ColorDrawable(Color.RED));
            h.viewStatus.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            h.viewStatus.setClipToOutline(true);
        }

        if (u.avatarUrl != null && !u.avatarUrl.isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(u.avatarUrl)
                    .placeholder(R.drawable.user_solid_full)
                    .circleCrop()
                    .into(h.avatar);
        } else {
            h.avatar.setImageResource(R.drawable.user_solid_full);
            h.avatar.setColorFilter(null);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(u);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name, contact, level, points;
        View viewStatus;

        Holder(View v) {
            super(v);
            avatar = v.findViewById(R.id.imgAvatar);
            name = v.findViewById(R.id.tvCustomerName);
            contact = v.findViewById(R.id.tvCustomerContact);
            level = v.findViewById(R.id.tvCustomerLevel);
            points = v.findViewById(R.id.tvCustomerPoints);
            viewStatus = v.findViewById(R.id.viewStatusIndicator);
        }
    }
}
