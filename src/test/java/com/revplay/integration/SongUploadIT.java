package com.revplay.integration;

import com.revplay.model.Artist;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import com.revplay.util.IntegrationTestBase;
import com.revplay.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests — Song and Album upload/management endpoints (Member 6).
 *
 * Endpoints under test:
 *   POST   /api/artists/songs              upload a new song
 *   PUT    /api/artists/songs/{id}         update song metadata
 *   DELETE /api/artists/songs/{id}         delete own song
 *   PUT    /api/artists/songs/{id}/visibility  toggle song visibility
 *   POST   /api/artists/albums             create a new album
 *   PUT    /api/artists/albums/{id}        update album metadata
 *   DELETE /api/artists/albums/{id}        delete own album
 *
 * Security model:
 *   ARTIST  → allowed to manage their own songs and albums
 *   LISTENER → 403 on all write endpoints
 *   Unauthenticated → 401 on all endpoints
 *   ARTIST accessing another artist's resource → 403
 *
 * Principal resolution: endpoints use SecurityUtils.getCurrentUser()
 * → SecurityContextHolder → getName() → userRepository.findByEmailOrUsername().
 * Every test that reaches the service layer must have a User+Artist row in the DB
 * whose username matches the one placed in the SecurityContext.
 *
 * @Transactional on IntegrationTestBase auto-rolls back after every test.
 */
@DisplayName("Song & Album Upload — Integration Tests")
class SongUploadIT extends IntegrationTestBase {

    @Autowired private UserRepository   userRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private SongRepository   songRepository;

    private static final String SONGS_BASE  = "/api/artists/songs";
    private static final String ALBUMS_BASE = "/api/artists/albums";

    private static final String ARTIST_USERNAME       = "it_upload_artist";
    private static final String OTHER_ARTIST_USERNAME = "it_upload_other";

    private User   artistUser;
    private Artist savedArtist;
    private User   otherArtistUser;
    private Artist otherArtist;

    // ── Seed helpers ──────────────────────────────────────────────────────────

    @BeforeEach
    void seedArtists() {
        artistUser = userRepository.save(
                TestDataBuilder.aUser()
                        .withId(null)
                        .withUsername(ARTIST_USERNAME)
                        .withEmail(ARTIST_USERNAME + "@revplay.com")
                        .asArtist()
                        .build());

        savedArtist = artistRepository.save(
                TestDataBuilder.anArtist()
                        .withId(null)
                        .withUserId(artistUser.getId())
                        .withArtistName("Aria")
                        .withGenre("Indie")
                        .build());

        otherArtistUser = userRepository.save(
                TestDataBuilder.aUser()
                        .withId(null)
                        .withUsername(OTHER_ARTIST_USERNAME)
                        .withEmail(OTHER_ARTIST_USERNAME + "@revplay.com")
                        .asArtist()
                        .build());

        otherArtist = artistRepository.save(
                TestDataBuilder.anArtist()
                        .withId(null)
                        .withUserId(otherArtistUser.getId())
                        .withArtistName("Other Artist")
                        .withGenre("Pop")
                        .build());
    }

    /** Seed a song belonging to the primary artist. */
    private Song seedSong(Artist artist, String title) {
        return songRepository.save(
                TestDataBuilder.aSong()
                        .withId(null)
                        .withTitle(title)
                        .withGenre("Indie")
                        .withArtist(artist)
                        .withPlayCount(0L)
                        .build());
    }

    /** ARTIST principal whose username matches ARTIST_USERNAME. */
    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asArtist() {
        return user(ARTIST_USERNAME).roles("ARTIST");
    }

    /** Other ARTIST principal — used for cross-ownership 403 tests. */
    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asOtherArtist() {
        return user(OTHER_ARTIST_USERNAME).roles("ARTIST");
    }

    /** LISTENER principal — used to assert 403 on artist-only endpoints. */
    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asListener() {
        return user("it_upload_listener").roles("LISTENER");
    }

    // =========================================================================
    // POST /api/artists/songs — Upload Song
    // =========================================================================

    @Nested
    @DisplayName("POST /api/artists/songs")
    class UploadSong {

        @Test
        @DisplayName("valid request — returns 201 with song data")
        void uploadSong_validRequest_returns201() throws Exception {
            mockMvc.perform(post(SONGS_BASE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Golden Hour",
                                      "genre": "Indie",
                                      "duration": 214,
                                      "audioUrl": "https://storage.revplay.com/songs/golden_hour.mp3"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Golden Hour"))
                    .andExpect(jsonPath("$.genre").value("Indie"))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("valid request — song is linked to the authenticated artist")
        void uploadSong_linkedToAuthenticatedArtist() throws Exception {
            mockMvc.perform(post(SONGS_BASE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Overdrive",
                                      "genre": "Electronic",
                                      "duration": 198,
                                      "audioUrl": "https://storage.revplay.com/songs/overdrive.mp3"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.artistId").value(savedArtist.getId()))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("missing required title — returns 400")
        void uploadSong_missingTitle_returns400() throws Exception {
            mockMvc.perform(post(SONGS_BASE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "genre": "Indie",
                                      "duration": 214,
                                      "audioUrl": "https://storage.revplay.com/songs/notitle.mp3"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("missing required audioUrl — returns 400")
        void uploadSong_missingAudioUrl_returns400() throws Exception {
            mockMvc.perform(post(SONGS_BASE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "No Audio",
                                      "genre": "Indie",
                                      "duration": 214
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("LISTENER role — returns 403")
        void uploadSong_listenerRole_returns403() throws Exception {
            mockMvc.perform(post(SONGS_BASE)
                            .with(asListener())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"Blocked","genre":"Indie","duration":200,
                                     "audioUrl":"https://storage.revplay.com/songs/x.mp3"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void uploadSong_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(SONGS_BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"Ghost","genre":"Indie","duration":200,
                                     "audioUrl":"https://storage.revplay.com/songs/ghost.mp3"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PUT /api/artists/songs/{id} — Update Song
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/artists/songs/{id}")
    class UpdateSong {

        @Test
        @DisplayName("owner updates own song — returns 200 with updated fields")
        void updateSong_owner_returns200() throws Exception {
            Song song = seedSong(savedArtist, "Old Title");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId())
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"New Title","genre":"Electronic"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("New Title"))
                    .andExpect(jsonPath("$.genre").value("Electronic"));
        }

        @Test
        @DisplayName("other artist updates song they don't own — returns 403")
        void updateSong_notOwner_returns403() throws Exception {
            Song song = seedSong(savedArtist, "Aria Song");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId())
                            .with(asOtherArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"Hijacked Title"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("non-existing song — returns 404")
        void updateSong_notFound_returns404() throws Exception {
            mockMvc.perform(put(SONGS_BASE + "/99999")
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"Ghost Song"}
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void updateSong_unauthenticated_returns401() throws Exception {
            Song song = seedSong(savedArtist, "Some Song");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"Attempt"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // DELETE /api/artists/songs/{id} — Delete Song
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/artists/songs/{id}")
    class DeleteSong {

        @Test
        @DisplayName("owner deletes own song — returns 204 No Content")
        void deleteSong_owner_returns204() throws Exception {
            Song song = seedSong(savedArtist, "To Be Deleted");

            mockMvc.perform(delete(SONGS_BASE + "/" + song.getId())
                            .with(asArtist()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("other artist deletes song they don't own — returns 403")
        void deleteSong_notOwner_returns403() throws Exception {
            Song song = seedSong(savedArtist, "Protected Song");

            mockMvc.perform(delete(SONGS_BASE + "/" + song.getId())
                            .with(asOtherArtist()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("LISTENER deletes any song — returns 403")
        void deleteSong_listenerRole_returns403() throws Exception {
            Song song = seedSong(savedArtist, "Listener Target");

            mockMvc.perform(delete(SONGS_BASE + "/" + song.getId())
                            .with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("non-existing song — returns 404")
        void deleteSong_notFound_returns404() throws Exception {
            mockMvc.perform(delete(SONGS_BASE + "/99999")
                            .with(asArtist()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void deleteSong_unauthenticated_returns401() throws Exception {
            Song song = seedSong(savedArtist, "Unguarded Song");

            mockMvc.perform(delete(SONGS_BASE + "/" + song.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PUT /api/artists/songs/{id}/visibility — Toggle Visibility
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/artists/songs/{id}/visibility")
    class ToggleVisibility {

        @Test
        @DisplayName("owner sets visibility to UNLISTED — returns 200")
        void toggleVisibility_toUnlisted_returns200() throws Exception {
            Song song = seedSong(savedArtist, "Visible Song");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId() + "/visibility")
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"visibility":"UNLISTED"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.visibility").value("UNLISTED"));
        }

        @Test
        @DisplayName("owner sets visibility to PUBLIC — returns 200")
        void toggleVisibility_toPublic_returns200() throws Exception {
            Song song = seedSong(savedArtist, "Hidden Song");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId() + "/visibility")
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"visibility":"PUBLIC"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.visibility").value("PUBLIC"));
        }

        @Test
        @DisplayName("other artist toggles visibility of song they don't own — returns 403")
        void toggleVisibility_notOwner_returns403() throws Exception {
            Song song = seedSong(savedArtist, "Aria's Song");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId() + "/visibility")
                            .with(asOtherArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"visibility":"UNLISTED"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("LISTENER role — returns 403")
        void toggleVisibility_listenerRole_returns403() throws Exception {
            Song song = seedSong(savedArtist, "Song");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId() + "/visibility")
                            .with(asListener())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"visibility":"UNLISTED"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void toggleVisibility_unauthenticated_returns401() throws Exception {
            Song song = seedSong(savedArtist, "Song");

            mockMvc.perform(put(SONGS_BASE + "/" + song.getId() + "/visibility")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"visibility":"UNLISTED"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // POST /api/artists/albums — Create Album
    // =========================================================================

    @Nested
    @DisplayName("POST /api/artists/albums")
    class CreateAlbum {

        @Test
        @DisplayName("valid request — returns 201 with album data")
        void createAlbum_validRequest_returns201() throws Exception {
            mockMvc.perform(post(ALBUMS_BASE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Echoes",
                                      "description": "Debut album",
                                      "releaseDate": "2024-03-15"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Echoes"))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("missing required album name — returns 400")
        void createAlbum_missingName_returns400() throws Exception {
            mockMvc.perform(post(ALBUMS_BASE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"description":"No name album"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("LISTENER role — returns 403")
        void createAlbum_listenerRole_returns403() throws Exception {
            mockMvc.perform(post(ALBUMS_BASE)
                            .with(asListener())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Blocked Album"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void createAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(ALBUMS_BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Ghost Album"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PUT /api/artists/albums/{id} — Update Album
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/artists/albums/{id}")
    class UpdateAlbum {

        @Test
        @DisplayName("non-existing album — returns 404")
        void updateAlbum_notFound_returns404() throws Exception {
            mockMvc.perform(put(ALBUMS_BASE + "/99999")
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Ghost Album"}
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("LISTENER role — returns 403")
        void updateAlbum_listenerRole_returns403() throws Exception {
            mockMvc.perform(put(ALBUMS_BASE + "/1")
                            .with(asListener())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Blocked"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void updateAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(ALBUMS_BASE + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Attempt"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // DELETE /api/artists/albums/{id} — Delete Album
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/artists/albums/{id}")
    class DeleteAlbum {

        @Test
        @DisplayName("LISTENER role — returns 403")
        void deleteAlbum_listenerRole_returns403() throws Exception {
            mockMvc.perform(delete(ALBUMS_BASE + "/1")
                            .with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("non-existing album — returns 404")
        void deleteAlbum_notFound_returns404() throws Exception {
            mockMvc.perform(delete(ALBUMS_BASE + "/99999")
                            .with(asArtist()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("unauthenticated — returns 401")
        void deleteAlbum_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete(ALBUMS_BASE + "/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}