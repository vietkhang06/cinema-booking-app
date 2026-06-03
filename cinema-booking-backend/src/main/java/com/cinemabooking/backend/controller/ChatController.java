package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.ChatMessage;
import com.cinemabooking.backend.dto.Conversation;
import com.cinemabooking.backend.dto.UserDTO;
import com.cinemabooking.backend.dto.request.SendMessageRequest;
import com.cinemabooking.backend.service.ChatService;
import com.cinemabooking.backend.service.ConversationService;
import com.cinemabooking.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired private ChatService chatService;
    @Autowired private ConversationService conversationService;
    @Autowired private UserService userService;

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

    @GetMapping("/users/staff")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getStaffUsers() throws ExecutionException, InterruptedException {
        List<UserDTO> staffUserIds = userService.getAllStaffs();
        return ResponseEntity.ok(
                ApiResponse.<List<UserDTO>>builder()
                    .success(true)
                    .data(staffUserIds)
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
}
