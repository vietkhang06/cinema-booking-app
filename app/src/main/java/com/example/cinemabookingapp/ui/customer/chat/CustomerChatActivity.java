package com.example.cinemabookingapp.ui.customer.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.example.cinemabookingapp.ui.customer.chat.adapter.ActiveUserAdapter;
import com.example.cinemabookingapp.ui.customer.chat.adapter.ConversationAdapter;
import com.example.cinemabookingapp.ui.customer.chat.model.ConversationItem;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerChatActivity extends AppCompatActivity {
    // Views
    private RecyclerView recyclerViewConversations, recyclerViewActiveUsers;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private ImageView backBtn;

    // Data
    private ConversationAdapter conversationAdapter;
    private ActiveUserAdapter activeUserAdapter;
    private final List<Conversation> allConversations = new ArrayList<>();
    private final List<User> activeStaffs = new ArrayList<>();

    ProfileService profileService = ServiceProvider.getInstance().getProfileService();
    DataNavigator navigator = DataNavigator.getInstance();
    ChatApiService chatApiService = RetrofitClient.getInstance().create(ChatApiService.class);

    // Active state
    private String activeFilter = "all";
    private String searchQuery  = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupRecyclerView();
        setupChipFilters();
        setupSearch();
        loadData();
    }

    private void bindViews() {
        recyclerViewConversations = findViewById(R.id.recyclerViewConversations);
        recyclerViewActiveUsers = findViewById(R.id.rvActiveUsers);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());
    }


    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(profileService.getCachedProfile().uid, conversation -> {
            Intent intent = new Intent(CustomerChatActivity.this, MessageActivity.class);
            intent.putExtra("convoResourceId", navigator.pushData(conversation.conversation));
            startActivity(intent);
        });
        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewConversations.setAdapter(conversationAdapter);
        recyclerViewConversations.setNestedScrollingEnabled(false);

        activeUserAdapter = new ActiveUserAdapter(activeStaffs);
        recyclerViewActiveUsers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewActiveUsers.setAdapter(activeUserAdapter);
        recyclerViewActiveUsers.setNestedScrollingEnabled(false);
        activeUserAdapter.setOnUserClickListener(user -> {
            Intent intent = new Intent(CustomerChatActivity.this, MessageActivity.class);
            intent.putExtra("userResourceId", navigator.pushData(user));
            startActivity(intent);
        });
    }

    private void setupChipFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chipAll)      activeFilter = "all";
            else if (id == R.id.chipUnread)   activeFilter = "unread";
            else if (id == R.id.chipOnline)   activeFilter = "online";
            applyFilters();
        });
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

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private void loadData() {
        chatApiService.getAllStaffs().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeStaffs.clear();
                    activeStaffs.addAll(response.body().getData());

                    activeUserAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CustomerChatActivity.this, "Failed to load staff data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                Toast.makeText(CustomerChatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        addRealtimeConversationListener();
    }

    // -------------------------------------------------------------------------
    // Filter + search logic
    // -------------------------------------------------------------------------

    private void applyFilters() {
        List<ConversationItem> conversationItems = allConversations.stream()
                .map(conversation -> new ConversationItem(conversation, true, profileService.getCachedProfile().uid))
                .filter(c -> matchesFilter(c) && matchesSearch(c))
                .collect(Collectors.toList());
        conversationAdapter.submitList(conversationItems);
    }

    private boolean matchesFilter(ConversationItem c) {
        switch (activeFilter) {
            case "unread":   return c.getUnreadCounts() > 0;
            case "online":   return c.isOnline;
            default:         return true;    // "all"
        }
    }

    private boolean matchesSearch(ConversationItem c) {
        if (searchQuery.isEmpty()) return true;
        String q = searchQuery.toLowerCase();
        return c.getQueryString().toLowerCase().contains(q);
    }

    ListenerRegistration listener;
    private void addRealtimeConversationListener(){
        listener = FirebaseFirestore.getInstance().collection("conversations")
                .whereArrayContains("participantIds", profileService.getCachedProfile().uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error listening for messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        Log.i("CustomerChatActivity", "New message received: " + snapshot.size());
                        List<Conversation> conversations = snapshot.toObjects(Conversation.class).stream()
                                .filter(conversation -> conversation != null && conversation.lastMessage != null)
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