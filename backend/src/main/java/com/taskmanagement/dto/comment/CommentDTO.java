package com.taskmanagement.dto.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskmanagement.dto.user.UserDTO;
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
public class CommentDTO {

    private Long id;
    private String content;
    private Long taskId;
    private UserDTO user;
    private LocalDateTime createdAt;
}
