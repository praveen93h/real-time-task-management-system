package com.taskmanagement.dto.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage<T> {

    private String type;          // Event type: TASK_CREATED, TASK_UPDATED, etc.
    private String action;        // Action: CREATE, UPDATE, DELETE, STATUS_CHANGE, etc.
    private Long projectId;       // Project context
    private Long entityId;        // Entity ID that was affected
    private T payload;            // The actual data
    private String triggeredBy;   // Username who triggered the event
    private LocalDateTime timestamp;

    public static <T> WebSocketMessage<T> of(String type, String action, Long projectId, Long entityId, T payload, String triggeredBy) {
        return WebSocketMessage.<T>builder()
                .type(type)
                .action(action)
                .projectId(projectId)
                .entityId(entityId)
                .payload(payload)
                .triggeredBy(triggeredBy)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
