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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for AnalyticsService.
 *
 * ── Auth chain ──────────────────────────────────────────────────────────────
 * All three methods call getCurrentArtist() internally:
 *   1. securityUtils.getCurrentUser()    → User
 *   2. artistRepository.findByUserId()   → Optional<Artist>
 *
 * Both steps can fail with ResourceNotFoundException.
 * Each method has dedicated negative tests for both failure points.
 *
 * ── Object[] row mapping ────────────────────────────────────────────────────
 * JPQL projection queries return Object[] rows. Values at index 0 and 3
 * are returned as Long by most JDBC drivers but the service casts via
 * ((Number) row[x]).longValue() to be DB-agnostic.
 * Test rows use Long values directly — this matches the cast correctly.
 *
 * ── TestDataBuilder ─────────────────────────────────────────────────────────
 * Uses project-standard TestDataBuilder and TestConstants to keep fixtures
 * consistent with the rest of the test suite.
 */
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

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private User   currentUser;
    private Artist currentArtist;

    @BeforeEach
    void setUp() {
        // Artist user — role ARTIST, id = TEST_ARTIST_USER_ID
        currentUser = TestDataBuilder.aUser()
                .withId(TEST_ARTIST_USER_ID)
                .withUsername(TEST_USER_USERNAME)
                .asArtist()
                .build();

        // Artist profile linked to currentUser
        currentArtist = TestDataBuilder.anArtist()
                .withId(TEST_ARTIST_ID)
                .withUserId(TEST_ARTIST_USER_ID)
                .withArtistName(TEST_ARTIST_NAME)
                .build();

        // Default happy-path: current user resolves to currentArtist
        lenient().when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        lenient().when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                .thenReturn(Optional.of(currentArtist));
    }

    // =========================================================================
    // getOverview
    // =========================================================================

    @Test
    @DisplayName("getOverview - returns artistId and artistName in response")
    void getOverview_returnsArtistIdentifiers() {
        // Given
        when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(5L);
        when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(1000L);
        when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(200L);

        // When
        AnalyticsDTO result = analyticsService.getOverview();

        // Then
        assertEquals(TEST_ARTIST_ID,   result.getArtistId());
        assertEquals(TEST_ARTIST_NAME, result.getArtistName());
    }

    @Test
    @DisplayName("getOverview - returns correct totalSongs, totalPlays, totalFavorites from repos")
    void getOverview_returnsCorrectCountsFromRepositories() {
        // Given
        when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(8L);
        when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(31200L);
        when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(450L);

        // When
        AnalyticsDTO result = analyticsService.getOverview();

        // Then
        assertEquals(8L,     result.getTotalSongs());
        assertEquals(31200L, result.getTotalPlays());
        assertEquals(450L,   result.getTotalFavorites());
    }

    @Test
    @DisplayName("getOverview - songPlayCounts and topListeners are null (not populated in overview)")
    void getOverview_songPlayCountsAndTopListenersAreNull() {
        // Given
        when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(3L);
        when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(500L);
        when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(50L);

        // When
        AnalyticsDTO result = analyticsService.getOverview();

        // Then — overview does not populate detail-only fields
        assertNull(result.getSongPlayCounts(),
                "songPlayCounts should be null in overview response");
        assertNull(result.getTopListeners(),
                "topListeners should be null in overview response");
    }

    @Test
    @DisplayName("getOverview - calls all three count repositories with correct artistId")
    void getOverview_callsAllThreeCountReposWithCorrectArtistId() {
        // Given
        when(songRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
        when(playEventRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);
        when(favoriteRepository.countByArtistId(TEST_ARTIST_ID)).thenReturn(0L);

        // When
        analyticsService.getOverview();

        // Then — all three must be called with the artist's id, not the user's id
        verify(songRepository).countByArtistId(TEST_ARTIST_ID);
        verify(playEventRepository).countByArtistId(TEST_ARTIST_ID);
        verify(favoriteRepository).countByArtistId(TEST_ARTIST_ID);
    }

    @Test
    @DisplayName("getOverview - no artist profile for current user - throws ResourceNotFoundException")
    void getOverview_noArtistProfile_throwsResourceNotFoundException() {
        // Given — user is authenticated but has no artist profile
        when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.getOverview());

        // Repositories must never be queried if artist resolution fails
        verifyNoInteractions(songRepository, playEventRepository, favoriteRepository);
    }

    @Test
    @DisplayName("getOverview - SecurityUtils throws - propagates ResourceNotFoundException")
    void getOverview_securityUtilsThrows_propagatesException() {
        // Given — no authenticated user
        when(securityUtils.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.getOverview());

        verifyNoInteractions(artistRepository, songRepository,
                playEventRepository, favoriteRepository);
    }

    // =========================================================================
    // getSongPlayCounts
    // =========================================================================

    @Test
    @DisplayName("getSongPlayCounts - maps all four row fields correctly")
    void getSongPlayCounts_mapsAllRowFieldsCorrectly() {
        // Given — Object[] row: {songId, songTitle, coverImageUrl, playCount}
        Object[] row = {101L, "Golden Hour", "/covers/golden_hour.jpg", 18420L};
        when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                .thenReturn(List.<Object[]>of(row));

        // When
        AnalyticsDTO result = analyticsService.getSongPlayCounts();

        // Then
        assertEquals(1, result.getSongPlayCounts().size());
        AnalyticsDTO.SongPlayCountDTO dto = result.getSongPlayCounts().get(0);
        assertEquals(101L,                     dto.getSongId());
        assertEquals("Golden Hour",            dto.getSongTitle());
        assertEquals("/covers/golden_hour.jpg",dto.getCoverImageUrl());
        assertEquals(18420L,                   dto.getPlayCount());
    }

    @Test
    @DisplayName("getSongPlayCounts - returns artistId and artistName in response")
    void getSongPlayCounts_returnsArtistIdentifiers() {
        // Given
        when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                .thenReturn(List.of());

        // When
        AnalyticsDTO result = analyticsService.getSongPlayCounts();

        // Then
        assertEquals(TEST_ARTIST_ID,   result.getArtistId());
        assertEquals(TEST_ARTIST_NAME, result.getArtistName());
    }

    @Test
    @DisplayName("getSongPlayCounts - artist with no play events returns empty list")
    void getSongPlayCounts_noPlayEvents_returnsEmptyList() {
        // Given
        when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                .thenReturn(List.of());

        // When
        AnalyticsDTO result = analyticsService.getSongPlayCounts();

        // Then
        assertNotNull(result.getSongPlayCounts());
        assertTrue(result.getSongPlayCounts().isEmpty());
    }

    @Test
    @DisplayName("getSongPlayCounts - multiple songs preserve order from repository (highest plays first)")
    void getSongPlayCounts_multipleSongs_preservesRepositoryOrder() {
        // Given — repo returns in descending play count order (DB handles ordering)
        Object[] song1 = {1L, "Overdrive",    "/covers/pulse.jpg",     31200L};
        Object[] song2 = {2L, "Resonance",    "/covers/pulse.jpg",     22800L};
        Object[] song3 = {3L, "Midnight Grid","/covers/pulse.jpg",     17500L};
        when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                .thenReturn(List.of(song1, song2, song3));

        // When
        AnalyticsDTO result = analyticsService.getSongPlayCounts();

        // Then — service must preserve the order returned by the repository
        assertEquals(3,       result.getSongPlayCounts().size());
        assertEquals(31200L,  result.getSongPlayCounts().get(0).getPlayCount());
        assertEquals(22800L,  result.getSongPlayCounts().get(1).getPlayCount());
        assertEquals(17500L,  result.getSongPlayCounts().get(2).getPlayCount());
    }

    @Test
    @DisplayName("getSongPlayCounts - topListeners is null (not populated in this endpoint)")
    void getSongPlayCounts_topListenersIsNull() {
        // Given
        when(playEventRepository.findSongPlayCountsByArtistId(TEST_ARTIST_ID))
                .thenReturn(List.of());

        // When
        AnalyticsDTO result = analyticsService.getSongPlayCounts();

        // Then
        assertNull(result.getTopListeners(),
                "topListeners should be null in getSongPlayCounts response");
    }

    @Test
    @DisplayName("getSongPlayCounts - no artist profile - throws ResourceNotFoundException")
    void getSongPlayCounts_noArtistProfile_throwsResourceNotFoundException() {
        // Given
        when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.getSongPlayCounts());

        verify(playEventRepository, never()).findSongPlayCountsByArtistId(any());
    }

    @Test
    @DisplayName("getSongPlayCounts - SecurityUtils throws - propagates ResourceNotFoundException")
    void getSongPlayCounts_securityUtilsThrows_propagatesException() {
        // Given
        when(securityUtils.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.getSongPlayCounts());

        verifyNoInteractions(artistRepository, playEventRepository);
    }

    // =========================================================================
    // getTopListeners
    // =========================================================================

    @Test
    @DisplayName("getTopListeners - maps all four row fields correctly")
    void getTopListeners_mapsAllRowFieldsCorrectly() {
        // Given — Object[] row: {userId, username, displayName, playCount}
        Object[] row = {10L, "alice_music", "Alice", 42L};
        when(playEventRepository.findTopListenersByArtistId(
                eq(TEST_ARTIST_ID), any(PageRequest.class)))
                .thenReturn(List.<Object[]>of(row));

        // When
        AnalyticsDTO result = analyticsService.getTopListeners();

        // Then
        assertEquals(1, result.getTopListeners().size());
        AnalyticsDTO.TopListenerDTO dto = result.getTopListeners().get(0);
        assertEquals(10L,          dto.getUserId());
        assertEquals("alice_music", dto.getUsername());
        assertEquals("Alice",      dto.getDisplayName());
        assertEquals(42L,          dto.getPlayCount());
    }

    @Test
    @DisplayName("getTopListeners - returns artistId and artistName in response")
    void getTopListeners_returnsArtistIdentifiers() {
        // Given
        when(playEventRepository.findTopListenersByArtistId(
                eq(TEST_ARTIST_ID), any(PageRequest.class)))
                .thenReturn(List.of());

        // When
        AnalyticsDTO result = analyticsService.getTopListeners();

        // Then
        assertEquals(TEST_ARTIST_ID,   result.getArtistId());
        assertEquals(TEST_ARTIST_NAME, result.getArtistName());
    }

    @Test
    @DisplayName("getTopListeners - no play events returns empty list")
    void getTopListeners_noPlayEvents_returnsEmptyList() {
        // Given
        when(playEventRepository.findTopListenersByArtistId(
                eq(TEST_ARTIST_ID), any(PageRequest.class)))
                .thenReturn(List.of());

        // When
        AnalyticsDTO result = analyticsService.getTopListeners();

        // Then
        assertNotNull(result.getTopListeners());
        assertTrue(result.getTopListeners().isEmpty());
    }

    @Test
    @DisplayName("getTopListeners - uses PageRequest.of(0, 10) for DB-level limit")
    void getTopListeners_usesPageRequestWithLimitOfTen() {
        // Given
        when(playEventRepository.findTopListenersByArtistId(
                eq(TEST_ARTIST_ID), any(PageRequest.class)))
                .thenReturn(List.of());

        // When
        analyticsService.getTopListeners();

        // Then — must pass PageRequest.of(0, 10) — not a stream limit — to the repo
        verify(playEventRepository).findTopListenersByArtistId(
                TEST_ARTIST_ID,
                PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("getTopListeners - songPlayCounts is null (not populated in this endpoint)")
    void getTopListeners_songPlayCountsIsNull() {
        // Given
        when(playEventRepository.findTopListenersByArtistId(
                eq(TEST_ARTIST_ID), any(PageRequest.class)))
                .thenReturn(List.of());

        // When
        AnalyticsDTO result = analyticsService.getTopListeners();

        // Then
        assertNull(result.getSongPlayCounts(),
                "songPlayCounts should be null in getTopListeners response");
    }

    @Test
    @DisplayName("getTopListeners - no artist profile - throws ResourceNotFoundException")
    void getTopListeners_noArtistProfile_throwsResourceNotFoundException() {
        // Given
        when(artistRepository.findByUserId(TEST_ARTIST_USER_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.getTopListeners());

        verify(playEventRepository, never())
                .findTopListenersByArtistId(any(), any());
    }

    @Test
    @DisplayName("getTopListeners - SecurityUtils throws - propagates ResourceNotFoundException")
    void getTopListeners_securityUtilsThrows_propagatesException() {
        // Given
        when(securityUtils.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("User", "username", "unknown"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> analyticsService.getTopListeners());

        verifyNoInteractions(artistRepository, playEventRepository);
    }
}