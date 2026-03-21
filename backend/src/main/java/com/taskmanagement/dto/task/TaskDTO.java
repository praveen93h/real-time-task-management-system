package com.taskmanagement.dto.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.enums.TaskPriority;
import com.taskmanagement.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDTO {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long projectId;
    private UserDTO assignedTo;
    private UserDTO createdBy;
    private LocalDate dueDate;
    private Integer position;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
