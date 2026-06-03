package com.example.cinemabookingapp.ui.customer.chat.model;

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
        Integer unreadCounts = conversation.unreadCounts.get(conversation.participantIds.stream().filter(id -> !id.equals(authUserId)).findFirst().orElse(""));
        this.myUnreadCounts = unreadCounts != null ? unreadCounts : 0;
    }

    public int getUnreadCounts() {
        return myUnreadCounts;
    }

    public String getQueryString() {
        return conversation.participants.stream().filter(p -> !p.userId.equals(authUserId)).findFirst().orElseThrow().name + " "
                + conversation.lastMessage.content;
    }


}
