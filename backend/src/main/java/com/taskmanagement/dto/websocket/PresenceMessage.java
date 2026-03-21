package com.taskmanagement.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceMessage {

    private String type;           // PRESENCE_UPDATE, USER_JOINED, USER_LEFT
    private Long projectId;
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private boolean online;
    private Set<Long> onlineUserIds; // For full presence list
    private LocalDateTime timestamp;

    public static PresenceMessage userJoined(Long projectId, Long userId, String username, String fullName, String avatarUrl) {
        return PresenceMessage.builder()
                .type("USER_JOINED")
                .projectId(projectId)
                .userId(userId)
                .username(username)
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .online(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static PresenceMessage userLeft(Long projectId, Long userId, String username) {
        return PresenceMessage.builder()
                .type("USER_LEFT")
                .projectId(projectId)
                .userId(userId)
                .username(username)
                .online(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static PresenceMessage presenceList(Long projectId, Set<Long> onlineUserIds) {
        return PresenceMessage.builder()
                .type("PRESENCE_UPDATE")
                .projectId(projectId)
                .onlineUserIds(onlineUserIds)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
