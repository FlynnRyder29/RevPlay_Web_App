package com.revplay.util;

import com.revplay.model.Role;

/**
 * Shared test constants for all RevPlay test classes.
 *
 * Usage:
 *   import static com.revplay.util.TestConstants.*;
 *
 *   String email = TEST_USER_EMAIL;
 *   Long   id    = TEST_SONG_ID;
 */
public final class TestConstants {

    private TestConstants() {
        // utility class — no instantiation
    }

    // ── User Constants ────────────────────────────────────────────

    public static final Long   TEST_USER_ID            = 1L;
    public static final String TEST_USER_EMAIL         = "testuser@revplay.com";
    public static final String TEST_USER_USERNAME      = "testuser";
    public static final String TEST_USER_PASSWORD      = "Password@123";
    public static final String TEST_USER_DISPLAY_NAME  = "Test User";

    // ── Artist Constants ──────────────────────────────────────────

    public static final Long   TEST_ARTIST_ID         = 1L;
    public static final Long   TEST_ARTIST_USER_ID    = 2L;
    public static final String TEST_ARTIST_NAME       = "Test Artist";
    public static final String TEST_ARTIST_BIO        = "Test artist bio.";
    public static final String TEST_ARTIST_GENRE      = "Indie";

    // ── Song Constants ────────────────────────────────────────────

    public static final Long   TEST_SONG_ID                    = 1L;
    public static final String TEST_SONG_TITLE                 = "Test Song";
    public static final String TEST_SONG_GENRE                 = "Pop";
    public static final int    TEST_SONG_DURATION              = 210;
    public static final String TEST_SONG_AUDIO_URL             = "/audio/test/test_song.mp3";
    public static final String TEST_SONG_VISIBILITY_PUBLIC     = "PUBLIC";
    public static final String TEST_SONG_VISIBILITY_UNLISTED   = "UNLISTED";

    // ── Album Constants ───────────────────────────────────────────

    public static final Long   TEST_ALBUM_ID          = 1L;
    public static final String TEST_ALBUM_NAME        = "Test Album";
    public static final String TEST_ALBUM_DESCRIPTION = "Test album description.";

    // ── Playlist Constants ────────────────────────────────────────

    public static final Long    TEST_PLAYLIST_ID      = 1L;
    public static final String  TEST_PLAYLIST_NAME    = "Test Playlist";
    public static final String  TEST_PLAYLIST_DESC    = "Test playlist description.";
    public static final boolean TEST_PLAYLIST_PUBLIC  = true;
    public static final boolean TEST_PLAYLIST_PRIVATE = false;

    // ── Pagination Constants ──────────────────────────────────────

    public static final int DEFAULT_PAGE      = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE     = 50;

    // ── API Base Paths ────────────────────────────────────────────

    public static final String API_BASE      = "/api";
    public static final String API_AUTH      = "/api/auth";
    public static final String API_USERS     = "/api/users";
    public static final String API_SONGS     = "/api/songs";
    public static final String API_ALBUMS    = "/api/albums";
    public static final String API_ARTISTS   = "/api/artists";
    public static final String API_PLAYLISTS = "/api/playlists";
    public static final String API_FAVORITES = "/api/favorites";
    public static final String API_HISTORY   = "/api/history";
    public static final String API_ANALYTICS = "/api/analytics";

    // ── Auth Header ───────────────────────────────────────────────

    public static final String AUTH_HEADER = "Authorization";

    // ── File Upload Constants ─────────────────────────────────────

    public static final String TEST_AUDIO_FILENAME  = "test_audio.mp3";
    public static final String TEST_IMAGE_FILENAME  = "test_cover.jpg";
    public static final String AUDIO_CONTENT_TYPE   = "audio/mpeg";
    public static final String IMAGE_CONTENT_TYPE   = "image/jpeg";
    public static final long   MAX_AUDIO_SIZE_BYTES = 50L * 1024 * 1024;
    public static final long   MAX_IMAGE_SIZE_BYTES = 5L  * 1024 * 1024;

    // ── Role Constants — String (for Spring Security MockMvc .roles() calls) ──
    // Used with: mockMvc.perform(get(url).with(user("name").roles(ROLE)))
    // Spring Security prepends ROLE_ internally: LISTENER → ROLE_LISTENER

    public static final String TEST_SECURITY_ROLE_LISTENER = "LISTENER";
    public static final String TEST_SECURITY_ROLE_ARTIST   = "ARTIST";
    public static final String TEST_SECURITY_ROLE_ADMIN    = "ADMIN";

    // ── Role Constants — Enum (for TestDataBuilder / entity-level usage) ──
    // Used when building User objects to set user.setRole(...)

    public static final Role TEST_ROLE_LISTENER = Role.LISTENER;
    public static final Role TEST_ROLE_ARTIST   = Role.ARTIST;
    public static final Role TEST_ROLE_ADMIN    = Role.ADMIN;
}