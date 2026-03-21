package com.taskmanagement.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String avatarUrl;
}
