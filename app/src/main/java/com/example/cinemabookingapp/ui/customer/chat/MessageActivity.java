package com.example.cinemabookingapp.ui.customer.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.navigation.DataNavigator;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.request.SendMessageRequest;
import com.example.cinemabookingapp.data.remote.api.ChatApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.ChatMessage;
import com.example.cinemabookingapp.domain.model.Conversation;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.ui.customer.chat.adapter.MessageAdapter;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    // Views
    private RecyclerView recyclerViewMessages;
    private EditText etMessage;
    private ImageButton btnBack;
    private ImageButton btnSendOrMic;
    TextView tvContactName;
    ImageView ivContactAvatar;
    View onlineBadge;

    // Adapter & data
    private MessageAdapter messageAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private long nextId = 1L;

    ChatApiService chatApiService = RetrofitClient.getInstance().create(ChatApiService.class);
    Conversation conversation;
    Conversation.UserSnapShot sender, receiver;
    String authUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        conversation = DataNavigator.getInstance().<Conversation>popData(getIntent().getIntExtra("convoResourceId", 0));
        User t_receiver = DataNavigator.getInstance().<User>popData(getIntent().getIntExtra("userResourceId", 0));
        authUserId = ServiceProvider.getInstance().getProfileService().getCachedProfile().uid;
        if(conversation != null){
            onDataLoaded();
        }else{
            getConversationWithReceiverId(t_receiver.uid, new ResultCallback<Conversation>(){
                @Override
                public void onSuccess(Conversation data) {
                    conversation = data;
                    onDataLoaded();
                }

                @Override
                public void onError(String message) {
                    receiver = new Conversation.UserSnapShot(t_receiver);
                    sender = new Conversation.UserSnapShot(ServiceProvider.getInstance().getProfileService().getCachedProfile());

                    bindViews();
                    setupToolbar();
                    setupRecyclerView();
                    setupInputBar();

                    Log.e("MessageActivity", "Error getting conversation: " + message);
                }
            });
        }
    }

    private void onDataLoaded(){
        sender = conversation.participants.stream().filter(user -> user.userId.equals(authUserId)).findFirst().orElse(null);
        receiver = conversation.participants.stream().filter(user -> !user.equals(sender)).findFirst().orElse(null);
//        loadMessagesByConvoId(conversation.convoId);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupInputBar();

        addRealtimeMessageListener(conversation.convoId);
        readConversation(conversation.convoId);
    }

    private void bindViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage            = findViewById(R.id.etMessage);
        btnBack              = findViewById(R.id.btnBack);
        btnSendOrMic         = findViewById(R.id.btnSend);

        tvContactName        = findViewById(R.id.tvContactName);
        ivContactAvatar      = findViewById(R.id.avatarImageView);
        onlineBadge          = findViewById(R.id.onlineBadge);
    }

    private void bindData(){
        tvContactName.setText(receiver.name);
        Glide.with(this)
                .load(receiver.avatarUrl)
                .into(ivContactAvatar);
        onlineBadge.setVisibility(false ? View.VISIBLE : View.GONE);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        ImageButton btnVideoCall = findViewById(R.id.btnVideoCall);
        ImageButton btnVoiceCall = findViewById(R.id.btnVoiceCall);
        ImageButton btnMore      = findViewById(R.id.btnMore);

        btnVideoCall.setOnClickListener(v -> { /* handle video call */ });
        btnVoiceCall.setOnClickListener(v -> { /* handle voice call */ });
        btnMore.setOnClickListener(v -> { /* show popup menu */ });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(authUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // newest messages at the bottom

        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    public void loadMessagesByConvoId(String convoId){
        chatApiService.getMessagesByConvoId(convoId).enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatMessage>>> call, Response<ApiResponse<List<ChatMessage>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChatMessage>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<ChatMessage> fetchedMessages = apiResponse.getData();
//                        Collections.reverse(fetchedMessages);
//                        messages.clear();
//                        messages.addAll(fetchedMessages);

                        readConversation(convoId);

                        Log.i("MessageActivity", "Fetched messages: " + messages.size());
                        messageAdapter.submitList(new ArrayList<>(fetchedMessages));
                    } else {
                        Toast.makeText(MessageActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MessageActivity.this, "Failed to load messages " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatMessage>>> call, Throwable t) {}
        });
    }

    public void readConversation(String conversationId) {
        chatApiService.markAsRead(conversationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                }else{

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    private void setupInputBar() {
        // Toggle mic ↔ send icon based on whether input has text
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && !s.toString().isBlank();
//                btnSendOrMic.setImageResource(hasText ? R.drawable.ic_send : R.drawable.ic_mic);
            }
        });

        btnSendOrMic.setOnClickListener(v -> {
            String text = etMessage.getText() != null
                    ? etMessage.getText().toString().trim()
                    : "";
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });

        ImageButton btnAttach = findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(v -> { /* handle attachment */ });
    }

    // -------------------------------------------------------------------------
    // Send message
    // -------------------------------------------------------------------------

    private void sendMessage(String text) {
        if(text == null || text.isBlank()) return;

        SendMessageRequest message = new SendMessageRequest();
        message.receiverId = receiver.userId;
        message.content = text;

        chatApiService.sendMessage(message).enqueue(new Callback<ApiResponse<ChatMessage>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatMessage>> call, Response<ApiResponse<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ChatMessage> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        ChatMessage newMessage = apiResponse.getData();

                        addRealtimeMessageListener(newMessage.convoId);
                    } else {
                        // Handle API-level error (e.g., show a toast)
                    }
                } else {
                    // Handle HTTP-level error (e.g., show a toast)
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChatMessage>> call, Throwable t) {}
        });

        etMessage.setText("");
        recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    ListenerRegistration listener;
    void addRealtimeMessageListener(String convoId){
        if(listener != null)
            return;
        listener = FirebaseFirestore.getInstance().collection("chat_messages")
                .whereEqualTo("convoId", convoId)
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        // Handle error
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        // Convert snapshot to ChatMessage objects and update UI
//                        snapshot.getDocumentChanges().forEach(change -> {
//                            if(change.getType() == DocumentChange.Type.ADDED){
//                                ChatMessage message = change.getDocument().toObject(ChatMessage.class);
//                                Log.i("MessageActivity", "New message received: " + message.content + " (senderId: " + message.senderId + ")");
//                                messages.add(message);
//                                messages.add(0, message);
//                                messageAdapter.submitList(new ArrayList<>(messages));
//
//                                // mark as read when receive new message
//                                if(!message.senderId.equals(sender.userId)){
//                                    readConversation(conversation.convoId);
//                                }
//
//                                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
//                            }
//                        });

                        List<ChatMessage> newMessages = snapshot.getDocumentChanges().stream().filter(change -> change.getType() == DocumentChange.Type.ADDED)
                                .map(documentChange -> {
                                    ChatMessage message = documentChange.getDocument().toObject(ChatMessage.class);
                                    return message;
                                }).collect(Collectors.toList());

                        if(newMessages == null || newMessages.isEmpty()) return;

                        if(!newMessages.get(0).senderId.equals(sender.userId)){
                            readConversation(conversation.convoId);
                        }

                        Collections.reverse(newMessages);
                        messages.addAll(newMessages);
                        messageAdapter.submitList(new ArrayList<>(messages));

                        recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount());
                    }
                });
    }

    void getConversationWithReceiverId(String receiverId, ResultCallback<Conversation> callback){
        chatApiService.getConversationByReceiverId(receiverId).enqueue(new Callback<ApiResponse<Conversation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Conversation>> call, Response<ApiResponse<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Conversation convo = response.body().getData();
                    callback.onSuccess(convo);
                } else {
                    callback.onError("Conversation not found.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Conversation>> call, Throwable t) {
                callback.onError("Network error" + t.getMessage());
            }
        });
    }

    void removeRealtimeMessageListener(){
        if(listener != null) {
            listener.remove();
            listener = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeRealtimeMessageListener();
    }


}