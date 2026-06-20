package com.example.cinemabookingapp.service;

import android.util.Log;
import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.model.Notification;
import com.example.cinemabookingapp.domain.model.NotificationType;
import com.example.cinemabookingapp.domain.model.Voucher;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
public class WelcomeVoucherService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public void sendWelcomeVoucher(String userId) {
        if (userId == null || userId.isEmpty()) return;

        WriteBatch batch = db.batch();
        long currentTime = System.currentTimeMillis();
        // 1. Khá»Ÿi táº¡o tháº» Voucher 200,000Ä‘
        DocumentReference voucherRef = db.collection("vouchers").document();
        Voucher voucher = new Voucher();
        voucher.voucherId = voucherRef.getId();
        voucher.userId = userId;
        voucher.code = "WELCOME_" + userId;
        voucher.discountPercent = 20;
        voucher.status = "ACTIVE";
        voucher.expiredAt = currentTime + 30L * 24 * 60 * 60 * 1000; // 30 days
        voucher.createdAt = currentTime;
        voucher.updatedAt = currentTime;
        batch.set(voucherRef, voucher);

        // 2. Khá»Ÿi táº¡o ThÃ´ng bÃ¡o gá»­i tá»›i há»™p thÆ° khÃ¡ch hÃ ng
        DocumentReference notifRef = db.collection(FirestoreCollections.NOTIFICATIONS).document();
        Notification notif = new Notification();
        notif.notificationId = notifRef.getId();
        notif.userId = userId;
        notif.title = "ChÃ o má»«ng KhÃ¡ch hÃ ng má»›i!";
        notif.message = "Báº¡n vá»«a nháº­n Ä‘Æ°á»£c Voucher 200.000Ä‘ Ã¡p dá»¥ng cho má»i giao dá»‹ch. ChÃºc báº¡n xem phim vui váº»!";
        notif.type = NotificationType.VOUCHER_RECEIVED.name();
        notif.isRead = false;
        notif.createdAt = currentTime;
        notif.updatedAt = currentTime;
        batch.set(notifRef, notif);

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d("WelcomeVoucher", "ÄÃ£ gá»­i voucher thÃ nh cÃ´ng cho user: " + userId))
                .addOnFailureListener(e -> Log.e("WelcomeVoucher", "Lá»—i gá»­i voucher: " + e.getMessage()));
    }
}
