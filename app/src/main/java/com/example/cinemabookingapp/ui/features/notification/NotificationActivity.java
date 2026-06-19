package com.example.cinemabookingapp.ui.features.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.repository.NotificationRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }
    private RecyclerView rvNotification;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private NotificationAdapter adapter;
    private ListenerRegistration listenerRegistration;

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvNotification = findViewById(R.id.rv_notifications);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);

        rvNotification.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            if (!notification.isRead) {
                // Gọi API cập nhật isRead = true trên Firestore
                // Giao diện sẽ tự nhận realtime và đổi màu nền về Trắng
                new NotificationRepositoryImpl().markAsRead(notification.notificationId, null);
            }
        });
        rvNotification.setAdapter(adapter);
    }

    private void bindActions() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        progressBar.setVisibility(View.VISIBLE);
        
        listenerRegistration = new NotificationRepositoryImpl().listenToUserNotifications(userId, new ResultCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                progressBar.setVisibility(View.GONE);
                if (result == null || result.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvNotification.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvNotification.setVisibility(View.VISIBLE);
                    adapter.setNotifications(result);
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // CỰC KỲ QUAN TRỌNG: Gỡ bỏ Listener để chống rò rỉ bộ nhớ (Memory Leak)
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}