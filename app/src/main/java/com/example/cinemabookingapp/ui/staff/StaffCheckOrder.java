package com.example.cinemabookingapp.ui.staff;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.google.android.material.button.MaterialButton;

public class StaffCheckOrder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_check_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }
    TextView totalSnackPriceTV, amountSnackTV;
    RecyclerView snackContainerView;

    MaterialButton backBtn;
    LinearLayout snackLayout;
    TextView noOrderFoundTV;

    private void initViews() {
        totalSnackPriceTV = findViewById(R.id.snack_total_price);
        amountSnackTV = findViewById(R.id.snack_amount);
        snackContainerView = findViewById(R.id.snack_container);

        snackLayout = findViewById(R.id.customer_order_layout);
        noOrderFoundTV = findViewById(R.id.no_order_found);

        backBtn = findViewById(R.id.back_btn);
    }

    private void bindActions() {
    }

}