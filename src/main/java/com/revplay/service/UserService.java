package com.revplay.service;

import com.revplay.dto.ArtistRequestDTO;
import com.revplay.dto.UserDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.ArtistRequest;
import com.revplay.model.RequestStatus;
import com.revplay.model.Role;
import com.revplay.model.User;
import com.revplay.repository.ArtistRequestRepository;
import com.revplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ArtistRequestRepository artistRequestRepository;

    // ═══════════════════════════════════════════
    // EXISTING METHODS (unchanged)
    // ═══════════════════════════════════════════

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

    @Transactional
    public void updateProfilePicture(String email, String pictureUrl) {
        log.info("Updating profile picture for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        user.setProfilePictureUrl(pictureUrl);
        userRepository.save(user);
        log.info("Profile picture updated for: {}", email);
    }

    // ═══════════════════════════════════════════
    // NEW — ARTIST UPGRADE REQUEST
    // ═══════════════════════════════════════════

    @Transactional
    public ArtistRequestDTO submitArtistRequest(String email, String artistName, String genre, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Already an artist
        if (user.getRole() == Role.ARTIST || user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("You are already an " + user.getRole().name().toLowerCase());
        }

        // Already has a pending request
        if (artistRequestRepository.existsByUserIdAndStatus(user.getId(), RequestStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending upgrade request");
        }

        ArtistRequest request = ArtistRequest.builder()
                .userId(user.getId())
                .artistName(artistName != null && !artistName.isBlank() ? artistName.trim() :
                        (user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()))
                .genre(genre != null && !genre.isBlank() ? genre.trim() : null)
                .reason(reason != null && !reason.isBlank() ? reason.trim() : null)
                .status(RequestStatus.PENDING)
                .build();

        ArtistRequest saved = artistRequestRepository.save(request);
        log.info("Artist upgrade request submitted by user {} ({})", user.getId(), user.getUsername());

        return mapToRequestDTO(saved, user);
    }

    public ArtistRequestDTO getMyArtistRequest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return artistRequestRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(req -> mapToRequestDTO(req, user))
                .orElse(null);
    }

    // ═══════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════

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

    private ArtistRequestDTO mapToRequestDTO(ArtistRequest req, User user) {
        return ArtistRequestDTO.builder()
                .id(req.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .artistName(req.getArtistName())
                .genre(req.getGenre())
                .reason(req.getReason())
                .status(req.getStatus())
                .adminNote(req.getAdminNote())
                .reviewedBy(req.getReviewedBy())
                .reviewedAt(req.getReviewedAt())
                .createdAt(req.getCreatedAt())
                .build();
    }
}