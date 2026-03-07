package com.revplay.controller;

import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.repository.UserRepository;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.PlaylistFollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.revplay.util.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaylistFollowController.class)
@DisplayName("PlaylistFollowController Integration Tests")
class PlaylistFollowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private PlaylistFollowService playlistFollowService;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

    @BeforeEach
    void configureAuthEntryPoint() throws Exception {
        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(authEntryPoint).commence(any(), any(), any());
    }

    // ── POST /api/playlists/{id}/follow ───────────────────────────

    @Test
    @DisplayName("POST /api/playlists/{id}/follow - authenticated - returns 200")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void followPlaylist_authenticated_returns200() throws Exception {
        doNothing().when(playlistFollowService).followPlaylist(TEST_PLAYLIST_ID);

        mockMvc.perform(post("/api/playlists/{id}/follow", TEST_PLAYLIST_ID).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/playlists/{id}/follow - authenticated - calls service")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void followPlaylist_authenticated_callsService() throws Exception {
        doNothing().when(playlistFollowService).followPlaylist(TEST_PLAYLIST_ID);

        mockMvc.perform(post("/api/playlists/{id}/follow", TEST_PLAYLIST_ID).with(csrf()))
                .andExpect(status().isOk());

        verify(playlistFollowService).followPlaylist(TEST_PLAYLIST_ID);
    }

    @Test
    @DisplayName("POST /api/playlists/{id}/follow - unauthenticated - returns 401")
    void followPlaylist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/playlists/{id}/follow", TEST_PLAYLIST_ID).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── DELETE /api/playlists/{id}/follow ─────────────────────────

    @Test
    @DisplayName("DELETE /api/playlists/{id}/follow - authenticated - returns 204")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void unfollowPlaylist_authenticated_returns204() throws Exception {
        doNothing().when(playlistFollowService).unfollowPlaylist(TEST_PLAYLIST_ID);

        mockMvc.perform(delete("/api/playlists/{id}/follow", TEST_PLAYLIST_ID).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/playlists/{id}/follow - authenticated - calls service")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void unfollowPlaylist_authenticated_callsService() throws Exception {
        doNothing().when(playlistFollowService).unfollowPlaylist(TEST_PLAYLIST_ID);

        mockMvc.perform(delete("/api/playlists/{id}/follow", TEST_PLAYLIST_ID).with(csrf()))
                .andExpect(status().isNoContent());

        verify(playlistFollowService).unfollowPlaylist(TEST_PLAYLIST_ID);
    }

    @Test
    @DisplayName("DELETE /api/playlists/{id}/follow - unauthenticated - returns 401")
    void unfollowPlaylist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/playlists/{id}/follow", TEST_PLAYLIST_ID).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/playlists/{id}/follow ────────────────────────────

    @Test
    @DisplayName("GET /api/playlists/{id}/follow - following - returns true")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void isFollowing_following_returnsTrue() throws Exception {
        when(playlistFollowService.isFollowing(TEST_PLAYLIST_ID)).thenReturn(true);

        mockMvc.perform(get("/api/playlists/{id}/follow", TEST_PLAYLIST_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/playlists/{id}/follow - not following - returns false")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void isFollowing_notFollowing_returnsFalse() throws Exception {
        when(playlistFollowService.isFollowing(TEST_PLAYLIST_ID)).thenReturn(false);

        mockMvc.perform(get("/api/playlists/{id}/follow", TEST_PLAYLIST_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("GET /api/playlists/{id}/follow - unauthenticated - returns 401")
    void isFollowing_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/playlists/{id}/follow", TEST_PLAYLIST_ID))
                .andExpect(status().isUnauthorized());
    }
}