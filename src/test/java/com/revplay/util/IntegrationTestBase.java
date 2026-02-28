package com.revplay.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for RevPlay full-stack integration tests.
 *
 * Loads the FULL Spring application context with H2 in-memory DB.
 * Every test rolls back automatically via @Transactional — DB is
 * always clean at the start of each test.
 *
 * Use this base class when you need to test the full request/response
 * cycle including real DB calls, real service logic, and real security.
 * For controller-only tests (mocked services), use ControllerTestBase.
 * For service-only tests (no HTTP), use ServiceTestBase.
 *
 * ── SECURITY TESTING ─────────────────────────────────────────────
 *
 * Option A — Annotate each test with @WithMockUser (simplest):
 *
 *   @Test
 *   @WithMockUser(username = "alice", roles = {"LISTENER"})
 *   public void getFavorites_asListener_returnsOk() throws Exception {
 *       getRequest(API_FAVORITES).andExpect(status().isOk());
 *   }
 *
 *   @Test
 *   @WithMockUser(username = "aria", roles = {"ARTIST"})
 *   public void uploadSong_asArtist_returnsCreated() throws Exception {
 *       postRequest(API_SONGS, songDto).andExpect(status().isCreated());
 *   }
 *
 *   @Test
 *   @WithMockUser(username = "alice", roles = {"LISTENER"})
 *   public void uploadSong_asListener_returnsForbidden() throws Exception {
 *       postRequest(API_SONGS, songDto).andExpect(status().isForbidden());
 *   }
 *
 *   @Test
 *   public void getPublicSongs_unauthenticated_returnsOk() throws Exception {
 *       getRequest(API_SONGS).andExpect(status().isOk());
 *   }
 *
 *   @Test
 *   public void getPlaylists_unauthenticated_returnsUnauthorized() throws Exception {
 *       getRequest(API_PLAYLISTS).andExpect(status().isUnauthorized());
 *   }
 *
 * Option B — Use the helper methods with inline role (programmatic):
 *
 *   getRequestAs(API_FAVORITES, "alice", "LISTENER")
 *       .andExpect(status().isOk());
 *
 *   getRequestAs(API_SONGS, "aria_artist", "ARTIST")
 *       .andExpect(status().isOk());
 *
 * ── ROLE REFERENCE ────────────────────────────────────────────────
 *   LISTENER — standard user: browse, play, favorites, playlists
 *   ARTIST   — all listener features + upload songs/albums, analytics
 *   ADMIN    — full access
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ── Convenience API path constants ────────────────────────────

    protected static final String API_SONGS     = TestConstants.API_SONGS;
    protected static final String API_ALBUMS    = TestConstants.API_ALBUMS;
    protected static final String API_ARTISTS   = TestConstants.API_ARTISTS;
    protected static final String API_PLAYLISTS = TestConstants.API_PLAYLISTS;
    protected static final String API_USERS     = TestConstants.API_USERS;
    protected static final String API_AUTH      = TestConstants.API_AUTH;
    protected static final String API_FAVORITES = TestConstants.API_FAVORITES;
    protected static final String API_HISTORY   = TestConstants.API_HISTORY;
    protected static final String API_ANALYTICS = TestConstants.API_ANALYTICS;

    // ── Standard HTTP helpers (unauthenticated) ───────────────────

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

    // ── Security-aware HTTP helpers (Option B — programmatic) ─────

    /**
     * GET as a specific user with a given role.
     * Use when you need programmatic control over the authenticated user.
     *
     * Example:
     *   getRequestAs("/api/favorites", "alice", "LISTENER")
     *       .andExpect(status().isOk());
     */
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

    /** GET as a LISTENER role user */
    protected ResultActions getRequestAsListener(String url) throws Exception {
        return getRequestAs(url, TestConstants.TEST_USER_USERNAME, "LISTENER");
    }

    /** GET as an ARTIST role user */
    protected ResultActions getRequestAsArtist(String url) throws Exception {
        return getRequestAs(url, TestConstants.TEST_USER_USERNAME, "ARTIST");
    }

    /** GET as an ADMIN role user */
    protected ResultActions getRequestAsAdmin(String url) throws Exception {
        return getRequestAs(url, TestConstants.TEST_USER_USERNAME, "ADMIN");
    }

    // ── Utility ───────────────────────────────────────────────────

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}