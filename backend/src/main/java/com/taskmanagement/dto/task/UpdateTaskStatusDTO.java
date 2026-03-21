package com.taskmanagement.dto.task;

import com.taskmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusDTO {

    @NotNull(message = "Status is required")
    private TaskStatus status;

    // Optional: new position after status change (for drag-drop ordering)
    private Integer position;
}
