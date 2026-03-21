package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import com.taskmanagement.service.AuthService;
import com.taskmanagement.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final PresenceService presenceService;
    private final AuthService authService;

    /**
     * Handle user joining a project's real-time channel
     */
    @MessageMapping("/project/{projectId}/join")
    public void joinProject(@DestinationVariable Long projectId,
                           SimpMessageHeaderAccessor headerAccessor,
                           Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated user tried to join project {}", projectId);
            return;
        }

        String sessionId = headerAccessor.getSessionId();
        User user = authService.getUserByUsername(principal.getName());
        
        presenceService.userJoined(sessionId, projectId, user);
        presenceService.sendPresenceList(projectId);
    }

    /**
     * Handle user leaving a project's real-time channel
     */
    @MessageMapping("/project/{projectId}/leave")
    public void leaveProject(@DestinationVariable Long projectId,
                            SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        presenceService.userLeft(sessionId);
    }

    /**
     * Get online users for a project
     */
    @MessageMapping("/project/{projectId}/presence")
    public Set<Long> getPresence(@DestinationVariable Long projectId) {
        return presenceService.getOnlineUsers(projectId);
    }
}
