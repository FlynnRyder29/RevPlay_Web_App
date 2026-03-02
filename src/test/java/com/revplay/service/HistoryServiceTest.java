package com.revplay.service;

import com.revplay.dto.HistoryDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.ListeningHistory;
import com.revplay.model.Role;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.HistoryRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoryService.
 *
 * FavoriteService uses SecurityUtils (injected), so we mock it
 * and stub getCurrentUser() to return a pre-built User.
 *
 * Key behaviour verified:
 *   - limit clamping: <=0 → 50, >200 → 200, boundary 200 → 200
 *   - same song can be recorded multiple times (no duplicate guard)
 *   - full DTO mapping including playedAt
 */
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock private HistoryRepository historyRepository;
    @Mock private SongRepository    songRepository;
    @Mock private SecurityUtils     securityUtils;

    @InjectMocks
    private HistoryService historyService;

    private User             currentUser;
    private Song             testSong;
    private ListeningHistory testHistory;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L).username("alice")
                .email("alice@revplay.com")
                .passwordHash("hash")
                .role(Role.LISTENER)
                .build();

        Artist testArtist = new Artist();
        testArtist.setId(1L);
        testArtist.setArtistName("Test Artist");
        testArtist.setUserId(2L);

        testSong = Song.builder()
                .id(10L)
                .title("Test Song")
                .duration(210)
                .coverImageUrl("/covers/test.jpg")
                .visibility(Song.Visibility.PUBLIC)
                .artist(testArtist)
                .build();

        testHistory = new ListeningHistory();
        testHistory.setId(1L);
        testHistory.setUser(currentUser);
        testHistory.setSong(testSong);
        testHistory.setPlayedAt(LocalDateTime.of(2024, 6, 1, 10, 0));

        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    // ── addToHistory ──────────────────────────────────────────────

    @Test
    @DisplayName("addToHistory - valid song - history record saved")
    void addToHistory_validSong_savedSuccessfully() {
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));

        historyService.addToHistory(10L);

        verify(historyRepository, times(1)).save(any(ListeningHistory.class));
    }

    @Test
    @DisplayName("addToHistory - saved record has correct user and song")
    void addToHistory_savedRecordHasCorrectUserAndSong() {
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));

        historyService.addToHistory(10L);

        verify(historyRepository).save(argThat(h ->
                h.getUser().getId().equals(1L)
                        && h.getSong().getId().equals(10L)
        ));
    }

    @Test
    @DisplayName("addToHistory - same song played twice - saved twice (no duplicate guard)")
    void addToHistory_sameSongPlayedTwice_savedTwice() {
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));

        historyService.addToHistory(10L);
        historyService.addToHistory(10L);

        // Unlike favorites, history has no duplicate prevention
        verify(historyRepository, times(2)).save(any(ListeningHistory.class));
    }

    @Test
    @DisplayName("addToHistory - song not found - throws ResourceNotFoundException")
    void addToHistory_songNotFound_throwsResourceNotFoundException() {
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> historyService.addToHistory(99L));
    }

    @Test
    @DisplayName("addToHistory - song not found - history never saved")
    void addToHistory_songNotFound_historyNeverSaved() {
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> historyService.addToHistory(99L));

        verify(historyRepository, never()).save(any());
    }

    // ── getMyHistory — DTO mapping ────────────────────────────────

    @Test
    @DisplayName("getMyHistory - maps all DTO fields correctly including playedAt")
    void getMyHistory_mapsAllDtoFields() {
        Page<ListeningHistory> page = new PageImpl<>(List.of(testHistory));
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<HistoryDTO> result = historyService.getMyHistory(50);

        HistoryDTO dto = result.getContent().get(0);
        assertEquals(1L,             dto.getId());
        assertEquals(10L,            dto.getSongId());
        assertEquals("Test Song",    dto.getSongTitle());
        assertEquals("Test Artist",  dto.getArtistName());
        assertEquals("/covers/test.jpg", dto.getCoverImageUrl());
        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 0), dto.getPlayedAt());
    }

    @Test
    @DisplayName("getMyHistory - empty history - returns empty page")
    void getMyHistory_emptyHistory_returnsEmptyPage() {
        Page<ListeningHistory> page = new PageImpl<>(List.of());
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<HistoryDTO> result = historyService.getMyHistory(50);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getMyHistory - queries by current user id only")
    void getMyHistory_queriesWithCurrentUserId() {
        Page<ListeningHistory> page = new PageImpl<>(List.of());
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        historyService.getMyHistory(10);

        verify(historyRepository).findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class));
        verify(historyRepository, never())
                .findByUser_IdOrderByPlayedAtDesc(eq(2L), any(Pageable.class));
    }

    // ── getMyHistory — limit clamping ─────────────────────────────

    @Test
    @DisplayName("getMyHistory - valid limit 50 - used as-is")
    void getMyHistory_validLimit_usedAsIs() {
        Page<ListeningHistory> page = new PageImpl<>(List.of());
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        historyService.getMyHistory(50);

        verify(historyRepository).findByUser_IdOrderByPlayedAtDesc(
                eq(1L),
                argThat(p -> p.getPageSize() == 50)
        );
    }

    @Test
    @DisplayName("getMyHistory - limit 0 - clamped to 50")
    void getMyHistory_zeroLimit_clampedTo50() {
        Page<ListeningHistory> page = new PageImpl<>(List.of());
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        historyService.getMyHistory(0);

        verify(historyRepository).findByUser_IdOrderByPlayedAtDesc(
                eq(1L),
                argThat(p -> p.getPageSize() == 50)
        );
    }

    @Test
    @DisplayName("getMyHistory - negative limit - clamped to 50")
    void getMyHistory_negativelimit_clampedTo50() {
        Page<ListeningHistory> page = new PageImpl<>(List.of());
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        historyService.getMyHistory(-5);

        verify(historyRepository).findByUser_IdOrderByPlayedAtDesc(
                eq(1L),
                argThat(p -> p.getPageSize() == 50)
        );
    }

    @Test
    @DisplayName("getMyHistory - limit > 200 - clamped to 200")
    void getMyHistory_oversizedLimit_clampedTo200() {
        Page<ListeningHistory> page = new PageImpl<>(List.of());
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        historyService.getMyHistory(999);

        verify(historyRepository).findByUser_IdOrderByPlayedAtDesc(
                eq(1L),
                argThat(p -> p.getPageSize() == 200)
        );
    }

    @Test
    @DisplayName("getMyHistory - limit exactly 200 - not clamped")
    void getMyHistory_exactlyMaxLimit_notClamped() {
        Page<ListeningHistory> page = new PageImpl<>(List.of());
        when(historyRepository.findByUser_IdOrderByPlayedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        historyService.getMyHistory(200);

        verify(historyRepository).findByUser_IdOrderByPlayedAtDesc(
                eq(1L),
                argThat(p -> p.getPageSize() == 200)
        );
    }

    // ── clearHistory ──────────────────────────────────────────────

    @Test
    @DisplayName("clearHistory - deletes all history for current user")
    void clearHistory_deletesAllHistoryForCurrentUser() {
        historyService.clearHistory();

        verify(historyRepository, times(1)).deleteByUser_Id(1L);
    }

    @Test
    @DisplayName("clearHistory - only deletes for current user, not others")
    void clearHistory_onlyDeletesForCurrentUser() {
        historyService.clearHistory();

        verify(historyRepository).deleteByUser_Id(1L);
        verify(historyRepository, never()).deleteByUser_Id(2L);
    }

    @Test
    @DisplayName("clearHistory - called even when user has no history")
    void clearHistory_calledEvenWhenNoHistoryExists() {
        // deleteByUser_Id is a void method — it does not throw when there's nothing to delete
        doNothing().when(historyRepository).deleteByUser_Id(1L);

        assertDoesNotThrow(() -> historyService.clearHistory());

        verify(historyRepository).deleteByUser_Id(1L);
    }
}