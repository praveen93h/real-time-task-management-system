package com.taskmanagement.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.enums.ProjectRole;
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
public class ProjectMemberDTO {

    private UserDTO user;
    private ProjectRole role;
    private LocalDateTime joinedAt;
}
