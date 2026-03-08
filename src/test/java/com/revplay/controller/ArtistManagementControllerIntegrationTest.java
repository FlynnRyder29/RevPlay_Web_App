package com.revplay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;
import com.revplay.dto.ArtistUpdateRequest;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.model.Artist;
import com.revplay.model.User;
import com.revplay.model.Role;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.UserRepository;
import com.revplay.service.ArtistService;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.FileStorageService;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

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

@WebMvcTest(ArtistManagementController.class)
@Import(SecurityConfig.class)
@DisplayName("ArtistManagementController Integration Tests")
class ArtistManagementControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ArtistService artistService;
    @MockitoBean private ArtistRepository artistRepository;
    @MockitoBean private FileStorageService fileStorageService;
    @MockitoBean private SecurityUtils securityUtils;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

    private ArtistProfileResponse testProfile;
    private User testUser;
    private Artist testArtist;

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

        testProfile = ArtistProfileResponse.builder()
                .id(TEST_ARTIST_ID)
                .artistName(TEST_ARTIST_NAME)
                .bio(TEST_ARTIST_BIO)
                .genre(TEST_ARTIST_GENRE)
                .build();

        testUser = User.builder()
                .id(TEST_ARTIST_USER_ID).email(TEST_USER_EMAIL).username(TEST_USER_USERNAME)
                .role(Role.ARTIST).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        testArtist = new Artist();
        testArtist.setId(TEST_ARTIST_ID);
        testArtist.setUserId(TEST_ARTIST_USER_ID);
        testArtist.setArtistName(TEST_ARTIST_NAME);
    }

    // ── POST /api/artists/register ────────────────────────────────

    @Test
    @DisplayName("POST /api/artists/register - valid request - returns 201")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void registerArtist_validRequest_returns201() throws Exception {
        when(artistService.registerArtist(any(ArtistRegisterRequest.class))).thenReturn(testProfile);

        mockMvc.perform(post("/api/artists/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artistName\":\"Test Artist\",\"bio\":\"Bio\",\"genre\":\"Pop\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.artistName").value(TEST_ARTIST_NAME));
    }

    @Test
    @DisplayName("POST /api/artists/register - unauthenticated - returns 401")
    void registerArtist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/artists/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artistName\":\"Test Artist\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/artists/me ───────────────────────────────────────

    @Test
    @DisplayName("GET /api/artists/me - artist - returns profile")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void getMyProfile_artist_returns200() throws Exception {
        when(artistService.getMyProfile()).thenReturn(testProfile);

        mockMvc.perform(get("/api/artists/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artistName").value(TEST_ARTIST_NAME))
                .andExpect(jsonPath("$.bio").value(TEST_ARTIST_BIO));
    }

    @Test
    @DisplayName("GET /api/artists/me - listener - returns 403")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getMyProfile_listener_returns403() throws Exception {
        mockMvc.perform(get("/api/artists/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/artists/me - unauthenticated - returns 401")
    void getMyProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/artists/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/artists/update ───────────────────────────────────

    @Test
    @DisplayName("PUT /api/artists/update - artist - returns updated profile")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void updateProfile_artist_returns200() throws Exception {
        ArtistProfileResponse updated = ArtistProfileResponse.builder()
                .id(TEST_ARTIST_ID).artistName("Updated Name").bio("New bio").genre("Rock").build();
        when(artistService.updateProfile(any(ArtistUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/artists/update").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artistName\":\"Updated Name\",\"bio\":\"New bio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artistName").value("Updated Name"));
    }

    // ── POST /api/artists/me/picture ──────────────────────────────

    @Test
    @DisplayName("POST /api/artists/me/picture - valid image - returns 200")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void uploadPicture_validImage_returns200() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file", "pic.jpg", "image/jpeg", "imgdata".getBytes());

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(artistRepository.findByUserId(TEST_ARTIST_USER_ID)).thenReturn(Optional.of(testArtist));
        when(fileStorageService.storeFile(any(), eq("artist-pictures"))).thenReturn("artist-pictures/pic.jpg");
        when(artistService.getMyProfile()).thenReturn(testProfile);

        mockMvc.perform(multipart("/api/artists/me/picture").file(imageFile).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/artists/me/picture - empty file - returns 400")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void uploadPicture_emptyFile_returns400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/artists/me/picture").file(emptyFile).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/artists/me/picture - non-image file - returns 400")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void uploadPicture_nonImageFile_returns400() throws Exception {
        MockMultipartFile badFile = new MockMultipartFile(
                "file", "file.txt", "text/plain", "data".getBytes());

        mockMvc.perform(multipart("/api/artists/me/picture").file(badFile).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/artists/me/picture - listener - returns 403")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void uploadPicture_listener_returns403() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file", "pic.jpg", "image/jpeg", "imgdata".getBytes());

        mockMvc.perform(multipart("/api/artists/me/picture").file(imageFile).with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── POST /api/artists/me/banner ───────────────────────────────

    @Test
    @DisplayName("POST /api/artists/me/banner - valid image - returns 200")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void uploadBanner_validImage_returns200() throws Exception {
        MockMultipartFile bannerFile = new MockMultipartFile(
                "file", "banner.jpg", "image/jpeg", "bannerdata".getBytes());

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(artistRepository.findByUserId(TEST_ARTIST_USER_ID)).thenReturn(Optional.of(testArtist));
        when(fileStorageService.storeFile(any(), eq("artist-banners"))).thenReturn("artist-banners/banner.jpg");
        when(artistService.getMyProfile()).thenReturn(testProfile);

        mockMvc.perform(multipart("/api/artists/me/banner").file(bannerFile).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/artists/me/banner - listener - returns 403")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void uploadBanner_listener_returns403() throws Exception {
        MockMultipartFile bannerFile = new MockMultipartFile(
                "file", "banner.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/artists/me/banner").file(bannerFile).with(csrf()))
                .andExpect(status().isForbidden());
    }
}