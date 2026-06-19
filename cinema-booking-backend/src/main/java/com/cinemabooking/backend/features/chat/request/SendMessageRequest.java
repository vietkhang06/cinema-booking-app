package com.cinemabooking.backend.features.chat.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String receiverId;
    private String content;
    private String imgUrl;
}