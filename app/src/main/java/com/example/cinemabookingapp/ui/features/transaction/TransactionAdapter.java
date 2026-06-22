package com.example.cinemabookingapp.ui.features.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.MyApp;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.model.Movie;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_CINE_SHOP = 0;
    public static final int VIEW_TYPE_MOVIE_TICKET = 1;
    private final List<Booking> bookings = new ArrayList<>();
    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(Booking booking);
    }

    public TransactionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> newBookings) {
        this.bookings.clear();
        if (newBookings != null) {
            this.bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        if(viewType == VIEW_TYPE_CINE_SHOP)
            return new CineShopOrderViewHolder(view);
        return new MovieTicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        if(holder instanceof MovieTicketViewHolder){
            ((MovieTicketViewHolder) holder).bind(booking);
        }else if(holder instanceof CineShopOrderViewHolder){
            ((CineShopOrderViewHolder) holder).bind(booking);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Booking booking = bookings.get(position);
        if (booking.showtimeId != null) {
            return VIEW_TYPE_MOVIE_TICKET;
        }
        return VIEW_TYPE_CINE_SHOP;
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private void bindUnifiedStatus(TextView tvStatus, MaterialCardView cardStatus, Booking booking, long now) {
        String bookingStatus = booking.bookingStatus != null ? booking.bookingStatus.toUpperCase() : "PENDING";
        String paymentStatus = booking.paymentStatus != null ? booking.paymentStatus.toUpperCase() : "PENDING";
        boolean isCineShop = (booking.showtimeId == null);

        // 1. FAILED hoặc CANCELLED -> Đã huỷ
        if ("FAILED".equals(bookingStatus) || "CANCELLED".equals(bookingStatus) ||
            "FAILED".equals(paymentStatus) || "CANCELLED".equals(paymentStatus)) {
            tvStatus.setText("Đã huỷ");
            tvStatus.setTextColor(0xFFC62828); // Red
            cardStatus.setCardBackgroundColor(0xFFFFEBEE);
            return;
        }

        // 2. checkInAt > 0 -> Đã sử dụng
        if (booking.checkInAt > 0) {
            tvStatus.setText("Đã sử dụng");
            tvStatus.setTextColor(0xFF757575); // Grey
            cardStatus.setCardBackgroundColor(0xFFEEEEEE);
            return;
        }

        // 3. showtimeStartAtSnapshot < now -> Đã hết suất
        if (!isCineShop && booking.showtimeStartAtSnapshot > 0 && booking.showtimeStartAtSnapshot < now) {
            tvStatus.setText("Đã hết suất");
            tvStatus.setTextColor(0xFF757575); // Grey
            cardStatus.setCardBackgroundColor(0xFFEEEEEE);
            return;
        }

        // 4. SUCCESS + suất chiếu chưa diễn ra -> Có hiệu lực
        boolean isSuccess = "SUCCESS".equals(paymentStatus) || "PAID".equals(paymentStatus) ||
                            "CONFIRMED".equals(bookingStatus) || "SUCCESS".equals(bookingStatus);
        if (isSuccess) {
            tvStatus.setText("Có hiệu lực");
            tvStatus.setTextColor(0xFF2E7D32); // Green
            cardStatus.setCardBackgroundColor(0xFFE8F5E9);
            return;
        }

        // 5. PENDING -> Đang chờ thanh toán
        tvStatus.setText("Đang chờ thanh toán");
        tvStatus.setTextColor(0xFFE8640C); // Orange
        cardStatus.setCardBackgroundColor(0xFFFFF3E0);
    }

    class MovieTicketViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvCinema, tvRoom, tvSeats, tvShowtime;
        TextView tvStatus;
        MaterialCardView cardStatus;
        TextView tvBookingCode, tvCreatedAt, tvPrice;

        public MovieTicketViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_movie_poster);
            tvTitle = itemView.findViewById(R.id.tv_movie_title);
            tvCinema = itemView.findViewById(R.id.tv_cinema_name);
            tvRoom = itemView.findViewById(R.id.tv_room);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvShowtime = itemView.findViewById(R.id.tv_showtime);
            tvStatus = itemView.findViewById(R.id.tv_status);
            cardStatus = itemView.findViewById(R.id.card_status);
            tvBookingCode = itemView.findViewById(R.id.tv_booking_code);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvPrice = itemView.findViewById(R.id.tv_total_price);
        }

        public void bind(Booking booking) {
            tvTitle.setText(booking.movieTitleSnapshot);
            tvCinema.setText(booking.cinemaNameSnapshot);

            // Check if CineShop order (showtimeId is null)
            boolean isCineShop = (booking.showtimeId == null);

            if (isCineShop) {
                tvRoom.setText(booking.roomNameSnapshot != null ? booking.roomNameSnapshot : "");
                tvSeats.setVisibility(View.GONE);
                tvShowtime.setVisibility(View.GONE);
            } else {
                tvSeats.setVisibility(View.VISIBLE);
                tvShowtime.setVisibility(View.VISIBLE);
                tvRoom.setText(booking.roomNameSnapshot != null ? booking.roomNameSnapshot : "Chưa xác định");
                if (booking.seatCodes != null && !booking.seatCodes.isEmpty()) {
                    tvSeats.setText("Ghế: " + String.join(", ", booking.seatCodes));
                } else {
                    tvSeats.setText("Ghế: Chưa xác định");
                }

                if (booking.showtimeStartAtSnapshot > 0) {
                    tvShowtime.setText("Suất: " + dateFormat.format(new Date(booking.showtimeStartAtSnapshot)));
                } else {
                    tvShowtime.setText("Suất: Chưa xác định");
                }
            }

            long now = System.currentTimeMillis();
            boolean isExpired = (!isCineShop && booking.showtimeStartAtSnapshot > 0 && booking.showtimeStartAtSnapshot < now);
            String bookingStatus = booking.bookingStatus != null ? booking.bookingStatus.toUpperCase() : "PENDING";
            String paymentStatus = booking.paymentStatus != null ? booking.paymentStatus.toUpperCase() : "PENDING";
            boolean isCancelled = "FAILED".equals(bookingStatus) || "CANCELLED".equals(bookingStatus) ||
                                  "FAILED".equals(paymentStatus) || "CANCELLED".equals(paymentStatus);
            boolean isUsed = booking.checkInAt > 0;

            // Bind unified status
            bindUnifiedStatus(tvStatus, cardStatus, booking, now);

            // Booking code & Date
            tvBookingCode.setText("Mã vé: " + (booking.paymentCode != null ? booking.paymentCode : booking.bookingId));
            if (booking.createdAt > 0) {
                tvCreatedAt.setText("Đặt ngày: " + dateFormat.format(new Date(booking.createdAt)));
            } else {
                tvCreatedAt.setText("Đặt ngày: Chưa rõ");
            }

            tvPrice.setText(String.format("%,.0fđ", booking.total).replace(',', '.'));

            // Click handling and visual indication
            if (isExpired || isCancelled || isUsed) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> {
                if (isExpired || isCancelled || isUsed) {
                    android.widget.Toast.makeText(v.getContext(), "Vé này đã dùng, hết hạn hoặc bị hủy", android.widget.Toast.LENGTH_SHORT).show();
                }
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(bookings.get(pos));
                }
            });

            // Poster Loading Fallback logic
            if (!android.text.TextUtils.isEmpty(booking.movieImageUrlSnapshot)) {
                Glide.with(itemView.getContext())
                        .load(booking.movieImageUrlSnapshot)
                        .placeholder(R.drawable.square_solid_full)
                        .error(R.drawable.square_solid_full)
                        .into(imgPoster);
            } else if (!android.text.TextUtils.isEmpty(booking.movieId)) {
                // Fetch from MovieRepository
                try {
                    MyApp app = (MyApp) itemView.getContext().getApplicationContext();
                    app.getAppContainer().getMovieRepository().getMovieById(booking.movieId, new com.example.cinemabookingapp.domain.common.ResultCallback<Movie>() {
                        @Override
                        public void onSuccess(Movie movie) {
                            if (movie != null && !android.text.TextUtils.isEmpty(movie.posterUrl)) {
                                booking.movieImageUrlSnapshot = movie.posterUrl;
                                itemView.post(() -> Glide.with(itemView.getContext())
                                        .load(movie.posterUrl)
                                        .placeholder(R.drawable.square_solid_full)
                                        .error(R.drawable.square_solid_full)
                                        .into(imgPoster));
                            } else {
                                itemView.post(() -> Glide.with(itemView.getContext())
                                        .load(R.drawable.square_solid_full)
                                        .into(imgPoster));
                            }
                        }

                        @Override
                        public void onError(String message) {
                            itemView.post(() -> Glide.with(itemView.getContext())
                                    .load(R.drawable.square_solid_full)
                                    .into(imgPoster));
                        }
                    });
                } catch (Exception e) {
                    Glide.with(itemView.getContext())
                            .load(R.drawable.square_solid_full)
                            .into(imgPoster);
                }
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.square_solid_full)
                        .into(imgPoster);
            }
        }
    }

    class CineShopOrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvCinema, tvRoom, tvSeats, tvShowtime;
        TextView tvStatus;
        MaterialCardView cardStatus;
        TextView tvBookingCode, tvCreatedAt, tvPrice;

        public CineShopOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_movie_poster);
            tvTitle = itemView.findViewById(R.id.tv_movie_title);
            tvCinema = itemView.findViewById(R.id.tv_cinema_name);
            tvRoom = itemView.findViewById(R.id.tv_room);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvShowtime = itemView.findViewById(R.id.tv_showtime);
            tvStatus = itemView.findViewById(R.id.tv_status);
            cardStatus = itemView.findViewById(R.id.card_status);
            tvBookingCode = itemView.findViewById(R.id.tv_booking_code);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvPrice = itemView.findViewById(R.id.tv_total_price);
        }

        public void bind(Booking booking) {
            tvTitle.setText(booking.movieTitleSnapshot);
            tvCinema.setText(booking.cinemaNameSnapshot);

            // Check if CineShop order (showtimeId is null)
            boolean isCineShop = (booking.showtimeId == null);

            if (isCineShop) {
                tvRoom.setText(booking.roomNameSnapshot != null ? booking.roomNameSnapshot : "");
                tvSeats.setVisibility(View.GONE);
                tvShowtime.setVisibility(View.GONE);
            } else {
                tvSeats.setVisibility(View.VISIBLE);
                tvShowtime.setVisibility(View.VISIBLE);
                tvRoom.setText(booking.roomNameSnapshot != null ? booking.roomNameSnapshot : "Chưa xác định");
                if (booking.seatCodes != null && !booking.seatCodes.isEmpty()) {
                    tvSeats.setText("Ghế: " + String.join(", ", booking.seatCodes));
                } else {
                    tvSeats.setText("Ghế: Chưa xác định");
                }

                if (booking.showtimeStartAtSnapshot > 0) {
                    tvShowtime.setText("Suất: " + dateFormat.format(new Date(booking.showtimeStartAtSnapshot)));
                } else {
                    tvShowtime.setText("Suất: Chưa xác định");
                }
            }

            long now = System.currentTimeMillis();
            boolean isExpired = (!isCineShop && booking.showtimeStartAtSnapshot > 0 && booking.showtimeStartAtSnapshot < now);
            String bookingStatus = booking.bookingStatus != null ? booking.bookingStatus.toUpperCase() : "PENDING";
            String paymentStatus = booking.paymentStatus != null ? booking.paymentStatus.toUpperCase() : "PENDING";
            boolean isCancelled = "FAILED".equals(bookingStatus) || "CANCELLED".equals(bookingStatus) ||
                    "FAILED".equals(paymentStatus) || "CANCELLED".equals(paymentStatus);
            boolean isUsed = booking.checkInAt > 0;

            // Bind unified status
            bindUnifiedStatus(tvStatus, cardStatus, booking, now);

            // Booking code & Date
            tvBookingCode.setText("Mã vé: " + (booking.paymentCode != null ? booking.paymentCode : booking.bookingId));
            if (booking.createdAt > 0) {
                tvCreatedAt.setText("Đặt ngày: " + dateFormat.format(new Date(booking.createdAt)));
            } else {
                tvCreatedAt.setText("Đặt ngày: Chưa rõ");
            }

            tvPrice.setText(String.format("%,.0fđ", booking.total).replace(',', '.'));

            // Click handling and visual indication
            if (isExpired || isCancelled || isUsed) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(bookings.get(pos));
                }
            });

            // Poster Loading Fallback logic
            if (!android.text.TextUtils.isEmpty(booking.movieImageUrlSnapshot)) {
                Glide.with(itemView.getContext())
                        .load(booking.movieImageUrlSnapshot)
                        .placeholder(R.drawable.square_solid_full)
                        .error(R.drawable.square_solid_full)
                        .into(imgPoster);
            } else if (!android.text.TextUtils.isEmpty(booking.movieId)) {
                // Fetch from MovieRepository
                try {
                    MyApp app = (MyApp) itemView.getContext().getApplicationContext();
                    app.getAppContainer().getMovieRepository().getMovieById(booking.movieId, new com.example.cinemabookingapp.domain.common.ResultCallback<Movie>() {
                        @Override
                        public void onSuccess(Movie movie) {
                            if (movie != null && !android.text.TextUtils.isEmpty(movie.posterUrl)) {
                                booking.movieImageUrlSnapshot = movie.posterUrl;
                                itemView.post(() -> Glide.with(itemView.getContext())
                                        .load(movie.posterUrl)
                                        .placeholder(R.drawable.square_solid_full)
                                        .error(R.drawable.square_solid_full)
                                        .into(imgPoster));
                            } else {
                                itemView.post(() -> Glide.with(itemView.getContext())
                                        .load(R.drawable.square_solid_full)
                                        .into(imgPoster));
                            }
                        }

                        @Override
                        public void onError(String message) {
                            itemView.post(() -> Glide.with(itemView.getContext())
                                    .load(R.drawable.square_solid_full)
                                    .into(imgPoster));
                        }
                    });
                } catch (Exception e) {
                    Glide.with(itemView.getContext())
                            .load(R.drawable.square_solid_full)
                            .into(imgPoster);
                }
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.square_solid_full)
                        .into(imgPoster);
            }
        }
    }
}
