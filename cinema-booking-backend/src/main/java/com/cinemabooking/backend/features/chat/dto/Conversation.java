package com.cinemabooking.backend.features.chat.dto;

import com.cinemabooking.backend.features.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    public static final String COLLECTION_NAME = "conversations";

    private String convoId;
    private List<String> participantIds;
    private List<UserSnapShot> participants;

    private Long lastMessageAt;
    private ChatMessage lastMessage;

    private Map<String, Integer> unreadCounts;
    private Map<String, Long> lastSeenAt;

    private String status; // "BOT_ONLY", "WAITING_STAFF", "ASSIGNED_TO_STAFF", "IN_PROGRESS", "RESOLVED", "CLOSED"
    private String assignedStaffId;
    private Boolean hadStaff;

    private Long createdAt;
    private Long updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSnapShot {
        private String userId;
        private String name;
        private String email;
        private String avatarUrl;

        public static UserSnapShot mapper(UserDTO userDTO) {
            return UserSnapShot.builder()
                    .userId(userDTO.getUid())
                    .name(userDTO.getName())
                    .email(userDTO.getEmail())
                    .avatarUrl(userDTO.getPhone())
                    .build();
        }
    }
}
