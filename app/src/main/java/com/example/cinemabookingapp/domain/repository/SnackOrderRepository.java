package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.SnackOrder;

import java.util.List;

public interface SnackOrderRepository {
    void createSnackOrder(SnackOrder order, ResultCallback<SnackOrder> callback);
    void getSnackOrderById(String snackOrderId, ResultCallback<SnackOrder> callback);
    void getSnackOrdersByUserId(String userId, ResultCallback<List<SnackOrder>> callback);
    void getSnackOrdersByBookingId(String bookingId, ResultCallback<List<SnackOrder>> callback);
    void updateSnackOrderStatus(String snackOrderId, String status, ResultCallback<SnackOrder> callback);
    void softDeleteSnackOrder(String snackOrderId, ResultCallback<Void> callback);
}