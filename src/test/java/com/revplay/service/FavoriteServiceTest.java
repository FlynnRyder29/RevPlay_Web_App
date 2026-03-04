package com.revplay.service;

import com.revplay.dto.FavoriteDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.*;
import com.revplay.repository.FavoriteRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private SongRepository songRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private FavoriteService favoriteService;

    private User currentUser;
    private Song testSong;
    private Artist testArtist;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@revplay.com")
                .passwordHash("hash")
                .role(Role.LISTENER)
                .build();

        testArtist = new Artist();
        testArtist.setId(1L);
        testArtist.setArtistName("Test Artist");

        testSong = Song.builder()
                .id(10L)
                .title("Favorite Song")
                .duration(200)
                .coverImageUrl("/covers/fav.jpg")
                .artist(testArtist)
                .visibility(Song.Visibility.PUBLIC)
                .build();

        testFavorite = new Favorite();
        testFavorite.setId(1L);
        testFavorite.setUser(currentUser);
        testFavorite.setSong(testSong);
        testFavorite.setCreatedAt(LocalDateTime.of(2024, 6, 15, 14, 30));

        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    // ══════════════════════════════════════════════════════════
    //  addFavorite
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addFavorite")
    class AddFavorite {

        @Test
        @DisplayName("valid song — favorite saved with correct user and song")
        void addFavorite_validSong_saved() {
            when(songRepository.findById(10L))
                    .thenReturn(Optional.of(testSong));
            when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L))
                    .thenReturn(false);

            favoriteService.addFavorite(10L);

            verify(favoriteRepository).save(argThat(f ->
                    f.getUser().getId().equals(1L)
                            && f.getSong().getId().equals(10L)
            ));
        }

        @Test
        @DisplayName("already favorited — throws BadRequestException (duplicate guard)")
        void addFavorite_alreadyFavorited_throwsBadRequest() {
            when(songRepository.findById(10L))
                    .thenReturn(Optional.of(testSong));
            when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L))
                    .thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> favoriteService.addFavorite(10L));

            verify(favoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("song not found — throws ResourceNotFoundException")
        void addFavorite_songNotFound_throwsResourceNotFound() {
            when(songRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> favoriteService.addFavorite(99L));

            verify(favoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("save called exactly once for valid input")
        void addFavorite_savedExactlyOnce() {
            when(songRepository.findById(10L))
                    .thenReturn(Optional.of(testSong));
            when(favoriteRepository.existsByUser_IdAndSong_Id(1L, 10L))
                    .thenReturn(false);

            favoriteService.addFavorite(10L);

            verify(favoriteRepository, times(1)).save(any(Favorite.class));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  removeFavorite
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavorite {

        @Test
        @DisplayName("existing favorite — deleted successfully")
        void removeFavorite_exists_deleted() {
            when(favoriteRepository.findByUser_IdAndSong_Id(1L, 10L))
                    .thenReturn(Optional.of(testFavorite));

            favoriteService.removeFavorite(10L);

            verify(favoriteRepository).delete(testFavorite);
        }

        @Test
        @DisplayName("not favorited — throws ResourceNotFoundException")
        void removeFavorite_notFavorited_throwsResourceNotFound() {
            when(favoriteRepository.findByUser_IdAndSong_Id(1L, 99L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> favoriteService.removeFavorite(99L));

            verify(favoriteRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes with correct user and song IDs")
        void removeFavorite_deletesCorrectRecord() {
            when(favoriteRepository.findByUser_IdAndSong_Id(1L, 10L))
                    .thenReturn(Optional.of(testFavorite));

            favoriteService.removeFavorite(10L);

            verify(favoriteRepository).findByUser_IdAndSong_Id(1L, 10L);
            verify(favoriteRepository, never())
                    .findByUser_IdAndSong_Id(eq(2L), anyLong());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  getMyFavorites
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMyFavorites")
    class GetMyFavorites {

        @Test
        @DisplayName("returns all DTO fields mapped correctly including duration")
        void getMyFavorites_mapsAllFields() {
            when(favoriteRepository.findByUser_Id(1L))
                    .thenReturn(List.of(testFavorite));

            List<FavoriteDTO> result = favoriteService.getMyFavorites();

            assertEquals(1, result.size());
            FavoriteDTO dto = result.get(0);
            assertEquals(1L, dto.getId());
            assertEquals(10L, dto.getSongId());
            assertEquals("Favorite Song", dto.getSongTitle());
            assertEquals("Test Artist", dto.getArtistName());
            assertEquals("/covers/fav.jpg", dto.getCoverImageUrl());
            assertEquals(200, dto.getDuration());                       // ← ADDED
            assertEquals(
                    LocalDateTime.of(2024, 6, 15, 14, 30),
                    dto.getCreatedAt()
            );
        }

        @Test
        @DisplayName("empty favorites — returns empty list")
        void getMyFavorites_noFavorites_returnsEmpty() {
            when(favoriteRepository.findByUser_Id(1L))
                    .thenReturn(List.of());

            List<FavoriteDTO> result = favoriteService.getMyFavorites();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("queries current user only")
        void getMyFavorites_queriesCurrentUserOnly() {
            when(favoriteRepository.findByUser_Id(1L))
                    .thenReturn(List.of());

            favoriteService.getMyFavorites();

            verify(favoriteRepository).findByUser_Id(1L);
            verify(favoriteRepository, never()).findByUser_Id(2L);
        }

        @Test
        @DisplayName("multiple favorites — all returned")
        void getMyFavorites_multiple_allReturned() {
            Song song2 = Song.builder()
                    .id(20L)
                    .title("Another Song")
                    .duration(180)
                    .artist(testArtist)
                    .build();

            Favorite fav2 = new Favorite();
            fav2.setId(2L);
            fav2.setUser(currentUser);
            fav2.setSong(song2);
            fav2.setCreatedAt(LocalDateTime.of(2024, 6, 10, 10, 0));

            when(favoriteRepository.findByUser_Id(1L))
                    .thenReturn(List.of(testFavorite, fav2));

            List<FavoriteDTO> result = favoriteService.getMyFavorites();

            assertEquals(2, result.size());
            assertEquals(10L, result.get(0).getSongId());
            assertEquals(20L, result.get(1).getSongId());
        }
    }
}