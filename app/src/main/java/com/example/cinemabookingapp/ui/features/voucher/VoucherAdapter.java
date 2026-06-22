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
            // First copy code to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) v.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Voucher Code", voucher.code);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                android.widget.Toast.makeText(v.getContext(), "Đã sao chép mã: " + voucher.code, android.widget.Toast.LENGTH_SHORT).show();
            }

            // Show details dialog
            android.content.Context context = v.getContext();
            android.app.Dialog dialog = new android.app.Dialog(context);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_my_voucher_detail);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(lp);
            }

            TextView tvDialogCode = dialog.findViewById(R.id.tvDialogCode);
            TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            TextView tvDialogDesc = dialog.findViewById(R.id.tvDialogDesc);
            TextView tvDialogCond = dialog.findViewById(R.id.tvDialogCond);
            TextView tvDialogExpiry = dialog.findViewById(R.id.tvDialogExpiry);
            android.widget.Button btnDialogClose = dialog.findViewById(R.id.btnDialogClose);

            if (tvDialogCode != null) {
                tvDialogCode.setText(voucher.code != null ? voucher.code.toUpperCase(Locale.getDefault()) : "");
            }
            if (tvDialogTitle != null) {
                tvDialogTitle.setText("Giảm " + voucher.discountPercent + "%");
            }
            if (tvDialogDesc != null) {
                tvDialogDesc.setText("Voucher hệ thống dành cho bạn.");
            }
            if (tvDialogCond != null) {
                tvDialogCond.setText("Áp dụng cho đơn vé mọi giá trị.");
            }
            if (tvDialogExpiry != null) {
                String expiryStr = "HSD: Không giới hạn";
                if (voucher.expiredAt > 0) {
                    expiryStr = "HSD: " + sdf.format(new Date(voucher.expiredAt));
                }
                tvDialogExpiry.setText(expiryStr);
            }

            if (btnDialogClose != null) {
                btnDialogClose.setOnClickListener(v2 -> dialog.dismiss());
            }

            dialog.show();
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
