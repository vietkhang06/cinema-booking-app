package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.SnackCategory;

import java.util.List;

public interface SnackCategoryRepository {
    void createCategory(SnackCategory category, ResultCallback<SnackCategory> callback);
    void getCategoryById(String categoryId, ResultCallback<SnackCategory> callback);
    void getAllCategories(ResultCallback<List<SnackCategory>> callback);
    void updateCategory(SnackCategory category, ResultCallback<SnackCategory> callback);
    void softDeleteCategory(String categoryId, ResultCallback<Void> callback);
}