package com.example.cinemabookingapp.ui.admin.room.seatplan;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SeatPlanCellAdapter extends RecyclerView.Adapter<SeatPlanCellAdapter.CellVH> {

    public interface OnSeatCellClickListener {
        void onSeatCellClicked(int seatPosition);
    }

    private final List<SeatPlanCell> cells;
    private final OnSeatCellClickListener listener;

    public SeatPlanCellAdapter(List<SeatPlanCell> cells, OnSeatCellClickListener listener) {
        this.cells = cells;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CellVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_seat_plan_cell, parent, false);
        return new CellVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CellVH holder, int position) {
        SeatPlanCell cell = cells.get(position);

        holder.tvSeatCode.setText(cell.seatCode);
        holder.cardSeat.setCardBackgroundColor(colorForType(cell.type));
        holder.cardSeat.setStrokeWidth(cell.type == SeatPlanCell.TYPE_LOCKED ? 2 : 0);
        holder.cardSeat.setStrokeColor(Color.parseColor("#E2E8F0"));

        holder.tvSeatCode.setTextColor(
                cell.type == SeatPlanCell.TYPE_LOCKED
                        ? Color.parseColor("#F8FAFC")
                        : Color.WHITE
        );

        holder.itemView.setOnClickListener(v -> listener.onSeatCellClicked(position));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    private int colorForType(int type) {
        switch (type) {
            case SeatPlanCell.TYPE_VIP:
                return Color.parseColor("#6D28D9");
            case SeatPlanCell.TYPE_COUPLE:
                return Color.parseColor("#C026D3");
            case SeatPlanCell.TYPE_LOCKED:
                return Color.parseColor("#475569");
            default:
                return Color.parseColor("#334155");
        }
    }

    static class CellVH extends RecyclerView.ViewHolder {
        MaterialCardView cardSeat;
        TextView tvSeatCode;

        CellVH(@NonNull View itemView) {
            super(itemView);
            cardSeat = itemView.findViewById(R.id.cardSeatCell);
            tvSeatCode = itemView.findViewById(R.id.tvSeatCode);
        }
    }
}