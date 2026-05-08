package com.example.cinemabookingapp.service;

import android.net.Uri;
import android.util.Log;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class UploadService {
    FirebaseStorage storage;
    public UploadService(){
        storage = FirebaseStorage.getInstance();
    }

    public void uploadImage(Uri uri, ResultCallback<Uri> callback){
        StorageReference storageRef = storage.getReference().child("images/" + UUID.randomUUID().toString());
        storageRef.putFile(uri)
            .continueWithTask(task -> {
                if(!task.isSuccessful())
                    throw new Exception("Upload Image Failed.");
                return task.getResult().getStorage().getDownloadUrl();
            })
            .addOnSuccessListener(t_uri -> {
                callback.onSuccess(t_uri);
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage());
            });
    }
}
