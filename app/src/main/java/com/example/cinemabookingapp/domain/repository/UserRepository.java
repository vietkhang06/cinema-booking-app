package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;

import java.util.List;

public interface UserRepository {
    void createUser(User user, ResultCallback<User> callback);
    void getUserById(String uid, ResultCallback<User> callback);
    void getAllUsers(ResultCallback<List<User>> callback);
    void updateUser(User user, ResultCallback<User> callback);
    void updateRole(String uid, String role, ResultCallback<User> callback);
    void updateStatus(String uid, String status, ResultCallback<User> callback);
    void softDeleteUser(String uid, ResultCallback<Void> callback);
}