package com.cinemabooking.backend.features.chat.service;

import com.cinemabooking.backend.features.chat.dto.ChatMessage;
import com.cinemabooking.backend.features.chat.dto.Conversation;
import com.cinemabooking.backend.features.chat.repository.ChatRepository;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.repository.UserRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConversationService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Conversation> getConversationsWithUserDetail(String userId) throws ExecutionException, InterruptedException {
        List<Conversation> conversations = chatRepository.findConvosByParticipantId(userId);

        conversations.sort((c1, c2) -> {
            Long t1 = c1.getLastMessageAt() != null ? c1.getLastMessageAt() : 0L;
            Long t2 = c2.getLastMessageAt() != null ? c2.getLastMessageAt() : 0L;
            return t2.compareTo(t1);
        });

        return conversations;
    }

    public Conversation createNewConversation(List<String> users, long now) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = chatRepository.getNewConvoDocumentReference();

        Map<String, Integer> unreadCounts = new HashMap<>();
        for (String user : users) {
            unreadCounts.put(user, 0);
        }

        Map<String, Long> lastSeenAt = new HashMap<>();
        for (String user : users) {
            lastSeenAt.put(user, now);
        }

        List<Conversation.UserSnapShot> userSnapshots = userRepository.findUsersByIds(users)
                .stream()
                .map(Conversation.UserSnapShot::mapper)
                .collect(Collectors.toList());

        Conversation conversation = Conversation.builder()
                .convoId(documentReference.getId())
                .participantIds(users)
                .unreadCounts(unreadCounts)
                .lastSeenAt(lastSeenAt)
                .participants(userSnapshots)
                .createdAt(now)
                .updatedAt(now)
                .build();

        chatRepository.saveConvo(conversation);

        return conversation;
    }

    public void updateConversationAfterMessage(
            String convoId, ChatMessage message, String receiverId, long timestamp
    ) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", message);
        updates.put("lastMessageAt", message.getSentAt());
        updates.put("updatedAt", timestamp);
        updates.put("unreadCounts." + receiverId, FieldValue.increment(1));

        chatRepository.updateConvoFields(convoId, updates);
    }

    public void markAsRead(String convoId, String userId, long timestamp) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("unreadCounts." + userId, 0);
        updates.put("lastSeenAt." + userId, timestamp);

        chatRepository.updateConvoFields(convoId, updates);
    }

    public Conversation getConversationByUserIds(String user1, String user2) throws ExecutionException, InterruptedException {
        return chatRepository.findConvoByParticipants(user1, user2);
    }

    public void deleteAllConversations() {
        chatRepository.deleteAllConversations();
    }

    public Conversation createSupportConversation(String customerId, long now) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = chatRepository.getNewConvoDocumentReference();

        List<String> users = Arrays.asList(customerId, "SUPPORT_BOT");

        Map<String, Integer> unreadCounts = new HashMap<>();
        unreadCounts.put(customerId, 0);
        unreadCounts.put("SUPPORT_BOT", 0);

        Map<String, Long> lastSeenAt = new HashMap<>();
        lastSeenAt.put(customerId, now);
        lastSeenAt.put("SUPPORT_BOT", now);

        List<Conversation.UserSnapShot> userSnapshots = new ArrayList<>();
        DocumentSnapshot customerDoc = userRepository.findById(customerId);
        if (customerDoc.exists()) {
            UserDTO customerDTO = customerDoc.toObject(UserDTO.class);
            if (customerDTO != null) {
                userSnapshots.add(Conversation.UserSnapShot.mapper(customerDTO));
            }
        }

        userSnapshots.add(Conversation.UserSnapShot.builder()
                .userId("SUPPORT_BOT")
                .name("Trợ lý ảo (Bot)")
                .email("bot@cinemabooking.com")
                .avatarUrl("bot_avatar")
                .build());

        Conversation conversation = Conversation.builder()
                .convoId(documentReference.getId())
                .participantIds(users)
                .unreadCounts(unreadCounts)
                .lastSeenAt(lastSeenAt)
                .participants(userSnapshots)
                .status("BOT_ONLY")
                .createdAt(now)
                .updatedAt(now)
                .build();

        chatRepository.saveConvo(conversation);

        ChatMessage welcomeMessage = ChatMessage.builder()
                .convoId(conversation.getConvoId())
                .senderId("SUPPORT_BOT")
                .receiverId(customerId)
                .content("Xin chào! Tôi là Trợ lý ảo của CinemaBookingApp. Tôi có thể giúp gì cho bạn hôm nay? Bạn có thể chọn các câu hỏi thường gặp bên dưới hoặc nhập câu hỏi trực tiếp.")
                .sentAt(now)
                .build();

        chatRepository.saveMessage(welcomeMessage);

        conversation.setLastMessage(welcomeMessage);
        conversation.setLastMessageAt(now);
        chatRepository.saveConvo(conversation);

        return conversation;
    }

    public Conversation getConversationById(String convoId) throws ExecutionException, InterruptedException {
        return chatRepository.findConvoById(convoId);
    }

    public void updateConversationStatus(String convoId, String status) throws ExecutionException, InterruptedException {
        chatRepository.updateConvoStatus(convoId, status);
    }
}
