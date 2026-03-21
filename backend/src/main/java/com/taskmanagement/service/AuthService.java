package com.taskmanagement.service;

import com.taskmanagement.dto.auth.AuthResponse;
import com.taskmanagement.dto.auth.LoginRequest;
import com.taskmanagement.dto.auth.RegisterRequest;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.BadRequestException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtUtil;
import com.taskmanagement.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthenticationManager authenticationManager,
                      UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.success(
                token,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl()
        );
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userDetailsService.loadUserEntityByUsername(userDetails.getUsername());

            String token = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            return AuthResponse.success(
                    token,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getAvatarUrl()
            );
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid username/email or password");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userDetailsService.loadUserEntityByUsername(username);

        String newToken = jwtUtil.generateToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.success(
                newToken,
                newRefreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl()
        );
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        String username = authentication.getName();
        return userDetailsService.loadUserEntityByUsername(username);
    }

    public AuthResponse getCurrentUserInfo() {
        User user = getCurrentUser();
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public User getUserByUsername(String username) {
        return userDetailsService.loadUserEntityByUsername(username);
    }
}
