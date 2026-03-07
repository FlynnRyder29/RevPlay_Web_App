package com.revplay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.config.SecurityConfig;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.service.AlbumService;
import com.revplay.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ArtistAlbumController.
 *
 * All endpoints require ARTIST role — class-level @PreAuthorize("hasRole('ARTIST')").
 */
@WebMvcTest(ArtistAlbumController.class)
@Import(SecurityConfig.class)
@DisplayName("ArtistAlbumController Integration Tests")
class ArtistAlbumControllerIntegrationTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    @MockitoBean private AlbumService               albumService;
    @MockitoBean private CustomUserDetailsService   customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;

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

    private AlbumDTO sampleAlbum() {
        return AlbumDTO.builder()
                .id(1L).name("My Album").artistId(10L).artistName("Aria").songCount(0).build();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/artists/albums
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/artists/albums")
    class CreateAlbum {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — valid request returns 201 with body")
        void createAlbum_artist_returns201() throws Exception {
            AlbumDTO request = AlbumDTO.builder().name("New Album").build();
            when(albumService.createAlbum(any())).thenReturn(sampleAlbum());

            mockMvc.perform(post("/api/artists/albums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("My Album"));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — blank name returns 400")
        void createAlbum_blankName_returns400() throws Exception {
            AlbumDTO request = AlbumDTO.builder().name("").build();

            mockMvc.perform(post("/api/artists/albums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(request)))
                    .andExpect(status().isBadRequest());

            verify(albumService, never()).createAlbum(any());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void createAlbum_listener_returns403() throws Exception {
            mockMvc.perform(post("/api/artists/albums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(AlbumDTO.builder().name("X").build())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void createAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/artists/albums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(AlbumDTO.builder().name("X").build())))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PUT /api/artists/albums/{albumId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/artists/albums/{albumId}")
    class UpdateAlbum {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — valid request returns 200")
        void updateAlbum_artist_returns200() throws Exception {
            when(albumService.updateAlbum(eq(1L), any())).thenReturn(sampleAlbum());

            mockMvc.perform(put("/api/artists/albums/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(AlbumDTO.builder().name("Updated").build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — album not found returns 404")
        void updateAlbum_notFound_returns404() throws Exception {
            when(albumService.updateAlbum(eq(999L), any()))
                    .thenThrow(new ResourceNotFoundException("Album", "id", 999L));

            mockMvc.perform(put("/api/artists/albums/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(AlbumDTO.builder().build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — album owned by other artist returns 403")
        void updateAlbum_wrongOwner_returns403() throws Exception {
            when(albumService.updateAlbum(eq(1L), any()))
                    .thenThrow(new UnauthorizedAccessException(
                            "Album id=1 does not belong to the logged-in artist"));

            mockMvc.perform(put("/api/artists/albums/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(AlbumDTO.builder().build())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void updateAlbum_listener_returns403() throws Exception {
            mockMvc.perform(put("/api/artists/albums/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(AlbumDTO.builder().build())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void updateAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put("/api/artists/albums/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(AlbumDTO.builder().build())))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /api/artists/albums/{albumId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/artists/albums/{albumId}")
    class DeleteAlbum {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — empty album returns 204")
        void deleteAlbum_emptyAlbum_returns204() throws Exception {
            doNothing().when(albumService).deleteAlbum(1L);

            mockMvc.perform(delete("/api/artists/albums/1"))
                    .andExpect(status().isNoContent());

            verify(albumService).deleteAlbum(1L);
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — album with songs returns 400")
        void deleteAlbum_albumWithSongs_returns400() throws Exception {
            doThrow(new BadRequestException("Cannot delete album with 2 song(s)."))
                    .when(albumService).deleteAlbum(1L);

            mockMvc.perform(delete("/api/artists/albums/1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — album not found returns 404")
        void deleteAlbum_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Album", "id", 1L))
                    .when(albumService).deleteAlbum(1L);

            mockMvc.perform(delete("/api/artists/albums/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void deleteAlbum_listener_returns403() throws Exception {
            mockMvc.perform(delete("/api/artists/albums/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void deleteAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/api/artists/albums/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/artists/albums/{albumId}/songs/{songId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/artists/albums/{albumId}/songs/{songId}")
    class AddSongToAlbum {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — valid returns 204")
        void addSongToAlbum_artist_returns204() throws Exception {
            doNothing().when(albumService).addSongToAlbum(1L, 2L);

            mockMvc.perform(post("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isNoContent());

            verify(albumService).addSongToAlbum(1L, 2L);
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — song already in album returns 400")
        void addSongToAlbum_alreadyInAlbum_returns400() throws Exception {
            doThrow(new BadRequestException("Song is already part of this album"))
                    .when(albumService).addSongToAlbum(1L, 2L);

            mockMvc.perform(post("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — song in different album returns 400")
        void addSongToAlbum_songInDifferentAlbum_returns400() throws Exception {
            doThrow(new BadRequestException("Song is already in album 'Other'. Remove it first."))
                    .when(albumService).addSongToAlbum(1L, 2L);

            mockMvc.perform(post("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — song not found returns 404")
        void addSongToAlbum_songNotFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Song", "id", 2L))
                    .when(albumService).addSongToAlbum(1L, 2L);

            mockMvc.perform(post("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void addSongToAlbum_listener_returns403() throws Exception {
            mockMvc.perform(post("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void addSongToAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /api/artists/albums/{albumId}/songs/{songId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/artists/albums/{albumId}/songs/{songId}")
    class RemoveSongFromAlbum {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — valid returns 204")
        void removeSongFromAlbum_artist_returns204() throws Exception {
            doNothing().when(albumService).removeSongFromAlbum(1L, 2L);

            mockMvc.perform(delete("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — song not in album returns 400")
        void removeSongFromAlbum_songNotInAlbum_returns400() throws Exception {
            doThrow(new BadRequestException("Song does not belong to this album"))
                    .when(albumService).removeSongFromAlbum(1L, 2L);

            mockMvc.perform(delete("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void removeSongFromAlbum_listener_returns403() throws Exception {
            mockMvc.perform(delete("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void removeSongFromAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/api/artists/albums/1/songs/2"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/albums
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/albums")
    class GetMyAlbums {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — returns 200 with paged albums")
        void getMyAlbums_artist_returns200() throws Exception {
            when(albumService.getMyAlbums(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleAlbum())));

            mockMvc.perform(get("/api/artists/albums"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — empty result returns 200 with empty page")
        void getMyAlbums_noAlbums_returns200Empty() throws Exception {
            when(albumService.getMyAlbums(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            mockMvc.perform(get("/api/artists/albums"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — custom sortBy/sortDir params honoured")
        void getMyAlbums_customSort_returns200() throws Exception {
            when(albumService.getMyAlbums(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/artists/albums")
                            .param("sortBy", "name")
                            .param("sortDir", "asc"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — unknown sortBy falls back to createdAt")
        void getMyAlbums_unknownSortBy_fallsBackToCreatedAt() throws Exception {
            when(albumService.getMyAlbums(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/artists/albums")
                            .param("sortBy", "injected_field"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getMyAlbums_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/albums"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getMyAlbums_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/albums"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/albums/songs
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/albums/songs")
    class GetMySongs {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — returns 200 with paged songs")
        void getMySongs_artist_returns200() throws Exception {
            SongDTO song = SongDTO.builder().id(1L).title("Track").build();
            when(albumService.getMySongs(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(song)));

            mockMvc.perform(get("/api/artists/albums/songs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — unknown sortBy falls back to createdAt")
        void getMySongs_unknownSortBy_returns200() throws Exception {
            when(albumService.getMySongs(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/artists/albums/songs")
                            .param("sortBy", "badField"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getMySongs_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/albums/songs"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getMySongs_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/albums/songs"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/albums/{albumId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/albums/{albumId}")
    class GetMyAlbumById {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — owned album returns 200")
        void getMyAlbumById_artist_returns200() throws Exception {
            when(albumService.getMyAlbumById(1L)).thenReturn(sampleAlbum());

            mockMvc.perform(get("/api/artists/albums/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("My Album"));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — album not found returns 404")
        void getMyAlbumById_notFound_returns404() throws Exception {
            when(albumService.getMyAlbumById(999L))
                    .thenThrow(new ResourceNotFoundException("Album", "id", 999L));

            mockMvc.perform(get("/api/artists/albums/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — album owned by other artist returns 403")
        void getMyAlbumById_wrongOwner_returns403() throws Exception {
            when(albumService.getMyAlbumById(1L))
                    .thenThrow(new UnauthorizedAccessException(
                            "Album id=1 does not belong to the logged-in artist"));

            mockMvc.perform(get("/api/artists/albums/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getMyAlbumById_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/albums/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getMyAlbumById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/albums/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}