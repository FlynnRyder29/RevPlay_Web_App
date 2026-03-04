package com.revplay.controller;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.service.AlbumCatalogService;
import com.revplay.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.revplay.util.TestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AlbumController.
 *
 * GET /api/albums      — paginated album list
 * GET /api/albums/{id} — album detail with tracklist
 */
@WebMvcTest(AlbumController.class)
@DisplayName("AlbumController Integration Tests")
class AlbumControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumCatalogService albumCatalogService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;

    // ── Helpers ───────────────────────────────────────────────────

    private AlbumDTO buildAlbumDTO(Long id, String name) {
        return AlbumDTO.builder()
                .id(id)
                .name(name)
                .description("Description for " + name)
                .coverImageUrl("/images/albums/" + id + "_cover.jpg")
                .releaseDate(LocalDate.of(2024, 6, 15))
                .artistId(TEST_ARTIST_ID)
                .artistName(TEST_ARTIST_NAME)
                .songCount(5)
                .build();
    }

    // =================================================================
    // GET /api/albums — List All
    // =================================================================

    @Nested
    @DisplayName("GET /api/albums")
    class ListAll {

        @Test
        @WithMockUser
        @DisplayName("returns paginated album list")
        void returnsPaginatedAlbums() throws Exception {
            AlbumDTO dto1 = buildAlbumDTO(1L, "Echoes");
            AlbumDTO dto2 = buildAlbumDTO(2L, "Pulse");
            Page<AlbumDTO> page = new PageImpl<>(List.of(dto1, dto2));

            when(albumCatalogService.getAllAlbums(any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get(API_ALBUMS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].name").value("Echoes"))
                    .andExpect(jsonPath("$.content[1].name").value("Pulse"))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("empty database — returns empty page")
        void emptyDatabase_returnsEmptyPage() throws Exception {
            when(albumCatalogService.getAllAlbums(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            mockMvc.perform(get(API_ALBUMS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @WithMockUser
        @DisplayName("list view does not include tracks")
        void listView_noTracks() throws Exception {
            AlbumDTO dto = buildAlbumDTO(1L, "Echoes");
            Page<AlbumDTO> page = new PageImpl<>(List.of(dto));

            when(albumCatalogService.getAllAlbums(any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get(API_ALBUMS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].tracks").doesNotExist());
        }
    }

    // =================================================================
    // GET /api/albums/{id} — Detail with Tracklist
    // =================================================================

    @Nested
    @DisplayName("GET /api/albums/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("existing album — returns detail with tracklist")
        void existingAlbum_returnsDetailWithTracks() throws Exception {
            AlbumDTO dto = buildAlbumDTO(1L, "Echoes");
            dto.setTracks(List.of(
                    SongDTO.builder().id(10L).title("Track 1")
                            .artistId(TEST_ARTIST_ID).artistName(TEST_ARTIST_NAME)
                            .albumId(1L).albumName("Echoes").build(),
                    SongDTO.builder().id(11L).title("Track 2")
                            .artistId(TEST_ARTIST_ID).artistName(TEST_ARTIST_NAME)
                            .albumId(1L).albumName("Echoes").build()
            ));

            when(albumCatalogService.getAlbumById(1L)).thenReturn(dto);

            mockMvc.perform(get(API_ALBUMS + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Echoes"))
                    .andExpect(jsonPath("$.artistName").value(TEST_ARTIST_NAME))
                    .andExpect(jsonPath("$.tracks", hasSize(2)))
                    .andExpect(jsonPath("$.tracks[0].title").value("Track 1"))
                    .andExpect(jsonPath("$.tracks[1].title").value("Track 2"));
        }

        @Test
        @WithMockUser
        @DisplayName("non-existing album — returns 404")
        void nonExistingAlbum_returns404() throws Exception {
            when(albumCatalogService.getAlbumById(999L))
                    .thenThrow(new ResourceNotFoundException("Album", "id", 999L));

            mockMvc.perform(get(API_ALBUMS + "/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("album with no tracks — returns empty tracklist")
        void noTracks_returnsEmptyList() throws Exception {
            AlbumDTO dto = buildAlbumDTO(1L, "Empty Album");
            dto.setTracks(Collections.emptyList());

            when(albumCatalogService.getAlbumById(1L)).thenReturn(dto);

            mockMvc.perform(get(API_ALBUMS + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tracks", hasSize(0)));
        }

        @Test
        @DisplayName("unauthenticated — returns redirect to login")
        void unauthenticated_returnsRedirect() throws Exception {
            mockMvc.perform(get(API_ALBUMS + "/1"))
                    .andExpect(status().is3xxRedirection());
        }
    }
}