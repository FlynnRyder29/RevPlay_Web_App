package com.revplay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.dto.ArtistRequestDTO;
import com.revplay.dto.UserDTO;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.model.RequestStatus;
import com.revplay.model.Role;
import com.revplay.repository.UserRepository;
import com.revplay.service.AdminService;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.model.User;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.revplay.util.TestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.revplay.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@DisplayName("AdminController Integration Tests")
class AdminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AdminService adminService;
    @MockitoBean private SecurityUtils securityUtils;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

    private UserDTO testUserDTO;
    private ArtistRequestDTO testRequestDTO;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(authEntryPoint).commence(any(), any(), any());

        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            return null;
        }).when(accessDeniedHandler).handle(any(), any(), any());

        testUserDTO = UserDTO.builder()
                .id(10L).email("user@revplay.com").username("testuser")
                .displayName("Test User").role(Role.LISTENER)
                .createdAt(LocalDateTime.now()).build();

        testRequestDTO = ArtistRequestDTO.builder()
                .id(1L).userId(10L).username("testuser")
                .artistName("Test Artist").genre("Pop")
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now()).build();

        adminUser = User.builder()
                .id(1L).email("admin@revplay.com").username("admin")
                .role(Role.ADMIN).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    // ── GET /api/admin/stats ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/stats - admin - returns 200 with stats")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getDashboardStats_admin_returns200() throws Exception {
        when(adminService.getDashboardStats()).thenReturn(Map.of("totalUsers", 50L));

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(50));
    }

    @Test
    @DisplayName("GET /api/admin/stats - unauthenticated - returns 401")
    void getDashboardStats_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/admin/stats/roles ────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/stats/roles - admin - returns role distribution")
    @WithMockUser(roles = "ADMIN")
    void getRoleDistribution_admin_returns200() throws Exception {
        when(adminService.getRoleDistribution()).thenReturn(Map.of("LISTENER", 35L, "ARTIST", 10L));

        mockMvc.perform(get("/api/admin/stats/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LISTENER").value(35));
    }

    // ── GET /api/admin/stats/growth ───────────────────────────────

    @Test
    @DisplayName("GET /api/admin/stats/growth - admin - returns growth data")
    @WithMockUser(roles = "ADMIN")
    void getUserGrowth_admin_returns200() throws Exception {
        when(adminService.getNewUsersPerDay(30)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/stats/growth").param("days", "30"))
                .andExpect(status().isOk());
    }

    // ── GET /api/admin/stats/top-songs ────────────────────────────

    @Test
    @DisplayName("GET /api/admin/stats/top-songs - admin - returns top songs list")
    @WithMockUser(roles = "ADMIN")
    void getTopSongs_admin_returns200() throws Exception {
        when(adminService.getTopSongs(10)).thenReturn(List.of(Map.of("id", 1L, "title", "Song")));

        mockMvc.perform(get("/api/admin/stats/top-songs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ── GET /api/admin/users ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/users - admin - returns paged users")
    @WithMockUser(roles = "ADMIN")
    void getUsers_admin_returnsPagedUsers() throws Exception {
        Page<UserDTO> page = new PageImpl<>(List.of(testUserDTO));
        when(adminService.getUsers(isNull(), isNull(), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/admin/users - with search param - passes search to service")
    @WithMockUser(roles = "ADMIN")
    void getUsers_withSearch_passesSearchToService() throws Exception {
        Page<UserDTO> page = new PageImpl<>(List.of(testUserDTO));
        when(adminService.getUsers(eq("test"), isNull(), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users").param("search", "test"))
                .andExpect(status().isOk());

        verify(adminService).getUsers(eq("test"), isNull(), eq(0), eq(20));
    }

    // ── GET /api/admin/users/{id} ─────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/users/{id} - admin - returns user")
    @WithMockUser(roles = "ADMIN")
    void getUser_admin_returnsUser() throws Exception {
        when(adminService.getUserById(10L)).thenReturn(testUserDTO);

        mockMvc.perform(get("/api/admin/users/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@revplay.com"));
    }

    // ── GET /api/admin/artist-requests ───────────────────────────

    @Test
    @DisplayName("GET /api/admin/artist-requests - admin - returns paged requests")
    @WithMockUser(roles = "ADMIN")
    void getArtistRequests_admin_returns200() throws Exception {
        Page<ArtistRequestDTO> page = new PageImpl<>(List.of(testRequestDTO));
        when(adminService.getArtistRequests(isNull(), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/admin/artist-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].artistName").value("Test Artist"));
    }

    @Test
    @DisplayName("GET /api/admin/artist-requests/stats - admin - returns stats")
    @WithMockUser(roles = "ADMIN")
    void getArtistRequestStats_admin_returns200() throws Exception {
        when(adminService.getArtistRequestStats()).thenReturn(Map.of("pending", 3L));

        mockMvc.perform(get("/api/admin/artist-requests/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(3));
    }

    // ── PUT /api/admin/artist-requests/{id}/approve ───────────────

    @Test
    @DisplayName("PUT /api/admin/artist-requests/{id}/approve - admin - returns 200")
    @WithMockUser(roles = "ADMIN")
    void approveArtistRequest_admin_returns200() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(adminService.approveArtistRequest(eq(1L), eq(1L), any())).thenReturn(testRequestDTO);

        mockMvc.perform(put("/api/admin/artist-requests/{id}/approve", 1L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Great!\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/admin/artist-requests/{id}/approve - already processed - returns 400")
    @WithMockUser(roles = "ADMIN")
    void approveArtistRequest_alreadyProcessed_returns400() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(adminService.approveArtistRequest(any(), any(), any()))
                .thenThrow(new IllegalStateException("Request has already been approved"));

        mockMvc.perform(put("/api/admin/artist-requests/{id}/approve", 1L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── PUT /api/admin/artist-requests/{id}/reject ────────────────

    @Test
    @DisplayName("PUT /api/admin/artist-requests/{id}/reject - admin - returns 200")
    @WithMockUser(roles = "ADMIN")
    void rejectArtistRequest_admin_returns200() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(adminService.rejectArtistRequest(eq(1L), eq(1L), any())).thenReturn(testRequestDTO);

        mockMvc.perform(put("/api/admin/artist-requests/{id}/reject", 1L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Not suitable\"}"))
                .andExpect(status().isOk());
    }

    // ── PUT /api/admin/users/{id}/role ────────────────────────────

    @Test
    @DisplayName("PUT /api/admin/users/{id}/role - valid role - returns 200")
    @WithMockUser(roles = "ADMIN")
    void changeUserRole_validRole_returns200() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(adminService.changeUserRole(eq(10L), eq(Role.ARTIST))).thenReturn(testUserDTO);

        mockMvc.perform(put("/api/admin/users/{id}/role", 10L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ARTIST\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/role - missing role - returns 400")
    @WithMockUser(roles = "ADMIN")
    void changeUserRole_missingRole_returns400() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);

        mockMvc.perform(put("/api/admin/users/{id}/role", 10L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/role - changing own role - returns 400")
    @WithMockUser(roles = "ADMIN")
    void changeUserRole_ownAccount_returns400() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);

        mockMvc.perform(put("/api/admin/users/{id}/role", 1L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"LISTENER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot change your own role"));
    }

    // ── DELETE /api/admin/users/{id} ──────────────────────────────

    @Test
    @DisplayName("DELETE /api/admin/users/{id} - valid user - returns 204")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_validUser_returns204() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        doNothing().when(adminService).deleteUser(10L);

        mockMvc.perform(delete("/api/admin/users/{id}", 10L).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} - own account - returns 400")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ownAccount_returns400() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);

        mockMvc.perform(delete("/api/admin/users/{id}", 1L).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete your own account"));
    }

    // ── DELETE /api/admin/songs/{id} ──────────────────────────────

    @Test
    @DisplayName("DELETE /api/admin/songs/{id} - admin - returns 204")
    @WithMockUser(roles = "ADMIN")
    void deleteSong_admin_returns204() throws Exception {
        doNothing().when(adminService).deleteSong(5L);

        mockMvc.perform(delete("/api/admin/songs/{id}", 5L).with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ── DELETE /api/admin/playlists/{id} ──────────────────────────

    @Test
    @DisplayName("DELETE /api/admin/playlists/{id} - admin - returns 204")
    @WithMockUser(roles = "ADMIN")
    void deletePlaylist_admin_returns204() throws Exception {
        doNothing().when(adminService).deletePlaylist(3L);

        mockMvc.perform(delete("/api/admin/playlists/{id}", 3L).with(csrf()))
                .andExpect(status().isNoContent());
    }
}