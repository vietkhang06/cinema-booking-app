package com.example.cinemabookingapp.ui.features.voucher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Voucher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.tvCode.setText(voucher.code);
        holder.tvDiscount.setText("Discount: " + voucher.discountPercent + "%");
        holder.tvExpired.setText("Expired at: " + sdf.format(new Date(voucher.expiredAt)));
        holder.tvStatus.setText(voucher.status);

        if ("ACTIVE".equalsIgnoreCase(voucher.status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_expired);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) v.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Voucher Code", voucher.code);
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(v.getContext(), "Đã sao chép mã: " + voucher.code, android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return voucherList == null ? 0 : voucherList.size();
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvDiscount, tvExpired;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_voucher_code);
            tvStatus = itemView.findViewById(R.id.tv_voucher_status);
            tvDiscount = itemView.findViewById(R.id.tv_voucher_discount);
            tvExpired = itemView.findViewById(R.id.tv_voucher_expired);
        }
    }
}
