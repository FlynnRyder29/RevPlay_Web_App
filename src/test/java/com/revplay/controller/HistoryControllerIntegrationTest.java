package com.revplay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.dto.HistoryDTO;
import com.revplay.dto.HistoryRequest;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HistoryController.
 *
 * POST   /api/history        — add to history (returns 200)
 * GET    /api/history        — get recent history paged
 * GET    /api/history/all    — get full history list
 * DELETE /api/history        — clear history (returns 204)
 */
@WebMvcTest(HistoryController.class)
@DisplayName("HistoryController Integration Tests")
class HistoryControllerIntegrationTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    @MockitoBean private HistoryService                  historyService;
    @MockitoBean private CustomUserDetailsService        customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler      accessDeniedHandler;

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
    // POST /api/history
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/history")
    class AddToHistory {

        @Test
        @WithMockUser
        @DisplayName("authenticated — valid request returns 200")
        void addToHistory_authenticated_returns200() throws Exception {
            doNothing().when(historyService).addToHistory(1L);

            HistoryRequest request = new HistoryRequest();
            request.setSongId(1L);

            mockMvc.perform(post("/api/history")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(historyService).addToHistory(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("null songId — returns 400 (@NotNull on HistoryRequest.songId)")
        void addToHistory_nullSongId_returns400() throws Exception {
            // HistoryRequest.songId is annotated @NotNull — null value triggers 400
            HistoryRequest request = new HistoryRequest(); // songId = null

            mockMvc.perform(post("/api/history")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(historyService, never()).addToHistory(any());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void addToHistory_unauthenticated_returns401() throws Exception {
            HistoryRequest request = new HistoryRequest();
            request.setSongId(1L);

            mockMvc.perform(post("/api/history")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/history
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/history")
    class GetMyHistory {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 200 with paged history")
        void getMyHistory_authenticated_returns200() throws Exception {
            HistoryDTO entry = new HistoryDTO();
            when(historyService.getMyHistory(50))
                    .thenReturn(new PageImpl<>(List.of(entry)));

            mockMvc.perform(get("/api/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(historyService).getMyHistory(50);
        }

        @Test
        @WithMockUser
        @DisplayName("custom limit — passed to service")
        void getMyHistory_customLimit_passedToService() throws Exception {
            when(historyService.getMyHistory(10))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/history").param("limit", "10"))
                    .andExpect(status().isOk());

            verify(historyService).getMyHistory(10);
        }

        @Test
        @WithMockUser
        @DisplayName("empty history — returns 200 with empty page")
        void getMyHistory_empty_returns200Empty() throws Exception {
            when(historyService.getMyHistory(50))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getMyHistory_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/history"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/history/all
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/history/all")
    class GetAllHistory {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 200 with full list")
        void getAllHistory_authenticated_returns200() throws Exception {
            HistoryDTO entry = new HistoryDTO();
            when(historyService.getAllHistory()).thenReturn(List.of(entry));

            mockMvc.perform(get("/api/history/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(historyService).getAllHistory();
        }

        @Test
        @WithMockUser
        @DisplayName("empty history — returns 200 with empty list")
        void getAllHistory_empty_returns200Empty() throws Exception {
            when(historyService.getAllHistory()).thenReturn(List.of());

            mockMvc.perform(get("/api/history/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getAllHistory_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/history/all"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /api/history
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/history")
    class ClearHistory {

        @Test
        @WithMockUser
        @DisplayName("authenticated — returns 204 No Content")
        void clearHistory_authenticated_returns204() throws Exception {
            doNothing().when(historyService).clearHistory();

            mockMvc.perform(delete("/api/history"))
                    .andExpect(status().isNoContent());

            verify(historyService).clearHistory();
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void clearHistory_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/api/history"))
                    .andExpect(status().isUnauthorized());
        }
    }
}