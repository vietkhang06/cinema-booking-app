package com.example.cinemabookingapp.ui.component.EInvoice;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.utils.QRCodeGenerator;

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

    void bindViewData(Booking bookingDetail){
        invoiceIdTV.setText(bookingDetail.bookingId);
        try {
            invoiceIdQR.setImageBitmap(QRCodeGenerator.generateQRCodeFromString(bookingDetail.bookingId));
        }catch (Exception e){

        }

    }

    Booking mBooking;
    public void setInvoiceDetail(Booking bookingDetail){
        mBooking = bookingDetail;
        bindViewData(bookingDetail);
    }
}
