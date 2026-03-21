package com.taskmanagement.service;

import com.taskmanagement.dto.websocket.PresenceMessage;
import com.taskmanagement.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PresenceService {

    private final Map<Long, Set<Long>> projectPresence = new ConcurrentHashMap<>();
    private final Map<String, UserSession> sessionMap = new ConcurrentHashMap<>();

    private final WebSocketEventService webSocketEventService;

    public PresenceService(@Lazy WebSocketEventService webSocketEventService) {
        this.webSocketEventService = webSocketEventService;
    }

    /**
     * User joins a project - add to presence tracking
     */
    public void userJoined(String sessionId, Long projectId, User user) {
        sessionMap.put(sessionId, new UserSession(projectId, user.getId()));
        
        projectPresence.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet())
                .add(user.getId());
        
        log.info("User {} joined project {}", user.getUsername(), projectId);
        
        PresenceMessage message = PresenceMessage.userJoined(
                projectId, user.getId(), user.getUsername(), 
                user.getFullName(), user.getAvatarUrl());
        webSocketEventService.notifyUserJoined(projectId, message);
    }

    /**
     * User leaves a project - remove from presence tracking
     */
    public void userLeft(String sessionId) {
        UserSession session = sessionMap.remove(sessionId);
        
        if (session != null) {
            Set<Long> users = projectPresence.get(session.projectId);
            if (users != null) {
                users.remove(session.userId);
                
                if (users.isEmpty()) {
                    projectPresence.remove(session.projectId);
                }
            }
            
            log.info("User {} left project {}", session.userId, session.projectId);
            
            PresenceMessage message = PresenceMessage.userLeft(
                    session.projectId, session.userId, null);
            webSocketEventService.notifyUserLeft(session.projectId, message);
        }
    }

    /**
     * Get all online users for a project
     */
    public Set<Long> getOnlineUsers(Long projectId) {
        return projectPresence.getOrDefault(projectId, Set.of());
    }

    /**
     * Check if a user is online in a project
     */
    public boolean isUserOnline(Long projectId, Long userId) {
        Set<Long> users = projectPresence.get(projectId);
        return users != null && users.contains(userId);
    }

    /**
     * Send current presence list to a user
     */
    public void sendPresenceList(Long projectId) {
        Set<Long> onlineUsers = getOnlineUsers(projectId);
        PresenceMessage message = PresenceMessage.presenceList(projectId, onlineUsers);
        webSocketEventService.sendPresenceList(projectId, message);
    }

    // Inner class to track session data
    private record UserSession(Long projectId, Long userId) {}
}
