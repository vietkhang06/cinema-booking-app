package com.example.cinemabookingapp.ui.features.staff.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.DataNavigator;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.remote.api.ChatApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.model.Conversation;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.ProfileService;
import com.example.cinemabookingapp.ui.features.chat.CustomerChatActivity;
import com.example.cinemabookingapp.ui.features.chat.MessageActivity;
import com.example.cinemabookingapp.ui.features.chat.adapter.ActiveUserAdapter;
import com.example.cinemabookingapp.ui.features.chat.adapter.ConversationAdapter;
import com.example.cinemabookingapp.ui.features.chat.model.ConversationItem;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffCustomerChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewWaiting, recyclerViewAssigned;
    private android.view.View tvEmptyWaiting, tvEmptyAssigned;
    private TextInputEditText etSearch;
    private ImageView backBtn;

    // Data
    private ConversationAdapter waitingAdapter, assignedAdapter;
    private final List<Conversation> allConversations = new ArrayList<>();

    ProfileService profileService = ServiceProvider.getInstance().getProfileService();
    String searchQuery = "";
    private String authUserId;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_customer_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        User profile = profileService.getCachedProfile();
        if (profile != null) {
            authUserId = profile.uid;
        } else {
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null) {
                authUserId = fUser.getUid();
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        bindViews();
        setupRecyclerView();
        setupSearch();
        addRealtimeConversationListener();
    }

    private void bindViews() {
        recyclerViewWaiting = findViewById(R.id.recyclerViewWaiting);
        recyclerViewAssigned = findViewById(R.id.recyclerViewAssigned);
        tvEmptyWaiting = findViewById(R.id.tvEmptyWaiting);
        tvEmptyAssigned = findViewById(R.id.tvEmptyAssigned);
        etSearch = findViewById(R.id.etSearch);
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        waitingAdapter = new ConversationAdapter(authUserId, conversation -> {
            Intent intent = new Intent(StaffCustomerChatActivity.this, StaffSupportChatActivity.class);
            intent.putExtra("convoId", conversation.conversation.convoId);
            startActivity(intent);
        });
        recyclerViewWaiting.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWaiting.setAdapter(waitingAdapter);

        assignedAdapter = new ConversationAdapter(authUserId, conversation -> {
            Intent intent = new Intent(StaffCustomerChatActivity.this, StaffSupportChatActivity.class);
            intent.putExtra("convoId", conversation.conversation.convoId);
            startActivity(intent);
        });
        recyclerViewAssigned.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAssigned.setAdapter(assignedAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim() : "";
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        List<ConversationItem> waitingItems = allConversations.stream()
                .filter(c -> "WAITING_STAFF".equals(c.status) || "REOPENED".equals(c.status))
                .map(conversation -> new ConversationItem(conversation, false, authUserId))
                .filter(this::matchesSearch)
                .collect(Collectors.toList());

        List<ConversationItem> assignedItems = allConversations.stream()
                .filter(c -> authUserId.equals(c.assignedStaffId))
                .map(conversation -> new ConversationItem(conversation, false, authUserId))
                .filter(this::matchesSearch)
                .collect(Collectors.toList());

        waitingAdapter.submitList(waitingItems);
        assignedAdapter.submitList(assignedItems);

        tvEmptyWaiting.setVisibility(waitingItems.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmptyAssigned.setVisibility(assignedItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesSearch(ConversationItem c) {
        if (searchQuery.isEmpty()) return true;
        String q = searchQuery.toLowerCase();
        return c.getQueryString().toLowerCase().contains(q);
    }

    private void addRealtimeConversationListener(){
        listener = FirebaseFirestore.getInstance().collection("conversations")
                .whereArrayContains("participantIds", "SUPPORT_BOT")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi lắng nghe: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot != null) {
                        List<Conversation> conversations = snapshot.toObjects(Conversation.class).stream()
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        allConversations.clear();
                        allConversations.addAll(conversations);

                        applyFilters();
                    }
                });
    }

    private void removeRealtimeConversationListener(){
        if(listener != null){
            listener.remove();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRealtimeConversationListener();
    }
}