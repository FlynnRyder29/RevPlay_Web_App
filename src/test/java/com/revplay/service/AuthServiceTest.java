package com.revplay.service;

import com.revplay.dto.RegisterRequest;
import com.revplay.dto.UserDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * No Spring context — pure Mockito.
 * All repository and encoder calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setEmail("alice@revplay.com");
        validRequest.setUsername("alice");
        validRequest.setPassword("Password@123");
        validRequest.setDisplayName("Alice Wonder");

        savedUser = User.builder()
                .id(1L)
                .email("alice@revplay.com")
                .username("alice")
                .passwordHash("$2a$10$hashedpassword")
                .displayName("Alice Wonder")
                .role(Role.LISTENER)
                .build();
    }

    // ── Happy Path ────────────────────────────────────────────────

    @Test
    @DisplayName("register - valid request - returns UserDTO with correct fields")
    void register_validRequest_returnsUserDTO() {
        when(userRepository.existsByEmail("alice@revplay.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = authService.register(validRequest);

        assertNotNull(result);
        assertEquals("alice@revplay.com", result.getEmail());
        assertEquals("alice",            result.getUsername());
        assertEquals("Alice Wonder",     result.getDisplayName());
        assertEquals(Role.LISTENER,      result.getRole());
    }

    @Test
    @DisplayName("register - valid request - password is encoded before saving")
    void register_validRequest_encodesPassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(validRequest);

        // Verify raw password was never saved — encoder was called
        verify(passwordEncoder, times(1)).encode("Password@123");
        verify(userRepository).save(argThat(user ->
                user.getPasswordHash().equals("$2a$10$hashedpassword")
        ));
    }

    @Test
    @DisplayName("register - no displayName in request - defaults to username")
    void register_noDisplayName_defaultsToUsername() {
        validRequest.setDisplayName(null);

        User savedWithUsername = User.builder()
                .id(2L)
                .email("alice@revplay.com")
                .username("alice")
                .passwordHash("$2a$10$hashedpassword")
                .displayName("alice") // defaults to username
                .role(Role.LISTENER)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedWithUsername);

        UserDTO result = authService.register(validRequest);

        assertEquals("alice", result.getDisplayName());
    }

    @Test
    @DisplayName("register - valid request - role is always LISTENER")
    void register_validRequest_roleIsListener() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = authService.register(validRequest);

        assertEquals(Role.LISTENER, result.getRole());
        // Confirm the saved entity also has LISTENER role
        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.LISTENER
        ));
    }

    @Test
    @DisplayName("register - valid request - userRepository.save is called exactly once")
    void register_validRequest_savesUserOnce() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(validRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    // ── Duplicate Email ───────────────────────────────────────────

    @Test
    @DisplayName("register - duplicate email - throws RuntimeException")
    void register_duplicateEmail_throwsRuntimeException() {
        when(userRepository.existsByEmail("alice@revplay.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(validRequest));

        assertTrue(ex.getMessage().contains("alice@revplay.com"));
    }

    @Test
    @DisplayName("register - duplicate email - user is never saved")
    void register_duplicateEmail_doesNotSave() {
        when(userRepository.existsByEmail("alice@revplay.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(validRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    // ── Duplicate Username ────────────────────────────────────────

    @Test
    @DisplayName("register - duplicate username - throws RuntimeException")
    void register_duplicateUsername_throwsRuntimeException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(validRequest));

        assertTrue(ex.getMessage().contains("alice"));
    }

    @Test
    @DisplayName("register - duplicate username - user is never saved")
    void register_duplicateUsername_doesNotSave() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(validRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    // ── Verification ──────────────────────────────────────────────

    @Test
    @DisplayName("register - always checks email before username")
    void register_checksEmailBeforeUsername() {
        when(userRepository.existsByEmail("alice@revplay.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(validRequest));

        // Email check happened
        verify(userRepository, times(1)).existsByEmail("alice@revplay.com");
        // Username check should NOT happen — email failed first
        verify(userRepository, never()).existsByUsername(anyString());
    }
}