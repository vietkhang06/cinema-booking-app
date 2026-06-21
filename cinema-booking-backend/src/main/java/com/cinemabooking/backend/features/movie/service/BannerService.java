package com.cinemabooking.backend.features.movie.service;

import com.cinemabooking.backend.features.movie.dto.BannerDTO;
import com.cinemabooking.backend.features.movie.repository.BannerRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    public List<BannerDTO> getAllBanners() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = bannerRepository.findAll();
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

    // Method moved outside of getAllBanners()
    public void seedMockBanners() throws ExecutionException, InterruptedException {
        String[] mockUrls = {
                "https://www.galaxycine.vn/media/2024/3/6/z5217435133604-0ee030cd3eb04ea12ed2539d09c6f932_1709712711718.jpg",
                "https://www.galaxycine.vn/media/2024/5/1/combo-g-1_1714552467319.jpg",
                "https://www.galaxycine.vn/media/2024/5/1/combo-g-2_1714552469493.jpg"
        };

        for (int i = 0; i < mockUrls.length; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("imageUrl", mockUrls[i]);
            data.put("isActive", true);

            // Add to Firestore
            bannerRepository.save("mock_banner_" + i, data);
        }
    }
}