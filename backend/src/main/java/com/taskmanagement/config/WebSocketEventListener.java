package com.taskmanagement.config;

import com.taskmanagement.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.debug("WebSocket session disconnected: {}", sessionId);
        presenceService.userLeft(sessionId);
    }
}
