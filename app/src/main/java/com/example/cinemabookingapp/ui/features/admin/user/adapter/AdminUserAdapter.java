package com.example.cinemabookingapp.ui.features.admin.user.adapter;

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

        h.name.setText(u.name != null ? u.name : "N/A");
        h.email.setText(u.email != null ? u.email : "N/A");
        
        String cinema = u.cinemaName != null && !u.cinemaName.isEmpty() ? u.cinemaName : "Chưa phân rạp";
        h.roleAndCinema.setText("Vai trò: " + (u.role != null ? u.role.toUpperCase() : "CUSTOMER") + " • Rạp: " + cinema);

        if ("active".equalsIgnoreCase(u.status)) {
            h.status.setText("HOẠT ĐỘNG");
            h.status.setTextColor(android.graphics.Color.parseColor("#10B981"));
            h.status.setBackgroundResource(R.drawable.bg_status_active_pill);
        } else {
            h.status.setText("TẠM KHÓA");
            h.status.setTextColor(android.graphics.Color.parseColor("#E53935"));
            h.status.setBackgroundResource(R.drawable.bg_status_inactive_pill);
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
        TextView name, email, roleAndCinema, status;

        Holder(View v) {
            super(v);
            avatar = v.findViewById(R.id.ivUserAvatar);
            name = v.findViewById(R.id.tvUserName);
            email = v.findViewById(R.id.tvUserEmail);
            roleAndCinema = v.findViewById(R.id.tvUserRoleAndCinema);
            status = v.findViewById(R.id.tvUserStatus);
        }
    }
}
