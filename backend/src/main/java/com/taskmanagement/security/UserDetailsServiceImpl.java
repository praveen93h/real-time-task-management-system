package com.taskmanagement.security;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username or email: " + username)));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Transactional(readOnly = true)
    public User loadUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username or email: " + username)));
    }
}
