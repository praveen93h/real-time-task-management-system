package com.taskmanagement.dto.user;

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
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private LocalDateTime createdAt;

    // Minimal version for lists and references
    public static UserDTO minimal(Long id, String username, String fullName, String avatarUrl) {
        return UserDTO.builder()
                .id(id)
                .username(username)
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .build();
    }
}
