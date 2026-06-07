package com.example.cinemabookingapp.ui.customer.voucher;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.remote.api.VoucherApiService;
import com.example.cinemabookingapp.domain.model.Voucher;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerVoucherActivity extends AppCompatActivity {

    private RecyclerView rvVouchers;
    private TextView tvEmptyState;
    private Button btnTestVoucher;
    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_voucher);

        Toolbar toolbar = findViewById(R.id.toolbar_voucher);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Vouchers");
        }

        rvVouchers = findViewById(R.id.rv_vouchers);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        btnTestVoucher = findViewById(R.id.btn_test_voucher);

        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        voucherAdapter = new VoucherAdapter(voucherList);
        rvVouchers.setAdapter(voucherAdapter);

        btnTestVoucher.setOnClickListener(v -> generateTestVoucher());

        loadVouchers();
    }

    private void loadVouchers() {
        VoucherApiService apiService = RetrofitClient.getInstance().create(VoucherApiService.class);
        apiService.getMyVouchers().enqueue(new Callback<ApiResponse<List<Voucher>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Voucher>>> call, Response<ApiResponse<List<Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    voucherList.clear();
                    voucherList.addAll(response.body().getData());
                    voucherAdapter.notifyDataSetChanged();
                    
                    if (voucherList.isEmpty()) {
                        rvVouchers.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvVouchers.setVisibility(View.VISIBLE);
                        tvEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(CustomerVoucherActivity.this, "Failed to load vouchers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Voucher>>> call, Throwable t) {
                Toast.makeText(CustomerVoucherActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void generateTestVoucher() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        VoucherApiService apiService = RetrofitClient.getInstance().create(VoucherApiService.class);
        
        Toast.makeText(this, "Đang tạo voucher...", Toast.LENGTH_SHORT).show();
        apiService.testGrantVoucher().enqueue(new Callback<ApiResponse<Voucher>>() {
            @Override
            public void onResponse(Call<ApiResponse<Voucher>> call, Response<ApiResponse<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CustomerVoucherActivity.this, "Tạo thành công!", Toast.LENGTH_SHORT).show();
                    loadVouchers();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Unknown error";
                    String errorLog = "Error " + response.code() + ": " + msg;
                    Log.e("CustomerVoucherActivity", errorLog);
                    Toast.makeText(CustomerVoucherActivity.this, "Lỗi " + response.code() + ": " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Voucher>> call, Throwable t) {
                Log.e("CustomerVoucherActivity", "Network error: " + t.getMessage(), t);
                Toast.makeText(CustomerVoucherActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
