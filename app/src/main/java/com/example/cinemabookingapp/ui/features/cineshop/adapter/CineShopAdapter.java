package com.example.cinemabookingapp.ui.features.cineshop.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Snack;
import com.example.cinemabookingapp.ui.features.cineshop.CineCartActivity;
import com.example.cinemabookingapp.ui.features.cineshop.CineCartManager;
import com.google.android.material.card.MaterialCardView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * CineShopAdapter â€” adapter cho mÃ n hÃ¬nh Cine Shop.
 * Má»—i item hiá»ƒn thá»‹:
 *   - áº¢nh sáº£n pháº©m (hÃ¬nh trÃ²n)
 *   - TÃªn & giÃ¡
 *   - NÃºt "MUA NGAY"       â†’ dialog chá»n sá»‘ lÆ°á»£ng â†’ thÃªm vÃ o cart â†’ má»Ÿ CineCartActivity
 *   - NÃºt "THÊM VÀO GIỎ"  â†’ dialog chá»n sá»‘ lÆ°á»£ng â†’ thÃªm vÃ o CineCartManager
 *
 * Cart state Ä‘Æ°á»£c lÆ°u trong {@link CineCartManager} singleton.
 */
public class CineShopAdapter extends RecyclerView.Adapter<CineShopAdapter.ProductViewHolder> {

    // â”€â”€ Data â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private List<Snack> productList = new ArrayList<>();

    // â”€â”€ Cart change callback (cáº­p nháº­t badge icon giá») â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public interface OnCartChangedListener {
        void onCartUpdated(int totalCount);
    }
    private OnCartChangedListener cartListener;

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartListener = listener;
    }

    public void setProducts(List<Snack> products) {
        this.productList = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    // â”€â”€ RecyclerView â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cine_shop, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() { return productList.size(); }

    // â”€â”€ ViewHolder â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, btnBuyNow, btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct     = itemView.findViewById(R.id.imgProduct);
            tvProductName  = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnBuyNow      = itemView.findViewById(R.id.btnBuyNow);
            btnAddToCart   = itemView.findViewById(R.id.btnAddToCart);
        }

        void bind(Snack snack) {
            tvProductName.setText(snack.name);
            DecimalFormat fmt = new DecimalFormat("#,###");
            tvProductPrice.setText(fmt.format(snack.price) + "đ");

            // Load áº£nh sáº£n pháº©m tá»« Firestore thÃ´ng qua Glide
            Glide.with(itemView.getContext())
                    .load(snack.imageUrl)
                    .placeholder(R.drawable.bg_banner_placeholder)
                    .error(R.drawable.bg_banner_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgProduct);

            btnBuyNow.setOnClickListener(v ->
                    showBuyNowDialog(itemView.getContext(), snack));

            btnAddToCart.setOnClickListener(v ->
                    showAddToCartDialog(itemView.getContext(), snack));
        }
    }

    // â”€â”€ Dialog: MUA NGAY â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private void showBuyNowDialog(Context ctx, Snack snack) {
        Dialog dialog = buildBottomDialog(ctx, R.layout.dialog_cine_buy_now);

        ImageView imgProduct = dialog.findViewById(R.id.dialogImgProduct);
        TextView tvName     = dialog.findViewById(R.id.dialogTvProductName);
        TextView tvPrice    = dialog.findViewById(R.id.dialogTvProductPrice);
        TextView tvQty      = dialog.findViewById(R.id.dialogTvQuantity);
        TextView tvSubtotal = dialog.findViewById(R.id.dialogTvSubtotal);
        MaterialCardView btnMinus  = dialog.findViewById(R.id.btnDialogMinus);
        MaterialCardView btnPlus   = dialog.findViewById(R.id.btnDialogPlus);
        TextView btnCancel  = dialog.findViewById(R.id.btnDialogCancel);
        TextView btnConfirm = dialog.findViewById(R.id.btnDialogConfirmBuy);

        if (imgProduct != null) {
            Glide.with(ctx)
                    .load(snack.imageUrl)
                    .placeholder(R.drawable.bg_banner_placeholder)
                    .error(R.drawable.bg_banner_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgProduct);
        }

        DecimalFormat fmt = new DecimalFormat("#,###");
        tvName.setText(snack.name);
        tvPrice.setText(fmt.format(snack.price) + "đ");

        final int[] qty = {1};
        tvQty.setText(String.valueOf(qty[0]));
        tvSubtotal.setText(fmt.format(snack.price) + "đ");

        btnMinus.setOnClickListener(v -> {
            if (qty[0] > 1) {
                qty[0]--;
                tvQty.setText(String.valueOf(qty[0]));
                tvSubtotal.setText(fmt.format(snack.price * qty[0]) + "đ");
            }
        });
        btnPlus.setOnClickListener(v -> {
            qty[0]++;
            tvQty.setText(String.valueOf(qty[0]));
            tvSubtotal.setText(fmt.format(snack.price * qty[0]) + "đ");
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            // ThÃªm vÃ o giá» qua CartManager rá»“i má»Ÿ mÃ n hÃ¬nh giá» hÃ ng
            CineCartManager.getInstance().addItem(snack, qty[0]);
            if (cartListener != null)
                cartListener.onCartUpdated(CineCartManager.getInstance().getTotalCount());
            dialog.dismiss();
            ctx.startActivity(new Intent(ctx, CineCartActivity.class));
        });

        dialog.show();
    }

    // â”€â”€ Dialog: THÃŠM VÃ€O GIá»Ž HÃ€NG â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private void showAddToCartDialog(Context ctx, Snack snack) {
        Dialog dialog = buildBottomDialog(ctx, R.layout.dialog_cine_add_to_cart);

        ImageView imgProduct = dialog.findViewById(R.id.cartDialogImgProduct);
        TextView tvName  = dialog.findViewById(R.id.cartDialogTvProductName);
        TextView tvPrice = dialog.findViewById(R.id.cartDialogTvProductPrice);
        TextView tvQty   = dialog.findViewById(R.id.cartDialogTvQuantity);
        MaterialCardView btnMinus  = dialog.findViewById(R.id.btnCartDialogMinus);
        MaterialCardView btnPlus   = dialog.findViewById(R.id.btnCartDialogPlus);
        TextView btnCancel  = dialog.findViewById(R.id.btnCartDialogCancel);
        TextView btnConfirm = dialog.findViewById(R.id.btnCartDialogConfirm);

        if (imgProduct != null) {
            Glide.with(ctx)
                    .load(snack.imageUrl)
                    .placeholder(R.drawable.bg_banner_placeholder)
                    .error(R.drawable.bg_banner_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgProduct);
        }

        DecimalFormat fmt = new DecimalFormat("#,###");
        tvName.setText(snack.name);
        tvPrice.setText(fmt.format(snack.price) + "đ");

        final int[] qty = {1};
        tvQty.setText(String.valueOf(qty[0]));

        btnMinus.setOnClickListener(v -> {
            if (qty[0] > 1) { qty[0]--; tvQty.setText(String.valueOf(qty[0])); }
        });
        btnPlus.setOnClickListener(v -> {
            qty[0]++; tvQty.setText(String.valueOf(qty[0]));
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            CineCartManager.getInstance().addItem(snack, qty[0]);
            if (cartListener != null)
                cartListener.onCartUpdated(CineCartManager.getInstance().getTotalCount());
            Toast.makeText(ctx,
                    "Đã thêm " + qty[0] + " × " + snack.name + " vào giỏ ✓",
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private Dialog buildBottomDialog(Context ctx, int layoutRes) {
        Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutRes);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }
        return dialog;
    }
}
