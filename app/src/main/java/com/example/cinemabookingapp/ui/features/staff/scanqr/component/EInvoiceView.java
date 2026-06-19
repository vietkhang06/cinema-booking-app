package com.example.cinemabookingapp.ui.features.staff.scanqr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.utils.DateTimeConverter;
import com.example.cinemabookingapp.utils.QRCodeGenerator;

import java.util.List;
import java.util.Locale;

public class EInvoiceView extends FrameLayout {
    public EInvoiceView(Context context, AttributeSet attrs){
        super(context, attrs);
        inflate(context, R.layout.invoice_view_layout, this);
        initViews();
    }

    TextView invoiceIdTV, createDateTimeTV, movieNameTV, movieGernesTV, cinemaNameTV, showtimeTV,
            bookingSeatsTV, seatsPriceTV, snackTotalPriceTV, discountTV, totalPriceTV, paymentMethodTV, transactionIdTV;
    RecyclerView snackListView;
    ImageView invoiceIdQR, movieBannerImage;

    void initViews(){
        invoiceIdTV = findViewById(R.id.invoice_number);
        createDateTimeTV = findViewById(R.id.invoice_create_datetime);
        movieNameTV = findViewById(R.id.invoice_movie_name);
        movieGernesTV = findViewById(R.id.invoice_movie_gernes);
        cinemaNameTV = findViewById(R.id.invoice_cinema_name);
        showtimeTV = findViewById(R.id.invoice_showtime_datetime);
        bookingSeatsTV = findViewById(R.id.invoice_booking_seats);
        seatsPriceTV = findViewById(R.id.invoice_seats_price);
        snackTotalPriceTV = findViewById(R.id.invoice_snacks_total_price);
        discountTV = findViewById(R.id.invoice_price_discount);
        totalPriceTV = findViewById(R.id.invoice_price_total);
        paymentMethodTV = findViewById(R.id.invoice_payment_method);
        transactionIdTV = findViewById(R.id.invoice_transaction_id);

        snackListView = findViewById(R.id.invoice_snacks_listview);

        invoiceIdQR = findViewById(R.id.invoice_qr_bitmap);
        movieBannerImage = findViewById(R.id.invoice_movie_banner);
    }

    void bindViewData(Booking invoiceDetail){

        invoiceIdTV.setText(invoiceDetail.bookingId);
        try {
            invoiceIdQR.setImageBitmap(QRCodeGenerator.generateQRCodeFromString(invoiceDetail.bookingId));
        }catch (Exception ignored){ }

        invoiceIdTV.setText(invoiceDetail.bookingId);
        createDateTimeTV.setText(DateTimeConverter.convertToDateTimeString(invoiceDetail.createdAt));
        cinemaNameTV.setText(invoiceDetail.cinemaNameSnapshot);
        movieNameTV.setText(invoiceDetail.movieTitleSnapshot);
//        movieGernesTV.setText(String.join(", ", invoiceDetail.movieGenresSnapshot));
        Glide.with(this)
                .load(invoiceDetail.movieImageUrlSnapshot)
                .into(movieBannerImage);

//        cinemaNameTV.setText();
        showtimeTV.setText(DateTimeConverter.convertToDateTimeString(invoiceDetail.showtimeStartAtSnapshot));
        bookingSeatsTV.setText(String.join(",", invoiceDetail.seatCodes));

        seatsPriceTV.setText(String.format(Locale.ENGLISH, "%,dvnd", (int) invoiceDetail.subtotal));
        discountTV.setText(String.format("%,dvnd",(int) invoiceDetail.discount));
        totalPriceTV.setText(String.format("%,dvnd",(int) invoiceDetail.total));
        discountTV.setText(String.format("%,dvnd",(int) invoiceDetail.discount));

        paymentMethodTV.setText(invoiceDetail.paymentMethod);
        if(invoiceDetail.snackOrder != null && !invoiceDetail.snackOrder.isEmpty()) {
            snackTotalPriceTV.setText(String.format(Locale.ENGLISH, "%,dvnd", (int) invoiceDetail.snackOrder.stream().mapToDouble(item -> item.price * item.quantity).sum()));

            snackListView.setLayoutManager(new LinearLayoutManager(getContext()));
            snackListView.setAdapter(new InvoiceSnackItemAdapter(invoiceDetail.snackOrder));
        }else {
            snackTotalPriceTV.setText(String.format(Locale.ENGLISH, "%,dvnd", 0));
            snackListView.setVisibility(View.GONE);
        }
    }

    public void setInvoiceDetail(Booking invoiceDetail){
        bindViewData(invoiceDetail);
    }

    public class InvoiceSnackItemAdapter extends RecyclerView.Adapter<InvoiceSnackItemAdapter.ItemViewHolder> {
        private List<Booking.SnackOrderSnapshot> itemList;

        public InvoiceSnackItemAdapter(List<Booking.SnackOrderSnapshot> itemList) {
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.invoice_snack_item, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            if(itemList == null) return;
            Booking.SnackOrderSnapshot item = itemList.get(position);
            holder.itemNameAndQuantityTextView.setText(String.format("%sx%s", item.snackName, item.quantity));
            holder.itemPriceTextView.setText(String.format("%,d vnd", item.price));
        }

        @Override
        public int getItemCount() {
            return itemList != null ? itemList.size() : 0;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView itemNameAndQuantityTextView, itemPriceTextView;
            public ItemViewHolder(View itemView) {
                super(itemView);
                itemNameAndQuantityTextView = itemView.findViewById(R.id.item_name_x_quantity);
                itemPriceTextView = itemView.findViewById(R.id.item_price);
            }
        }
    }

}
