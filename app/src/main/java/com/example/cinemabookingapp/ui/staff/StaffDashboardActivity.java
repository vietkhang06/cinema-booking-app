package com.example.cinemabookingapp.ui.staff;

import android.os.Bundle;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.AuthActivity;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.google.android.material.card.MaterialCardView;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.HashMap;
import java.util.Map;

public class StaffDashboardActivity extends AuthActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        bindActions();
    }

    MaterialCardView checkInvoiceButton;

    private void initViews() {

        checkInvoiceButton = findViewById(R.id.staff_db_check_invoice);
    }

    private void bindActions() {
        checkInvoiceButton.setOnClickListener(v -> {
            openGoogleScanner();
        });
    }

    void openGoogleScanner(){
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options); // or pass 'options'

        // test: scan with any QR code
        scanner.startScan()
            .addOnSuccessListener(barcode -> {
                String rawValue = barcode.getRawValue();
                Map<String, String> bundle = new HashMap<>();
                bundle.put("invoiceId", rawValue);
                AppNavigator.navigateWithData(this, StaffInvoiceActivity.class, bundle);
            })
            .addOnCanceledListener(() -> {
            })
            .addOnFailureListener(e -> {
            });
    }
}