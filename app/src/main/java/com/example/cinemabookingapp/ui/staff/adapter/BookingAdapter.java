package com.example.cinemabookingapp.ui.staff.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.BookingDTO;
import com.example.cinemabookingapp.ui.staff.StaffInvoiceActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(BookingDTO booking);
    }
    private List<BookingDTO> items;
    private OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public BookingAdapter(List<BookingDTO> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_booking_card, parent, false);
        return new BookingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingAdapter.ViewHolder holder, int position) {
        BookingDTO dto = items.get(position);
        holder.tvMovieTitle.setText(dto.movieTitleSnapshot);
        holder.tvBookingId.setText("Mã vé: " + dto.bookingId);
        holder.tvCinemaRoom.setText(dto.cinemaNameSnapshot + " - " + dto.roomNameSnapshot);

        String formattedTime = dateFormat.format(new Date(dto.showtimeStartAtSnapshot));
        holder.tvShowtime.setText("Suất chiếu: " + formattedTime);

        String seats = dto.seatCodes != null ? String.join(", ", dto.seatCodes) : "";
        holder.tvSeats.setText("Ghế: " + seats);

        holder.tvTotalPrice.setText(String.format("%,.0f vnd", dto.total));

        // Load poster image
        if (dto.movieImageUrlSnapshot != null && !dto.movieImageUrlSnapshot.isEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(dto.movieImageUrlSnapshot)
                    .placeholder(R.drawable.login_icon)
                    .into(holder.imgPoster);
        } else {
            holder.imgPoster.setImageResource(R.drawable.login_icon);
        }

        // Status design
        String status = dto.paymentStatus != null ? dto.paymentStatus.toUpperCase(Locale.getDefault()) : "PENDING";
        holder.tvStatus.setText(status);
        if ("CONFIRMED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundColor(0xFF4CAF50); // Green
        } else if ("PENDING".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundColor(0xFFFF9800); // Orange
        } else {
            holder.tvStatus.setBackgroundColor(0xFFE53935); // Red
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(dto);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvStatus, tvBookingId, tvCinemaRoom, tvShowtime, tvSeats, tvTotalPrice;
        android.widget.ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvBookingId = itemView.findViewById(R.id.tv_booking_id);
            tvCinemaRoom = itemView.findViewById(R.id.tv_cinema_room);
            tvShowtime = itemView.findViewById(R.id.tv_showtime);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            imgPoster = itemView.findViewById(R.id.img_poster);
        }
    }
}