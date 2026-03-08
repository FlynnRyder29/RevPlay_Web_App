package com.revplay.controller;

import com.revplay.dto.AnalyticsDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.repository.UserRepository;
import com.revplay.service.AnalyticsService;
import com.revplay.config.SecurityConfig;
import com.revplay.service.CustomUserDetailsService;
import org.springframework.context.annotation.Import;
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
 * Integration tests for AnalyticsController.
 *
 * All endpoints require ARTIST role — class-level @PreAuthorize("hasRole('ARTIST')").
 */
@WebMvcTest(AnalyticsController.class)
@Import(SecurityConfig.class)
@DisplayName("AnalyticsController Integration Tests")
class AnalyticsControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AnalyticsService              analyticsService;
    @MockitoBean private CustomUserDetailsService      customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler    accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

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
    // GET /api/artists/analytics/overview
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/analytics/overview")
    class GetOverview {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — returns 200 with analytics body")
        void getOverview_artist_returns200() throws Exception {
            AnalyticsDTO dto = AnalyticsDTO.builder()
                    .artistId(10L).artistName("Aria")
                    .totalSongs(5L).totalPlays(100L).totalFavorites(20L)
                    .build();
            when(analyticsService.getOverview()).thenReturn(dto);

            mockMvc.perform(get("/api/artists/analytics/overview"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistId").value(10L))
                    .andExpect(jsonPath("$.totalSongs").value(5))
                    .andExpect(jsonPath("$.totalPlays").value(100));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — no artist profile returns 404")
        void getOverview_noArtistProfile_returns404() throws Exception {
            when(analyticsService.getOverview())
                    .thenThrow(new ResourceNotFoundException("Artist", "userId", 1L));

            mockMvc.perform(get("/api/artists/analytics/overview"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getOverview_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/overview"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getOverview_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/overview"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/analytics/songs
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/analytics/songs")
    class GetSongPlayCounts {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — returns 200 with songPlayCounts list")
        void getSongPlayCounts_artist_returns200() throws Exception {
            AnalyticsDTO.SongPlayCountDTO spc = AnalyticsDTO.SongPlayCountDTO.builder()
                    .songId(1L).songTitle("Track").playCount(42L).build();
            AnalyticsDTO dto = AnalyticsDTO.builder()
                    .artistId(10L).artistName("Aria")
                    .songPlayCounts(List.of(spc)).build();
            when(analyticsService.getSongPlayCounts()).thenReturn(dto);

            mockMvc.perform(get("/api/artists/analytics/songs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts[0].songId").value(1L))
                    .andExpect(jsonPath("$.songPlayCounts[0].playCount").value(42));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — empty list returns 200 with empty array")
        void getSongPlayCounts_noSongs_returns200Empty() throws Exception {
            when(analyticsService.getSongPlayCounts())
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).songPlayCounts(List.of()).build());

            mockMvc.perform(get("/api/artists/analytics/songs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts").isEmpty());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getSongPlayCounts_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getSongPlayCounts_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/analytics/top-listeners
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/analytics/top-listeners")
    class GetTopListeners {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — returns 200 with topListeners list")
        void getTopListeners_artist_returns200() throws Exception {
            AnalyticsDTO.TopListenerDTO listener = AnalyticsDTO.TopListenerDTO.builder()
                    .userId(5L).username("fan1").playCount(30L).build();
            when(analyticsService.getTopListeners())
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).topListeners(List.of(listener)).build());

            mockMvc.perform(get("/api/artists/analytics/top-listeners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners[0].userId").value(5L));
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getTopListeners_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/top-listeners"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getTopListeners_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/top-listeners"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/analytics/trends
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/analytics/trends")
    class GetListeningTrends {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — default period (daily) returns 200")
        void getListeningTrends_defaultPeriod_returns200() throws Exception {
            when(analyticsService.getListeningTrends("daily"))
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).trendPeriod("daily").trends(List.of()).build());

            mockMvc.perform(get("/api/artists/analytics/trends"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trendPeriod").value("daily"));

            verify(analyticsService).getListeningTrends("daily");
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — period=weekly passes 'weekly' to service")
        void getListeningTrends_weekly_returns200() throws Exception {
            when(analyticsService.getListeningTrends("weekly"))
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).trendPeriod("weekly").trends(List.of()).build());

            mockMvc.perform(get("/api/artists/analytics/trends")
                            .param("period", "weekly"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trendPeriod").value("weekly"));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — period=monthly passes 'monthly' to service")
        void getListeningTrends_monthly_returns200() throws Exception {
            when(analyticsService.getListeningTrends("monthly"))
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).trendPeriod("monthly").trends(List.of()).build());

            mockMvc.perform(get("/api/artists/analytics/trends")
                            .param("period", "monthly"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — trend data points mapped correctly")
        void getListeningTrends_returnsDataPoints() throws Exception {
            AnalyticsDTO.TrendPointDTO point = AnalyticsDTO.TrendPointDTO.builder()
                    .period("2025-03-01").playCount(50L).build();
            when(analyticsService.getListeningTrends("daily"))
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).trendPeriod("daily").trends(List.of(point)).build());

            mockMvc.perform(get("/api/artists/analytics/trends"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trends[0].period").value("2025-03-01"))
                    .andExpect(jsonPath("$.trends[0].playCount").value(50));
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getListeningTrends_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/trends"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getListeningTrends_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/trends"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/analytics/fans
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/analytics/fans")
    class GetFans {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — returns 200 with fans list")
        void getFans_artist_returns200() throws Exception {
            AnalyticsDTO.FanDTO fan = AnalyticsDTO.FanDTO.builder()
                    .userId(7L).username("superfan").favoriteCount(5L).build();
            when(analyticsService.getFans())
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).fans(List.of(fan)).build());

            mockMvc.perform(get("/api/artists/analytics/fans"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans[0].userId").value(7L))
                    .andExpect(jsonPath("$.fans[0].favoriteCount").value(5));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — no fans returns 200 with empty list")
        void getFans_noFans_returns200Empty() throws Exception {
            when(analyticsService.getFans())
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).fans(List.of()).build());

            mockMvc.perform(get("/api/artists/analytics/fans"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans").isEmpty());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getFans_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/fans"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getFans_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/fans"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /api/artists/analytics/songs/{songId}/fans
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/artists/analytics/songs/{songId}/fans")
    class GetSongFans {

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — owned song returns 200 with fans")
        void getSongFans_artist_returns200() throws Exception {
            AnalyticsDTO.FanDTO fan = AnalyticsDTO.FanDTO.builder()
                    .userId(9L).username("trackfan").favoriteCount(2L).build();
            when(analyticsService.getSongFans(42L))
                    .thenReturn(AnalyticsDTO.builder()
                            .artistId(10L).fans(List.of(fan)).build());

            mockMvc.perform(get("/api/artists/analytics/songs/42/fans"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans[0].userId").value(9L));
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — song not owned returns 404")
        void getSongFans_songNotOwned_returns404() throws Exception {
            when(analyticsService.getSongFans(99L))
                    .thenThrow(new ResourceNotFoundException("Song", "id", 99L));

            mockMvc.perform(get("/api/artists/analytics/songs/99/fans"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ARTIST")
        @DisplayName("ARTIST — non-numeric songId returns 400")
        void getSongFans_nonNumericId_returns400() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/abc/fans"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "LISTENER")
        @DisplayName("LISTENER — returns 403")
        void getSongFans_listener_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/1/fans"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void getSongFans_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/1/fans"))
                    .andExpect(status().isUnauthorized());
        }
    }
}