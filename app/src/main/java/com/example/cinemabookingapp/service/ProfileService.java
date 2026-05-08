package com.example.cinemabookingapp.service;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
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
        userRepo.updateUser(user, new ResultCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authService.setCurrentAuthUser(data);
                if (callback != null) callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onError(message);
            }
        });
    }

    public double getUserTotalSpending(){
        try {
            QuerySnapshot querySnapshot = Tasks.await(
                    firestore.collection(FirestoreCollections.BOOKINGS)
                            .where(Filter.and(
                                    Filter.equalTo("userId", getCachedProfile().uid),
                                    Filter.equalTo("bookingStatus", "confirmed")
                            )).get()
            );
            return querySnapshot.toObjects(Booking.class).stream()
                    .mapToDouble(b -> b.total)
                    .sum();
        }catch (Exception ignored){ }
        return 0;
    }
}
