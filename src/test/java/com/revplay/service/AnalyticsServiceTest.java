package com.revplay.service;

import com.revplay.dto.AnalyticsDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.FavoriteRepository;
import com.revplay.repository.PlayEventRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import com.revplay.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static com.revplay.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

    @Mock private PlayEventRepository playEventRepository;
    @Mock private FavoriteRepository  favoriteRepository;
    @Mock private SongRepository      songRepository;
    @Mock private ArtistRepository    artistRepository;
    @Mock private SecurityUtils       securityUtils;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User   artistUser;
    private Artist artist;

    @BeforeEach
    void setUp() {
        artistUser = TestDataBuilder.aUser()
                .withId(TEST_ARTIST_USER_ID)
                .withUsername(TEST_USER_USERNAME)
                .withRole(com.revplay.model.Role.ARTIST)
                .build();

        artist = TestDataBuilder.anArtist()
                .withId(TEST_ARTIST_ID)
                .withArtistName(TEST_ARTIST_NAME)
                .withUser(artistUser)
                .build();

        lenient().when(securityUtils.getCurrentUser()).thenReturn(artistUser);
        lenient().when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                .thenReturn(Optional.of(artist));
    }

    // ══════════════════════════════════════════════════════════════════════
    // getOverview
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getOverview")
    class GetOverview {

        @Test
        @DisplayName("returns artist identifiers")
        void getOverview_returnsArtistIdentifiers() {
            when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
            when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
            when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);

            AnalyticsDTO result = analyticsService.getOverview();

            assertEquals(TEST_ARTIST_ID,   result.getArtistId());
            assertEquals(TEST_ARTIST_NAME, result.getArtistName());
        }

        @Test
        @DisplayName("returns correct counts from repositories")
        void getOverview_returnsCorrectCountsFromRepositories() {
            when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(5L);
            when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(100L);
            when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(20L);

            AnalyticsDTO result = analyticsService.getOverview();

            assertEquals(5L,   result.getTotalSongs());
            assertEquals(100L, result.getTotalPlays());
            assertEquals(20L,  result.getTotalFavorites());
        }

        @Test
        @DisplayName("all counts zero when no data")
        void getOverview_songPlayCountsAndTopListenersAreNull() {
            when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
            when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
            when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);

            AnalyticsDTO result = analyticsService.getOverview();

            assertEquals(0L, result.getTotalSongs());
            assertEquals(0L, result.getTotalPlays());
            assertEquals(0L, result.getTotalFavorites());
        }

        @Test
        @DisplayName("calls all three count repos with correct artistId")
        void getOverview_callsAllThreeCountReposWithCorrectArtistId() {
            when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
            when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
            when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);

            analyticsService.getOverview();

            verify(songRepository).countByArtistId(TEST_ARTIST_ID);
            verify(playEventRepository).countByArtistId(TEST_ARTIST_ID);
            verify(favoriteRepository).countByArtistId(TEST_ARTIST_ID);
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void getOverview_noArtistProfile_throwsResourceNotFoundException() {
            when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getOverview());
        }

        @Test
        @DisplayName("SecurityUtils throws — propagates exception")
        void getOverview_securityUtilsThrows_propagatesException() {
            when(securityUtils.getCurrentUser())
                    .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getOverview());

            verifyNoInteractions(artistRepository, songRepository);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getSongPlayCounts
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSongPlayCounts")
    class GetSongPlayCounts {

        @Test
        @DisplayName("maps all row fields correctly")
        void getSongPlayCounts_mapsAllRowFieldsCorrectly() {
            Object[] row = {1L, "Midnight Run", "http://cover.jpg", 42L};
            when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.<Object[]>of(row));

            AnalyticsDTO result = analyticsService.getSongPlayCounts();

            assertNotNull(result.getSongPlayCounts());
            assertEquals(1, result.getSongPlayCounts().size());
            AnalyticsDTO.SongPlayCountDTO dto = result.getSongPlayCounts().get(0);
            assertEquals(1L,                 dto.getSongId());
            assertEquals("Midnight Run",     dto.getSongTitle());
            assertEquals("http://cover.jpg", dto.getCoverImageUrl());
            assertEquals(42L,                dto.getPlayCount());
        }

        @Test
        @DisplayName("returns artist identifiers")
        void getSongPlayCounts_returnsArtistIdentifiers() {
            when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getSongPlayCounts();

            assertEquals(TEST_ARTIST_ID,   result.getArtistId());
            assertEquals(TEST_ARTIST_NAME, result.getArtistName());
        }

        @Test
        @DisplayName("no play events — returns empty list")
        void getSongPlayCounts_noPlayEvents_returnsEmptyList() {
            when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getSongPlayCounts();

            assertNotNull(result.getSongPlayCounts());
            assertTrue(result.getSongPlayCounts().isEmpty());
        }

        @Test
        @DisplayName("multiple songs preserves repository order")
        void getSongPlayCounts_multipleSongs_preservesRepositoryOrder() {
            Object[] row1 = {1L, "Song A", null, 100L};
            Object[] row2 = {2L, "Song B", null, 50L};
            Object[] row3 = {3L, "Song C", null, 25L};
            when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.of(row1, row2, row3));

            AnalyticsDTO result = analyticsService.getSongPlayCounts();

            assertEquals(3, result.getSongPlayCounts().size());
            assertEquals("Song A", result.getSongPlayCounts().get(0).getSongTitle());
            assertEquals("Song C", result.getSongPlayCounts().get(2).getSongTitle());
        }

        @Test
        @DisplayName("topListeners is null (not populated in this endpoint)")
        void getSongPlayCounts_topListenersIsNull() {
            when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.of());

            assertNull(analyticsService.getSongPlayCounts().getTopListeners());
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void getSongPlayCounts_noArtistProfile_throwsResourceNotFoundException() {
            when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getSongPlayCounts());

            verify(playEventRepository, never()).findSongPlayCountsByArtistId(any());
        }

        @Test
        @DisplayName("SecurityUtils throws — propagates exception")
        void getSongPlayCounts_securityUtilsThrows_propagatesException() {
            when(securityUtils.getCurrentUser())
                    .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getSongPlayCounts());

            verifyNoInteractions(artistRepository, playEventRepository);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getTopListeners
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTopListeners")
    class GetTopListeners {

        @Test
        @DisplayName("maps all row fields correctly")
        void getTopListeners_mapsAllRowFieldsCorrectly() {
            Object[] row = {5L, "listener1", "Listener One", 30L};
            when(playEventRepository.findTopListenersByArtistId(
                    eq(TEST_ARTIST_ID), any(PageRequest.class)))
                    .thenReturn(List.<Object[]>of(row));

            AnalyticsDTO result = analyticsService.getTopListeners();

            assertNotNull(result.getTopListeners());
            AnalyticsDTO.TopListenerDTO dto = result.getTopListeners().get(0);
            assertEquals(5L,             dto.getUserId());
            assertEquals("listener1",    dto.getUsername());
            assertEquals("Listener One", dto.getDisplayName());
            assertEquals(30L,            dto.getPlayCount());
        }

        @Test
        @DisplayName("returns artist identifiers")
        void getTopListeners_returnsArtistIdentifiers() {
            when(playEventRepository.findTopListenersByArtistId(
                    eq(TEST_ARTIST_ID), any(PageRequest.class)))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getTopListeners();

            assertEquals(TEST_ARTIST_ID,   result.getArtistId());
            assertEquals(TEST_ARTIST_NAME, result.getArtistName());
        }

        @Test
        @DisplayName("no play events — returns empty list")
        void getTopListeners_noPlayEvents_returnsEmptyList() {
            when(playEventRepository.findTopListenersByArtistId(
                    eq(TEST_ARTIST_ID), any(PageRequest.class)))
                    .thenReturn(List.of());

            assertTrue(analyticsService.getTopListeners().getTopListeners().isEmpty());
        }

        @Test
        @DisplayName("uses PageRequest with limit of 10")
        void getTopListeners_usesPageRequestWithLimitOfTen() {
            when(playEventRepository.findTopListenersByArtistId(
                    eq(TEST_ARTIST_ID), any(PageRequest.class)))
                    .thenReturn(List.of());

            analyticsService.getTopListeners();

            verify(playEventRepository).findTopListenersByArtistId(
                    TEST_ARTIST_ID, PageRequest.of(0, 10));
        }

        @Test
        @DisplayName("songPlayCounts is null (not populated in this endpoint)")
        void getTopListeners_songPlayCountsIsNull() {
            when(playEventRepository.findTopListenersByArtistId(
                    eq(TEST_ARTIST_ID), any(PageRequest.class)))
                    .thenReturn(List.of());

            assertNull(analyticsService.getTopListeners().getSongPlayCounts(),
                    "songPlayCounts should be null in getTopListeners response");
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void getTopListeners_noArtistProfile_throwsResourceNotFoundException() {
            when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getTopListeners());

            verify(playEventRepository, never())
                    .findTopListenersByArtistId(any(), any());
        }

        @Test
        @DisplayName("SecurityUtils throws — propagates ResourceNotFoundException")
        void getTopListeners_securityUtilsThrows_propagatesException() {
            when(securityUtils.getCurrentUser())
                    .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getTopListeners());

            verifyNoInteractions(artistRepository, playEventRepository);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getListeningTrends
    // Source: switch on period.toLowerCase() → daily / weekly / monthly
    //         default falls back to daily (with log.warn for unknown values)
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getListeningTrends")
    class GetListeningTrends {

        @Test
        @DisplayName("period=daily — calls findDailyTrendsByArtistId")
        void getListeningTrends_daily_callsDailyRepo() {
            when(playEventRepository.findDailyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any()))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getListeningTrends("daily");

            assertEquals("daily", result.getTrendPeriod());
            verify(playEventRepository).findDailyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any());
            verify(playEventRepository, never()).findWeeklyTrendsByArtistId(any(), any(), any());
            verify(playEventRepository, never()).findMonthlyTrendsByArtistId(any(), any(), any());
        }

        @Test
        @DisplayName("period=weekly — calls findWeeklyTrendsByArtistId")
        void getListeningTrends_weekly_callsWeeklyRepo() {
            when(playEventRepository.findWeeklyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any()))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getListeningTrends("weekly");

            assertEquals("weekly", result.getTrendPeriod());
            verify(playEventRepository).findWeeklyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any());
            verify(playEventRepository, never()).findDailyTrendsByArtistId(any(), any(), any());
        }

        @Test
        @DisplayName("period=monthly — calls findMonthlyTrendsByArtistId")
        void getListeningTrends_monthly_callsMonthlyRepo() {
            when(playEventRepository.findMonthlyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any()))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getListeningTrends("monthly");

            assertEquals("monthly", result.getTrendPeriod());
            verify(playEventRepository).findMonthlyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any());
        }

        @Test
        @DisplayName("unknown period — falls back to daily")
        void getListeningTrends_unknownPeriod_fallsBackToDaily() {
            // default branch in switch hits daily
            when(playEventRepository.findDailyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any()))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getListeningTrends("bogus");

            // period is stored lowercased as-is from the parameter
            assertEquals("bogus", result.getTrendPeriod());
            verify(playEventRepository).findDailyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any());
        }

        @Test
        @DisplayName("maps trend row fields: period + playCount")
        void getListeningTrends_mapsRowFieldsCorrectly() {
            Object[] row = {"2025-03-04", 15L};
            when(playEventRepository.findDailyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any()))
                    .thenReturn(List.<Object[]>of(row));

            AnalyticsDTO result = analyticsService.getListeningTrends("daily");

            assertNotNull(result.getTrends());
            assertEquals(1, result.getTrends().size());
            assertEquals("2025-03-04", result.getTrends().get(0).getPeriod());
            assertEquals(15L,          result.getTrends().get(0).getPlayCount());
        }

        @Test
        @DisplayName("no play events — returns empty trends list")
        void getListeningTrends_noPlayEvents_returnsEmptyList() {
            when(playEventRepository.findDailyTrendsByArtistId(
                    eq(TEST_ARTIST_ID), any(), any()))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getListeningTrends("daily");

            assertNotNull(result.getTrends());
            assertTrue(result.getTrends().isEmpty());
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void getListeningTrends_noArtistProfile_throwsResourceNotFoundException() {
            when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getListeningTrends("daily"));
        }

        @Test
        @DisplayName("SecurityUtils throws — propagates exception")
        void getListeningTrends_securityUtilsThrows_propagatesException() {
            when(securityUtils.getCurrentUser())
                    .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getListeningTrends("daily"));

            verifyNoInteractions(playEventRepository);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getFans
    // Source: favoriteRepository.findFansByArtistId(artistId)
    //         row: [userId, username, displayName, profilePictureUrl, favoriteCount]
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getFans")
    class GetFans {

        @Test
        @DisplayName("maps fan row fields correctly")
        void getFans_mapsFanRowFieldsCorrectly() {
            Object[] row = {7L, "superfan", "Super Fan", "http://pic.jpg", 5L};
            when(favoriteRepository.findFansByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.<Object[]>of(row));

            AnalyticsDTO result = analyticsService.getFans();

            assertEquals(1, result.getFans().size());
            AnalyticsDTO.FanDTO dto = result.getFans().get(0);
            assertEquals(7L,               dto.getUserId());
            assertEquals("superfan",       dto.getUsername());
            assertEquals("Super Fan",      dto.getDisplayName());
            assertEquals("http://pic.jpg", dto.getProfilePictureUrl());
            assertEquals(5L,               dto.getFavoriteCount());
        }

        @Test
        @DisplayName("no favorites — returns empty list")
        void getFans_noFavorites_returnsEmptyList() {
            when(favoriteRepository.findFansByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.of());

            assertTrue(analyticsService.getFans().getFans().isEmpty());
        }

        @Test
        @DisplayName("null profilePictureUrl — safeString returns null")
        void getFans_nullProfilePicture_handledSafely() {
            Object[] row = {7L, "user1", "User One", null, 3L};
            when(favoriteRepository.findFansByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.<Object[]>of(row));

            assertNull(analyticsService.getFans().getFans().get(0).getProfilePictureUrl());
        }

        @Test
        @DisplayName("returns artist identifiers")
        void getFans_returnsArtistIdentifiers() {
            when(favoriteRepository.findFansByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getFans();

            assertEquals(TEST_ARTIST_ID,   result.getArtistId());
            assertEquals(TEST_ARTIST_NAME, result.getArtistName());
        }

        @Test
        @DisplayName("multiple fans — preserves repository order")
        void getFans_multipleFans_preservesOrder() {
            Object[] row1 = {1L, "fan_a", "Fan A", null, 10L};
            Object[] row2 = {2L, "fan_b", "Fan B", null, 5L};
            when(favoriteRepository.findFansByArtistId(TEST_ARTIST_ID))
                    .thenReturn(List.of(row1, row2));

            AnalyticsDTO result = analyticsService.getFans();

            assertEquals(2, result.getFans().size());
            assertEquals("fan_a", result.getFans().get(0).getUsername());
            assertEquals("fan_b", result.getFans().get(1).getUsername());
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void getFans_noArtistProfile_throwsResourceNotFoundException() {
            when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getFans());

            verify(favoriteRepository, never()).findFansByArtistId(any());
        }

        @Test
        @DisplayName("SecurityUtils throws — propagates exception")
        void getFans_securityUtilsThrows_propagatesException() {
            when(securityUtils.getCurrentUser())
                    .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getFans());

            verifyNoInteractions(favoriteRepository);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getSongFans
    // Source: songRepository.existsByIdAndArtistId → throw 404 if false
    //         favoriteRepository.findFansBySongId
    //         row: [userId, username, displayName, profilePictureUrl, favoriteCount]
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSongFans")
    class GetSongFans {

        private static final Long SONG_ID = 42L;

        @Test
        @DisplayName("maps song fan row fields correctly")
        void getSongFans_mapsFanRowFieldsCorrectly() {
            when(songRepository.existsByIdAndArtistId(SONG_ID, TEST_ARTIST_ID)).thenReturn(true);
            Object[] row = {9L, "superfan2", "Super Fan 2", "http://img.png", 2L};
            when(favoriteRepository.findFansBySongId(SONG_ID)).thenReturn(List.<Object[]>of(row));

            AnalyticsDTO result = analyticsService.getSongFans(SONG_ID);

            assertEquals(1, result.getFans().size());
            AnalyticsDTO.FanDTO dto = result.getFans().get(0);
            assertEquals(9L,               dto.getUserId());
            assertEquals("superfan2",      dto.getUsername());
            assertEquals("Super Fan 2",    dto.getDisplayName());
            assertEquals("http://img.png", dto.getProfilePictureUrl());
            assertEquals(2L,               dto.getFavoriteCount());
        }

        @Test
        @DisplayName("song not owned by artist — throws ResourceNotFoundException")
        void getSongFans_songNotOwnedByArtist_throwsResourceNotFoundException() {
            when(songRepository.existsByIdAndArtistId(SONG_ID, TEST_ARTIST_ID)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getSongFans(SONG_ID));

            verify(favoriteRepository, never()).findFansBySongId(any());
        }

        @Test
        @DisplayName("no favorites for song — returns empty list")
        void getSongFans_noFavorites_returnsEmptyList() {
            when(songRepository.existsByIdAndArtistId(SONG_ID, TEST_ARTIST_ID)).thenReturn(true);
            when(favoriteRepository.findFansBySongId(SONG_ID)).thenReturn(List.of());

            assertTrue(analyticsService.getSongFans(SONG_ID).getFans().isEmpty());
        }

        @Test
        @DisplayName("returns artist identifiers in response")
        void getSongFans_returnsArtistIdentifiers() {
            when(songRepository.existsByIdAndArtistId(SONG_ID, TEST_ARTIST_ID)).thenReturn(true);
            when(favoriteRepository.findFansBySongId(SONG_ID)).thenReturn(List.of());

            AnalyticsDTO result = analyticsService.getSongFans(SONG_ID);

            assertEquals(TEST_ARTIST_ID,   result.getArtistId());
            assertEquals(TEST_ARTIST_NAME, result.getArtistName());
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void getSongFans_noArtistProfile_throwsResourceNotFoundException() {
            when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getSongFans(SONG_ID));
        }

        @Test
        @DisplayName("SecurityUtils throws — propagates exception")
        void getSongFans_securityUtilsThrows_propagatesException() {
            when(securityUtils.getCurrentUser())
                    .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

            assertThrows(ResourceNotFoundException.class,
                    () -> analyticsService.getSongFans(SONG_ID));
        }
    }
}