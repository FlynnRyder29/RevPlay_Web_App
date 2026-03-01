package com.revplay.service;

import com.revplay.dto.UserDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Role;
import com.revplay.model.User;
import com.revplay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 *
 * No Spring context — pure Mockito.
 * All repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .email("alice@revplay.com")
                .username("alice")
                .passwordHash("$2a$10$hashedpassword")
                .displayName("Alice Wonder")
                .bio("Music lover.")
                .profilePictureUrl("/uploads/pfp/alice.jpg")
                .role(Role.LISTENER)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
    }

    // ── getUserProfile ────────────────────────────────────────────

    @Test
    @DisplayName("getUserProfile - existing email - returns correct UserDTO")
    void getUserProfile_existingEmail_returnsUserDTO() {
        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));

        UserDTO result = userService.getUserProfile("alice@revplay.com");

        assertNotNull(result);
        assertEquals(1L,                   result.getId());
        assertEquals("alice@revplay.com",  result.getEmail());
        assertEquals("alice",              result.getUsername());
        assertEquals("Alice Wonder",       result.getDisplayName());
        assertEquals("Music lover.",       result.getBio());
        assertEquals(Role.LISTENER,        result.getRole());
    }

    @Test
    @DisplayName("getUserProfile - existing email - maps profilePictureUrl correctly")
    void getUserProfile_existingEmail_mapsProfilePictureUrl() {
        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));

        UserDTO result = userService.getUserProfile("alice@revplay.com");

        assertEquals("/uploads/pfp/alice.jpg", result.getProfilePictureUrl());
    }

    @Test
    @DisplayName("getUserProfile - existing email - maps createdAt correctly")
    void getUserProfile_existingEmail_mapsCreatedAt() {
        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));

        UserDTO result = userService.getUserProfile("alice@revplay.com");

        assertNotNull(result.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.getCreatedAt());
    }

    @Test
    @DisplayName("getUserProfile - email not found - throws ResourceNotFoundException")
    void getUserProfile_emailNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("ghost@revplay.com"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserProfile("ghost@revplay.com"));

        assertTrue(ex.getMessage().contains("ghost@revplay.com"));
    }

    @Test
    @DisplayName("getUserProfile - email not found - exception message contains field and value")
    void getUserProfile_emailNotFound_exceptionMessageIsDescriptive() {
        when(userRepository.findByEmail("ghost@revplay.com"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserProfile("ghost@revplay.com"));

        // ResourceNotFoundException formats: "User not found with email: ghost@revplay.com"
        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains("email"));
        assertTrue(ex.getMessage().contains("ghost@revplay.com"));
    }

    // ── updateProfile ─────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile - valid displayName - updates displayName")
    void updateProfile_validDisplayName_updatesDisplayName() {
        User updatedUser = User.builder()
                .id(1L)
                .email("alice@revplay.com")
                .username("alice")
                .passwordHash("$2a$10$hashedpassword")
                .displayName("Alice Updated")
                .bio("Music lover.")
                .role(Role.LISTENER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDTO result = userService.updateProfile("alice@revplay.com", "Alice Updated", null);

        assertEquals("Alice Updated", result.getDisplayName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - valid bio - updates bio")
    void updateProfile_validBio_updatesBio() {
        User updatedUser = User.builder()
                .id(1L)
                .email("alice@revplay.com")
                .username("alice")
                .passwordHash("$2a$10$hashedpassword")
                .displayName("Alice Wonder")
                .bio("Updated bio text.")
                .role(Role.LISTENER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDTO result = userService.updateProfile("alice@revplay.com", null, "Updated bio text.");

        assertEquals("Updated bio text.", result.getBio());
    }

    @Test
    @DisplayName("updateProfile - blank displayName - does not overwrite existing displayName")
    void updateProfile_blankDisplayName_doesNotOverwriteDisplayName() {
        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDTO result = userService.updateProfile("alice@revplay.com", "   ", null);

        // Blank displayName should be ignored — existing value kept
        assertEquals("Alice Wonder", result.getDisplayName());
        // Confirm save was still called (bio/other fields may be saved)
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - null displayName and null bio - saves without changes")
    void updateProfile_allNulls_savesWithoutChanges() {
        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDTO result = userService.updateProfile("alice@revplay.com", null, null);

        // Nothing changed — existing values preserved
        assertEquals("Alice Wonder",  result.getDisplayName());
        assertEquals("Music lover.", result.getBio());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - email not found - throws ResourceNotFoundException")
    void updateProfile_emailNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("ghost@revplay.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile("ghost@revplay.com", "New Name", "New Bio"));

        // User was never saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - email not found - repository save is never called")
    void updateProfile_emailNotFound_doesNotSave() {
        when(userRepository.findByEmail("ghost@revplay.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile("ghost@revplay.com", "Name", "Bio"));

        verify(userRepository, never()).save(any(User.class));
    }

    // ── getUserByEmail ────────────────────────────────────────────

    @Test
    @DisplayName("getUserByEmail - existing email - returns User entity")
    void getUserByEmail_existingEmail_returnsUserEntity() {
        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));

        User result = userService.getUserByEmail("alice@revplay.com");

        assertNotNull(result);
        assertEquals("alice@revplay.com", result.getEmail());
        assertEquals("alice",             result.getUsername());
    }

    @Test
    @DisplayName("getUserByEmail - email not found - throws ResourceNotFoundException")
    void getUserByEmail_emailNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("ghost@revplay.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("ghost@revplay.com"));
    }

    @Test
    @DisplayName("getUserByEmail - returns raw User entity, not DTO")
    void getUserByEmail_returnsUserEntity_notDTO() {
        when(userRepository.findByEmail("alice@revplay.com"))
                .thenReturn(Optional.of(existingUser));

        // Return type must be User, not UserDTO
        User result = userService.getUserByEmail("alice@revplay.com");

        assertInstanceOf(User.class, result);
        assertEquals(Role.LISTENER, result.getRole());
        assertEquals("$2a$10$hashedpassword", result.getPasswordHash());
    }
}