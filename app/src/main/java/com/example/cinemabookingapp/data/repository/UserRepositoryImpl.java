package com.example.cinemabookingapp.data.repository;

import android.util.Log;
import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.domain.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private static final String TAG = "UserRepositoryImpl";

    FirebaseFirestore firestore;
    private final com.example.cinemabookingapp.data.remote.api.ProfileApiService profileApi;

    public UserRepositoryImpl(){
        this.firestore = FirebaseFirestore.getInstance();
        this.profileApi = com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance().create(com.example.cinemabookingapp.data.remote.api.ProfileApiService.class);
    }
    @Override
    public void createUser(User user, ResultCallback<User> callback) {
        firestore.collection(FirestoreCollections.USERS).document(user.uid)
                .set(user)
                .addOnSuccessListener(doc -> {
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e->{
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getUserById(String uid, ResultCallback<User> callback) {
        Log.d(TAG, "Requesting profile for UID: " + uid);
        profileApi.getMyProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                int code = response.code();
                Log.d(TAG, "Response Code: " + code);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Profile fetched successfully for UID: " + uid);
                        if (callback != null) callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "API success but no data/message";
                        Log.e(TAG, "API Business Error: " + errorMsg);
                        if (callback != null) callback.onError(errorMsg);
                    }
                } else {
                    // Handle non-successful response (4xx, 5xx)
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    
                    String msg = "Lỗi hệ thống (Code: " + code + ")";
                    if (code == 401 || code == 403) {
                        msg = "Phiên đăng nhập hết hạn hoặc không có quyền truy cập.";
                    } else if (code >= 500) {
                        msg = "Máy chủ đang gặp sự cố. Vui lòng thử lại sau. (Code: " + code + ")";
                    }
                    
                    Log.e(TAG, "API Request Failed. Code: " + code + ", Error: " + errorBody);
                    if (callback != null) callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                if (callback != null) callback.onError("Không thể kết nối đến máy chủ. Vui lòng kiểm tra internet.");
            }
        });
    }

    @Override
    public void getAllUsers(ResultCallback<List<User>> callback) {

    }

    @Override
    public void updateUser(User user, ResultCallback<User> callback) {
        firestore.collection(FirestoreCollections.USERS)
                .document(user.uid)
                .set(user, SetOptions.mergeFields("phone", "avatarUrl", "name", "birthDate", "gender"))
                .addOnSuccessListener((a) -> {
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
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
