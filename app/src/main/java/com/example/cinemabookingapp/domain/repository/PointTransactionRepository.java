package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.PointTransaction;

import java.util.List;

public interface PointTransactionRepository {
    void createTransaction(PointTransaction transaction, ResultCallback<PointTransaction> callback);
    void getTransactionById(String transactionId, ResultCallback<PointTransaction> callback);
    void getTransactionsByUserId(String userId, ResultCallback<List<PointTransaction>> callback);
    void getTransactionsByRefId(String refId, ResultCallback<List<PointTransaction>> callback);
    void getAllTransactions(ResultCallback<List<PointTransaction>> callback);
}