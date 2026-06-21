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
        notif.title = "Chào mừng Khách hàng mới!";
        notif.message = "Bạn vừa nhận được Voucher 200.000đ áp dụng cho mọi giao dịch. Chúc bạn xem phim vui vẻ!";
        notif.type = NotificationType.VOUCHER_RECEIVED.name();
        notif.isRead = false;
        notif.createdAt = currentTime;
        notif.updatedAt = currentTime;
        batch.set(notifRef, notif);

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d("WelcomeVoucher", "Đã gửi voucher thành công cho user: " + userId))
                .addOnFailureListener(e -> Log.e("WelcomeVoucher", "Lỗi gửi voucher: " + e.getMessage()));
    }
}
