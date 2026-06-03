package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.ChatMessage;
import com.cinemabooking.backend.dto.Conversation;
import com.cinemabooking.backend.dto.request.SendMessageRequest;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ChatService {

    @Autowired private Firestore firestore;
    @Autowired
    private ConversationService conversationService;

    // ─── Send a message ──────────────────────────────────────────────────────

    public ChatMessage sendMessage(String senderId, SendMessageRequest req)
            throws ExecutionException, InterruptedException {

        String receiverId = req.getReceiverId();
        long now = System.currentTimeMillis();

        // 1. Get or create conversation
        Conversation convo = conversationService.getConversationByUserIds(senderId, receiverId);

        if (convo == null) {
            convo = conversationService.createNewConversation(Arrays.asList(senderId, receiverId), now);;
        }

        // 2. Build and persist the message
        ChatMessage message = ChatMessage.builder()
                .convoId(convo.getConvoId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(req.getContent())
                .imgUrl(req.getImgUrl())
                .sentAt(now)
                .build();

        message = saveMessage(message);

        // 3. Update conversation metadata
        conversationService.updateConversationAfterMessage(
                convo.getConvoId(), message, receiverId, now);

//        // 4. Push to receiver via WebSocket: /queue/messages/{receiverId}
//        messagingTemplate.convertAndSendToUser(
//                receiverId,
//                "/queue/messages",
//                message
//        );

        log.info("Message {} sent from {} to {} in convo {}", message.getMessageId(), senderId, receiverId, convo.getConvoId());

        return message;
    }

    // ─── Fetch messages (paginated) ──────────────────────────────────────────
    public List<ChatMessage> getMessages(String convoId, int limit, Long beforeTimestamp) throws ExecutionException, InterruptedException {
        List<ChatMessage> messages = firestore.collection(ChatMessage.COLLECTION_NAME)
                .whereEqualTo("convoId", convoId)
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get().toObjects(ChatMessage.class);
        return messages;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private ChatMessage saveMessage(ChatMessage message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(ChatMessage.COLLECTION_NAME).document();
        message.setMessageId(docRef.getId());
        docRef.set(message).get();
        return message;

    }

    private void realtimeUseCase(){
        firestore.collection(ChatMessage.COLLECTION_NAME)
                .whereEqualTo("receiverId", "someUserId")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        log.error("Listen failed.", e);
                        return;
                    }

                    for (var change : snapshots.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            ChatMessage newMessage = change.getDocument().toObject(ChatMessage.class);
                            log.info("New message for user: {}", newMessage);
                        }
                    }
                });
    }

    public void deleteAllMessages() throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        firestore.collection(ChatMessage.COLLECTION_NAME).listDocuments()
                .forEach(doc -> batch.delete(doc));
        batch.commit();
    }
}