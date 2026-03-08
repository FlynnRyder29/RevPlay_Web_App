package com.revplay.controller;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.ArtistDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.repository.UserRepository;
import com.revplay.service.ArtistCatalogService;
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

import java.util.Collections;
import java.util.List;

import static com.revplay.util.TestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ArtistCatalogController.
 *
 * GET /api/artists      — paginated artist list (list view, no songs/albums)
 * GET /api/artists/{id} — artist detail (includes songs and albums)
 */
@WebMvcTest(ArtistCatalogController.class)
@DisplayName("ArtistCatalogController Integration Tests")
class ArtistCatalogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistCatalogService artistCatalogService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;


    @org.junit.jupiter.api.BeforeEach
    void configureAuthEntryPoint() throws Exception {
        org.mockito.Mockito.doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(authEntryPoint).commence(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }


    // ── Helpers ───────────────────────────────────────────────────

    private ArtistDTO buildArtistDTO(Long id, String name, String genre) {
        return ArtistDTO.builder()
                .id(id)
                .artistName(name)
                .bio("Bio for " + name)
                .genre(genre)
                .profilePictureUrl("/images/artists/" + id + "_pfp.jpg")
                .bannerImageUrl("/images/artists/" + id + "_banner.jpg")
                .build();
    }

    // =================================================================
    // GET /api/artists — List All
    // =================================================================

    @Nested
    @DisplayName("GET /api/artists")
    class ListAll {

        @Test
        @WithMockUser
        @DisplayName("returns paginated artist list")
        void returnsPagedArtists() throws Exception {
            ArtistDTO dto1 = buildArtistDTO(1L, "Aria", "Indie");
            ArtistDTO dto2 = buildArtistDTO(2L, "Neon Pulse", "Electronic");
            Page<ArtistDTO> page = new PageImpl<>(List.of(dto1, dto2));

            when(artistCatalogService.getAllArtists(any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get(API_ARTISTS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].artistName").value("Aria"))
                    .andExpect(jsonPath("$.content[1].artistName").value("Neon Pulse"))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("empty database — returns empty page")
        void emptyDatabase_returnsEmptyPage() throws Exception {
            when(artistCatalogService.getAllArtists(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            mockMvc.perform(get(API_ARTISTS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @WithMockUser
        @DisplayName("list view does not include songs or albums")
        void listView_noSongsOrAlbums() throws Exception {
            ArtistDTO dto = buildArtistDTO(1L, "Aria", "Indie");
            // songs and albums are null in list view
            Page<ArtistDTO> page = new PageImpl<>(List.of(dto));

            when(artistCatalogService.getAllArtists(any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get(API_ARTISTS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].songs").doesNotExist())
                    .andExpect(jsonPath("$.content[0].albums").doesNotExist());
        }
    }

    // =================================================================
    // GET /api/artists/{id} — Detail View
    // =================================================================

    @Nested
    @DisplayName("GET /api/artists/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("existing artist — returns full profile with songs and albums")
        void existingArtist_returnsFullProfile() throws Exception {
            ArtistDTO dto = buildArtistDTO(1L, "Aria", "Indie");
            dto.setSongs(List.of(
                    SongDTO.builder().id(10L).title("Golden Hour")
                            .artistId(1L).artistName("Aria").build(),
                    SongDTO.builder().id(11L).title("Wanderlust")
                            .artistId(1L).artistName("Aria").build()
            ));
            dto.setAlbums(List.of(
                    AlbumDTO.builder().id(20L).name("Echoes")
                            .artistId(1L).artistName("Aria").build()
            ));

            when(artistCatalogService.getArtistById(1L)).thenReturn(dto);

            mockMvc.perform(get(API_ARTISTS + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistName").value("Aria"))
                    .andExpect(jsonPath("$.genre").value("Indie"))
                    .andExpect(jsonPath("$.songs", hasSize(2)))
                    .andExpect(jsonPath("$.songs[0].title").value("Golden Hour"))
                    .andExpect(jsonPath("$.albums", hasSize(1)))
                    .andExpect(jsonPath("$.albums[0].name").value("Echoes"));
        }

        @Test
        @WithMockUser
        @DisplayName("non-existing artist — returns 404")
        void nonExistingArtist_returns404() throws Exception {
            when(artistCatalogService.getArtistById(999L))
                    .thenThrow(new ResourceNotFoundException("Artist", "id", 999L));

            mockMvc.perform(get(API_ARTISTS + "/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("artist with no songs or albums — returns empty lists")
        void noSongsOrAlbums_returnsEmptyLists() throws Exception {
            ArtistDTO dto = buildArtistDTO(1L, "New Artist", "Rock");
            dto.setSongs(Collections.emptyList());
            dto.setAlbums(Collections.emptyList());

            when(artistCatalogService.getArtistById(1L)).thenReturn(dto);

            mockMvc.perform(get(API_ARTISTS + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songs", hasSize(0)))
                    .andExpect(jsonPath("$.albums", hasSize(0)));
        }

        @Test
        @DisplayName("unauthenticated — returns 401 Unauthorized")
        void unauthenticated_returnsRedirect() throws Exception {
            // SecurityConfig uses RevPlayAuthenticationEntryPoint which returns
            // HTTP 401 for REST clients — not a form-login redirect (302).
            mockMvc.perform(get(API_ARTISTS + "/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}