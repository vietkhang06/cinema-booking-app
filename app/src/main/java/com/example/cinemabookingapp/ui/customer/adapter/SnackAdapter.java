package com.example.cinemabookingapp.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Snack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnackAdapter extends RecyclerView.Adapter<SnackAdapter.SnackViewHolder> {

    // Danh sách món ăn hiển thị
    private List<Snack> snackList = new ArrayList<>();

    // Map đóng vai trò là "Giỏ hàng" lưu trữ: Key là snackId, Value là số lượng đang chọn
    private Map<String, Integer> cartQuantities = new HashMap<>();

    private OnCartChangedListener cartListener;

    // Interface để báo ra ngoài Activity mỗi khi số lượng món thay đổi (để tính tổng tiền)
    public interface OnCartChangedListener {
        void onCartUpdated(Map<String, Integer> cartQuantities, List<Snack> snacks);
    }

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartListener = listener;
    }

    // Nạp dữ liệu từ Firebase vào Adapter
    public void setSnacks(List<Snack> snacks) {
        this.snackList = snacks;
        notifyDataSetChanged();
    }

    // Xóa giỏ hàng (nếu cần dùng sau khi thanh toán)
    public void clearCart() {
        cartQuantities.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SnackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_snack, parent, false);
        return new SnackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SnackViewHolder holder, int position) {
        Snack snack = snackList.get(position);
        holder.bind(snack);
    }

    @Override
    public int getItemCount() {
        return snackList.size();
    }

    class SnackViewHolder extends RecyclerView.ViewHolder {
        TextView tvSnackName, tvSnackPrice, tvQuantity;
        ImageView btnMinus, btnPlus, imgSnack;

        public SnackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSnackName = itemView.findViewById(R.id.tvSnackName);
            tvSnackPrice = itemView.findViewById(R.id.tvSnackPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            imgSnack = itemView.findViewById(R.id.imgSnack);
        }

        public void bind(Snack snack) {
            // Gán thông tin từ model Snack
            tvSnackName.setText(snack.name);

            // Format giá tiền (Ví dụ: 55000 -> 55.000 ₫)
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvSnackPrice.setText(formatter.format(snack.price) + " ₫");

            // Lấy số lượng hiện tại trong giỏ hàng (Mặc định là 0)
            int currentQty = cartQuantities.containsKey(snack.snackId) ? cartQuantities.get(snack.snackId) : 0;
            tvQuantity.setText(String.valueOf(currentQty));

            // Xử lý nút Cộng
            btnPlus.setOnClickListener(v -> {
                int qty = cartQuantities.containsKey(snack.snackId) ? cartQuantities.get(snack.snackId) : 0;
                qty++;
                cartQuantities.put(snack.snackId, qty);
                tvQuantity.setText(String.valueOf(qty));

                if (cartListener != null) {
                    cartListener.onCartUpdated(cartQuantities, snackList);
                }
            });

            // Xử lý nút Trừ
            btnMinus.setOnClickListener(v -> {
                int qty = cartQuantities.containsKey(snack.snackId) ? cartQuantities.get(snack.snackId) : 0;
                if (qty > 0) {
                    qty--;
                    if (qty == 0) {
                        cartQuantities.remove(snack.snackId); // Bỏ khỏi giỏ nếu số lượng = 0
                    } else {
                        cartQuantities.put(snack.snackId, qty);
                    }
                    tvQuantity.setText(String.valueOf(qty));

                    if (cartListener != null) {
                        cartListener.onCartUpdated(cartQuantities, snackList);
                    }
                }
            });

            // Xử lý load ảnh bằng Glide (Bạn có thể thêm sau nếu dùng Glide/Picasso)
            // Glide.with(itemView).load(snack.imageUrl).into(imgSnack);
        }
    }
}
