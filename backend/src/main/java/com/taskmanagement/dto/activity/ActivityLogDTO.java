package com.taskmanagement.dto.activity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.enums.ActivityAction;
import com.taskmanagement.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityLogDTO {

    private Long id;
    private ActivityAction action;
    private EntityType entityType;
    private Long entityId;
    private UserDTO user;
    private Long projectId;
    private Map<String, Object> details;
    private LocalDateTime createdAt;

    // Human-readable description
    private String description;
}
