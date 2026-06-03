package com.example.cinemabookingapp.ui.customer.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Notification;
import com.example.cinemabookingapp.utils.DateTimeConverter;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindViewData(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTV, contentTV, dateTV;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.notification_img);
            titleTV = itemView.findViewById(R.id.notification_title);
            contentTV = itemView.findViewById(R.id.notification_content);
            dateTV = itemView.findViewById(R.id.notification_date);
        }

        public void bindViewData(Notification notification){
            titleTV.setText(notification.title);
            contentTV.setText(notification.body);
            dateTV.setText(DateTimeConverter.convertToDateTimeString(notification.createdAt));
//            Glide.with(itemView)
//                            .load(notification.refId)
//                            .into(imageView);
        }
    }
}
