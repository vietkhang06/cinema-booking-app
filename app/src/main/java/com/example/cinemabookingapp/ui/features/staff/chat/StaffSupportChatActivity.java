package com.example.cinemabookingapp.ui.features.staff.chat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.ApiResponse;
import com.example.cinemabookingapp.data.dto.request.SendMessageRequest;
import com.example.cinemabookingapp.data.remote.api.ChatApiService;
import com.example.cinemabookingapp.data.remote.api.RetrofitClient;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.model.ChatMessage;
import com.example.cinemabookingapp.domain.model.Conversation;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.ui.features.chat.adapter.SupportMessageAdapter;
import com.example.cinemabookingapp.utils.DateTimeConverter;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffSupportChatActivity extends AppCompatActivity {

    // Views
    private TextView tvCustomerName;
    private TextView tvCustomerEmail;
    private TextView tvCustomerPhone;
    private ImageButton btnMoreOptions;
    private TextView tvStatusText;
    private View statusBanner;
    
    // Collapsable Info views
    private RelativeLayout layoutInfoToggle;
    private LinearLayout layoutInfoContent;
    private ImageView ivToggleArrow;
    private TextView tvBookingMovie;
    private TextView tvBookingCinema;
    private TextView tvBookingTime;
    private Chip chipPaymentStatus;
    private TextView tvNoBooking;
    private LinearLayout layoutBookingDetails;

    // Chat views
    private RecyclerView recyclerViewMessages;
    private View bottomWidgetFrame;
    private View layoutNormalInput;
    private View layoutClaimArea;
    private View layoutClosedNotice;
    private EditText etMessage;
    private ImageButton btnSend;
    private Button btnClaim;

    // Data
    private String convoId;
    private String authUserId;
    private Conversation conversation;
    private SupportMessageAdapter supportMessageAdapter;
    private final List<ChatMessage> messagesList = new ArrayList<>();
    private final ChatApiService chatApiService = RetrofitClient.getInstance().create(ChatApiService.class);
    
    private ListenerRegistration convoListener;
    private ListenerRegistration messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_support_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Current Logged In User
        User cachedProfile = ServiceProvider.getInstance().getProfileService().getCachedProfile();
        if (cachedProfile != null) {
            authUserId = cachedProfile.uid;
        } else {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                authUserId = firebaseUser.getUid();
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        convoId = getIntent().getStringExtra("convoId");
        if (convoId == null || convoId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã hội thoại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupRecyclerView();
        verifyStaffPermissions();
        setupListeners();
    }

    private void bindViews() {
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerEmail = findViewById(R.id.tvCustomerEmail);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        tvStatusText = findViewById(R.id.tvStatusText);
        statusBanner = findViewById(R.id.statusBanner);

        layoutInfoToggle = findViewById(R.id.layoutInfoToggle);
        layoutInfoContent = findViewById(R.id.layoutInfoContent);
        ivToggleArrow = findViewById(R.id.ivToggleArrow);
        tvBookingMovie = findViewById(R.id.tvBookingMovie);
        tvBookingCinema = findViewById(R.id.tvBookingCinema);
        tvBookingTime = findViewById(R.id.tvBookingTime);
        chipPaymentStatus = findViewById(R.id.chipPaymentStatus);
        tvNoBooking = findViewById(R.id.tvNoBooking);
        layoutBookingDetails = findViewById(R.id.layoutBookingDetails);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        bottomWidgetFrame = findViewById(R.id.bottomWidgetFrame);
        layoutNormalInput = findViewById(R.id.layoutNormalInput);
        layoutClaimArea = findViewById(R.id.layoutClaimArea);
        layoutClosedNotice = findViewById(R.id.layoutClosedNotice);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnClaim = findViewById(R.id.btnClaim);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        supportMessageAdapter = new SupportMessageAdapter(authUserId, true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(supportMessageAdapter);
    }

    private void verifyStaffPermissions() {
        FirebaseFirestore.getInstance().collection("users").document(authUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        String status = doc.getString("status");
                        Boolean deleted = doc.getBoolean("deleted");
                        if (!("staff".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role))
                                || "inactive".equalsIgnoreCase(status)
                                || Boolean.TRUE.equals(deleted)) {
                            Toast.makeText(this, "Tài khoản của bạn đã bị khóa hoặc không có quyền truy cập", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void setupListeners() {
        // Toggle collapsable info card
        layoutInfoToggle.setOnClickListener(v -> {
            if (layoutInfoContent.getVisibility() == View.VISIBLE) {
                layoutInfoContent.setVisibility(View.GONE);
                ivToggleArrow.setRotation(0);
            } else {
                layoutInfoContent.setVisibility(View.VISIBLE);
                ivToggleArrow.setRotation(90);
            }
        });

        // More options dropdown
        btnMoreOptions.setOnClickListener(this::showPopupMenu);

        // Claim support ticket
        btnClaim.setOnClickListener(v -> claimSupportTicket());

        // Send message
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });

        // Start listening to conversation and message changes
        addRealtimeConvoListener();
        addRealtimeMessageListener();
    }

    private void showPopupMenu(View view) {
        if (conversation == null) return;
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, view);
        
        // Add menu items dynamically based on whether conversation is active
        String status = conversation.status != null ? conversation.status : "BOT_ONLY";
        boolean isActive = "ASSIGNED_TO_STAFF".equals(status) || "IN_PROGRESS".equals(status);
        
        if (isActive) {
            popup.getMenu().add(1, 1, 1, "Giải quyết hỗ trợ (RESOLVED)");
            popup.getMenu().add(1, 2, 2, "Đóng cuộc trò chuyện (CLOSED)");
            popup.getMenu().add(1, 3, 3, "Chuyển trả về Chatbot");
        }
        popup.getMenu().add(1, 4, 4, "Xóa lịch sử tin nhắn");
        
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    new AlertDialog.Builder(this)
                            .setTitle("Giải quyết hỗ trợ")
                            .setMessage("Đánh dấu đã giải quyết xong cuộc hỗ trợ này?")
                            .setPositiveButton("Đồng ý", (d, w) -> resolveSupportTicket())
                            .setNegativeButton("Hủy", null)
                            .show();
                    return true;
                case 2:
                    new AlertDialog.Builder(this)
                            .setTitle("Đóng cuộc hỗ trợ")
                            .setMessage("Bạn có chắc muốn đóng hoàn toàn ticket này?")
                            .setPositiveButton("Đồng ý", (d, w) -> closeSupportTicket())
                            .setNegativeButton("Hủy", null)
                            .show();
                    return true;
                case 3:
                    new AlertDialog.Builder(this)
                            .setTitle("Chuyển về Chatbot")
                            .setMessage("Kết thúc hỗ trợ và chuyển giao khách hàng lại cho Chatbot?")
                            .setPositiveButton("Đồng ý", (d, w) -> returnToChatbot())
                            .setNegativeButton("Hủy", null)
                            .show();
                    return true;
                case 4:
                    new AlertDialog.Builder(this)
                            .setTitle("Xóa lịch sử")
                            .setMessage("Bạn có chắc muốn xoá toàn bộ tin nhắn của cuộc hội thoại này không?")
                            .setPositiveButton("Xóa", (d, w) -> clearChatMessages())
                            .setNegativeButton("Hủy", null)
                            .show();
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void addRealtimeConvoListener() {
        convoListener = FirebaseFirestore.getInstance().collection("conversations").document(convoId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("StaffSupportChat", "Listener error: " + error.getMessage());
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Conversation convo = snapshot.toObject(Conversation.class);
                        if (convo != null) {
                            conversation = convo;
                            updateUI();
                        }
                    }
                });
    }

    private void addRealtimeMessageListener() {
        if (messageListener != null) return;

        messageListener = FirebaseFirestore.getInstance().collection("chat_messages")
                .whereEqualTo("convoId", convoId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("StaffSupportChat", "Message listener error: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null) {
                        List<ChatMessage> allMessages = snapshot.getDocuments().stream()
                                .map(doc -> doc.toObject(ChatMessage.class))
                                .filter(java.util.Objects::nonNull)
                                .sorted((m1, m2) -> Long.compare(m1.sentAt, m2.sentAt))
                                .collect(Collectors.toList());

                        messagesList.clear();
                        messagesList.addAll(allMessages);
                        supportMessageAdapter.submitList(new ArrayList<>(messagesList));
                        recyclerViewMessages.scrollToPosition(supportMessageAdapter.getItemCount() - 1);

                        // Mark read if last message is not from current staff
                        if (!messagesList.isEmpty()) {
                            ChatMessage lastMsg = messagesList.get(messagesList.size() - 1);
                            if (!lastMsg.senderId.equals(authUserId)) {
                                markAsRead();
                            }
                        }
                    }
                });
    }

    private void updateUI() {
        if (conversation == null) return;

        String status = conversation.status != null ? conversation.status : "BOT_ONLY";

        // Find customer details from participants
        String customerId = null;
        if (conversation.participantIds != null) {
            customerId = conversation.participantIds.stream()
                    .filter(id -> !"SUPPORT_BOT".equals(id) && !id.equals(authUserId))
                    .findFirst().orElse(null);
        }

        if (customerId != null) {
            loadCustomerDetails(customerId);
            loadCustomerLatestBooking(customerId);
        }

        switch (status) {
            case "BOT_ONLY":
                tvStatusText.setText("🤖 Khách hàng đang trò chuyện với Chatbot");
                tvStatusText.setTextColor(Color.parseColor("#3F51B5"));
                statusBanner.setBackgroundColor(Color.parseColor("#E8EAF6"));

                layoutClaimArea.setVisibility(View.GONE);
                layoutNormalInput.setVisibility(View.GONE);
                layoutClosedNotice.setVisibility(View.VISIBLE);
                break;
            case "WAITING_STAFF":
                tvStatusText.setText("⏳ Đang chờ nhân viên tiếp nhận");
                tvStatusText.setTextColor(Color.parseColor("#E65100"));
                statusBanner.setBackgroundColor(Color.parseColor("#FFF3E0"));

                layoutNormalInput.setVisibility(View.GONE);
                layoutClosedNotice.setVisibility(View.GONE);
                layoutClaimArea.setVisibility(View.VISIBLE);
                break;
            case "REOPENED":
                tvStatusText.setText("🔄 Ticket hỗ trợ đã được mở lại. Đang chờ gán.");
                tvStatusText.setTextColor(Color.parseColor("#E65100"));
                statusBanner.setBackgroundColor(Color.parseColor("#FFF3E0"));

                layoutNormalInput.setVisibility(View.GONE);
                layoutClosedNotice.setVisibility(View.GONE);
                layoutClaimArea.setVisibility(View.VISIBLE);
                break;
            case "ASSIGNED_TO_STAFF":
            case "IN_PROGRESS":
                tvStatusText.setText("👥 Đang hỗ trợ khách hàng");
                tvStatusText.setTextColor(Color.parseColor("#2E7D32"));
                statusBanner.setBackgroundColor(Color.parseColor("#E8F5E9"));

                layoutClaimArea.setVisibility(View.GONE);
                layoutClosedNotice.setVisibility(View.GONE);
                layoutNormalInput.setVisibility(View.VISIBLE);
                break;
            case "RESOLVED":
                tvStatusText.setText("✅ Hỗ trợ đã hoàn thành.");
                tvStatusText.setTextColor(Color.parseColor("#37474F"));
                statusBanner.setBackgroundColor(Color.parseColor("#ECEFF1"));

                layoutClaimArea.setVisibility(View.GONE);
                layoutNormalInput.setVisibility(View.GONE);
                layoutClosedNotice.setVisibility(View.VISIBLE);
                break;
            case "CLOSED":
                tvStatusText.setText("✅ Cuộc trò chuyện đã đóng.");
                tvStatusText.setTextColor(Color.parseColor("#37474F"));
                statusBanner.setBackgroundColor(Color.parseColor("#ECEFF1"));

                layoutClaimArea.setVisibility(View.GONE);
                layoutNormalInput.setVisibility(View.GONE);
                layoutClosedNotice.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadCustomerDetails(String customerId) {
        FirebaseFirestore.getInstance().collection("users").document(customerId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String name = userDoc.getString("name");
                        String email = userDoc.getString("email");
                        String phone = userDoc.getString("phone");
                        tvCustomerName.setText(name != null ? name : "Khách hàng");
                        tvCustomerEmail.setText(email != null ? email : "");
                        tvCustomerPhone.setText(phone != null && !phone.isEmpty() ? phone : "Chưa cập nhật");
                    }
                });
    }

    private void loadCustomerLatestBooking(String customerId) {
        FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("userId", customerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot bookingDoc = querySnapshot.getDocuments().get(0);
                        layoutBookingDetails.setVisibility(View.VISIBLE);
                        tvNoBooking.setVisibility(View.GONE);

                        String movieTitle = bookingDoc.getString("movieTitleSnapshot");
                        String cinemaName = bookingDoc.getString("cinemaNameSnapshot");
                        String roomName = bookingDoc.getString("roomNameSnapshot");
                        Long showtimeStart = bookingDoc.getLong("showtimeStartAtSnapshot");
                        String paymentStatus = bookingDoc.getString("paymentStatus");

                        tvBookingMovie.setText("Phim: " + (movieTitle != null ? movieTitle : ""));
                        tvBookingCinema.setText("Rạp: " + (cinemaName != null ? cinemaName : "") + " - " + (roomName != null ? roomName : ""));

                        if (showtimeStart != null && showtimeStart > 0) {
                            tvBookingTime.setText(DateTimeConverter.convertToDateTimeString(showtimeStart));
                        } else {
                            tvBookingTime.setText("");
                        }

                        chipPaymentStatus.setText(paymentStatus != null ? paymentStatus : "PENDING");
                        if ("SUCCESS".equalsIgnoreCase(paymentStatus) || "CONFIRMED".equalsIgnoreCase(paymentStatus) || "PAID".equalsIgnoreCase(paymentStatus)) {
                            chipPaymentStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                        } else if ("FAILED".equalsIgnoreCase(paymentStatus) || "CANCELLED".equalsIgnoreCase(paymentStatus)) {
                            chipPaymentStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                        } else {
                            chipPaymentStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                        }
                    } else {
                        layoutBookingDetails.setVisibility(View.GONE);
                        tvNoBooking.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    layoutBookingDetails.setVisibility(View.GONE);
                    tvNoBooking.setVisibility(View.VISIBLE);
                    Log.e("StaffSupportChat", "Error loading booking", e);
                });
    }

    private void claimSupportTicket() {
        chatApiService.claimConversation(convoId).enqueue(new Callback<ApiResponse<Conversation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Conversation>> call, Response<ApiResponse<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    conversation = response.body().getData();
                    updateUI();
                    Toast.makeText(StaffSupportChatActivity.this, "Bạn đã tiếp nhận hỗ trợ cuộc trò chuyện này", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StaffSupportChatActivity.this, "Không thể tiếp nhận hỗ trợ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Conversation>> call, Throwable t) {
                Toast.makeText(StaffSupportChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resolveSupportTicket() {
        chatApiService.resolveConversation(convoId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(StaffSupportChatActivity.this, "Cuộc trò chuyện đã hoàn thành hỗ trợ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StaffSupportChatActivity.this, "Không thể giải quyết cuộc hỗ trợ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(StaffSupportChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeSupportTicket() {
        chatApiService.closeConversation(convoId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(StaffSupportChatActivity.this, "Ticket đã được đóng hoàn toàn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StaffSupportChatActivity.this, "Không thể đóng ticket", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(StaffSupportChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToChatbot() {
        chatApiService.returnToBot(convoId).enqueue(new Callback<ApiResponse<Conversation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Conversation>> call, Response<ApiResponse<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    conversation = response.body().getData();
                    updateUI();
                    Toast.makeText(StaffSupportChatActivity.this, "Đã chuyển cuộc gọi về lại Chatbot", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StaffSupportChatActivity.this, "Không thể chuyển về Chatbot", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Conversation>> call, Throwable t) {
                Toast.makeText(StaffSupportChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearChatMessages() {
        chatApiService.clearConversationMessages(convoId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(StaffSupportChatActivity.this, "Đã xóa toàn bộ tin nhắn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StaffSupportChatActivity.this, "Không thể xóa tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(StaffSupportChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String text) {
        if (text == null || text.isBlank() || conversation == null) return;

        SendMessageRequest req = new SendMessageRequest();
        req.content = text;
        
        // Find customer's ID to set as receiver
        String customerId = conversation.participantIds.stream()
                .filter(id -> !"SUPPORT_BOT".equals(id) && !id.equals(authUserId))
                .findFirst().orElse(null);
        
        req.receiverId = customerId != null ? customerId : "SUPPORT_BOT";

        chatApiService.sendMessage(req).enqueue(new Callback<ApiResponse<ChatMessage>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatMessage>> call, Response<ApiResponse<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    etMessage.setText(""); // Only clear on success!
                } else {
                    Toast.makeText(StaffSupportChatActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChatMessage>> call, Throwable t) {
                Toast.makeText(StaffSupportChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAsRead() {
        chatApiService.markAsRead(convoId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (convoListener != null) {
            convoListener.remove();
        }
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}
