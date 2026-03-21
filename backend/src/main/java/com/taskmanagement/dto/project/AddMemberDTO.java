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
public class AddMemberDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Builder.Default
    private ProjectRole role = ProjectRole.MEMBER;
}
