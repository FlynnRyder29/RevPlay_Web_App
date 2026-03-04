package com.revplay.integration;

import com.revplay.model.Artist;
import com.revplay.model.PlayEvent;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.PlayEventRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import com.revplay.util.IntegrationTestBase;
import com.revplay.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests — Analytics endpoints (Member 6).
 *
 * All three endpoints require ARTIST role:
 *   GET /api/artists/analytics/overview       total songs, plays, favorites
 *   GET /api/artists/analytics/songs          per-song play count breakdown
 *   GET /api/artists/analytics/top-listeners  top 10 listeners by play count
 *
 * Security — double-layer (SecurityConfig + @PreAuthorize("hasRole('ARTIST')")):
 *   ARTIST  → 200
 *   LISTENER → 403
 *   Unauthenticated → 401
 *
 * Principal resolution (AnalyticsService.getCurrentArtist):
 *   SecurityUtils.getCurrentUser() → SecurityContextHolder → getName()
 *   → userRepository lookup → artistRepository.findByUserId().
 *   DB must have a matching User+Artist chain for every happy-path test.
 *
 * Important query behaviours to test against:
 *   findSongPlayCountsByArtistId — uses GROUP BY song, so songs with 0 plays
 *   do NOT appear in the result. "No events" → empty list.
 *   findTopListenersByArtistId — WHERE pe.user IS NOT NULL, so anonymous
 *   play events are excluded from the top-listeners list.
 *   DB-level LIMIT via Pageable(0, 10) → at most 10 rows returned.
 *
 * @Transactional on IntegrationTestBase auto-rolls back after every test.
 */
@DisplayName("Analytics — Integration Tests")
class AnalyticsIT extends IntegrationTestBase {

    @Autowired private UserRepository      userRepository;
    @Autowired private ArtistRepository    artistRepository;
    @Autowired private SongRepository      songRepository;
    @Autowired private PlayEventRepository playEventRepository;

    private static final String OVERVIEW      = "/api/artists/analytics/overview";
    private static final String SONGS         = "/api/artists/analytics/songs";
    private static final String TOP_LISTENERS = "/api/artists/analytics/top-listeners";

    // Username placed in SecurityContext — must match seeded User row
    private static final String ARTIST_USERNAME = "it_analytics_artist";

    private User   artistUser;
    private Artist savedArtist;
    private Song   songGoldenHour;
    private Song   songOverdrive;

    // ── Seed helpers ──────────────────────────────────────────────────────────

    @BeforeEach
    void seedOwnershipChain() {
        // User → Artist → 2 Songs — seeded for every test in this class
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

        songGoldenHour = songRepository.save(
                TestDataBuilder.aSong()
                        .withId(null)
                        .withTitle("Golden Hour")
                        .withGenre("Indie")
                        .withArtist(savedArtist)
                        .withPlayCount(0L)
                        .build());

        songOverdrive = songRepository.save(
                TestDataBuilder.aSong()
                        .withId(null)
                        .withTitle("Overdrive")
                        .withGenre("Electronic")
                        .withArtist(savedArtist)
                        .withPlayCount(0L)
                        .build());
    }

    /**
     * Seeds N play events for the given song, attributed to a named listener.
     * Creates the listener User automatically so each call uses a distinct user.
     * Returns the listener User for assertions on userId/username.
     */
    private User seedPlayEvents(Song song, String listenerUsername, int count) {
        User listener = userRepository.save(
                TestDataBuilder.aUser()
                        .withId(null)
                        .withUsername(listenerUsername)
                        .withEmail(listenerUsername + "@revplay.com")
                        .asArtist()
                        .build());
        for (int i = 0; i < count; i++) {
            PlayEvent event = new PlayEvent();
            event.setSong(song);
            event.setUser(listener);
            playEventRepository.save(event);
        }
        return listener;
    }

    /**
     * Seeds one anonymous play event (user = null).
     * Tests that findTopListenersByArtistId excludes these via WHERE pe.user IS NOT NULL.
     */
    private void seedAnonymousPlayEvent(Song song) {
        PlayEvent event = new PlayEvent();
        event.setSong(song);
        event.setUser(null); // anonymous — allowed by schema
        playEventRepository.save(event);
    }

    /** ARTIST principal whose username matches ARTIST_USERNAME. */
    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asArtist() {
        return user(ARTIST_USERNAME).roles("ARTIST");
    }

    /** LISTENER principal — used to assert 403 on analytics endpoints. */
    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asListener() {
        return user("it_listener_analytics").roles("LISTENER");
    }

    // =========================================================================
    // GET /api/artists/analytics/overview
    // =========================================================================

    @Nested
    @DisplayName("GET /api/artists/analytics/overview")
    class GetOverview {

        @Test
        @DisplayName("seeded songs and play events - returns correct total counts")
        void getOverview_withSongsAndPlays_returnsCorrectTotals() throws Exception {
            // 3 plays on Golden Hour, 5 on Overdrive → totalPlays = 8
            seedPlayEvents(songGoldenHour, "ov_listener_1", 3);
            seedPlayEvents(songOverdrive,  "ov_listener_2", 5);

            mockMvc.perform(get(OVERVIEW).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSongs").value(2))
                    .andExpect(jsonPath("$.totalPlays").value(8));
        }

        @Test
        @DisplayName("no play events - totalPlays and totalFavorites are 0")
        void getOverview_noPlayEvents_countsAreZero() throws Exception {
            mockMvc.perform(get(OVERVIEW).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSongs").value(2))
                    .andExpect(jsonPath("$.totalPlays").value(0))
                    .andExpect(jsonPath("$.totalFavorites").value(0));
        }

        @Test
        @DisplayName("no songs at all - totalSongs is 0")
        void getOverview_noSongs_totalSongsIsZero() throws Exception {
            songRepository.deleteAll(); // remove songs seeded in @BeforeEach

            mockMvc.perform(get(OVERVIEW).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSongs").value(0))
                    .andExpect(jsonPath("$.totalPlays").value(0));
        }

        @Test
        @DisplayName("response contains artistId and artistName")
        void getOverview_responseContainsArtistIdentifiers() throws Exception {
            mockMvc.perform(get(OVERVIEW).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistId").value(savedArtist.getId()))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("songPlayCounts and topListeners are null in overview response")
        void getOverview_nestedListsAreNull() throws Exception {
            // getOverview() only sets totals — nested lists must not appear
            mockMvc.perform(get(OVERVIEW).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts").doesNotExist())
                    .andExpect(jsonPath("$.topListeners").doesNotExist());
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void getOverview_listenerRole_returns403() throws Exception {
            mockMvc.perform(get(OVERVIEW).with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void getOverview_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(OVERVIEW))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/artists/analytics/songs
    // =========================================================================

    @Nested
    @DisplayName("GET /api/artists/analytics/songs")
    class GetSongPlayCounts {

        @Test
        @DisplayName("with play events - songPlayCounts list has one entry per song played")
        void getSongPlayCounts_withPlayEvents_returnsPlayCountList() throws Exception {
            seedPlayEvents(songGoldenHour, "sp_listener_1", 4);
            seedPlayEvents(songOverdrive,  "sp_listener_2", 7);

            mockMvc.perform(get(SONGS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts").isArray())
                    .andExpect(jsonPath("$.songPlayCounts", hasSize(2)));
        }

        @Test
        @DisplayName("ordered by play count desc - highest played song first")
        void getSongPlayCounts_orderedByPlayCountDesc_highestFirst() throws Exception {
            // Overdrive: 7 plays, Golden Hour: 2 plays → Overdrive must be first
            seedPlayEvents(songGoldenHour, "sp_ord_1", 2);
            seedPlayEvents(songOverdrive,  "sp_ord_2", 7);

            mockMvc.perform(get(SONGS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts[0].songTitle").value("Overdrive"))
                    .andExpect(jsonPath("$.songPlayCounts[0].playCount").value(7))
                    .andExpect(jsonPath("$.songPlayCounts[1].songTitle").value("Golden Hour"))
                    .andExpect(jsonPath("$.songPlayCounts[1].playCount").value(2));
        }

        @Test
        @DisplayName("each SongPlayCountDTO entry maps all fields")
        void getSongPlayCounts_eachEntryMapsAllFields() throws Exception {
            seedPlayEvents(songGoldenHour, "sp_fields_1", 3);

            mockMvc.perform(get(SONGS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts[0].songId").exists())
                    .andExpect(jsonPath("$.songPlayCounts[0].songTitle").exists())
                    .andExpect(jsonPath("$.songPlayCounts[0].playCount").exists());
        }

        @Test
        @DisplayName("no play events - returns empty songPlayCounts list")
        void getSongPlayCounts_noPlayEvents_returnsEmptyList() throws Exception {
            // Songs exist but no PlayEvents — GROUP BY query returns no rows
            mockMvc.perform(get(SONGS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts").isArray())
                    .andExpect(jsonPath("$.songPlayCounts", hasSize(0)));
        }

        @Test
        @DisplayName("response contains artistId and artistName")
        void getSongPlayCounts_responseContainsArtistIdentifiers() throws Exception {
            mockMvc.perform(get(SONGS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistId").value(savedArtist.getId()))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("topListeners is null in songs response")
        void getSongPlayCounts_topListenersIsNull() throws Exception {
            // getSongPlayCounts() does not populate topListeners
            mockMvc.perform(get(SONGS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners").doesNotExist());
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void getSongPlayCounts_listenerRole_returns403() throws Exception {
            mockMvc.perform(get(SONGS).with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void getSongPlayCounts_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(SONGS))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/artists/analytics/top-listeners
    // =========================================================================

    @Nested
    @DisplayName("GET /api/artists/analytics/top-listeners")
    class GetTopListeners {

        @Test
        @DisplayName("with play events - returns topListeners list")
        void getTopListeners_withPlayEvents_returnsListenerList() throws Exception {
            seedPlayEvents(songGoldenHour, "tl_listener_1", 5);
            seedPlayEvents(songOverdrive,  "tl_listener_2", 3);

            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners").isArray())
                    .andExpect(jsonPath("$.topListeners", hasSize(2)));
        }

        @Test
        @DisplayName("ordered by play count desc - most active listener first")
        void getTopListeners_orderedByPlayCountDesc_mostActiveFirst() throws Exception {
            // heavy: 8 plays, light: 2 plays → heavy must be first
            seedPlayEvents(songGoldenHour, "tl_light", 2);
            seedPlayEvents(songGoldenHour, "tl_heavy", 8);

            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners[0].username").value("tl_heavy"))
                    .andExpect(jsonPath("$.topListeners[0].playCount").value(8))
                    .andExpect(jsonPath("$.topListeners[1].username").value("tl_light"))
                    .andExpect(jsonPath("$.topListeners[1].playCount").value(2));
        }

        @Test
        @DisplayName("each TopListenerDTO entry maps all fields")
        void getTopListeners_eachEntryMapsAllFields() throws Exception {
            seedPlayEvents(songGoldenHour, "tl_fields_1", 3);

            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners[0].userId").exists())
                    .andExpect(jsonPath("$.topListeners[0].username").exists())
                    .andExpect(jsonPath("$.topListeners[0].playCount").exists());
        }

        @Test
        @DisplayName("anonymous play events excluded from top listeners")
        void getTopListeners_anonymousPlaysExcluded_notInList() throws Exception {
            // 1 anonymous event + 1 identified listener → only 1 entry in list
            seedAnonymousPlayEvent(songGoldenHour);
            seedPlayEvents(songGoldenHour, "tl_real_listener", 2);

            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners", hasSize(1)))
                    .andExpect(jsonPath("$.topListeners[0].username")
                            .value("tl_real_listener"));
        }

        @Test
        @DisplayName("more than 10 listeners - DB-level limit returns at most 10")
        void getTopListeners_moreThan10Listeners_returnsAtMost10() throws Exception {
            // 12 distinct listeners with 1 play each
            for (int i = 1; i <= 12; i++) {
                seedPlayEvents(songGoldenHour, "tl_limit_" + i, 1);
            }

            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners",
                            hasSize(lessThanOrEqualTo(10))));
        }

        @Test
        @DisplayName("no play events - returns empty topListeners list")
        void getTopListeners_noPlayEvents_returnsEmptyList() throws Exception {
            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topListeners").isArray())
                    .andExpect(jsonPath("$.topListeners", hasSize(0)));
        }

        @Test
        @DisplayName("response contains artistId and artistName")
        void getTopListeners_responseContainsArtistIdentifiers() throws Exception {
            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistId").value(savedArtist.getId()))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("songPlayCounts is null in top-listeners response")
        void getTopListeners_songPlayCountsIsNull() throws Exception {
            // getTopListeners() does not populate songPlayCounts
            mockMvc.perform(get(TOP_LISTENERS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songPlayCounts").doesNotExist());
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void getTopListeners_listenerRole_returns403() throws Exception {
            mockMvc.perform(get(TOP_LISTENERS).with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void getTopListeners_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(TOP_LISTENERS))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/artists/analytics/trends
    // =========================================================================

    private static final String TRENDS = "/api/artists/analytics/trends";

    @Nested
    @DisplayName("GET /api/artists/analytics/trends")
    class GetTrends {

        @Test
        @DisplayName("daily period - returns trendPeriod and trends array")
        void getTrends_daily_returnsTrendPoints() throws Exception {
            seedPlayEvents(songGoldenHour, "trend_listener_1", 3);

            mockMvc.perform(get(TRENDS)
                            .param("period", "daily")
                            .with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trendPeriod").value("daily"))
                    .andExpect(jsonPath("$.trends").isArray());
        }

        @Test
        @DisplayName("weekly period - returns trendPeriod=weekly and trends array")
        void getTrends_weekly_returnsTrendPoints() throws Exception {
            seedPlayEvents(songOverdrive, "trend_listener_2", 5);

            mockMvc.perform(get(TRENDS)
                            .param("period", "weekly")
                            .with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trendPeriod").value("weekly"))
                    .andExpect(jsonPath("$.trends").isArray());
        }

        @Test
        @DisplayName("no play events - returns empty trends array")
        void getTrends_noPlayEvents_returnsEmptyArray() throws Exception {
            mockMvc.perform(get(TRENDS)
                            .param("period", "daily")
                            .with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trends").isArray());
        }

        @Test
        @DisplayName("response contains artistId and artistName")
        void getTrends_responseContainsArtistIdentifiers() throws Exception {
            mockMvc.perform(get(TRENDS)
                            .param("period", "daily")
                            .with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistId").value(savedArtist.getId()))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void getTrends_listenerRole_returns403() throws Exception {
            mockMvc.perform(get(TRENDS)
                            .param("period", "daily")
                            .with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void getTrends_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(TRENDS).param("period", "daily"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/artists/analytics/fans
    // =========================================================================

    private static final String FANS = "/api/artists/analytics/fans";

    @Nested
    @DisplayName("GET /api/artists/analytics/fans")
    class GetFans {

        @Test
        @DisplayName("with play events - returns fans list")
        void getFans_withPlayEvents_returnsFansList() throws Exception {
            seedPlayEvents(songGoldenHour, "fan_listener_1", 4);
            seedPlayEvents(songOverdrive,  "fan_listener_2", 2);

            mockMvc.perform(get(FANS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans").isArray())
                    .andExpect(jsonPath("$.fans", hasSize(2)));
        }

        @Test
        @DisplayName("no play events - returns empty fans list")
        void getFans_noPlayEvents_returnsEmptyList() throws Exception {
            mockMvc.perform(get(FANS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans").isArray())
                    .andExpect(jsonPath("$.fans", hasSize(0)));
        }

        @Test
        @DisplayName("anonymous plays excluded - only identified listeners in fans")
        void getFans_anonymousPlaysExcluded() throws Exception {
            seedAnonymousPlayEvent(songGoldenHour);
            seedPlayEvents(songGoldenHour, "fan_real_1", 3);

            mockMvc.perform(get(FANS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans", hasSize(1)));
        }

        @Test
        @DisplayName("response contains artistId and artistName")
        void getFans_responseContainsArtistIdentifiers() throws Exception {
            mockMvc.perform(get(FANS).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistId").value(savedArtist.getId()))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void getFans_listenerRole_returns403() throws Exception {
            mockMvc.perform(get(FANS).with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void getFans_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(FANS))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/artists/analytics/songs/{id}/fans
    // =========================================================================

    @Nested
    @DisplayName("GET /api/artists/analytics/songs/{id}/fans")
    class GetSongFans {

        @Test
        @DisplayName("song with play events - returns fans for that specific song")
        void getSongFans_withPlayEvents_returnsFans() throws Exception {
            seedPlayEvents(songGoldenHour, "sf_listener_1", 3);
            // Overdrive has no plays — should not appear in Golden Hour fans
            seedPlayEvents(songOverdrive,  "sf_listener_2", 2);

            mockMvc.perform(get("/api/artists/analytics/songs/"
                            + songGoldenHour.getId() + "/fans")
                            .with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans").isArray())
                    .andExpect(jsonPath("$.fans", hasSize(1)))
                    .andExpect(jsonPath("$.fans[0].username").value("sf_listener_1"));
        }

        @Test
        @DisplayName("song with no play events - returns empty fans list")
        void getSongFans_noPlayEvents_returnsEmptyList() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/"
                            + songGoldenHour.getId() + "/fans")
                            .with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fans").isArray())
                    .andExpect(jsonPath("$.fans", hasSize(0)));
        }

        @Test
        @DisplayName("non-existing song - returns 404")
        void getSongFans_nonExistingSong_returns404() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/99999/fans")
                            .with(asArtist()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("response contains songId in body")
        void getSongFans_responseContainsSongId() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/"
                            + songGoldenHour.getId() + "/fans")
                            .with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.songId").value(songGoldenHour.getId()));
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void getSongFans_listenerRole_returns403() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/"
                            + songGoldenHour.getId() + "/fans")
                            .with(asListener()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void getSongFans_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/artists/analytics/songs/"
                            + songGoldenHour.getId() + "/fans"))
                    .andExpect(status().isUnauthorized());
        }
    }
}