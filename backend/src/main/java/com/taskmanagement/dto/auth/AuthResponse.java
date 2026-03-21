package com.taskmanagement.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String type;
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String message;

    // Static factory for successful auth
    public static AuthResponse success(String token, String refreshToken, 
            Long id, String username, String email, String fullName, String avatarUrl) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(id)
                .username(username)
                .email(email)
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .build();
    }

    // Static factory for messages
    public static AuthResponse message(String message) {
        return AuthResponse.builder()
                .message(message)
                .build();
    }
}
