package com.example.cinemabookingapp.data.remote.datasource;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Banner;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BannerRemoteDataSource {

    private static final String COLLECTION = "banners";

    private final FirebaseFirestore firestore;

    public BannerRemoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void getAllBanners(ResultCallback<List<Banner>> callback) {
        firestore.collection(COLLECTION)
                .get()
                .addOnSuccessListener(query -> {
                    List<Banner> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        Boolean isActive = doc.getBoolean("isActive");
                        if (isActive != null && !isActive) continue;

                        Banner banner = new Banner();
                        banner.bannerId = doc.getId();
                        banner.imageUrl = doc.getString("imageUrl");

                        list.add(banner);
                    }

                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null)
                        callback.onError(e.getMessage());
                });
    }
}