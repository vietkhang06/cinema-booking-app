package com.example.cinemabookingapp.service;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.cinemabookingapp.core.constants.UserRoles;
import com.example.cinemabookingapp.core.session.SessionManager;
import com.example.cinemabookingapp.data.repository.UserRepositoryImpl;
import com.example.cinemabookingapp.domain.common.AuthCallback;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
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

    public void signInWithEmailAndPassword(
            @NonNull String email,
            @NonNull String password,
            boolean isRemember,
            ResultCallback<User> callback
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
                            String role = data.role != null ? data.role : UserRoles.CUSTOMER ;

                            sessionManager.saveLoginState(true, role, uid);
                            if (isRemember) {
                                sessionManager.saveRememberedEmail(email);
                            }

                            callback.onSuccess(data);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }

                    });
//                    return firestore.collection("users")
//                            .document(uid)
//                            .get()
//                            .continueWith(dbTask -> {
//                                if (!dbTask.isSuccessful()) {
//                                    Exception e = dbTask.getException();
//                                    throw e != null ? e : new Exception("Lỗi lấy dữ liệu người dùng");
//                                }
//
//                                DocumentSnapshot doc = dbTask.getResult();
//                                if (!doc.exists()) {
//                                    throw new Exception("Không tìm thấy user.");
//                                }
//
//                                String role = doc.getString("role");
//                                if (role == null) role = "customer";
//
//                                callback.onSuccess(doc.toObject(User.class));
//
//                                sessionManager.saveLoginState(true, role, uid);
//
//                                if (isRemember) {
//                                    sessionManager.saveRememberedEmail(email);
//                                }
//
//                                return authResult;
//                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void signUpWithEmailAndPassword(
            @NonNull String email,
            @NonNull String password,
            @NonNull String phone,
            ResultCallback<User> callback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    userRepo.createUser(newUserDoc(uid, email, phone), new ResultCallback<User>() {
                        @Override
                        public void onSuccess(User data) {
                            if (data == null) {
                                callback.onError("Lỗi khởi tạo người dùng.");
                                return;
                            }

                            sessionManager.saveLoginState(true, "customer", uid);
                            sessionManager.saveRememberedEmail(email);
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
//                    firestore.collection(FirestoreCollections.USERS)
//                            .add(createUser(uid, email, phone))
//                            .continueWith(dbTask -> {
//                                if (!dbTask.isSuccessful()) {
//                                    Exception e = dbTask.getException();
//                                    throw e != null ? e : new Exception("Lỗi lưu thông tin người dùng");
//                                }
//
//                                sessionManager.saveLoginState(true, "customer", uid);
//                                sessionManager.saveRememberedEmail(email);
//
//                                return authResult;
//                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    private User newUserDoc(String uid, String email, String phone) {
        User user = new User();
        user.uid = uid;
        user.email = email;
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

    public void signInWithFacebook(String idToken, AuthCallback callback) {
        AuthCredential credential = FacebookAuthProvider.getCredential(idToken);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Google login failed";
                        callback.onFailure(error);
                    }
                });
    }

    public void signInWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Google login failed";
                        callback.onFailure(error);
                    }
                });
    }

    public Task<Void> forgetAndResetPassword(
        @NonNull String email
    ){
        return auth.sendPasswordResetEmail(email);
    }

    public void logOut(){
        auth.signOut();
        sessionManager.logout();
    }
}
