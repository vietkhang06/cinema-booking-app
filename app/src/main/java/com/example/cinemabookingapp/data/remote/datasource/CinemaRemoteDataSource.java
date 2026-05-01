package com.example.cinemabookingapp.data.remote.datasource;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CinemaRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION = "cinemas";

    // =========================
    // GET ALL
    // =========================
    public void getAllCinemas(ResultCallback<List<Cinema>> callback) {
        db.collection(COLLECTION)
                .get()
                .addOnSuccessListener(query -> {

                    List<Cinema> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        Cinema c = mapDocToCinema(doc);

                        // ❗ lọc soft delete
                        if (!c.deleted) {
                            list.add(c);
                        }
                    }

                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // =========================
    // CREATE (AUTO ID hoặc CUSTOM ID)
    // =========================
    public void createCinema(Cinema cinema, ResultCallback<Cinema> callback) {

        // 👉 OPTION 1: AUTO ID (recommended)
        DocumentReference docRef = db.collection(COLLECTION).document();

        cinema.cinemaId = docRef.getId();

        // 👉 OPTION 2: CUSTOM ID (nếu muốn dạng c001)
        // String customId = "c" + System.currentTimeMillis();
        // cinema.cinemaId = customId;
        // DocumentReference docRef = db.collection(COLLECTION).document(customId);

        long now = System.currentTimeMillis();

        cinema.createdAt = now;
        cinema.updatedAt = now;
        cinema.deleted = false;

        docRef.set(cinema)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(cinema);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // =========================
    // GET BY ID
    // =========================
    public void getCinemaById(String id, ResultCallback<Cinema> callback) {
        db.collection(COLLECTION)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError("Cinema không tồn tại");
                        return;
                    }
                    callback.onSuccess(mapDocToCinema(doc));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // =========================
    // UPDATE
    // =========================
    public void updateCinema(Cinema cinema, ResultCallback<Cinema> callback) {

        if (cinema.cinemaId == null) {
            callback.onError("CinemaId null");
            return;
        }

        cinema.updatedAt = System.currentTimeMillis();

        db.collection(COLLECTION)
                .document(cinema.cinemaId)
                .set(cinema)
                .addOnSuccessListener(unused -> callback.onSuccess(cinema))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // =========================
    // SOFT DELETE
    // =========================
    public void softDeleteCinema(String id, ResultCallback<Void> callback) {

        db.collection(COLLECTION)
                .document(id)
                .update("deleted", true,
                        "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // =========================
    // MAPPER
    // =========================
    private Cinema mapDocToCinema(DocumentSnapshot doc) {

        Cinema c = new Cinema();

        c.cinemaId = doc.getId();
        c.name = doc.getString("name");
        c.address = doc.getString("address");
        c.city = doc.getString("city");
        c.district = doc.getString("district");
        c.phone = doc.getString("phone");
        c.status = doc.getString("status");

        c.latitude = getDouble(doc.get("latitude"));
        c.longitude = getDouble(doc.get("longitude"));

        c.createdAt = getLong(doc.get("createdAt"));
        c.updatedAt = getLong(doc.get("updatedAt"));

        Boolean deleted = doc.getBoolean("deleted");
        c.deleted = deleted != null && deleted;

        return c;
    }

    // =========================
    // TYPE SAFE
    // =========================
    private long getLong(Object o) {
        try {
            if (o instanceof Long) return (Long) o;
            if (o instanceof String) return Long.parseLong((String) o);
        } catch (Exception ignored) {}
        return 0;
    }

    private double getDouble(Object o) {
        try {
            if (o instanceof Double) return (Double) o;
            if (o instanceof Long) return ((Long) o).doubleValue();
            if (o instanceof String) return Double.parseDouble((String) o);
        } catch (Exception ignored) {}
        return 0;
    }
}