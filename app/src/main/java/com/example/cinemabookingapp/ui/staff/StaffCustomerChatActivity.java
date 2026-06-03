package com.example.cinemabookingapp.ui.staff;

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
import com.example.cinemabookingapp.ui.customer.chat.CustomerChatActivity;
import com.example.cinemabookingapp.ui.customer.chat.MessageActivity;
import com.example.cinemabookingapp.ui.customer.chat.adapter.ActiveUserAdapter;
import com.example.cinemabookingapp.ui.customer.chat.adapter.ConversationAdapter;
import com.example.cinemabookingapp.ui.customer.chat.model.ConversationItem;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
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

    private RecyclerView recyclerViewConversations, recyclerViewActiveUsers;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private ImageView backBtn;

    // Data
    private ConversationAdapter conversationAdapter;
    private final List<Conversation> allConversations = new ArrayList<>();
    private final Map<String, Conversation> conversationMap = new HashMap<>();

    ProfileService profileService = ServiceProvider.getInstance().getProfileService();
    DataNavigator navigator = DataNavigator.getInstance();
    ChatApiService chatApiService = RetrofitClient.getInstance().create(ChatApiService.class);

    String searchQuery = "";

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

        bindViews();
        setupRecyclerView();
        setupSearch();
        addRealtimeConversationListener();
    }

    private void bindViews() {
        recyclerViewConversations = findViewById(R.id.recyclerViewConversations);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());
    }


    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(profileService.getCachedProfile().uid, conversation -> {
            Intent intent = new Intent(StaffCustomerChatActivity.this, MessageActivity.class);
            intent.putExtra("convoResourceId", navigator.pushData(conversation.conversation));
            startActivity(intent);
        });
        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewConversations.setAdapter(conversationAdapter);
        recyclerViewConversations.setNestedScrollingEnabled(false);
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
        chatApiService.getMyConversations().enqueue(new Callback<ApiResponse<List<Conversation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Conversation>>> call, Response<ApiResponse<List<Conversation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Conversation> conversations = response.body().getData();
                    allConversations.clear();
                    allConversations.addAll(conversations);

                    List<ConversationItem> conversationItems = allConversations.stream()
                            .map(conversation -> new ConversationItem(conversation, false, profileService.getCachedProfile().uid))
                            .collect(Collectors.toList());
                    conversationAdapter.submitList(conversationItems);

                } else {
                    Toast.makeText(StaffCustomerChatActivity.this, "Failed to load conversations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Conversation>>> call, Throwable t) {
                Toast.makeText(StaffCustomerChatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Filter + search logic
    // -------------------------------------------------------------------------

    private void applyFilters() {
        List<ConversationItem> conversationItems = allConversations.stream()
                .map(conversation -> new ConversationItem(conversation, false, profileService.getCachedProfile().uid))
                .filter(c -> matchesSearch(c))
                .collect(Collectors.toList());

        conversationAdapter.submitList(conversationItems);
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
                        Log.i("StaffCustomerChatActivity", "Realtime convo listener: " + snapshot.size() + " docs");
                        snapshot.getDocuments().forEach(doc -> Log.i("StaffCustomerChatActivity", " - " + doc.getId() + ": " + doc.getData()));
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