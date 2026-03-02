package com.revplay.service;

import com.revplay.dto.FavoriteDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.Favorite;
import com.revplay.model.Role;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.FavoriteRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FavoriteService.
 *
 * FavoriteService uses SecurityUtils (injected), so we mock it
 * and stub getCurrentUser() to return a pre-built User.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private SongRepository     songRepository;
    @Mock private SecurityUtils      securityUtils;

    @InjectMocks
    private FavoriteService favoriteService;

    private User     currentUser;
    private Song     testSong;
    private Favorite testFavorite;

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

        testFavorite = new Favorite();
        testFavorite.setId(1L);
        testFavorite.setUser(currentUser);
        testFavorite.setSong(testSong);
        testFavorite.setCreatedAt(LocalDateTime.of(2024, 6, 1, 10, 0));

        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    // ── addFavorite ───────────────────────────────────────────────

    @Test
    @DisplayName("addFavorite - new song - favorite saved once")
    void addFavorite_newSong_savedSuccessfully() {
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));
        when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L)).thenReturn(false);

        favoriteService.addFavorite(10L);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("addFavorite - saved favorite has correct user and song")
    void addFavorite_savedFavoriteHasCorrectUserAndSong() {
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));
        when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L)).thenReturn(false);

        favoriteService.addFavorite(10L);

        verify(favoriteRepository).save(argThat(f ->
                f.getUser().getId().equals(1L)
                        && f.getSong().getId().equals(10L)
        ));
    }

    @Test
    @DisplayName("addFavorite - duplicate song - not saved again")
    void addFavorite_duplicateSong_doesNotSave() {
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));
        when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L)).thenReturn(true);

        favoriteService.addFavorite(10L);

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    @DisplayName("addFavorite - song not found - throws ResourceNotFoundException")
    void addFavorite_songNotFound_throwsResourceNotFoundException() {
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> favoriteService.addFavorite(99L));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    @DisplayName("addFavorite - song not found - existence check is never called")
    void addFavorite_songNotFound_existenceCheckSkipped() {
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> favoriteService.addFavorite(99L));

        // Song lookup happens before duplicate check in the service
        verify(favoriteRepository, never()).existsByUser_IdAndSong_Id(anyLong(), anyLong());
    }

    // ── removeFavorite ────────────────────────────────────────────

    @Test
    @DisplayName("removeFavorite - existing favorite - deleted from repository")
    void removeFavorite_existing_deletedSuccessfully() {
        when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L)).thenReturn(true);

        favoriteService.removeFavorite(10L);

        verify(favoriteRepository).deleteByUser_IdAndSong_Id(1L, 10L);
    }

    @Test
    @DisplayName("removeFavorite - not favorited - throws ResourceNotFoundException")
    void removeFavorite_notFavorited_throwsResourceNotFoundException() {
        when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> favoriteService.removeFavorite(10L));
    }

    @Test
    @DisplayName("removeFavorite - not favorited - delete is never called")
    void removeFavorite_notFavorited_deleteNeverCalled() {
        when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> favoriteService.removeFavorite(99L));

        verify(favoriteRepository, never()).deleteByUser_IdAndSong_Id(anyLong(), anyLong());
    }

    @Test
    @DisplayName("removeFavorite - deletes with exact userId and songId")
    void removeFavorite_deletesWithCorrectIds() {
        when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L)).thenReturn(true);

        favoriteService.removeFavorite(10L);

        // Verify correct IDs passed — not any other combination
        verify(favoriteRepository).deleteByUser_IdAndSong_Id(1L, 10L);
        verify(favoriteRepository, never()).deleteByUser_IdAndSong_Id(2L, 10L);
    }

    // ── getMyFavorites ────────────────────────────────────────────

    @Test
    @DisplayName("getMyFavorites - returns all favorites for current user")
    void getMyFavorites_returnsFavoritesForCurrentUser() {
        when(favoriteRepository.findByUser_Id(1L))
                .thenReturn(List.of(testFavorite));

        List<FavoriteDTO> result = favoriteService.getMyFavorites();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getMyFavorites - maps all DTO fields correctly")
    void getMyFavorites_mapsAllFields() {
        when(favoriteRepository.findByUser_Id(1L))
                .thenReturn(List.of(testFavorite));

        List<FavoriteDTO> result = favoriteService.getMyFavorites();

        FavoriteDTO dto = result.get(0);
        assertEquals(1L,             dto.getId());
        assertEquals(10L,            dto.getSongId());
        assertEquals("Test Song",    dto.getSongTitle());
        assertEquals("Test Artist",  dto.getArtistName());
        assertEquals("/covers/test.jpg", dto.getCoverImageUrl());
        assertEquals(210,            dto.getDuration());
        assertNotNull(dto.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 0), dto.getCreatedAt());
    }

    @Test
    @DisplayName("getMyFavorites - no favorites - returns empty list")
    void getMyFavorites_noFavorites_returnsEmptyList() {
        when(favoriteRepository.findByUser_Id(1L)).thenReturn(List.of());

        List<FavoriteDTO> result = favoriteService.getMyFavorites();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getMyFavorites - queries by current user id only")
    void getMyFavorites_queriesWithCurrentUserId() {
        when(favoriteRepository.findByUser_Id(1L)).thenReturn(List.of());

        favoriteService.getMyFavorites();

        verify(favoriteRepository).findByUser_Id(1L);
        verify(favoriteRepository, never()).findByUser_Id(2L);
    }

    @Test
    @DisplayName("getMyFavorites - multiple favorites - all mapped correctly")
    void getMyFavorites_multipleFavorites_allMapped() {
        Artist artist2 = new Artist();
        artist2.setId(2L);
        artist2.setArtistName("Second Artist");
        artist2.setUserId(3L);

        Song song2 = Song.builder()
                .id(20L).title("Second Song").duration(180)
                .coverImageUrl("/covers/second.jpg")
                .visibility(Song.Visibility.PUBLIC)
                .artist(artist2)
                .build();

        Favorite favorite2 = new Favorite();
        favorite2.setId(2L);
        favorite2.setUser(currentUser);
        favorite2.setSong(song2);
        favorite2.setCreatedAt(LocalDateTime.now());

        when(favoriteRepository.findByUser_Id(1L))
                .thenReturn(List.of(testFavorite, favorite2));

        List<FavoriteDTO> result = favoriteService.getMyFavorites();

        assertEquals(2, result.size());
        assertEquals("Test Song",   result.get(0).getSongTitle());
        assertEquals("Second Song", result.get(1).getSongTitle());
    }
}