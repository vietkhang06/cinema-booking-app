package com.example.cinemabookingapp.ui.staff.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.SnackOrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemViewHolder> {
    private List<SnackOrderItem> itemList;

    public OrderItemAdapter(List<SnackOrderItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_order_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
//        InvoiceSnackItem item = itemList.get(position);
//        holder.itemNameAndQuantityTextView.setText(String.format("%sx%s", item.itemName, item.quantity));
//        holder.itemPriceTextView.setText(String.format("%,d vnd", item.price));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTV, itemPriceTV, itemQuantityTV;
        ImageView snackImageView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemNameTV = itemView.findViewById(R.id.snack_item_name);
            itemPriceTV = itemView.findViewById(R.id.snack_item_price);
            itemQuantityTV = itemView.findViewById(R.id.snack_item_quantity);
            snackImageView = itemView.findViewById(R.id.snacK_item_image);
        }
    }
}
