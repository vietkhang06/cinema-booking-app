package com.example.cinemabookingapp.service;

import android.net.Uri;
import android.util.Log;

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

    public String uploadImage(Uri uri){
        try {
            StorageReference storageRef = storage.getReference().child("images/" + UUID.randomUUID().toString());
            Task<Uri> getUrlTask = Tasks.await(
                    FirebaseStorage.getInstance().getReference().child("images")
                            .putFile(uri)
                            .continueWith(task -> {
                                if(!task.isSuccessful())
                                    throw new Exception("Upload Image Failed.");
                                return storageRef.getDownloadUrl();
                            })
            );
            return getUrlTask.getResult().toString();
        }catch (Exception e){
            Log.e("Upload Image", e.getMessage());
            return null;
        }
    }
}
