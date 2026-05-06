package com.example.cinemabookingapp.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.service.InvoiceService;
import com.example.cinemabookingapp.ui.component.EInvoice.EInvoiceView;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StaffInvoiceActivity extends AuthActivity {

    InvoiceService invoiceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_invoice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        invoiceService = ServiceProvider.getInstance().getInvoiceService();

        initViews();
        bindActions();

        retriveDataFromNavigator();
    }

    MaterialButton backButton, updatePaymentButton;
    TextView paymentStatusTV, transactionDateTV;
    ImageView paymentStatusImg;
    EInvoiceView eInvoiceView;

    private void initViews() {

        eInvoiceView = findViewById(R.id.staff_einvoice);
        backButton = findViewById(R.id.staff_invoice_back_btn);

        updatePaymentButton = findViewById(R.id.staff_invoice_update_payment);
        paymentStatusTV = findViewById(R.id.staff_invoice_payment_status_tv);
        transactionDateTV = findViewById(R.id.staff_invoice_transaction_date);

        paymentStatusImg = findViewById(R.id.staff_invoice_payment_status_img);
    }

    private void bindActions() {
        backButton.setOnClickListener(v -> {
            finish();
        });

        updatePaymentButton.setOnClickListener(v -> {
            invoiceDetail.booking.paymentStatus = "confirmed";
            invoiceService.updatePaymentStatus(invoiceDetail.booking, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void unused) { }
                @Override
                public void onError(String message) { }
            });
        });
    }

    InvoiceService.InvoiceDetail invoiceDetail;
    void retriveDataFromNavigator(){
        Intent intent = getIntent();
        String invoiceId = intent.getStringExtra("invoiceId");

        if(invoiceId == null || invoiceId.isBlank()){
            finish();
            showToast("Hóa đơn không hợp lệ");
            return;
        }

//        Booking testBooking = new Booking();
//        testBooking.bookingId = invoiceId;
//
//        eInvoiceView.setInvoiceDetail(testBooking);

        invoiceService.getInvoiceFromId(invoiceId, new ResultCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                if(booking == null){
                    showToast("Không tìm thấy hóa đơn :" + invoiceId);
                    finish();
                    return;
                }
                invoiceDetail = invoiceService.getInvoiceDetail(invoiceId);
                bindDataView(invoiceDetail);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    void bindDataView(InvoiceService.InvoiceDetail invoiceDetail){
        eInvoiceView.setInvoiceDetail(invoiceDetail);

        if(invoiceDetail.booking.paymentStatus.equals("confirmed")){
            updatePaymentButton.setEnabled(false);
            Glide.with(this)
                    .load(R.drawable.ic_confirm)
                    .into(paymentStatusImg);
        }
        else{
            Glide.with(this)
                    .load(R.drawable.ic_cross_circle)
                    .into(paymentStatusImg);
        }
    }





}