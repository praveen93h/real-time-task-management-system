package com.taskmanagement.service;

import com.taskmanagement.dto.websocket.EventType;
import com.taskmanagement.dto.websocket.PresenceMessage;
import com.taskmanagement.dto.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventService {

    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    /**
     * Broadcast a message to all subscribers of a project
     */
    public <T> void broadcastToProject(Long projectId, WebSocketMessage<T> message) {
        String destination = "/topic/project/" + projectId;
        log.debug("Broadcasting to {}: {}", destination, message.getType());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Send a message to a specific user
     */
    public <T> void sendToUser(String username, WebSocketMessage<T> message) {
        String destination = "/queue/notifications";
        log.debug("Sending to user {}: {}", username, message.getType());
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    // ==================== Task Events ====================

    public <T> void notifyTaskCreated(Long projectId, Long taskId, T taskData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.TASK_CREATED, "CREATE", projectId, taskId, taskData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public <T> void notifyTaskUpdated(Long projectId, Long taskId, T taskData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.TASK_UPDATED, "UPDATE", projectId, taskId, taskData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public void notifyTaskDeleted(Long projectId, Long taskId, String triggeredBy) {
        WebSocketMessage<Long> message = WebSocketMessage.of(
                EventType.TASK_DELETED, "DELETE", projectId, taskId, taskId, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public <T> void notifyTaskStatusChanged(Long projectId, Long taskId, T taskData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.TASK_STATUS_CHANGED, "STATUS_CHANGE", projectId, taskId, taskData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public <T> void notifyTaskAssigned(Long projectId, Long taskId, T taskData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.TASK_ASSIGNED, "ASSIGN", projectId, taskId, taskData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public <T> void notifyTaskPositionChanged(Long projectId, Long taskId, T taskData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.TASK_POSITION_CHANGED, "POSITION_CHANGE", projectId, taskId, taskData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    // ==================== Project Events ====================

    public <T> void notifyProjectUpdated(Long projectId, T projectData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.PROJECT_UPDATED, "UPDATE", projectId, projectId, projectData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public void notifyProjectDeleted(Long projectId, String triggeredBy) {
        WebSocketMessage<Long> message = WebSocketMessage.of(
                EventType.PROJECT_DELETED, "DELETE", projectId, projectId, projectId, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public <T> void notifyMemberAdded(Long projectId, T memberData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.MEMBER_ADDED, "ADD", projectId, null, memberData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public <T> void notifyMemberRemoved(Long projectId, Long userId, String triggeredBy) {
        WebSocketMessage<Long> message = WebSocketMessage.of(
                EventType.MEMBER_REMOVED, "REMOVE", projectId, userId, userId, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public <T> void notifyMemberRoleChanged(Long projectId, T memberData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.MEMBER_ROLE_CHANGED, "ROLE_CHANGE", projectId, null, memberData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    // ==================== Comment Events ====================

    public <T> void notifyCommentAdded(Long projectId, Long taskId, T commentData, String triggeredBy) {
        WebSocketMessage<T> message = WebSocketMessage.of(
                EventType.COMMENT_ADDED, "ADD", projectId, taskId, commentData, triggeredBy);
        broadcastToProject(projectId, message);
    }

    public void notifyCommentDeleted(Long projectId, Long taskId, Long commentId, String triggeredBy) {
        WebSocketMessage<Long> message = WebSocketMessage.of(
                EventType.COMMENT_DELETED, "DELETE", projectId, taskId, commentId, triggeredBy);
        broadcastToProject(projectId, message);
    }

    // ==================== Presence Events ====================

    public void notifyUserJoined(Long projectId, PresenceMessage presenceMessage) {
        String destination = "/topic/project/" + projectId + "/presence";
        messagingTemplate.convertAndSend(destination, presenceMessage);
    }

    public void notifyUserLeft(Long projectId, PresenceMessage presenceMessage) {
        String destination = "/topic/project/" + projectId + "/presence";
        messagingTemplate.convertAndSend(destination, presenceMessage);
    }

    public void sendPresenceList(Long projectId, PresenceMessage presenceMessage) {
        String destination = "/topic/project/" + projectId + "/presence";
        messagingTemplate.convertAndSend(destination, presenceMessage);
    }
}
