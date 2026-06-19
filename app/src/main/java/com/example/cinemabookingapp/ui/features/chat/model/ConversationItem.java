package com.example.cinemabookingapp.ui.features.chat.model;

import com.example.cinemabookingapp.domain.model.Conversation;
import com.example.cinemabookingapp.domain.model.User;

public class ConversationItem {
    public Conversation conversation;
    public boolean isOnline;
    public int myUnreadCounts;
    private String authUserId;
    public ConversationItem(Conversation conversation, boolean isOnline, String authUserId) {
        this.conversation = conversation;
        this.isOnline = isOnline;

        this.authUserId = authUserId;
        Integer unread = 0;
        if (conversation.unreadCounts != null && conversation.participantIds != null) {
            String peerId = conversation.participantIds.stream()
                    .filter(id -> !id.equals(authUserId))
                    .findFirst().orElse("");
            unread = conversation.unreadCounts.get(peerId);
        }
        this.myUnreadCounts = unread != null ? unread : 0;
    }

    public int getUnreadCounts() {
        return myUnreadCounts;
    }

    public String getQueryString() {
        String name = "Khách hàng";
        if (conversation.participants != null) {
            Conversation.UserSnapShot other = conversation.participants.stream()
                    .filter(p -> !p.userId.equals(authUserId) && !"SUPPORT_BOT".equals(p.userId))
                    .findFirst().orElse(null);
            if (other == null) {
                other = conversation.participants.stream()
                        .filter(p -> !p.userId.equals(authUserId))
                        .findFirst().orElse(null);
            }
            if (other != null) {
                name = other.name;
            }
        }
        String lastContent = conversation.lastMessage != null ? conversation.lastMessage.content : "";
        return name + " " + lastContent;
    }


}
