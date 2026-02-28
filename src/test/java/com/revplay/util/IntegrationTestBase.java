package com.revplay.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for RevPlay full-stack integration tests.
 *
 * Loads the FULL Spring application context with H2 in-memory DB.
 * Every test rolls back automatically via @Transactional.
 *
 * Uses JUnit 5 style (no @RunWith) — compatible with Spring Boot 3.x
 * which ships JUnit 5 by default via spring-boot-starter-test.
 *
 * ── SECURITY TESTING ─────────────────────────────────────────────
 *
 * Option A — annotate each test with @WithMockUser:
 *
 *   @Test
 *   @WithMockUser(username = "alice", roles = {"USER"})
 *   void getFavorites_asUser_returnsOk() throws Exception {
 *       getRequest(API_FAVORITES).andExpect(status().isOk());
 *   }
 *
 *   @Test
 *   @WithMockUser(username = "aria", roles = {"ARTIST"})
 *   void uploadSong_asArtist_returnsCreated() throws Exception {
 *       postRequest(API_SONGS, songDto).andExpect(status().isCreated());
 *   }
 *
 *   @Test
 *   void getPublicSongs_unauthenticated_returnsOk() throws Exception {
 *       getRequest(API_SONGS).andExpect(status().isOk());
 *   }
 *
 * Option B — use programmatic helpers (no annotation needed):
 *
 *   getRequestAs(API_FAVORITES, "alice", "USER")
 *       .andExpect(status().isOk());
 *
 *   getRequestAs(API_SONGS, "aria", "ARTIST")
 *       .andExpect(status().isOk());
 *
 * ── ROLES ─────────────────────────────────────────────────────────
 *   USER   — browse, play, favorites, playlists
 *   ARTIST — all USER features + upload songs/albums, analytics
 *   ADMIN  — full access
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ── API path constants ────────────────────────────────────────

    protected static final String API_SONGS     = TestConstants.API_SONGS;
    protected static final String API_ALBUMS    = TestConstants.API_ALBUMS;
    protected static final String API_ARTISTS   = TestConstants.API_ARTISTS;
    protected static final String API_PLAYLISTS = TestConstants.API_PLAYLISTS;
    protected static final String API_USERS     = TestConstants.API_USERS;
    protected static final String API_AUTH      = TestConstants.API_AUTH;
    protected static final String API_FAVORITES = TestConstants.API_FAVORITES;
    protected static final String API_HISTORY   = TestConstants.API_HISTORY;
    protected static final String API_ANALYTICS = TestConstants.API_ANALYTICS;

    // ── Unauthenticated HTTP helpers ──────────────────────────────

    protected ResultActions getRequest(String url) throws Exception {
        return mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));
    }

    protected ResultActions postRequest(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    protected ResultActions putRequest(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    protected ResultActions deleteRequest(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .accept(MediaType.APPLICATION_JSON));
    }

    // ── Authenticated HTTP helpers (programmatic) ─────────────────

    protected ResultActions getRequestAs(String url, String username, String role) throws Exception {
        return mockMvc.perform(get(url)
                .with(user(username).roles(role))
                .accept(MediaType.APPLICATION_JSON));
    }

    protected ResultActions postRequestAs(String url, Object body, String username, String role) throws Exception {
        return mockMvc.perform(post(url)
                .with(user(username).roles(role))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    protected ResultActions putRequestAs(String url, Object body, String username, String role) throws Exception {
        return mockMvc.perform(put(url)
                .with(user(username).roles(role))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    protected ResultActions deleteRequestAs(String url, String username, String role) throws Exception {
        return mockMvc.perform(delete(url)
                .with(user(username).roles(role))
                .accept(MediaType.APPLICATION_JSON));
    }

    // ── Role shortcut helpers ─────────────────────────────────────

    protected ResultActions getRequestAsUser(String url) throws Exception {
        return getRequestAs(url, TestConstants.TEST_USER_USERNAME, TestConstants.TEST_USER_ROLE_USER);
    }

    protected ResultActions getRequestAsArtist(String url) throws Exception {
        return getRequestAs(url, TestConstants.TEST_USER_USERNAME, TestConstants.TEST_USER_ROLE_ARTIST);
    }

    protected ResultActions getRequestAsAdmin(String url) throws Exception {
        return getRequestAs(url, TestConstants.TEST_USER_USERNAME, TestConstants.TEST_USER_ROLE_ADMIN);
    }

    // ── Utility ───────────────────────────────────────────────────

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}