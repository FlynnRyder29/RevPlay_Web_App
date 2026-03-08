package com.revplay.controller;

import com.revplay.dto.SongDTO;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.model.Genre;
import com.revplay.repository.GenreRepository;
import com.revplay.repository.UserRepository;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.revplay.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(LibraryController.class)
@Import(SecurityConfig.class)
@DisplayName("LibraryController Integration Tests")
class LibraryControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private SongService songService;
    @MockitoBean private GenreRepository genreRepository;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

    private SongDTO testSong;

    @BeforeEach
    void setUp() throws Exception {
        testSong = SongDTO.builder()
                .id(TEST_SONG_ID).title(TEST_SONG_TITLE)
                .genre(TEST_SONG_GENRE).artistName(TEST_ARTIST_NAME).build();

        when(genreRepository.findAll()).thenReturn(Collections.emptyList());
    }

    // ── GET /library — browse all ─────────────────────────────────

    @Test
    @DisplayName("GET /library - authenticated, no params - returns library view")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_noParams_returnsLibraryView() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.getAllSongs(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/library"))
                .andExpect(status().isOk())
                .andExpect(view().name("library"))
                .andExpect(model().attributeExists("songs", "genres"));
    }

    @Test
    @DisplayName("GET /library - no params - calls getAllSongs")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_noParams_callsGetAllSongs() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.getAllSongs(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/library"))
                .andExpect(status().isOk());

        verify(songService).getAllSongs(any(Pageable.class));
        verify(songService, never()).searchSongs(any(), any());
        verify(songService, never()).filterSongs(any(), any(), any(), any(), any());
    }

    // ── GET /library — search mode ────────────────────────────────

    @Test
    @DisplayName("GET /library?q=rock - calls searchSongs with trimmed query")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_withQuery_callsSearchSongs() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.searchSongs(eq("rock"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/library").param("q", "rock"))
                .andExpect(status().isOk())
                .andExpect(view().name("library"));

        verify(songService).searchSongs(eq("rock"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /library?q=rock - model has currentQuery")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_withQuery_modelHasCurrentQuery() throws Exception {
        Page<SongDTO> page = new PageImpl<>(Collections.emptyList());
        when(songService.searchSongs(any(), any())).thenReturn(page);

        mockMvc.perform(get("/library").param("q", "rock"))
                .andExpect(model().attribute("currentQuery", "rock"));
    }

    // ── GET /library — genre filter ───────────────────────────────

    @Test
    @DisplayName("GET /library?genre=Pop - calls filterSongs with genre")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_withGenre_callsFilterSongs() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.filterSongs(eq("Pop"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/library").param("genre", "Pop"))
                .andExpect(status().isOk());

        verify(songService).filterSongs(eq("Pop"), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /library?genre=Pop - model has currentGenre")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_withGenre_modelHasCurrentGenre() throws Exception {
        Page<SongDTO> page = new PageImpl<>(Collections.emptyList());
        when(songService.filterSongs(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/library").param("genre", "Pop"))
                .andExpect(model().attribute("currentGenre", "Pop"));
    }

    @Test
    @DisplayName("GET /library - genres list populated from repository")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_genresPopulatedFromRepository() throws Exception {
        Genre rock = new Genre();
        rock.setName("Rock");
        when(genreRepository.findAll()).thenReturn(List.of(rock));
        when(songService.getAllSongs(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/library"))
                .andExpect(model().attributeExists("genres"));

        verify(genreRepository).findAll();
    }

    // ── GET /player ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /player - authenticated - returns player view")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getPlayer_authenticated_returnsPlayerView() throws Exception {
        mockMvc.perform(get("/player"))
                .andExpect(status().isOk())
                .andExpect(view().name("player"));
    }

    @Test
    @DisplayName("GET /library - blank query - falls through to getAllSongs")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getLibrary_blankQuery_callsGetAllSongs() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.getAllSongs(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/library").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("library"));

        verify(songService).getAllSongs(any(Pageable.class));
        verify(songService, never()).searchSongs(any(), any());
    }

    @Test
    @DisplayName("GET /player - admin - returns player view")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ADMIN")
    void getPlayer_admin_returnsPlayerView() throws Exception {
        mockMvc.perform(get("/player"))
                .andExpect(status().isOk())
                .andExpect(view().name("player"));
    }
}