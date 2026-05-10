package com.example.cinemabookingapp.data.remote.api;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            try {
                // Synchronously get ID Token with timeout to prevent app hang
                Task<GetTokenResult> task = user.getIdToken(false);
                GetTokenResult tokenResult = com.google.android.gms.tasks.Tasks.await(task, 10, java.util.concurrent.TimeUnit.SECONDS);
                String token = tokenResult.getToken();
                
                if (token != null) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }
            } catch (Exception e) {
                android.util.Log.e("AuthInterceptor", "Error fetching Firebase token: " + e.getMessage());
            }
        }
        return chain.proceed(requestBuilder.build());
    }
}
