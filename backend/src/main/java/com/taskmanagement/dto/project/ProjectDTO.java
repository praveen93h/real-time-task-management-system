package com.taskmanagement.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskmanagement.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private UserDTO owner;
    private List<ProjectMemberDTO> members;
    private Integer taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Minimal version for lists
    public static ProjectDTO minimal(Long id, String name, String description, 
            UserDTO owner, Integer taskCount, LocalDateTime createdAt) {
        return ProjectDTO.builder()
                .id(id)
                .name(name)
                .description(description)
                .owner(owner)
                .taskCount(taskCount)
                .createdAt(createdAt)
                .build();
    }
}
