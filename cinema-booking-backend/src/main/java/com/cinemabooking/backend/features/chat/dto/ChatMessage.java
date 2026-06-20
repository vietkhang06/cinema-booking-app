package com.cinemabooking.backend.features.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    public static final String COLLECTION_NAME = "chat_messages";

    private String messageId;
    private String convoId;
    private String senderId;
    private String receiverId;

    private String content;
    private String imgUrl;
    private Long sentAt;
}
