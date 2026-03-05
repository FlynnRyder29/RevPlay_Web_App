package com.revplay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.dto.FavoriteDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FavoriteController.
 *
 * POST   /api/favorites/{songId}   — add favorite   → 201
 * DELETE /api/favorites/{songId}   — remove favorite → 204
 * GET    /api/favorites            — get my favorites → 200
 */
@WebMvcTest(FavoriteController.class)
@DisplayName("FavoriteController Integration Tests")
class FavoriteControllerIntegrationTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private FavoriteService              favoriteService;
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

    // ══════════════════════════════════════════════════════════════════════
    // POST /api/favorites/{songId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/favorites/{songId}")
    class AddFavorite {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 201 Created")
        void addFavorite_authenticated_returns201() throws Exception {
            doNothing().when(favoriteService).addFavorite(1L);

            mockMvc.perform(post("/api/favorites/1"))
                    .andExpect(status().isCreated());

            verify(favoriteService).addFavorite(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("song not found — returns 404")
        void addFavorite_songNotFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Song", "id", 99L))
                    .when(favoriteService).addFavorite(99L);

            mockMvc.perform(post("/api/favorites/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("duplicate favorite — returns 400")
        void addFavorite_duplicate_returns400() throws Exception {
            // FavoriteService throws BadRequestException when song is already favorited
            doThrow(new BadRequestException("Song 1 is already in your favorites"))
                    .when(favoriteService).addFavorite(1L);

            mockMvc.perform(post("/api/favorites/1"))
                    .andExpect(status().isBadRequest());

            verify(favoriteService).addFavorite(1L);
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void addFavorite_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/favorites/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /api/favorites/{songId}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/favorites/{songId}")
    class RemoveFavorite {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 204 No Content")
        void removeFavorite_authenticated_returns204() throws Exception {
            doNothing().when(favoriteService).removeFavorite(1L);

            mockMvc.perform(delete("/api/favorites/1"))
                    .andExpect(status().isNoContent());

            verify(favoriteService).removeFavorite(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("song not in favorites — returns 404")
        void removeFavorite_songNotFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Favorite", "songId", 99L))
                    .when(favoriteService).removeFavorite(99L);

            mockMvc.perform(delete("/api/favorites/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void removeFavorite_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/api/favorites/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/favorites
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/favorites")
    class GetMyFavorites {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 200 with list")
        void getMyFavorites_authenticated_returns200() throws Exception {
            FavoriteDTO fav = new FavoriteDTO();
            when(favoriteService.getMyFavorites()).thenReturn(List.of(fav));

            mockMvc.perform(get("/api/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("no favorites — returns 200 with empty list")
        void getMyFavorites_empty_returns200Empty() throws Exception {
            when(favoriteService.getMyFavorites()).thenReturn(List.of());

            mockMvc.perform(get("/api/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getMyFavorites_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/favorites"))
                    .andExpect(status().isUnauthorized());
        }
    }
}