package com.example.cinemabookingapp.ui.features.cineshop;

import com.example.cinemabookingapp.domain.model.Snack;

import java.util.ArrayList;
import java.util.List;

/**
 * CineCartManager â€” Singleton giá»¯ tráº¡ng thÃ¡i giá» hÃ ng cá»§a Cine Shop.
 * DÃ¹ng chung giá»¯a CineShopFragment, CineCartActivity vÃ  CineCheckoutActivity.
 */
public class CineCartManager {

    // â”€â”€ Singleton â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static CineCartManager instance;

    public static CineCartManager getInstance() {
        if (instance == null) instance = new CineCartManager();
        return instance;
    }

    private CineCartManager() {}

    // â”€â”€ Data model â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public static class CartItem {
        public Snack snack;
        public int quantity;

        public CartItem(Snack snack, int quantity) {
            this.snack    = snack;
            this.quantity = quantity;
        }

        public double subtotal() {
            return snack.price * quantity;
        }
    }

    private final List<CartItem> items = new ArrayList<>();

    // â”€â”€ Public API â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** ThÃªm hoáº·c cá»™ng dá»“n sá»‘ lÆ°á»£ng vÃ o giá». */
    public void addItem(Snack snack, int quantity) {
        for (CartItem item : items) {
            if (item.snack.snackId.equals(snack.snackId)) {
                item.quantity += quantity;
                return;
            }
        }
        items.add(new CartItem(snack, quantity));
    }

    /** Cáº­p nháº­t sá»‘ lÆ°á»£ng tuyá»‡t Ä‘á»‘i. Náº¿u qty <= 0 â†’ xoÃ¡ item. */
    public void updateQuantity(String snackId, int quantity) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).snack.snackId.equals(snackId)) {
                if (quantity <= 0) items.remove(i);
                else items.get(i).quantity = quantity;
                return;
            }
        }
    }

    public void removeItem(String snackId) {
        items.removeIf(it -> it.snack.snackId.equals(snackId));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalCount() {
        int total = 0;
        for (CartItem it : items) total += it.quantity;
        return total;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem it : items) total += it.subtotal();
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}
