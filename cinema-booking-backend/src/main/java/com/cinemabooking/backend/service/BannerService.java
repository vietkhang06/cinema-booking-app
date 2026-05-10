package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.BannerDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BannerService {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "banners";

    public List<BannerDTO> getAllBanners() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<BannerDTO> banners = new ArrayList<>();

        for (DocumentSnapshot doc : documents) {
            Boolean isActive = doc.getBoolean("isActive");
            if (isActive != null && !isActive) continue;

            banners.add(BannerDTO.builder()
                    .bannerId(doc.getId())
                    .imageUrl(doc.getString("imageUrl"))
                    .build());
        }
        return banners;
    }
}
