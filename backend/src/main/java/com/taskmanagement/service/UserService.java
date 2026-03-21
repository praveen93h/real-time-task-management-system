package com.taskmanagement.service;

import com.taskmanagement.config.RedisConfig.CacheNames;
import com.taskmanagement.dto.user.ChangePasswordDTO;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.dto.user.UserProfileUpdateDTO;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.BadRequestException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Cacheable(value = CacheNames.USER_BY_ID, key = "#id")
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDTO(user);
    }

    @Cacheable(value = CacheNames.USER_BY_USERNAME, key = "#username")
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToDTO(user);
    }

    public UserDTO getCurrentUserProfile() {
        User user = authService.getCurrentUser();
        return mapToDTO(user);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.USER_BY_ID, key = "#result.id"),
        @CacheEvict(value = CacheNames.USER_BY_USERNAME, allEntries = true)
    })
    public UserDTO updateProfile(UserProfileUpdateDTO updateDTO) {
        User user = authService.getCurrentUser();

        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getFullName() != null) {
            user.setFullName(updateDTO.getFullName());
        }

        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }

        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        User user = authService.getCurrentUser();

        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    public List<UserDTO> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Search query cannot be empty");
        }

        List<User> users = userRepository.searchUsers(query.trim());
        return users.stream()
                .map(this::mapToMinimalDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchUsersExcludingCurrent(String query) {
        User currentUser = authService.getCurrentUser();
        
        List<User> users = userRepository.searchUsers(query.trim());
        return users.stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .map(this::mapToMinimalDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByIds(List<Long> ids) {
        List<User> users = userRepository.findAllByIds(ids);
        return users.stream()
                .map(this::mapToMinimalDTO)
                .collect(Collectors.toList());
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserDTO mapToMinimalDTO(User user) {
        return UserDTO.minimal(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarUrl()
        );
    }
}
