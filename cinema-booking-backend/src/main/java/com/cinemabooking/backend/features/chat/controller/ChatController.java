package com.cinemabooking.backend.features.chat.controller;

import lombok.extern.slf4j.Slf4j;

import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.features.chat.model.ChatMessage;
import com.cinemabooking.backend.features.chat.model.Conversation;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.chat.request.SendMessageRequest;
import com.cinemabooking.backend.features.chat.service.ChatService;
import com.cinemabooking.backend.features.chat.service.ConversationService;
import com.cinemabooking.backend.features.user.service.UserService;
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
    @Autowired private UserService userService;
    @Autowired private com.google.cloud.firestore.Firestore firestore;

    /**
     * POST /api/chat/messages
     * Send a message. Creates the conversation automatically if it doesn't exist.
     *
     * Body: { senderId, receiverId, content, imgUrl? }
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

    @GetMapping("/users/admin")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAdminUsers() throws ExecutionException, InterruptedException {
        List<UserDTO> adminUserIds = userService.getAllAdmins();
        return ResponseEntity.ok(
                ApiResponse.<List<UserDTO>>builder()
                    .success(true)
                    .data(adminUserIds)
                    .build());
    }

    /**
     * GET /api/chat/conversations/{convoId}/messages?limit=20&before=<timestamp>
     * Paginated message history. Newest messages come last (ascending sentAt).
     * Use `before` for cursor-based pagination (pass sentAt of oldest message in last batch).
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

    @GetMapping("/conversations/{userId}/users")
    public ResponseEntity<ApiResponse<Conversation>> getConversationByReceiverId(
            @AuthenticationPrincipal String userId,
            @PathVariable("userId") String userId2
    ) throws ExecutionException, InterruptedException {
        Conversation conversation = conversationService.getConversationByUserIds(userId, userId2);
        if(conversation == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(conversation)
                        .build()
        );
    }

    @GetMapping("/users/{userId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessagesWithUser(
            @AuthenticationPrincipal String userId,
            @PathVariable("userId") String userId2
    ) throws ExecutionException, InterruptedException {
        Conversation convo = conversationService.getConversationByUserIds(userId, userId2);
        if (convo == null) {
            return ResponseEntity.ok(ApiResponse.<List<ChatMessage>>builder()
                    .success(true)
                    .data(List.of())
                    .build());
        }
        List<ChatMessage> messages = chatService.getMessages(convo.getConvoId(), 100, null);
        return ResponseEntity.ok(
                ApiResponse.<List<ChatMessage>>builder()
                        .success(true)
                        .data(messages)
                        .build()
        );
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations(
            @AuthenticationPrincipal String userId
    ) throws ExecutionException, InterruptedException {
        List<Conversation> conversations = conversationService.getConversationsWithUserDetail(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<Conversation>>builder()
                        .success(true)
                        .data(conversations)
                        .build());
    }

    /**
     * POST /api/chat/conversations/{convoId}/read?userId=<id>
     * Mark all messages in the conversation as read for the given user.
     * Resets unreadCounts[userId] to 0 and updates lastSeenAt[userId].
     */
    @PostMapping("/conversations/{convoId}/read")
    public ResponseEntity<Void> readConversation(
            @AuthenticationPrincipal String userId,
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        conversationService.markAsRead(convoId, userId, System.currentTimeMillis());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllChatData() throws ExecutionException, InterruptedException {
        conversationService.deleteAllConversations();
        chatService.deleteAllMessages();
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("All conversations and messages deleted")
                .build());
    }

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
            if ("RESOLVED".equals(convo.getStatus()) || "CLOSED".equals(convo.getStatus())) {
                convo = chatService.returnToBot(convo.getConvoId());
            }
        }

        if (convo != null) {
            log.info("SUPPORT_INIT_STATUS={}", convo.getStatus());
            log.info("SUPPORT_INIT_STAFF={}", convo.getAssignedStaffId());
            log.info("SUPPORT_INIT_CONVO={}", convo.getConvoId());
            log.info("SUPPORT_INIT_PARTICIPANTS={}", convo.getParticipantIds());
            log.info("SUPPORT_INIT_USER_SNAPSHOTS={}", convo.getParticipants());
            log.info("SUPPORT_INIT_CREATED={}", convo.getCreatedAt());
            log.info("SUPPORT_INIT_UPDATED={}", convo.getUpdatedAt());
            try {
                com.google.cloud.firestore.DocumentSnapshot doc = firestore.collection(Conversation.COLLECTION_NAME).document(convo.getConvoId()).get().get();
                if (doc.exists()) {
                    log.info("FIRESTORE_RAW_DOCUMENT={}", doc.getData());
                } else {
                    log.info("FIRESTORE_RAW_DOCUMENT_NOT_FOUND={}", convo.getConvoId());
                }
            } catch (Exception e) {
                log.error("FIRESTORE_RAW_DOCUMENT_ERROR={}", e.getMessage());
            }
        }

        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(convo)
                        .build()
        );
    }

    @PostMapping("/support/conversations/{convoId}/escalate")
    public ResponseEntity<ApiResponse<Conversation>> escalateSupport(
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = chatService.escalateConversationToStaff(convoId);
        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(convo)
                        .build()
        );
    }

    @PostMapping("/support/conversations/{convoId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveSupport(
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        chatService.resolveConversation(convoId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Support conversation resolved")
                        .build()
        );
    }

    @GetMapping("/support/waiting")
    public ResponseEntity<ApiResponse<List<Conversation>>> getWaitingConversations() throws ExecutionException, InterruptedException {
        List<Conversation> convos = chatService.getWaitingConversations();
        return ResponseEntity.ok(
                ApiResponse.<List<Conversation>>builder()
                        .success(true)
                        .data(convos)
                        .build()
        );
    }

    @PostMapping("/support/conversations/{convoId}/claim")
    public ResponseEntity<ApiResponse<Conversation>> claimSupport(
            @AuthenticationPrincipal String adminId,
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = chatService.claimConversation(convoId, adminId);
        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(convo)
                        .build()
        );
    }

    private boolean isUserActiveAdmin(String userId) throws ExecutionException, InterruptedException {
        UserDTO user = userService.getUserById(userId);
        if (user == null) return false;
        return "admin".equalsIgnoreCase(user.getRole())
                && !"inactive".equalsIgnoreCase(user.getStatus())
                && !Boolean.TRUE.equals(user.getDeleted());
    }

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
        boolean isAdmin = isUserActiveAdmin(userId);

        if (!isParticipant && !isAdmin) {
            return ResponseEntity.status(403).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Bạn không có quyền xóa hội thoại này.")
                            .build()
            );
        }

        UserDTO user = userService.getUserById(userId);
        if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
            if ("inactive".equalsIgnoreCase(user.getStatus()) || Boolean.TRUE.equals(user.getDeleted())) {
                return ResponseEntity.status(403).body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("Tài khoản của bạn đã bị khóa hoặc không còn hoạt động.")
                                .build()
                );
            }
        }

        chatService.clearConversationMessages(convoId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã xóa toàn bộ tin nhắn thành công.")
                .build());
    }

    @PostMapping("/support/conversations/{convoId}/return-to-bot")
    public ResponseEntity<ApiResponse<Conversation>> returnToBot(
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = chatService.returnToBot(convoId);
        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(convo)
                        .build()
        );
    }

    @PostMapping("/support/conversations/{convoId}/close")
    public ResponseEntity<ApiResponse<Void>> closeConversation(
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        chatService.closeConversation(convoId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Support conversation closed")
                        .build()
        );
    }

    @PostMapping("/support/conversations/{convoId}/reopen")
    public ResponseEntity<ApiResponse<Conversation>> reopenConversation(
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = chatService.reopenConversation(convoId);
        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(convo)
                        .build()
        );
    }
}
