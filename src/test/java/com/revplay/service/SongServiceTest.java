package com.revplay.service;

import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongDTO;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.model.Album;
import com.revplay.model.Artist;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.AlbumRepository;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SongService.
 *
 * SongService reads the principal from SecurityContextHolder directly,
 * so every test that triggers getCurrentUser() must prime the context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SongService Unit Tests")
class SongServiceTest {

    @Mock private SongRepository   songRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private AlbumRepository  albumRepository;
    @Mock private UserRepository   userRepository;

    @InjectMocks
    private SongService songService;

    // ── Shared fixtures ───────────────────────────────────────────────────────

    private static final Long   USER_ID   = 1L;
    private static final Long   ARTIST_ID = 10L;
    private static final Long   ALBUM_ID  = 20L;
    private static final Long   SONG_ID   = 1L;
    private static final String USERNAME  = "aria";

    private User   currentUser;
    private Artist artist;
    private Album  album;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(USER_ID);
        currentUser.setUsername(USERNAME);

        artist = new Artist();
        artist.setId(ARTIST_ID);
        artist.setArtistName("Mock Artist");
        artist.setUser(currentUser);

        album = new Album();
        album.setId(ALBUM_ID);
        album.setName("Mock Album");
        album.setArtist(artist);

        // Prime SecurityContextHolder — SongService calls
        // SecurityContextHolder.getContext().getAuthentication().getName()
        var auth = new UsernamePasswordAuthenticationToken(USERNAME, null, List.of());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    /** Wires the two lookups that getCurrentUser() always calls. */
    private void wireCurrentUser() {
        when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                .thenReturn(Optional.of(currentUser));
    }

    /** Builds a minimal Song owned by {@link #artist}. */
    private Song createMockSong(Long id) {
        Song song = new Song();
        song.setId(id);
        song.setTitle("Mock Song");
        song.setGenre("Rock");
        song.setArtist(artist);
        song.setAlbum(album);
        return song;
    }

    // ══════════════════════════════════════════════════════════════════════
    // getAllSongs
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllSongs")
    class GetAllSongs {

        @Test
        @DisplayName("returns only PUBLIC songs")
        void getAllSongs_returnsPublicSongsOnly() {
            Pageable pageable = PageRequest.of(0, 10);
            Song public1 = createMockSong(1L);
            public1.setVisibility(Song.Visibility.PUBLIC);
            when(songRepository.findByVisibility(Song.Visibility.PUBLIC, pageable))
                    .thenReturn(new PageImpl<>(List.of(public1)));

            Page<SongDTO> result = songService.getAllSongs(pageable);

            assertFalse(result.isEmpty());
            assertEquals(1, result.getContent().size());
            verify(songRepository).findByVisibility(Song.Visibility.PUBLIC, pageable);
            verify(songRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("empty database — returns empty page")
        void getAllSongs_emptyDatabase_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(songRepository.findByVisibility(Song.Visibility.PUBLIC, pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            assertTrue(songService.getAllSongs(pageable).isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getSongById
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSongById")
    class GetSongById {

        @Test
        @DisplayName("existing PUBLIC song — returns SongDTO")
        void getSongById_existingId_returnsSongDto() {
            Song mockSong = createMockSong(SONG_ID);
            mockSong.setVisibility(Song.Visibility.PUBLIC);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(mockSong));

            SongDTO result = songService.getSongById(SONG_ID);

            assertNotNull(result);
            assertEquals("Mock Song",   result.getTitle());
            assertEquals("Mock Artist", result.getArtistName());
            assertEquals("Mock Album",  result.getAlbumName());
            verify(songRepository).findById(SONG_ID);
        }

        @Test
        @DisplayName("UNLISTED song — accessible by direct link")
        void getSongById_unlistedSong_returnsDTO() {
            Song mockSong = createMockSong(SONG_ID);
            mockSong.setVisibility(Song.Visibility.UNLISTED);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(mockSong));

            SongDTO result = songService.getSongById(SONG_ID);

            assertNotNull(result);
        }

        @Test
        @DisplayName("PRIVATE song accessed by owner — returns DTO")
        void getSongById_privateSongByOwner_returnsDTO() {
            wireCurrentUser();
            Song mockSong = createMockSong(SONG_ID);
            mockSong.setVisibility(Song.Visibility.PRIVATE);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(mockSong));

            // artist.getUser().getId() == USER_ID — owner, should succeed
            SongDTO result = songService.getSongById(SONG_ID);

            assertNotNull(result);
        }

        @Test
        @DisplayName("PRIVATE song accessed by non-owner — throws ResourceNotFoundException")
        void getSongById_privateSongByNonOwner_throwsResourceNotFoundException() {
            User other = new User(); other.setId(999L); other.setUsername("other");
            when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                    .thenReturn(Optional.of(other));

            Song privateSong = createMockSong(SONG_ID);
            privateSong.setVisibility(Song.Visibility.PRIVATE);
            // artist.getUser().getId() == USER_ID, current user is 999L — not owner
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(privateSong));

            assertThrows(ResourceNotFoundException.class,
                    () -> songService.getSongById(SONG_ID));
        }

        @Test
        @DisplayName("non-existing song — throws ResourceNotFoundException")
        void getSongById_nonExistingId_throwsResourceNotFoundException() {
            when(songRepository.findById(999L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> songService.getSongById(999L));

            assertTrue(ex.getMessage().contains("Song not found"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // searchSongs
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("searchSongs")
    class SearchSongs {

        @Test
        @DisplayName("keyword search — calls searchByKeywordAndVisibility with PUBLIC")
        void searchSongs_validKeyword_returnsPagedSongs() {
            String keyword = "test";
            Pageable pageable = PageRequest.of(0, 10);
            when(songRepository.searchByKeywordAndVisibility(
                    eq(keyword), any(Song.Visibility.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(createMockSong(1L))));

            Page<SongDTO> result = songService.searchSongs(keyword, pageable);

            assertFalse(result.isEmpty());
            assertEquals(1, result.getContent().size());
            verify(songRepository).searchByKeywordAndVisibility(
                    eq(keyword), any(Song.Visibility.class), eq(pageable));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // filterSongs
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("filterSongs")
    class FilterSongs {

        @Test
        @DisplayName("filter criteria — returns paged filtered results")
        void filterSongs_validFilters_returnsFilteredPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(songRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(createMockSong(1L))));

            Page<SongDTO> result = songService.filterSongs("Rock", null, null, null, pageable);

            assertFalse(result.isEmpty());
            verify(songRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("all null filters — returns all PUBLIC songs")
        void filterSongs_allNullFilters_returnsPublicSongs() {
            Pageable pageable = PageRequest.of(0, 10);
            when(songRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<SongDTO> result = songService.filterSongs(null, null, null, null, pageable);

            assertNotNull(result);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // createSong
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createSong")
    class CreateSong {

        private SongCreateRequest buildRequest() {
            SongCreateRequest req = new SongCreateRequest();
            req.setTitle("New Track");
            req.setGenre("Pop");
            req.setDuration(210);
            req.setAudioUrl("http://audio.mp3");
            return req;
        }

        @Test
        @DisplayName("no albumId — creates song without album")
        void createSong_noAlbumId_createsSongWithoutAlbum() {
            wireCurrentUser();
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));

            Song saved = createMockSong(SONG_ID);
            saved.setAlbum(null);
            when(songRepository.save(any(Song.class))).thenReturn(saved);

            SongDTO result = songService.createSong(buildRequest());

            assertNotNull(result);
            assertEquals(SONG_ID, result.getId());
            verify(songRepository).save(any(Song.class));
        }

        @Test
        @DisplayName("with valid albumId owned by artist — creates song with album")
        void createSong_withValidAlbumId_createsSongWithAlbum() {
            wireCurrentUser();
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));

            SongCreateRequest req = buildRequest();
            req.setAlbumId(ALBUM_ID);

            Song saved = createMockSong(SONG_ID);
            when(songRepository.save(any(Song.class))).thenReturn(saved);

            SongDTO result = songService.createSong(req);

            assertNotNull(result);
            assertEquals(ALBUM_ID, result.getAlbumId());
        }

        @Test
        @DisplayName("albumId belongs to different artist — throws BadRequestException")
        void createSong_albumBelongsToDifferentArtist_throwsBadRequestException() {
            wireCurrentUser();
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));

            Artist other = new Artist(); other.setId(999L);
            Album  otherAlbum = new Album(); otherAlbum.setId(ALBUM_ID); otherAlbum.setArtist(other);
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(otherAlbum));

            SongCreateRequest req = buildRequest();
            req.setAlbumId(ALBUM_ID);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> songService.createSong(req));

            assertTrue(ex.getMessage().contains("another artist's album"));
            verify(songRepository, never()).save(any());
        }

        @Test
        @DisplayName("albumId not found — throws ResourceNotFoundException")
        void createSong_albumNotFound_throwsResourceNotFoundException() {
            wireCurrentUser();
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.empty());

            SongCreateRequest req = buildRequest();
            req.setAlbumId(ALBUM_ID);

            assertThrows(ResourceNotFoundException.class,
                    () -> songService.createSong(req));
        }

        @Test
        @DisplayName("visibility null — defaults to PUBLIC")
        void createSong_nullVisibility_defaultsToPublic() {
            wireCurrentUser();
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));

            Song saved = createMockSong(SONG_ID);
            saved.setVisibility(Song.Visibility.PUBLIC);
            when(songRepository.save(any(Song.class))).thenReturn(saved);

            SongCreateRequest req = buildRequest();
            req.setVisibility(null);

            SongDTO result = songService.createSong(req);

            assertEquals("PUBLIC", result.getVisibility());
        }

        @Test
        @DisplayName("visibility UNLISTED — persisted as UNLISTED")
        void createSong_visibilityUnlisted_persistedAsUnlisted() {
            wireCurrentUser();
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));

            Song saved = createMockSong(SONG_ID);
            saved.setVisibility(Song.Visibility.UNLISTED);
            when(songRepository.save(any(Song.class))).thenReturn(saved);

            SongCreateRequest req = buildRequest();
            req.setVisibility(Song.Visibility.UNLISTED);

            SongDTO result = songService.createSong(req);

            assertEquals("UNLISTED", result.getVisibility());
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void createSong_noArtistProfile_throwsResourceNotFoundException() {
            wireCurrentUser();
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> songService.createSong(buildRequest()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // updateSong
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateSong")
    class UpdateSong {

        @Test
        @DisplayName("owner updates title — updates and returns DTO")
        void updateSong_ownerUpdatesTitle_returnsUpdatedDTO() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
            when(songRepository.save(any(Song.class))).thenReturn(song);

            SongUpdateRequest req = new SongUpdateRequest();
            req.setTitle("Updated Title");

            SongDTO result = songService.updateSong(SONG_ID, req);

            assertEquals("Updated Title", song.getTitle());
            assertNotNull(result);
            verify(songRepository).save(song);
        }

        @Test
        @DisplayName("non-owner — throws UnauthorizedAccessException")
        void updateSong_nonOwner_throwsUnauthorizedAccessException() {
            // Different user in security context
            User other = new User(); other.setId(999L); other.setUsername(USERNAME);
            when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                    .thenReturn(Optional.of(other));

            Song song = createMockSong(SONG_ID); // artist.getUser().getId() == USER_ID
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            assertThrows(UnauthorizedAccessException.class,
                    () -> songService.updateSong(SONG_ID, new SongUpdateRequest()));
        }

        @Test
        @DisplayName("song not found — throws ResourceNotFoundException")
        void updateSong_songNotFound_throwsResourceNotFoundException() {
            wireCurrentUser();
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> songService.updateSong(SONG_ID, new SongUpdateRequest()));
        }

        @Test
        @DisplayName("albumId in request belongs to different artist — throws BadRequestException")
        void updateSong_albumBelongsToDifferentArtist_throwsBadRequestException() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            Artist other = new Artist(); other.setId(999L);
            Album  otherAlbum = new Album(); otherAlbum.setId(ALBUM_ID); otherAlbum.setArtist(other);
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(otherAlbum));
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));

            SongUpdateRequest req = new SongUpdateRequest();
            req.setAlbumId(ALBUM_ID);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> songService.updateSong(SONG_ID, req));

            assertTrue(ex.getMessage().contains("another artist's album"));
        }

        @Test
        @DisplayName("only non-null fields are applied (null fields ignored)")
        void updateSong_onlyNonNullFieldsApplied() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            String originalGenre = song.getGenre();
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
            when(songRepository.save(any(Song.class))).thenReturn(song);

            SongUpdateRequest req = new SongUpdateRequest();
            req.setTitle("New Title");
            // genre not set — should remain unchanged

            songService.updateSong(SONG_ID, req);

            assertEquals("New Title",   song.getTitle());
            assertEquals(originalGenre, song.getGenre()); // unchanged
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // deleteSong
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteSong")
    class DeleteSong {

        @Test
        @DisplayName("owner deletes song — song is removed")
        void deleteSong_owner_deletesSong() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            songService.deleteSong(SONG_ID);

            verify(songRepository).delete(song);
        }

        @Test
        @DisplayName("non-owner — throws UnauthorizedAccessException")
        void deleteSong_nonOwner_throwsUnauthorizedAccessException() {
            User other = new User(); other.setId(999L); other.setUsername(USERNAME);
            when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                    .thenReturn(Optional.of(other));

            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            assertThrows(UnauthorizedAccessException.class,
                    () -> songService.deleteSong(SONG_ID));

            verify(songRepository, never()).delete(any());
        }

        @Test
        @DisplayName("song not found — throws ResourceNotFoundException")
        void deleteSong_songNotFound_throwsResourceNotFoundException() {
            wireCurrentUser();
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> songService.deleteSong(SONG_ID));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // updateVisibility
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateVisibility")
    class UpdateVisibility {

        @Test
        @DisplayName("owner sets PUBLIC — returns DTO with PUBLIC")
        void updateVisibility_ownerSetsPublic_returnsPublic() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            song.setVisibility(Song.Visibility.PRIVATE);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
            when(songRepository.save(any(Song.class))).thenReturn(song);

            SongDTO result = songService.updateVisibility(SONG_ID, "PUBLIC");

            assertEquals(Song.Visibility.PUBLIC, song.getVisibility());
            assertEquals("PUBLIC", result.getVisibility());
        }

        @Test
        @DisplayName("owner sets UNLISTED — persisted as UNLISTED")
        void updateVisibility_ownerSetsUnlisted_persistedAsUnlisted() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
            when(songRepository.save(any(Song.class))).thenReturn(song);

            songService.updateVisibility(SONG_ID, "UNLISTED");

            assertEquals(Song.Visibility.UNLISTED, song.getVisibility());
        }

        @Test
        @DisplayName("owner sets PRIVATE — persisted as PRIVATE")
        void updateVisibility_ownerSetsPrivate_persistedAsPrivate() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
            when(songRepository.save(any(Song.class))).thenReturn(song);

            songService.updateVisibility(SONG_ID, "PRIVATE");

            assertEquals(Song.Visibility.PRIVATE, song.getVisibility());
        }

        @Test
        @DisplayName("non-owner — throws UnauthorizedAccessException")
        void updateVisibility_nonOwner_throwsUnauthorizedAccessException() {
            User other = new User(); other.setId(999L); other.setUsername(USERNAME);
            when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                    .thenReturn(Optional.of(other));

            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            assertThrows(UnauthorizedAccessException.class,
                    () -> songService.updateVisibility(SONG_ID, "PUBLIC"));
        }

        @Test
        @DisplayName("invalid visibility value — throws IllegalArgumentException")
        void updateVisibility_invalidValue_throwsIllegalArgumentException() {
            wireCurrentUser();
            Song song = createMockSong(SONG_ID);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            assertThrows(IllegalArgumentException.class,
                    () -> songService.updateVisibility(SONG_ID, "VISIBLE"));
        }

        @Test
        @DisplayName("song not found — throws ResourceNotFoundException")
        void updateVisibility_songNotFound_throwsResourceNotFoundException() {
            wireCurrentUser();
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> songService.updateVisibility(SONG_ID, "PUBLIC"));
        }
    }
}