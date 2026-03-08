package com.revplay.controller;

import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.model.Genre;
import com.revplay.repository.GenreRepository;
import com.revplay.repository.UserRepository;
import com.revplay.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GenreController.
 *
 * GET /api/genres — returns all genre lookup values.
 * GenreController uses GenreRepository directly (no service layer).
 */
@WebMvcTest(GenreController.class)
@DisplayName("GenreController Integration Tests")
class GenreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreRepository genreRepository;
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

    private Genre buildGenre(Long id, String name) {
        Genre genre = new Genre(name);
        genre.setId(id);
        return genre;
    }

    // =================================================================
    // GET /api/genres
    // =================================================================

    @Test
    @WithMockUser
    @DisplayName("returns all genres as flat list")
    void returnsAllGenres() throws Exception {
        List<Genre> genres = List.of(
                buildGenre(1L, "Pop"),
                buildGenre(2L, "Rock"),
                buildGenre(3L, "Electronic"),
                buildGenre(4L, "Hip-Hop"),
                buildGenre(5L, "Indie")
        );
        when(genreRepository.findAll()).thenReturn(genres);

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].name").value("Pop"))
                .andExpect(jsonPath("$[1].name").value("Rock"))
                .andExpect(jsonPath("$[4].name").value("Indie"));
    }

    @Test
    @WithMockUser
    @DisplayName("empty genres table — returns empty list, not error")
    void emptyTable_returnsEmptyList() throws Exception {
        when(genreRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    @DisplayName("genre object includes id and name fields")
    void genreHasIdAndName() throws Exception {
        when(genreRepository.findAll())
                .thenReturn(List.of(buildGenre(1L, "Jazz")));

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Jazz"));
    }

    @Test
    @DisplayName("unauthenticated — returns 401 Unauthorized")
    void unauthenticated_returnsRedirect() throws Exception {
        // SecurityConfig uses RevPlayAuthenticationEntryPoint which returns
        // HTTP 401 for REST clients — not a form-login redirect (302).
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isUnauthorized());
    }
}