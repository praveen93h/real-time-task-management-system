package com.taskmanagement.dto.task;

import com.taskmanagement.enums.TaskPriority;
import com.taskmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDTO {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private Long assignedToId;

    private LocalDate dueDate;
}
