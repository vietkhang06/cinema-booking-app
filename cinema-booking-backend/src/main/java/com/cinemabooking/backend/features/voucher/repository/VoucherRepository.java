package com.cinemabooking.backend.features.voucher.repository;

import com.cinemabooking.backend.features.voucher.dto.VoucherDTO;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class VoucherRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "vouchers";

    public void save(VoucherDTO voucher) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(voucher.getVoucherId()).set(voucher).get();
    }

    public List<QueryDocumentSnapshot> findByUserId(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .get().get().getDocuments();
    }

    public void updateStatus(String voucherId, String status, long updatedAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(voucherId)
                .update("status", status, "updatedAt", updatedAt)
                .get();
    }

    public List<QueryDocumentSnapshot> findByCodeAndUserId(String code, String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("code", code)
                .whereEqualTo("userId", userId)
                .get().get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findByCode(String code) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("code", code)
                .get().get().getDocuments();
    }
}
