package com.example.cinemabookingapp.data.repository;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.domain.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    FirebaseFirestore firestore;
    public UserRepositoryImpl(){
        firestore = FirebaseFirestore.getInstance();
    }
    @Override
    public void createUser(User user, ResultCallback<User> callback) {
        firestore.collection(FirestoreCollections.USERS)
                .add(user)
                .addOnSuccessListener(doc -> {
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e->{
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getUserById(String uid, ResultCallback<User> callback) {
        firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc ->{
                    callback.onSuccess(doc.toObject(User.class));
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getAllUsers(ResultCallback<List<User>> callback) {

    }

    @Override
    public void updateUser(User user, ResultCallback<User> callback) {

    }

    @Override
    public void updateRole(String uid, String role, ResultCallback<User> callback) {

    }

    @Override
    public void updateStatus(String uid, String status, ResultCallback<User> callback) {

    }

    @Override
    public void softDeleteUser(String uid, ResultCallback<Void> callback) {

    }
}
