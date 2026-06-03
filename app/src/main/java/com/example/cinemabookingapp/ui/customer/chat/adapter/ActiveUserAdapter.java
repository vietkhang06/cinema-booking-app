package com.example.cinemabookingapp.ui.customer.chat.adapter;

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

import java.util.List;

public class ActiveUserAdapter extends RecyclerView.Adapter<ActiveUserAdapter.UserViewHolder> {

    private List<User> userList;
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private OnUserClickListener onUserClickListener;

    public ActiveUserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public void setOnUserClickListener(OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.name);
        Glide.with(holder.itemView.getContext())
                .load(user.avatarUrl)
                .error(R.drawable.ic_user_avatar_24) // Placeholder image if loading fails
                .into(holder.avatarImageView);
        // Dynamically toggle the green dot visibility
        holder.viewStatusBadge.setVisibility(true ? View.VISIBLE : View.GONE);

        holder.mainItem.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView avatarImageView;
        View viewStatusBadge, mainItem;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            mainItem = itemView.findViewById(R.id.main);
            tvName = itemView.findViewById(R.id.tvName);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            viewStatusBadge = itemView.findViewById(R.id.viewStatusBadge);
        }
    }
}