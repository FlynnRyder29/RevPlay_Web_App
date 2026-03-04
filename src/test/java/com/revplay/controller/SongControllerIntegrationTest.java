package com.revplay.controller;

import com.revplay.dto.SongDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.SongService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SongController endpoints.
 *
 * Uses @WebMvcTest — loads only the web layer. SongService is mocked.
 * Spring Security filter chain IS active — tests verify auth behavior.
 *
 * SecurityConfig dependencies are mocked to allow context startup.
 */
@WebMvcTest(SongController.class)
@DisplayName("SongController Integration Tests")
class SongControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SongService songService;

    // ── SecurityConfig dependencies (needed for context startup) ──
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;

    // ── Helper ────────────────────────────────────────────────────

    private SongDTO buildSongDTO(Long id, String title, String genre) {
        return SongDTO.builder()
                .id(id)
                .title(title)
                .genre(genre)
                .duration(TEST_SONG_DURATION)
                .audioUrl(TEST_SONG_AUDIO_URL)
                .coverImageUrl("/images/cover.jpg")
                .releaseDate(LocalDate.of(2024, 3, 15))
                .playCount(500L)
                .visibility(TEST_SONG_VISIBILITY_PUBLIC)
                .artistId(TEST_ARTIST_ID)
                .artistName(TEST_ARTIST_NAME)
                .albumId(TEST_ALBUM_ID)
                .albumName(TEST_ALBUM_NAME)
                .build();
    }

    private Page<SongDTO> singleSongPage(SongDTO dto) {
        return new PageImpl<>(List.of(dto));
    }

    private Page<SongDTO> emptyPage() {
        return new PageImpl<>(Collections.emptyList());
    }

    // =================================================================
    // GET /api/songs — Browse All
    // =================================================================

    @Nested
    @DisplayName("GET /api/songs")
    class BrowseAll {

        @Test
        @WithMockUser
        @DisplayName("returns paginated song list with default params")
        void defaultParams_returnsPaginatedList() throws Exception {
            SongDTO dto = buildSongDTO(1L, "Overdrive", "Electronic");
            when(songService.getAllSongs(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(dto,
                            buildSongDTO(2L, "Resonance", "Synthwave"))));

            mockMvc.perform(get(API_SONGS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].title").value("Overdrive"))
                    .andExpect(jsonPath("$.content[1].title").value("Resonance"))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("custom page and size params are forwarded to service")
        void customPagination_forwardedToService() throws Exception {
            when(songService.getAllSongs(any(Pageable.class))).thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS)
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk());

            verify(songService).getAllSongs(argThat(p ->
                    p.getPageNumber() == 2 && p.getPageSize() == 5));
        }

        @Test
        @WithMockUser
        @DisplayName("invalid sortBy defaults to createdAt — does not throw")
        void invalidSortField_defaultsToCreatedAt() throws Exception {
            when(songService.getAllSongs(any(Pageable.class))).thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS)
                            .param("sortBy", "INVALID_FIELD"))
                    .andExpect(status().isOk());

            // Service must still be called (sort silently defaulted)
            verify(songService).getAllSongs(any(Pageable.class));
        }

        @Test
        @WithMockUser
        @DisplayName("sortBy=playCount&sortDir=desc — accepted as valid sort")
        void sortByPlayCount_accepted() throws Exception {
            when(songService.getAllSongs(any(Pageable.class))).thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS)
                            .param("sortBy", "playCount")
                            .param("sortDir", "desc"))
                    .andExpect(status().isOk());

            verify(songService).getAllSongs(argThat(p ->
                    p.getSort().getOrderFor("playCount") != null));
        }

        @Test
        @DisplayName("unauthenticated request — returns 401 or redirect")
        void unauthenticated_returnsUnauthorized() throws Exception {
            mockMvc.perform(get(API_SONGS))
                    .andExpect(status().is3xxRedirection());
            // Spring Security redirects to /auth/login for form-login
        }

        @Test
        @WithMockUser
        @DisplayName("empty database — returns empty page, not error")
        void emptyDatabase_returnsEmptyPage() throws Exception {
            when(songService.getAllSongs(any(Pageable.class))).thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    // =================================================================
    // GET /api/songs/{id} — Get By ID
    // =================================================================

    @Nested
    @DisplayName("GET /api/songs/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("existing song — returns 200 with all fields")
        void existingId_returnsSongDTO() throws Exception {
            SongDTO dto = buildSongDTO(1L, "Golden Hour", "Indie");
            when(songService.getSongById(1L)).thenReturn(dto);

            mockMvc.perform(get(API_SONGS + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Golden Hour"))
                    .andExpect(jsonPath("$.genre").value("Indie"))
                    .andExpect(jsonPath("$.artistName").value(TEST_ARTIST_NAME))
                    .andExpect(jsonPath("$.albumName").value(TEST_ALBUM_NAME))
                    .andExpect(jsonPath("$.duration").value(TEST_SONG_DURATION));
        }

        @Test
        @WithMockUser
        @DisplayName("non-existing song — returns 404")
        void nonExistingId_returns404() throws Exception {
            when(songService.getSongById(999L))
                    .thenThrow(new ResourceNotFoundException("Song", "id", 999L));

            mockMvc.perform(get(API_SONGS + "/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // =================================================================
    // GET /api/songs/search — Search
    // =================================================================

    // =================================================================
    // GET /api/songs/search — Search
    // =================================================================

    @Nested
    @DisplayName("GET /api/songs/search")
    class Search {

        @Test
        @WithMockUser
        @DisplayName("valid keyword — returns matching songs")
        void validKeyword_returnsMatches() throws Exception {
            SongDTO dto = buildSongDTO(1L, "Midnight City", "Synthwave");
            when(songService.searchSongs(eq("midnight"), any(Pageable.class)))
                    .thenReturn(singleSongPage(dto));

            mockMvc.perform(get(API_SONGS + "/search")
                            .param("q", "midnight"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title").value("Midnight City"));
        }

        @Test
        @WithMockUser
        @DisplayName("blank keyword — returns 400 Bad Request")
        void blankKeyword_returns400() throws Exception {
            mockMvc.perform(get(API_SONGS + "/search")
                            .param("q", "   "))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(songService);
        }

        @Test
        @WithMockUser
        @DisplayName("missing q param — returns 400")
        void missingQParam_returns400() throws Exception {
            mockMvc.perform(get(API_SONGS + "/search"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("no results — returns empty page, not 404")
        void noResults_returnsEmptyPage() throws Exception {
            when(songService.searchSongs(eq("zzzznonexistent"), any(Pageable.class)))
                    .thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS + "/search")
                            .param("q", "zzzznonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("keyword is trimmed before search")
        void keywordTrimmed_trimmedValuePassedToService() throws Exception {
            when(songService.searchSongs(eq("midnight"), any(Pageable.class)))
                    .thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS + "/search")
                            .param("q", "  midnight  "))
                    .andExpect(status().isOk());

            // Service receives trimmed keyword
            verify(songService).searchSongs(eq("midnight"), any(Pageable.class));
        }
    }

    // =================================================================
    // GET /api/songs/filter — Filter
    // =================================================================

    @Nested
    @DisplayName("GET /api/songs/filter")
    class Filter {

        @Test
        @WithMockUser
        @DisplayName("filter by genre — returns filtered results")
        void filterByGenre_returnsFiltered() throws Exception {
            SongDTO dto = buildSongDTO(1L, "Rock Anthem", "Rock");
            when(songService.filterSongs(eq("Rock"), isNull(), isNull(), isNull(),
                    any(Pageable.class)))
                    .thenReturn(singleSongPage(dto));

            mockMvc.perform(get(API_SONGS + "/filter")
                            .param("genre", "Rock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].genre").value("Rock"));
        }

        @Test
        @WithMockUser
        @DisplayName("multiple filters combined — all passed to service")
        void multipleFilters_allPassedToService() throws Exception {
            when(songService.filterSongs(eq("Rock"), eq("Artist1"), eq("Album1"),
                    eq(2024), any(Pageable.class)))
                    .thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS + "/filter")
                            .param("genre", "Rock")
                            .param("artist", "Artist1")
                            .param("album", "Album1")
                            .param("year", "2024"))
                    .andExpect(status().isOk());

            verify(songService).filterSongs(
                    eq("Rock"), eq("Artist1"), eq("Album1"), eq(2024),
                    any(Pageable.class));
        }

        @Test
        @WithMockUser
        @DisplayName("no filter params — returns all songs (no filters applied)")
        void noFilters_returnsAll() throws Exception {
            when(songService.filterSongs(isNull(), isNull(), isNull(), isNull(),
                    any(Pageable.class)))
                    .thenReturn(emptyPage());

            mockMvc.perform(get(API_SONGS + "/filter"))
                    .andExpect(status().isOk());

            verify(songService).filterSongs(
                    isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
        }
    }

    // =================================================================
    // PATCH /api/songs/{id}/visibility — Visibility Update
    // =================================================================

    @Nested
    @DisplayName("PATCH /api/songs/{id}/visibility")
    class Visibility {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("valid visibility — returns updated song")
        void validVisibility_returnsUpdated() throws Exception {
            SongDTO dto = buildSongDTO(1L, "Test Song", "Pop");
            when(songService.updateVisibility(1L, "UNLISTED")).thenReturn(dto);

            mockMvc.perform(patch(API_SONGS + "/1/visibility")
                            .param("visibility", "unlisted"))
                    .andExpect(status().isOk());

            verify(songService).updateVisibility(1L, "UNLISTED");
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("invalid visibility value — returns 400")
        void invalidVisibility_returns400() throws Exception {
            mockMvc.perform(patch(API_SONGS + "/1/visibility")
                            .param("visibility", "INVALID"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(songService);
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER role — returns 403 Forbidden")
        void listenerRole_returns403() throws Exception {
            mockMvc.perform(patch(API_SONGS + "/1/visibility")
                            .param("visibility", "PUBLIC"))
                    .andExpect(status().isForbidden());
        }
    }
}