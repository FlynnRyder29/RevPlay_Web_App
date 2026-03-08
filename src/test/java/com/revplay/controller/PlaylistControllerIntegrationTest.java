package com.revplay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.dto.PlaylistDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaylistController.class)
@DisplayName("PlaylistController Integration Tests")
class PlaylistControllerIntegrationTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    @MockitoBean private PlaylistService              playlistService;
    @MockitoBean private CustomUserDetailsService     customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler   accessDeniedHandler;

    @BeforeEach
    void configureSecurityHandlers() throws Exception {
        org.mockito.Mockito.doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(authEntryPoint).commence(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            return null;
        }).when(accessDeniedHandler).handle(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PlaylistDTO samplePlaylist() {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(1L);
        dto.setName("Chill Vibes");
        return dto;
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/playlists
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/playlists")
    class CreatePlaylist {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 201 with created playlist")
        void createPlaylist_authenticated_returns201() throws Exception {
            PlaylistDTO request = new PlaylistDTO();
            request.setName("Road Trip");
            when(playlistService.createPlaylist(any(PlaylistDTO.class), any()))
                    .thenReturn(samplePlaylist());

            mockMvc.perform(post("/api/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Chill Vibes"));
        }

        @Test
        @WithMockUser
        @DisplayName("blank name — returns 400")
        void createPlaylist_blankName_returns400() throws Exception {
            PlaylistDTO request = new PlaylistDTO();
            request.setName("");

            mockMvc.perform(post("/api/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(request)))
                    .andExpect(status().isBadRequest());

            verify(playlistService, never()).createPlaylist(any(PlaylistDTO.class), any());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void createPlaylist_unauthenticated_returns401() throws Exception {
            PlaylistDTO request = new PlaylistDTO();
            request.setName("Road Trip");

            mockMvc.perform(post("/api/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/playlists/me
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/playlists/me")
    class GetMyPlaylists {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 200 with list")
        void getMyPlaylists_authenticated_returns200() throws Exception {
            when(playlistService.getMyPlaylists()).thenReturn(List.of(samplePlaylist()));

            mockMvc.perform(get("/api/playlists/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        @WithMockUser
        @DisplayName("no playlists — returns 200 with empty list")
        void getMyPlaylists_empty_returns200Empty() throws Exception {
            when(playlistService.getMyPlaylists()).thenReturn(List.of());

            mockMvc.perform(get("/api/playlists/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getMyPlaylists_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/playlists/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/playlists/public
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/playlists/public")
    class GetPublicPlaylists {

        @Test
        @WithMockUser
        @DisplayName("no search param — returns all public playlists")
        void getPublicPlaylists_noSearch_returns200() throws Exception {
            when(playlistService.getPublicPlaylists(null))
                    .thenReturn(List.of(samplePlaylist()));

            mockMvc.perform(get("/api/playlists/public"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Chill Vibes"));

            verify(playlistService).getPublicPlaylists(null);
        }

        @Test
        @WithMockUser
        @DisplayName("with search param — passed to service")
        void getPublicPlaylists_withSearch_passedToService() throws Exception {
            when(playlistService.getPublicPlaylists("chill"))
                    .thenReturn(List.of(samplePlaylist()));

            mockMvc.perform(get("/api/playlists/public").param("search", "chill"))
                    .andExpect(status().isOk());

            verify(playlistService).getPublicPlaylists("chill");
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getPublicPlaylists_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/playlists/public"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/playlists/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/playlists/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("existing public playlist — returns 200")
        void getById_existingPlaylist_returns200() throws Exception {
            when(playlistService.getPlaylistById(1L)).thenReturn(samplePlaylist());

            mockMvc.perform(get("/api/playlists/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @WithMockUser
        @DisplayName("not found — returns 404")
        void getById_notFound_returns404() throws Exception {
            when(playlistService.getPlaylistById(999L))
                    .thenThrow(new ResourceNotFoundException("Playlist", "id", 999L));

            mockMvc.perform(get("/api/playlists/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("private playlist by non-owner — returns 403")
        void getById_privatePlaylistNonOwner_returns403() throws Exception {
            when(playlistService.getPlaylistById(2L))
                    .thenThrow(new UnauthorizedAccessException("Access denied to playlist: 2"));

            mockMvc.perform(get("/api/playlists/2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/playlists/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PUT /api/playlists/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/playlists/{id}")
    class UpdatePlaylist {

        @Test
        @WithMockUser
        @DisplayName("owner update — returns 200")
        void updatePlaylist_owner_returns200() throws Exception {
            PlaylistDTO request = new PlaylistDTO();
            request.setName("Updated Name");
            when(playlistService.updatePlaylist(eq(1L), any(PlaylistDTO.class), any()))
                    .thenReturn(samplePlaylist());

            mockMvc.perform(put("/api/playlists/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("non-owner — returns 403")
        void updatePlaylist_nonOwner_returns403() throws Exception {
            when(playlistService.updatePlaylist(eq(1L), any(PlaylistDTO.class), any()))
                    .thenThrow(new UnauthorizedAccessException("You don't own this playlist"));

            mockMvc.perform(put("/api/playlists/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new PlaylistDTO())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("not found — returns 404")
        void updatePlaylist_notFound_returns404() throws Exception {
            when(playlistService.updatePlaylist(eq(999L), any(PlaylistDTO.class), any()))
                    .thenThrow(new ResourceNotFoundException("Playlist", "id", 999L));

            mockMvc.perform(put("/api/playlists/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new PlaylistDTO())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void updatePlaylist_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put("/api/playlists/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new PlaylistDTO())))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /api/playlists/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/playlists/{id}")
    class DeletePlaylist {

        @Test
        @WithMockUser
        @DisplayName("owner delete — returns 204")
        void deletePlaylist_owner_returns204() throws Exception {
            doNothing().when(playlistService).deletePlaylist(1L);

            mockMvc.perform(delete("/api/playlists/1"))
                    .andExpect(status().isNoContent());

            verify(playlistService).deletePlaylist(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("non-owner — returns 403")
        void deletePlaylist_nonOwner_returns403() throws Exception {
            doThrow(new UnauthorizedAccessException("You don't own this playlist"))
                    .when(playlistService).deletePlaylist(1L);

            mockMvc.perform(delete("/api/playlists/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("not found — returns 404")
        void deletePlaylist_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Playlist", "id", 999L))
                    .when(playlistService).deletePlaylist(999L);

            mockMvc.perform(delete("/api/playlists/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void deletePlaylist_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/api/playlists/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/playlists/{id}/songs/{songId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/playlists/{id}/songs/{songId}")
    class AddSong {

        @Test
        @WithMockUser
        @DisplayName("valid request — returns 204")
        void addSong_valid_returns204() throws Exception {
            doNothing().when(playlistService).addSongToPlaylist(1L, 2L);

            mockMvc.perform(post("/api/playlists/1/songs/2"))
                    .andExpect(status().isNoContent());

            verify(playlistService).addSongToPlaylist(1L, 2L);
        }

        @Test
        @WithMockUser
        @DisplayName("song not found — returns 404")
        void addSong_songNotFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Song", "id", 2L))
                    .when(playlistService).addSongToPlaylist(1L, 2L);

            mockMvc.perform(post("/api/playlists/1/songs/2"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("non-owner — returns 403")
        void addSong_nonOwner_returns403() throws Exception {
            doThrow(new UnauthorizedAccessException("You don't own this playlist"))
                    .when(playlistService).addSongToPlaylist(1L, 2L);

            mockMvc.perform(post("/api/playlists/1/songs/2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void addSong_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/playlists/1/songs/2"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /api/playlists/{id}/songs/{songId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/playlists/{id}/songs/{songId}")
    class RemoveSong {

        @Test
        @WithMockUser
        @DisplayName("valid request — returns 204")
        void removeSong_valid_returns204() throws Exception {
            doNothing().when(playlistService).removeSongFromPlaylist(1L, 2L);

            mockMvc.perform(delete("/api/playlists/1/songs/2"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("non-owner — returns 403")
        void removeSong_nonOwner_returns403() throws Exception {
            doThrow(new UnauthorizedAccessException("You don't own this playlist"))
                    .when(playlistService).removeSongFromPlaylist(1L, 2L);

            mockMvc.perform(delete("/api/playlists/1/songs/2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void removeSong_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/api/playlists/1/songs/2"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PUT /api/playlists/{id}/reorder
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/playlists/{id}/reorder")
    class ReorderSongs {

        @Test
        @WithMockUser
        @DisplayName("valid list — returns 204")
        void reorderSongs_valid_returns204() throws Exception {
            doNothing().when(playlistService).reorderSongs(eq(1L), anyList());

            mockMvc.perform(put("/api/playlists/1/reorder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[1, 2, 3]"))
                    .andExpect(status().isNoContent());

            verify(playlistService).reorderSongs(eq(1L), anyList());
        }

        @Test
        @WithMockUser
        @DisplayName("non-owner — returns 403")
        void reorderSongs_nonOwner_returns403() throws Exception {
            doThrow(new UnauthorizedAccessException("You can only reorder songs in your own playlist"))
                    .when(playlistService).reorderSongs(eq(1L), anyList());

            mockMvc.perform(put("/api/playlists/1/reorder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[1, 2, 3]"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void reorderSongs_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put("/api/playlists/1/reorder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[1,2,3]"))
                    .andExpect(status().isUnauthorized());
        }
    }
}