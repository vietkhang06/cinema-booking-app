package com.cinemabooking.backend.features.voucher.repository;

import com.cinemabooking.backend.features.voucher.dto.VoucherDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class VoucherRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "vouchers";

    public VoucherDTO save(VoucherDTO voucher) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(voucher.getVoucherId()).set(voucher).get();
        return voucher;
    }

    public List<VoucherDTO> findByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<VoucherDTO> vouchers = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            vouchers.add(doc.toObject(VoucherDTO.class));
        }
        return vouchers;
    }

    public void updateStatus(String voucherId, String status, long updatedAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(voucherId)
                .update("status", status, "updatedAt", updatedAt)
                .get();
    }

    public VoucherDTO findByCodeAndUserId(String code, String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("code", code)
                .whereEqualTo("userId", userId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }
        return documents.get(0).toObject(VoucherDTO.class);
    }

    public VoucherDTO findByCode(String code) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("code", code)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }
        return documents.get(0).toObject(VoucherDTO.class);
    }
}
