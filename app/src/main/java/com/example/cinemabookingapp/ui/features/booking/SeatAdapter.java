package com.example.cinemabookingapp.ui.features.booking;

import java.util.ArrayList;
import android.view.LayoutInflater;
import java.util.ArrayList;
import android.view.View;
import java.util.ArrayList;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.*;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import com.example.cinemabookingapp.R;
import java.util.ArrayList;
import com.example.cinemabookingapp.data.dto.SeatDTO;
import java.util.ArrayList;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//                                                     ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Ëœ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i thÃƒÆ’Ã‚Â nh RecyclerView.ViewHolder (generic)

    // ThÃƒÆ’Ã‚Âªm 2 hÃƒÂ¡Ã‚ÂºÃ‚Â±ng sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ phÃƒÆ’Ã‚Â¢n biÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t loÃƒÂ¡Ã‚ÂºÃ‚Â¡i item
    private static final int TYPE_LABEL = 0;
    private static final int TYPE_SEAT  = 1;

    public interface OnSeatClickListener {
        void onSeatClick(SeatDTO seat, int position);
    }

    private final List<Object> items = new ArrayList<>();
    private final OnSeatClickListener listener;

    public SeatAdapter(List<SeatDTO> seatList, OnSeatClickListener listener) {
        this.listener = listener;
        setSeats(seatList); // gÃƒÂ¡Ã‚Â»Ã‚Âi setSeats thay vÃƒÆ’Ã‚Â¬ gÃƒÆ’Ã‚Â¡n trÃƒÂ¡Ã‚Â»Ã‚Â±c tiÃƒÂ¡Ã‚ÂºÃ‚Â¿p
    }

    public void setSeats(List<SeatDTO> seatList) {
        items.clear();
        String lastRow = null;
        for (SeatDTO seat : seatList) {
            if (!seat.rowName.equals(lastRow)) {
                items.add(seat.rowName); // chen label "A", "B"... vÃƒÆ’Ã‚Â o trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc mÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i hÃƒÆ’Ã‚Â ng
                lastRow = seat.rowName;
            }
            items.add(seat);
        }
        notifyDataSetChanged();
    }

    // ThÃƒÆ’Ã‚Âªm method nÃƒÆ’Ã‚Â y ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â quyÃƒÂ¡Ã‚ÂºÃ‚Â¿t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh item nÃƒÆ’Ã‚Â o lÃƒÆ’Ã‚Â  label, cÃƒÆ’Ã‚Â¡i nÃƒÆ’Ã‚Â o lÃƒÆ’Ã‚Â  ghÃƒÂ¡Ã‚ÂºÃ‚Â¿
    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_LABEL : TYPE_SEAT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_LABEL) {
            View view = inflater.inflate(R.layout.item_seat_label, parent, false);
            return new LabelViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_seat, parent, false);
            return new SeatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LabelViewHolder) {
            ((LabelViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof SeatViewHolder) {
            SeatDTO seat = (SeatDTO) items.get(position);
            ((SeatViewHolder) holder).bind(seat);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSeatClick(seat, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size(); // dÃƒÆ’Ã‚Â¹ng items thay vÃƒÆ’Ã‚Â¬ seatList
    }

    // --- ViewHolder cho label A, B, C... ---
    static class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        LabelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvRowLabel);
        }
        void bind(String label) { tvLabel.setText(label); }
    }

    // --- ViewHolder cho ghÃƒÂ¡Ã‚ÂºÃ‚Â¿ (giÃƒÂ¡Ã‚Â»Ã‚Â¯ nguyÃƒÆ’Ã‚Âªn bind() cÃƒÂ¡Ã‚Â»Ã‚Â§a bÃƒÂ¡Ã‚ÂºÃ‚Â¡n) ---
    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeat;
        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeat = itemView.findViewById(R.id.tvSeat);
        }

        void bind(SeatDTO seat) {
            tvSeat.setText(seat.seatCode != null ? seat.seatCode : "");
            
            long now = System.currentTimeMillis();
            boolean isBooked = "booked".equalsIgnoreCase(seat.status);
            
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "";
            
            boolean isHeldByMe = "held".equalsIgnoreCase(seat.status) 
                    && currentUserId.equals(seat.heldBy);
            
            boolean isHeldByOther = "held".equalsIgnoreCase(seat.status) 
                    && !currentUserId.equals(seat.heldBy);

            boolean isLocked = "LOCKED".equalsIgnoreCase(seat.status)
                    || "LOCKED".equalsIgnoreCase(seat.seatType);

            if (isLocked) {
                // PHYSICALLY LOCKED: dark gray color, disabled
                tvSeat.setBackgroundResource(R.drawable.couch_solid_lock);
                tvSeat.setTextColor(0xFF444444);
                itemView.setEnabled(false);
                itemView.setAlpha(0.6f);
            } else if (isBooked) {
                // BOOKED: slate color
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFF555566);
                itemView.setEnabled(false);
                itemView.setAlpha(0.6f);
            } else if (isHeldByOther || isHeldByMe) {
                // HELD: orange color
                tvSeat.setBackgroundResource(R.drawable.couch_solid_held);
                tvSeat.setTextColor(0xFFFFFFFF);
                itemView.setEnabled(false);
                itemView.setAlpha(1.0f);
            } else if (seat.isSelected ) {
                // HELD BY ME / SELECTED: selected color
                tvSeat.setBackgroundResource(R.drawable.couch_solid_selection);
                tvSeat.setTextColor(0xFFFFFFFF);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            } else if ("VIP".equalsIgnoreCase(seat.seatType)) {
                // VIP
                tvSeat.setBackgroundResource(R.drawable.couch_solid_vip);
                tvSeat.setTextColor(0xFF1A1A1A);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            } else {
                // AVAILABLE STANDARD
                tvSeat.setBackgroundResource(R.drawable.couch_solid_normal);
                tvSeat.setTextColor(0xFFCCCCCC);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            }
        }
    }
}