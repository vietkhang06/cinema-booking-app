package com.cinemabooking.backend.features.chat.repository;

import com.cinemabooking.backend.features.chat.dto.ChatMessage;
import com.cinemabooking.backend.features.chat.dto.Conversation;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class ChatRepository {

    @Autowired
    private Firestore firestore;

    private static final String CONVO_COL = Conversation.COLLECTION_NAME;
    private static final String MSG_COL = ChatMessage.COLLECTION_NAME;

    public Conversation findConvoById(String convoId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(CONVO_COL).document(convoId).get().get();
        if (doc.exists()) {
            return doc.toObject(Conversation.class);
        }
        return null;
    }

    public void saveConvo(Conversation convo) throws ExecutionException, InterruptedException {
        firestore.collection(CONVO_COL).document(convo.getConvoId()).set(convo).get();
    }

    public void updateConvoStatus(String convoId, String status) throws ExecutionException, InterruptedException {
        firestore.collection(CONVO_COL).document(convoId).update("status", status).get();
    }

    public void updateConvoFields(String convoId, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        firestore.collection(CONVO_COL).document(convoId).update(updates).get();
    }

    public List<Conversation> findConvosByParticipantId(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(CONVO_COL)
                .whereArrayContains("participantIds", userId)
                .get()
                .get()
                .toObjects(Conversation.class);
    }

    public Conversation findConvoByParticipants(String user1, String user2) throws ExecutionException, InterruptedException {
        List<Conversation> senderConvos = firestore.collection(CONVO_COL)
                .whereArrayContains("participantIds", user1)
                .get()
                .get()
                .toObjects(Conversation.class);

        return senderConvos.stream()
                .filter(c -> c.getParticipantIds().contains(user2))
                .findFirst()
                .orElse(null);
    }

    public List<Conversation> findWaitingConvos() throws ExecutionException, InterruptedException {
        return firestore.collection(CONVO_COL)
                .whereIn("status", Arrays.asList("WAITING_STAFF", "REOPENED"))
                .get()
                .get()
                .toObjects(Conversation.class);
    }

    public int countActiveConvosForStaff(String staffId) throws ExecutionException, InterruptedException {
        return firestore.collection(CONVO_COL)
                .whereEqualTo("assignedStaffId", staffId)
                .whereIn("status", Arrays.asList("ASSIGNED_TO_STAFF", "IN_PROGRESS"))
                .get()
                .get()
                .size();
    }

    public List<ChatMessage> findMessages(String convoId, int limit) throws ExecutionException, InterruptedException {
        return firestore.collection(MSG_COL)
                .whereEqualTo("convoId", convoId)
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get()
                .toObjects(ChatMessage.class);
    }

    public ChatMessage saveMessage(ChatMessage message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(MSG_COL).document();
        message.setMessageId(docRef.getId());
        docRef.set(message).get();
        return message;
    }

    public void deleteMessagesByConvoId(String convoId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> msgDocs = firestore.collection(MSG_COL)
                .whereEqualTo("convoId", convoId)
                .get()
                .get()
                .getDocuments();

        WriteBatch batch = firestore.batch();
        for (QueryDocumentSnapshot doc : msgDocs) {
            batch.delete(doc.getReference());
        }
        batch.commit().get();
    }

    public void deleteAllConversations() {
        WriteBatch batch = firestore.batch();
        firestore.collection(CONVO_COL).listDocuments()
                .forEach(batch::delete);
        batch.commit();
    }

    public void deleteAllMessages() {
        WriteBatch batch = firestore.batch();
        firestore.collection(MSG_COL).listDocuments()
                .forEach(batch::delete);
        batch.commit();
    }

    public DocumentReference getNewConvoDocumentReference() {
        return firestore.collection(CONVO_COL).document();
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
