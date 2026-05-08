package com.example.cinemabookingapp.service;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotificationService {
    FirebaseFirestore firestore;
    public NotificationService(){
        firestore = FirebaseFirestore.getInstance();
    }
    public void loadAllNotifications(String userId, ResultCallback<List<Notification>> callback) {
        firestore.collection(FirestoreCollections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = queryDocumentSnapshots.toObjects(Notification.class);
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                    e.printStackTrace();
                });
    }
}
