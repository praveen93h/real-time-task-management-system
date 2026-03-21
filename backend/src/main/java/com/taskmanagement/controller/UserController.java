package com.taskmanagement.controller;

import com.taskmanagement.dto.user.ChangePasswordDTO;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.dto.user.UserProfileUpdateDTO;
import com.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        UserDTO user = userService.updateProfile(updateDTO);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(changePasswordDTO);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        List<UserDTO> users = userService.searchUsersExcludingCurrent(query);
        return ResponseEntity.ok(users);
    }
}
