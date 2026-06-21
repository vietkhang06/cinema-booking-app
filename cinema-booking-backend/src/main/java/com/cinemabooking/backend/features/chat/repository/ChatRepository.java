package com.cinemabooking.backend.features.chat.repository;

import com.cinemabooking.backend.features.chat.model.ChatMessage;
import com.cinemabooking.backend.features.chat.model.Conversation;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class ChatRepository {

    @Autowired
    private Firestore firestore;

    public Conversation getConversationById(String convoId) throws ExecutionException, InterruptedException {
        var doc = firestore.collection(Conversation.COLLECTION_NAME).document(convoId).get().get();
        if (doc.exists()) {
            return doc.toObject(Conversation.class);
        }
        return null;
    }

    public List<Conversation> getConversationsByParticipant(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(Conversation.COLLECTION_NAME)
                .whereArrayContains("participantIds", userId)
                .get().get().toObjects(Conversation.class);
    }

    public void saveConversation(Conversation conversation) throws ExecutionException, InterruptedException {
        firestore.collection(Conversation.COLLECTION_NAME)
                .document(conversation.getConvoId())
                .set(conversation)
                .get();
    }

    public void updateConversation(String convoId, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        firestore.collection(Conversation.COLLECTION_NAME)
                .document(convoId)
                .update(updates)
                .get();
    }

    public List<Conversation> getWaitingConversations() throws ExecutionException, InterruptedException {
        return firestore.collection(Conversation.COLLECTION_NAME)
                .whereIn("status", List.of("WAITING_STAFF", "REOPENED"))
                .get().get().toObjects(Conversation.class);
    }

    public void saveMessage(ChatMessage message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(ChatMessage.COLLECTION_NAME).document();
        message.setMessageId(docRef.getId());
        docRef.set(message).get();
    }

    public List<ChatMessage> getMessages(String convoId, int limit) throws ExecutionException, InterruptedException {
        return firestore.collection(ChatMessage.COLLECTION_NAME)
                .whereEqualTo("convoId", convoId)
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get().toObjects(ChatMessage.class);
    }

    public List<QueryDocumentSnapshot> getMessageDocumentsByConvoId(String convoId) throws ExecutionException, InterruptedException {
        return firestore.collection(ChatMessage.COLLECTION_NAME)
                .whereEqualTo("convoId", convoId)
                .get().get().getDocuments();
    }

    public void deleteAllMessages() throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        firestore.collection(ChatMessage.COLLECTION_NAME).listDocuments()
                .forEach(batch::delete);
        batch.commit().get();
    }

    public void deleteAllConversations() throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        firestore.collection(Conversation.COLLECTION_NAME).listDocuments()
                .forEach(batch::delete);
        batch.commit().get();
    }

    public DocumentReference getConversationReference(String convoId) {
        return firestore.collection(Conversation.COLLECTION_NAME).document(convoId);
    }

    public DocumentReference createConversationReference() {
        return firestore.collection(Conversation.COLLECTION_NAME).document();
    }

    public int countActiveConversationsByStaff(String staffId) throws ExecutionException, InterruptedException {
        return firestore.collection(Conversation.COLLECTION_NAME)
                .whereEqualTo("assignedStaffId", staffId)
                .whereIn("status", List.of("ASSIGNED_TO_STAFF", "IN_PROGRESS"))
                .get().get().size();
    }

    public void clearConversationMessages(String convoId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> msgDocs = getMessageDocumentsByConvoId(convoId);
        WriteBatch batch = firestore.batch();
        for (QueryDocumentSnapshot doc : msgDocs) {
            batch.delete(doc.getReference());
        }
        batch.commit().get();

        firestore.collection(Conversation.COLLECTION_NAME).document(convoId)
                .update("lastMessage", null, "lastMessageAt", null).get();
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
