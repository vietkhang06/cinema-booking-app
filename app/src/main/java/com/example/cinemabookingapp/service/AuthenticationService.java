package com.example.cinemabookingapp.service;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.credentials.Credential;
import android.credentials.GetCredentialResponse;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.CustomCredential;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.core.constants.UserRoles;
import com.example.cinemabookingapp.core.session.SessionManager;
import com.example.cinemabookingapp.data.repository.UserRepositoryImpl;
import com.example.cinemabookingapp.domain.common.AuthCallback;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.domain.repository.UserRepository;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthenticationService {
    private final FirebaseAuth auth;
    private final SessionManager sessionManager;

    UserRepository userRepo;

    public AuthenticationService(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.sessionManager = new SessionManager(context);

        userRepo = new UserRepositoryImpl();
    }

    public void getCurrentAuthUser(){
        userRepo.getUserById(auth.getUid(), new ResultCallback<User>() {
            @Override
            public void onSuccess(User data) {
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    public void signInWithEmailAndPassword(
            @NonNull String email,
            @NonNull String password,
            boolean isRemember,
            AuthCallback callback
    ) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    userRepo.getUserById(uid, new ResultCallback<User>() {
                        @Override
                        public void onSuccess(User data) {
                            if(data == null){
                                callback.onError("Không tìm thấy user.");
                                return;
                            }

                            callback.onSuccess(data);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }

                    });
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void signUpWithEmailAndPassword(
            @NonNull String email,
            @NonNull String password,
            @NonNull String phone,
            AuthCallback callback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    userRepo.createUser(newUserDoc(authResult.getUser(), phone), new ResultCallback<User>() {
                        @Override
                        public void onSuccess(User data) {
                            if (data == null) {
                                callback.onError("Lỗi khởi tạo người dùng.");
                                return;
                            }
                            callback.onSuccess(data);
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void handleFacebookAccessToken(AccessToken token, AuthCallback callback) {
        Log.d("FacebookAuth", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
            .addOnCompleteListener( task ->  {
                Log.i("FacebookAuth", task.toString());
                if (task.isSuccessful()) {
                    Log.i("FacebookAuth", "Sign in with credential.");
                    FirebaseUser authUser = auth.getCurrentUser();
                    FirebaseFirestore.getInstance().collection(FirestoreCollections.USERS)
                        .document(authUser.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if(!documentSnapshot.exists()){
                                userRepo.createUser(newUserDoc(authUser, null), new ResultCallback<User>() {
                                    @Override
                                    public void onSuccess(User data) {
                                        callback.onSuccess(data);
                                    }

                                    @Override
                                    public void onError(String message) {
                                    }
                                });
                                return;
                            }
                            callback.onSuccess(documentSnapshot.toObject(User.class));
                        });
                } else {
                    callback.onError(task.getException().getMessage());
                }

            });
    }

    public void signInWithGoogle(Credential credential, AuthCallback callback) {
        if (credential instanceof CustomCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());

            AuthCredential authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.getIdToken(), null);


            auth.signInWithCredential(authCredential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser authUser = authResult.getUser();
                    // create user doc if not exist
                    FirebaseFirestore.getInstance().collection(FirestoreCollections.USERS)
                        .document(authUser.getUid())
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if(!documentSnapshot.exists()){
                                userRepo.createUser(newUserDoc(authUser, null), new ResultCallback<User>() {
                                    @Override
                                    public void onSuccess(User data) {
                                        callback.onSuccess(data);
                                    }

                                    @Override
                                    public void onError(String message) {
                                    }
                                });
                                return;
                            }
                            callback.onSuccess(documentSnapshot.toObject(User.class));
                        });
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e != null ? e.getMessage() : "Google login failed.");
                    });
        }
    }

    private User newUserDoc(@Nullable FirebaseUser fUser, @Nullable String phone) {
        if(fUser == null) return null;

        User user = new User();
        user.uid = fUser.getUid();
        user.email = fUser.getEmail();
        user.phone = phone;
        user.role = UserRoles.CUSTOMER;
        user.status = "active";
        user.memberLevel = "basic";
        user.createdAt = System.currentTimeMillis();
        user.updatedAt = System.currentTimeMillis();
        return user;
//        Map<String, Object> userData = new HashMap<>();
//        userData.put("uid", uid);
//        userData.put("name", "");
//        userData.put("email", email);
//        userData.put("phone", phone);
//        userData.put("avatarUrl", "");
//        userData.put("role", "customer");
//        userData.put("status", "active");
//        userData.put("memberLevel", "basic");
//        userData.put("points", 0);
//        userData.put("fcmToken", "");
//        userData.put("createdAt", System.currentTimeMillis());
//        userData.put("updatedAt", System.currentTimeMillis());
//        userData.put("deleted", false);
    }


    public Task<Void> forgetAndResetPassword(
        @NonNull String email
    ){
        return auth.sendPasswordResetEmail(email);
    }

    public void logOut(){
        auth.signOut();
    }
}
