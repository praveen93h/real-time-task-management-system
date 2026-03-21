package com.taskmanagement.dto.project;

import com.taskmanagement.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRoleDTO {

    @NotNull(message = "Role is required")
    private ProjectRole role;
}
