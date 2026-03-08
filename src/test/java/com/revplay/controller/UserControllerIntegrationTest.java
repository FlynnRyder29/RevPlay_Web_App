package com.revplay.controller;

import com.revplay.dto.ArtistRequestDTO;
import com.revplay.dto.UserDTO;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.model.RequestStatus;
import com.revplay.model.Role;
import com.revplay.repository.PlaylistRepository;
import com.revplay.repository.UserRepository;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.FavoriteService;
import com.revplay.service.FileStorageService;
import com.revplay.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserService userService;
    @MockitoBean private FileStorageService fileStorageService;
    @MockitoBean private FavoriteService favoriteService;
    @MockitoBean private PlaylistRepository playlistRepository;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() throws Exception {
        // Configure authEntryPoint: redirect MVC endpoints, 401 for /api/**
        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletRequest req =
                    inv.getArgument(0, jakarta.servlet.http.HttpServletRequest.class);
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            String uri = req.getRequestURI();
            if (uri != null && uri.startsWith("/user/api/")) {
                resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                resp.sendRedirect("/auth/login");
            }
            return null;
        }).when(authEntryPoint).commence(any(), any(), any());

        // Ensure userDetailsService does not inadvertently authenticate unknown users
        when(customUserDetailsService.loadUserByUsername(any()))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("not found"));

        testUserDTO = UserDTO.builder()
                .id(TEST_USER_ID).email(TEST_USER_EMAIL).username(TEST_USER_USERNAME)
                .displayName(TEST_USER_DISPLAY_NAME).role(Role.LISTENER)
                .createdAt(LocalDateTime.of(2024, 3, 15, 10, 0)).build();
    }

    // ── GET /user/profile ─────────────────────────────────────────

    @Test
    @DisplayName("GET /user/profile - authenticated - returns 200 and profile view")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getProfile_authenticated_returnsProfileView() throws Exception {
        when(userService.getUserProfile(TEST_USER_EMAIL)).thenReturn(testUserDTO);
        when(favoriteService.getMyFavoriteSongIds()).thenReturn(Collections.emptyList());
        when(playlistRepository.countByUser_Id(TEST_USER_ID)).thenReturn(2L);

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("GET /user/profile - authenticated - model has user and joinedDate")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getProfile_authenticated_modelHasJoinedDate() throws Exception {
        when(userService.getUserProfile(TEST_USER_EMAIL)).thenReturn(testUserDTO);
        when(favoriteService.getMyFavoriteSongIds()).thenReturn(Collections.emptyList());
        when(playlistRepository.countByUser_Id(TEST_USER_ID)).thenReturn(0L);

        mockMvc.perform(get("/user/profile"))
                .andExpect(model().attributeExists("joinedDate"));
    }

    @Test
    @DisplayName("GET /user/profile - unauthenticated - redirects to login")
    void getProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /user/profile - with artist request - model has artistRequest")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getProfile_withArtistRequest_modelHasArtistRequest() throws Exception {
        ArtistRequestDTO req = ArtistRequestDTO.builder()
                .id(1L).userId(TEST_USER_ID).artistName("Test Artist")
                .status(RequestStatus.PENDING).createdAt(LocalDateTime.now()).build();

        when(userService.getUserProfile(TEST_USER_EMAIL)).thenReturn(testUserDTO);
        when(favoriteService.getMyFavoriteSongIds()).thenReturn(Collections.emptyList());
        when(playlistRepository.countByUser_Id(TEST_USER_ID)).thenReturn(0L);
        when(userService.getMyArtistRequest(TEST_USER_EMAIL)).thenReturn(req);

        mockMvc.perform(get("/user/profile"))
                .andExpect(model().attributeExists("artistRequest"));
    }

    // ── POST /user/profile ────────────────────────────────────────

    @Test
    @DisplayName("POST /user/profile - valid update - redirects to profile")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void postProfile_validUpdate_redirectsToProfile() throws Exception {
        when(userService.updateProfile(eq(TEST_USER_EMAIL), eq("New Name"), eq("New Bio")))
                .thenReturn(testUserDTO);

        mockMvc.perform(post("/user/profile").with(csrf())
                        .param("displayName", "New Name")
                        .param("bio", "New Bio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile"));
    }

    @Test
    @DisplayName("POST /user/profile - valid update - calls userService")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void postProfile_validUpdate_callsService() throws Exception {
        when(userService.updateProfile(any(), any(), any())).thenReturn(testUserDTO);

        mockMvc.perform(post("/user/profile").with(csrf())
                        .param("displayName", "New Name")
                        .param("bio", "New Bio"))
                .andExpect(status().is3xxRedirection());

        verify(userService).updateProfile(eq(TEST_USER_EMAIL), eq("New Name"), eq("New Bio"));
    }

    @Test
    @DisplayName("POST /user/profile - service throws - redirects with error flash")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void postProfile_serviceThrows_redirectsWithError() throws Exception {
        doThrow(new RuntimeException("Update failed"))
                .when(userService).updateProfile(any(), any(), any());

        mockMvc.perform(post("/user/profile").with(csrf())
                        .param("displayName", "Name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile"));
    }

    @Test
    @DisplayName("POST /user/profile - unauthenticated - redirects to login")
    void postProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/user/profile").with(csrf())
                        .param("displayName", "Name"))
                .andExpect(status().is3xxRedirection());
    }

    // ── POST /user/profile/picture ────────────────────────────────

    @Test
    @DisplayName("POST /user/profile/picture - empty file - redirects with error")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void uploadPicture_emptyFile_redirectsWithError() throws Exception {
        org.springframework.mock.web.MockMultipartFile emptyFile =
                new org.springframework.mock.web.MockMultipartFile(
                        "profilePicture", "empty.jpg",
                        "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/user/profile/picture").file(emptyFile).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile"));

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("POST /user/profile/picture - non-image file - redirects with error")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void uploadPicture_nonImageFile_redirectsWithError() throws Exception {
        org.springframework.mock.web.MockMultipartFile badFile =
                new org.springframework.mock.web.MockMultipartFile(
                        "profilePicture", "file.txt",
                        "text/plain", "data".getBytes());

        mockMvc.perform(multipart("/user/profile/picture").file(badFile).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile"));

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("POST /user/profile/picture - valid image - stores file and updates profile")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void uploadPicture_validImage_storesFileAndUpdatesProfile() throws Exception {
        org.springframework.mock.web.MockMultipartFile imageFile =
                new org.springframework.mock.web.MockMultipartFile(
                        "profilePicture", "photo.jpg",
                        "image/jpeg", "imgdata".getBytes());

        when(fileStorageService.storeFile(any(), eq("profile-pictures")))
                .thenReturn("profile-pictures/photo.jpg");

        mockMvc.perform(multipart("/user/profile/picture").file(imageFile).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile"));

        verify(fileStorageService).storeFile(any(), eq("profile-pictures"));
        verify(userService).updateProfilePicture(eq(TEST_USER_EMAIL), contains("profile-pictures"));
    }

    // ── POST /user/api/artist-request ─────────────────────────────

    @Test
    @DisplayName("POST /user/api/artist-request - valid body - returns 200 with DTO")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void submitArtistRequest_validBody_returns200() throws Exception {
        ArtistRequestDTO dto = ArtistRequestDTO.builder()
                .id(1L).userId(TEST_USER_ID).artistName("My Artist")
                .status(RequestStatus.PENDING).createdAt(LocalDateTime.now()).build();

        when(userService.submitArtistRequest(eq(TEST_USER_EMAIL), eq("My Artist"), eq("Pop"), eq("I love music")))
                .thenReturn(dto);

        mockMvc.perform(post("/user/api/artist-request").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artistName\":\"My Artist\",\"genre\":\"Pop\",\"reason\":\"I love music\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artistName").value("My Artist"));
    }

    @Test
    @DisplayName("POST /user/api/artist-request - already artist - returns 400 with error")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void submitArtistRequest_alreadyArtist_returns400() throws Exception {
        when(userService.submitArtistRequest(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("You are already an artist"));

        mockMvc.perform(post("/user/api/artist-request").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artistName\":\"Name\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /user/api/artist-request - unauthenticated - returns 401")
    void submitArtistRequest_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/user/api/artist-request").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artistName\":\"Name\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /user/api/artist-request ──────────────────────────────

    @Test
    @DisplayName("GET /user/api/artist-request - has request - returns DTO")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getMyArtistRequest_hasRequest_returnsDTO() throws Exception {
        ArtistRequestDTO dto = ArtistRequestDTO.builder()
                .id(1L).userId(TEST_USER_ID).artistName("My Artist")
                .status(RequestStatus.PENDING).createdAt(LocalDateTime.now()).build();

        when(userService.getMyArtistRequest(TEST_USER_EMAIL)).thenReturn(dto);

        mockMvc.perform(get("/user/api/artist-request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /user/api/artist-request - no request - returns NONE status")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getMyArtistRequest_noRequest_returnsNoneStatus() throws Exception {
        when(userService.getMyArtistRequest(TEST_USER_EMAIL)).thenReturn(null);

        mockMvc.perform(get("/user/api/artist-request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NONE"));
    }

    @Test
    @DisplayName("GET /user/api/artist-request - unauthenticated - returns 401")
    void getMyArtistRequest_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/user/api/artist-request"))
                .andExpect(status().isUnauthorized());
    }
}