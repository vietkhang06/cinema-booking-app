package com.example.cinemabookingapp.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.SeatDTO;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.ViewHolder> {

    public interface OnSeatClickListener {
        void onSeatClick(SeatDTO seat, int position);
    }

    private final List<SeatDTO> seatList;
    private final OnSeatClickListener listener;

    public SeatAdapter(List<SeatDTO> seatList, OnSeatClickListener listener) {
        this.seatList = seatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SeatDTO seat = seatList.get(position);
        holder.bind(seat);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSeatClick(seat, position);
        });
    }

    @Override
    public int getItemCount() {
        return seatList != null ? seatList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeat;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeat = itemView.findViewById(R.id.tvSeat);
        }

        void bind(SeatDTO seat) {
            tvSeat.setText(seat.seatCode != null ? seat.seatCode : "");

            if ("booked".equalsIgnoreCase(seat.status)) {
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFF444455);
                itemView.setEnabled(false);
                itemView.setAlpha(0.5f);
            } else if (seat.isSelected) {
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFFFFFFFF);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            } else if ("VIP".equalsIgnoreCase(seat.seatType)) {
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFFFFD700);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            } else {
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFFCCCCCC);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            }
        }
    }
}