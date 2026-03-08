package com.revplay.service;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User listenerUser;
    private User artistUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        listenerUser = User.builder()
                .id(1L)
                .email("alice@revplay.com")
                .username("alice")
                .passwordHash("$2b$10$hashedpassword")
                .role(Role.LISTENER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        artistUser = User.builder()
                .id(2L)
                .email("bob@revplay.com")
                .username("bob_beats")
                .passwordHash("$2b$10$artisthash")
                .role(Role.ARTIST)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .id(3L)
                .email("admin@revplay.com")
                .username("admin")
                .passwordHash("$2b$10$adminhash")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── loadUserByUsername — found by email ───────────────────────

    @Test
    @DisplayName("loadUserByUsername - valid email - returns UserDetails")
    void loadUserByUsername_validEmail_returnsUserDetails() {
        when(userRepository.findByEmailOrUsername("alice@revplay.com", "alice@revplay.com"))
                .thenReturn(Optional.of(listenerUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice@revplay.com");

        assertNotNull(result);
    }

    @Test
    @DisplayName("loadUserByUsername - valid email - principal is the submitted emailOrUsername")
    void loadUserByUsername_validEmail_principalIsSubmittedInput() {
        when(userRepository.findByEmailOrUsername("alice@revplay.com", "alice@revplay.com"))
                .thenReturn(Optional.of(listenerUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice@revplay.com");

        // Principal must be the submitted value (not user.getEmail()) — critical for Spring Security auth
        assertEquals("alice@revplay.com", result.getUsername());
    }

    @Test
    @DisplayName("loadUserByUsername - valid email - password is user's passwordHash")
    void loadUserByUsername_validEmail_passwordIsHash() {
        when(userRepository.findByEmailOrUsername("alice@revplay.com", "alice@revplay.com"))
                .thenReturn(Optional.of(listenerUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice@revplay.com");

        assertEquals("$2b$10$hashedpassword", result.getPassword());
    }

    @Test
    @DisplayName("loadUserByUsername - valid email - authority is ROLE_LISTENER")
    void loadUserByUsername_listenerUser_hasRoleListener() {
        when(userRepository.findByEmailOrUsername("alice@revplay.com", "alice@revplay.com"))
                .thenReturn(Optional.of(listenerUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice@revplay.com");

        String authority = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        assertEquals("ROLE_LISTENER", authority);
    }

    // ── loadUserByUsername — found by username ────────────────────

    @Test
    @DisplayName("loadUserByUsername - valid username - returns UserDetails")
    void loadUserByUsername_validUsername_returnsUserDetails() {
        when(userRepository.findByEmailOrUsername("alice", "alice"))
                .thenReturn(Optional.of(listenerUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice");

        assertNotNull(result);
    }

    @Test
    @DisplayName("loadUserByUsername - valid username - returns UserDetails with password")
    void loadUserByUsername_validUsername_principalIsSubmittedInput() {
        when(userRepository.findByEmailOrUsername("alice", "alice"))
                .thenReturn(Optional.of(listenerUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice");

        assertNotNull(result);
        assertEquals("$2b$10$hashedpassword", result.getPassword());
    }

    // ── loadUserByUsername — role mapping ─────────────────────────

    @Test
    @DisplayName("loadUserByUsername - artist user - authority is ROLE_ARTIST")
    void loadUserByUsername_artistUser_hasRoleArtist() {
        when(userRepository.findByEmailOrUsername("bob_beats", "bob_beats"))
                .thenReturn(Optional.of(artistUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("bob_beats");

        String authority = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        assertEquals("ROLE_ARTIST", authority);
    }

    @Test
    @DisplayName("loadUserByUsername - admin user - authority is ROLE_ADMIN")
    void loadUserByUsername_adminUser_hasRoleAdmin() {
        when(userRepository.findByEmailOrUsername("admin", "admin"))
                .thenReturn(Optional.of(adminUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin");

        String authority = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        assertEquals("ROLE_ADMIN", authority);
    }

    @Test
    @DisplayName("loadUserByUsername - any user - exactly one authority granted")
    void loadUserByUsername_anyUser_exactlyOneAuthority() {
        when(userRepository.findByEmailOrUsername("alice@revplay.com", "alice@revplay.com"))
                .thenReturn(Optional.of(listenerUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("alice@revplay.com");

        assertEquals(1, result.getAuthorities().size());
    }

    // ── loadUserByUsername — not found ────────────────────────────

    @Test
    @DisplayName("loadUserByUsername - unknown input - throws UsernameNotFoundException")
    void loadUserByUsername_unknownInput_throwsUsernameNotFoundException() {
        when(userRepository.findByEmailOrUsername("ghost", "ghost"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("ghost"));
    }

    @Test
    @DisplayName("loadUserByUsername - unknown input - exception message contains the input")
    void loadUserByUsername_unknownInput_exceptionMessageContainsInput() {
        when(userRepository.findByEmailOrUsername("ghost@revplay.com", "ghost@revplay.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("ghost@revplay.com"));

        assertTrue(ex.getMessage().contains("ghost@revplay.com"));
    }

    @Test
    @DisplayName("loadUserByUsername - not found - repository called with both email and username args")
    void loadUserByUsername_notFound_repositoryCalledWithBothArgs() {
        when(userRepository.findByEmailOrUsername("alice", "alice"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("alice"));

        verify(userRepository).findByEmailOrUsername("alice", "alice");
    }
}