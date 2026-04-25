package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Snack;

import java.util.List;

public interface SnackRepository {
    void createSnack(Snack snack, ResultCallback<Snack> callback);
    void getSnackById(String snackId, ResultCallback<Snack> callback);
    void getAllSnacks(ResultCallback<List<Snack>> callback);
    void getSnacksByCategoryId(String categoryId, ResultCallback<List<Snack>> callback);
    void updateSnack(Snack snack, ResultCallback<Snack> callback);
    void setAvailability(String snackId, boolean available, ResultCallback<Snack> callback);
    void softDeleteSnack(String snackId, ResultCallback<Void> callback);
}