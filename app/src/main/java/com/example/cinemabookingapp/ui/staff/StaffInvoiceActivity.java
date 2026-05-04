package com.example.cinemabookingapp.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.service.InvoiceService;
import com.example.cinemabookingapp.ui.component.EInvoice.EInvoiceView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class StaffInvoiceActivity extends AppCompatActivity {

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

        initViews();
        bindActions();

        retriveDataFromNavigator();
    }

    InvoiceService invoiceService;

    MaterialButton backButton;
    EInvoiceView eInvoiceView;

    private void initViews() {

        eInvoiceView = findViewById(R.id.staff_einvoice);
        backButton = findViewById(R.id.staff_invoice_back_btn);
    }

    private void bindActions() {
        backButton.setOnClickListener(v -> {
            backToStaffDashboard();
        });
    }

    void retriveDataFromNavigator(){
        Intent intent = getIntent();
        String invoiceId = intent.getStringExtra("invoiceId");

        if(invoiceId == null || invoiceId.isBlank()){
            backToStaffDashboard();
            return;
        }

        Booking testBooking = new Booking();
        testBooking.bookingId = invoiceId;

        eInvoiceView.setInvoiceDetail(testBooking);

//        Booking booking = invoiceService.getInvoiceFromId(invoiceId);
//        if(booking == null){
//            backToStaffDashboard();
//            eInvoiceView.setInvoiceDetail();
//            return;
//        }
    }

    void backToStaffDashboard(){
        AppNavigator.goToStaffDashboard(this);
    }


}