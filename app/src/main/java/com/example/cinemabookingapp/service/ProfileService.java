package com.example.cinemabookingapp.service;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.request.UpdateProfileRequest;
import com.example.cinemabookingapp.data.remote.api.ProfileApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.data.repository.UserRepositoryImpl;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.domain.repository.BookingRepository;
import com.example.cinemabookingapp.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class ProfileService {

    AuthenticationService authService;
    UserRepository userRepo;
    FirebaseFirestore firestore;

    public ProfileService(){
        authService = ServiceProvider.getInstance().getAuthenticationService();
        userRepo = new UserRepositoryImpl();
        firestore = FirebaseFirestore.getInstance();
    }
    public void getUserProfile(ResultCallback<User> callback) {
        authService.getCurrentAuthUser(callback);
    }

    public User getCachedProfile() {
        return authService.getCachedUser();
    }

    public void updateUserProfile(User user, ResultCallback<User> callback){
        ProfileApiService profileApiService = RetrofitClient.getInstance().create(ProfileApiService.class);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.phone = user.phone;
        request.avatarUrl = user.avatarUrl;
        request.name = user.name;
        request.birthDate = user.birthDate;
        request.gender = user.gender;

        profileApiService.updateProfile(request).enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if(!response.body().isSuccess()){
                        User updatedUser = response.body().getData();
                        authService.setCurrentAuthUser(updatedUser);
                        callback.onSuccess(updatedUser);
                    }else {
                        callback.onError(response.body().getMessage());
                    }
                } else {
                    callback.onError("Failed to update profile");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUserTotalSpending(ResultCallback<Double> callback){
        User cached = getCachedProfile();
        if (cached == null || cached.uid == null) {
            if (callback != null) callback.onSuccess(0.0);
            return;
        }

        firestore.collection(FirestoreCollections.BOOKINGS)
                .where(com.google.firebase.firestore.Filter.and(
                        com.google.firebase.firestore.Filter.equalTo("userId", cached.uid),
                        com.google.firebase.firestore.Filter.equalTo("bookingStatus", "confirmed")
                ))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double total = querySnapshot.toObjects(Booking.class).stream()
                            .mapToDouble(b -> b.total)
                            .sum();
                    if (callback != null) callback.onSuccess(total);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSuccess(0.0); // Fallback to 0
                });
    }
}
