package com.example.cinemabookingapp.ui.features.notification;

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

    public interface OnItemClickListener {
        void onClick(Notification notification);
    }

    private List<Notification> notifications;
    private OnItemClickListener listener;

    public NotificationAdapter(List<Notification> notifications, OnItemClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
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
            contentTV.setText(notification.message);
            dateTV.setText(DateTimeConverter.convertToDateTimeString(notification.createdAt));
            
            // Default tint/style reset
            titleTV.setTextColor(android.graphics.Color.BLACK);
            
            if ("Hủy suất chiếu".equals(notification.title) || "SHOWTIME_CANCELLED".equals(notification.type)) {
                 titleTV.setTextColor(android.graphics.Color.RED);
                 imageView.setImageResource(R.drawable.ic_notification);
            } else if ("BOOKING_SUCCESS".equals(notification.type) || (notification.title != null && (notification.title.contains("Đặt vé") || notification.title.contains("Thanh toán") || notification.title.contains("bắp nước")))) {
                 boolean isSnackFallback = false;
                 if (notification.message != null && (notification.message.contains("bắp") || notification.message.contains("nước") || notification.message.contains("combo") || notification.message.contains("CineShop") || notification.title.contains("bắp nước"))) {
                     isSnackFallback = true;
                 }
                 
                 if (isSnackFallback) {
                     imageView.setImageResource(R.drawable.cart_shopping_solid_full);
                 } else {
                     imageView.setImageResource(R.drawable.ic_ticket_stars);
                 }
                 
                 if (notification.refId != null && !notification.refId.trim().isEmpty()) {
                     final String refId = notification.refId;
                     imageView.setTag(refId);
                     com.google.firebase.firestore.FirebaseFirestore.getInstance()
                         .collection("bookings")
                         .document(refId)
                         .get()
                         .addOnSuccessListener(doc -> {
                             if (doc.exists() && refId.equals(imageView.getTag())) {
                                 String showtimeId = doc.getString("showtimeId");
                                 if (showtimeId == null) {
                                     List<java.util.Map<String, Object>> snackOrders = (List<java.util.Map<String, Object>>) doc.get("snackOrder");
                                     if (snackOrders != null && !snackOrders.isEmpty()) {
                                         String imgUrl = (String) snackOrders.get(0).get("snackImgURL");
                                         if (imgUrl != null && !imgUrl.isEmpty()) {
                                             Glide.with(itemView.getContext())
                                                 .load(imgUrl)
                                                 .placeholder(R.drawable.cart_shopping_solid_full)
                                                 .error(R.drawable.cart_shopping_solid_full)
                                                 .into(imageView);
                                         } else {
                                             imageView.setImageResource(R.drawable.cart_shopping_solid_full);
                                         }
                                     } else {
                                         imageView.setImageResource(R.drawable.cart_shopping_solid_full);
                                     }
                                 } else {
                                     imageView.setImageResource(R.drawable.ic_ticket_stars);
                                 }
                             }
                         });
                 }
            } else if ("VOUCHER_RECEIVED".equals(notification.type) || (notification.title != null && (notification.title.contains("Voucher") || notification.title.contains("ưu đãi") || notification.title.contains("Ưu đãi") || notification.title.contains("Khuyến mãi") || notification.title.contains("khuyến mãi")))) {
                 imageView.setImageResource(R.drawable.ic_voucher_tag_orange);
            } else {
                 imageView.setImageResource(R.drawable.login_icon); // Fallback icon
            }

            if (!notification.isRead) {
                itemView.setBackgroundColor(android.graphics.Color.parseColor("#E1F5FE")); // Xanh nhạt nổi bật
                titleTV.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                itemView.setBackgroundColor(android.graphics.Color.WHITE);
                titleTV.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(notification);
            });
        }
    }
}
