package com.revplay.service;

import com.revplay.dto.UserDTO;
import com.revplay.model.User;
import com.revplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.revplay.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserDTO getUserProfile(String email) {
        log.debug("Fetching profile for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(String email, String displayName, String bio) {
        log.info("Updating profile for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (displayName != null && !displayName.isBlank()) {
            user.setDisplayName(displayName);
        }
        if (bio != null) {
            user.setBio(bio);
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for: {}", email);

        return mapToDTO(updatedUser);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
