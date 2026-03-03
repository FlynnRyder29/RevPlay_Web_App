package com.revplay.util;

import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.User;
import com.revplay.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "username", username));
    }
}