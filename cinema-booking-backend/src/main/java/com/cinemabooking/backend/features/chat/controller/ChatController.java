package com.cinemabooking.backend.features.chat.controller;

import lombok.extern.slf4j.Slf4j;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.chat.model.ChatMessage;
import com.cinemabooking.backend.features.chat.model.Conversation;
import com.cinemabooking.backend.features.chat.request.SendMessageRequest;
import com.cinemabooking.backend.features.chat.service.ChatService;
import com.cinemabooking.backend.features.chat.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired private ChatService chatService;
    @Autowired private ConversationService conversationService;
    @Autowired private com.google.cloud.firestore.Firestore firestore;

    /**
     * POST /api/v1/chat/messages
     * Gửi tin nhắn đến ChatBot.
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(
            @AuthenticationPrincipal String userId,
            @RequestBody SendMessageRequest req
    ) throws ExecutionException, InterruptedException {
        ChatMessage message = chatService.sendMessage(userId, req);
        return ResponseEntity.ok(
                ApiResponse.<ChatMessage>builder()
                    .success(true)
                    .data(message)
                    .build()
        );
    }

    /**
     * GET /api/v1/chat/conversations/{convoId}/messages?limit=20
     * Lịch sử hội thoại.
     */
    @GetMapping("/conversations/{convoId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(
            @AuthenticationPrincipal String userId,
            @PathVariable String convoId,
            @RequestParam(defaultValue = "20") int limit
    ) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(
                ApiResponse.<List<ChatMessage>>builder()
                        .success(true)
                        .data(chatService.getMessages(convoId, limit, null))
                        .build()
        );
    }

    /**
     * DELETE /api/v1/chat
     * Xóa toàn bộ dữ liệu Chat (Dev Utility).
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllChatData() throws ExecutionException, InterruptedException {
        conversationService.deleteAllConversations();
        chatService.deleteAllMessages();
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("All conversations and messages deleted")
                .build());
    }

    /**
     * POST /api/v1/chat/support/init
     * Khởi tạo hội thoại với Bot hỗ trợ cho User.
     */
    @PostMapping("/support/init")
    public ResponseEntity<ApiResponse<Conversation>> initSupportConversation(
            @AuthenticationPrincipal String userId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = conversationService.getConversationByUserIds(userId, "SUPPORT_BOT");
        if (convo == null) {
            convo = conversationService.createSupportConversation(userId, System.currentTimeMillis());
        } else {
            if (convo.getStatus() == null) {
                convo.setStatus("BOT_ONLY");
                firestore.collection(Conversation.COLLECTION_NAME).document(convo.getConvoId()).update("status", "BOT_ONLY").get();
            }
        }
        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(convo)
                        .build()
        );
    }

    /**
     * DELETE /api/v1/chat/conversations/{convoId}/messages
     * Xóa sạch tin nhắn của cuộc trò chuyện.
     */
    @DeleteMapping("/conversations/{convoId}/messages")
    public ResponseEntity<ApiResponse<Void>> clearConversationMessages(
            @AuthenticationPrincipal String userId,
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = conversationService.getConversationById(convoId);
        if (convo == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isParticipant = convo.getParticipantIds().contains(userId);
        if (!isParticipant) {
            return ResponseEntity.status(403).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Bạn không có quyền xóa hội thoại này.")
                            .build()
            );
        }

        chatService.clearConversationMessages(convoId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã xóa toàn bộ tin nhắn thành công.")
                .build());
    }
}
