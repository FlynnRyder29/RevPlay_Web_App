package com.revplay.service;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.mapper.SongMapper;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlbumServiceImpl.
 *
 * AlbumServiceImpl reads the principal from SecurityContextHolder directly
 * (like SongService), so every test must prime the SecurityContext.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumServiceImpl Unit Tests")
class AlbumServiceImplTest {

    @Mock private AlbumRepository  albumRepository;
    @Mock private SongRepository   songRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private UserRepository   userRepository;
    @Mock private SongMapper       songMapper;

    @InjectMocks
    private AlbumServiceImpl albumService;

    // ── Shared fixtures ───────────────────────────────────────────────────────

    private static final Long   USER_ID   = 1L;
    private static final Long   ARTIST_ID = 10L;
    private static final Long   ALBUM_ID  = 100L;
    private static final Long   SONG_ID   = 200L;
    private static final String USERNAME  = "aria";

    private User   user;
    private Artist artist;
    private Album  album;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);

        artist = new Artist();
        artist.setId(ARTIST_ID);
        artist.setArtistName("Aria");
        artist.setUserId(USER_ID);

        album = new Album();
        album.setId(ALBUM_ID);
        album.setName("Test Album");
        album.setArtist(artist);

        // Prime SecurityContextHolder — AlbumServiceImpl calls
        // SecurityContextHolder.getContext().getAuthentication().getName()
        var auth = new UsernamePasswordAuthenticationToken(USERNAME, null, List.of());
        var ctx  = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
    }

    /** Wires the two lookups that getCurrentArtist() always calls. */
    private void wireCurrentArtist() {
        when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                .thenReturn(Optional.of(user));
        when(artistRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(artist));
    }

    // ══════════════════════════════════════════════════════════════════════
    // createAlbum
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createAlbum")
    class CreateAlbum {

        @Test
        @DisplayName("valid request — persists and returns DTO")
        void createAlbum_validRequest_returnsAlbumDTO() {
            wireCurrentArtist();

            AlbumDTO request = AlbumDTO.builder()
                    .name("New Album")
                    .description("Desc")
                    .coverImageUrl("http://img.jpg")
                    .releaseDate(LocalDate.of(2025, 1, 1))
                    .build();

            Album saved = new Album();
            saved.setId(ALBUM_ID);
            saved.setName("New Album");
            saved.setDescription("Desc");
            saved.setArtist(artist);

            when(albumRepository.save(any(Album.class))).thenReturn(saved);
            when(albumRepository.countSongsByAlbumId(ALBUM_ID)).thenReturn(0);

            AlbumDTO result = albumService.createAlbum(request);

            assertNotNull(result);
            assertEquals(ALBUM_ID,    result.getId());
            assertEquals("New Album", result.getName());
            assertEquals(ARTIST_ID,   result.getArtistId());
            assertEquals("Aria",      result.getArtistName());
            assertEquals(0,           result.getSongCount());
            verify(albumRepository).save(any(Album.class));
        }

        @Test
        @DisplayName("no artist profile — throws ResourceNotFoundException")
        void createAlbum_noArtistProfile_throwsResourceNotFoundException() {
            when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                    .thenReturn(Optional.of(user));
            when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> albumService.createAlbum(AlbumDTO.builder().name("X").build()));

            verify(albumRepository, never()).save(any());
        }

        @Test
        @DisplayName("user not found — throws ResourceNotFoundException")
        void createAlbum_userNotFound_throwsResourceNotFoundException() {
            when(userRepository.findByEmailOrUsername(USERNAME, USERNAME))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> albumService.createAlbum(AlbumDTO.builder().name("X").build()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // updateAlbum
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateAlbum")
    class UpdateAlbum {

        @Test
        @DisplayName("partial update — only non-null fields are applied")
        void updateAlbum_partialUpdate_updatesOnlyProvidedFields() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            when(albumRepository.save(any(Album.class))).thenReturn(album);
            when(albumRepository.countSongsByAlbumId(ALBUM_ID)).thenReturn(0);

            AlbumDTO request = AlbumDTO.builder().name("Updated Name").build();
            albumService.updateAlbum(ALBUM_ID, request);

            assertEquals("Updated Name", album.getName());
            verify(albumRepository).save(album);
        }

        @Test
        @DisplayName("all fields provided — all are applied")
        void updateAlbum_allFieldsProvided_updatesAll() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            when(albumRepository.save(any(Album.class))).thenReturn(album);
            when(albumRepository.countSongsByAlbumId(ALBUM_ID)).thenReturn(2);

            AlbumDTO request = AlbumDTO.builder()
                    .name("Full Update")
                    .description("New desc")
                    .coverImageUrl("http://new.jpg")
                    .releaseDate(LocalDate.of(2025, 6, 1))
                    .build();

            AlbumDTO result = albumService.updateAlbum(ALBUM_ID, request);

            assertEquals("Full Update",    album.getName());
            assertEquals("New desc",       album.getDescription());
            assertEquals("http://new.jpg", album.getCoverImageUrl());
            assertEquals(2,                result.getSongCount());
        }

        @Test
        @DisplayName("album not found — throws ResourceNotFoundException")
        void updateAlbum_albumNotFound_throwsResourceNotFoundException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> albumService.updateAlbum(ALBUM_ID, AlbumDTO.builder().build()));
        }

        @Test
        @DisplayName("album belongs to different artist — throws BadRequestException")
        void updateAlbum_albumOwnedByOtherArtist_throwsBadRequestException() {
            wireCurrentArtist();
            Artist other = new Artist(); other.setId(999L);
            Album  otherAlbum = new Album(); otherAlbum.setId(ALBUM_ID); otherAlbum.setArtist(other);
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(otherAlbum));

            assertThrows(BadRequestException.class,
                    () -> albumService.updateAlbum(ALBUM_ID, AlbumDTO.builder().build()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // deleteAlbum
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteAlbum")
    class DeleteAlbum {

        @Test
        @DisplayName("empty album — deleted successfully")
        void deleteAlbum_emptyAlbum_deletesSuccessfully() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            when(albumRepository.countSongsByAlbumId(ALBUM_ID)).thenReturn(0);

            albumService.deleteAlbum(ALBUM_ID);

            verify(albumRepository).delete(album);
        }

        @Test
        @DisplayName("album has songs — throws BadRequestException with count in message")
        void deleteAlbum_albumWithSongs_throwsBadRequestExceptionWithCount() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            when(albumRepository.countSongsByAlbumId(ALBUM_ID)).thenReturn(3);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> albumService.deleteAlbum(ALBUM_ID));

            assertTrue(ex.getMessage().contains("3 song(s)"));
            verify(albumRepository, never()).delete(any());
        }

        @Test
        @DisplayName("album not found — throws ResourceNotFoundException")
        void deleteAlbum_albumNotFound_throwsResourceNotFoundException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> albumService.deleteAlbum(ALBUM_ID));
        }

        @Test
        @DisplayName("album owned by other artist — throws BadRequestException")
        void deleteAlbum_albumOwnedByOtherArtist_throwsBadRequestException() {
            wireCurrentArtist();
            Artist other = new Artist(); other.setId(999L);
            Album  otherAlbum = new Album(); otherAlbum.setId(ALBUM_ID); otherAlbum.setArtist(other);
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(otherAlbum));

            assertThrows(BadRequestException.class,
                    () -> albumService.deleteAlbum(ALBUM_ID));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // addSongToAlbum
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addSongToAlbum")
    class AddSongToAlbum {

        private Song buildSong(Album songAlbum) {
            Song s = new Song();
            s.setId(SONG_ID);
            s.setTitle("Track 1");
            s.setArtist(artist);
            s.setAlbum(songAlbum);
            return s;
        }

        @Test
        @DisplayName("song has no album — sets album and saves")
        void addSongToAlbum_songWithNoAlbum_addsSuccessfully() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Song song = buildSong(null);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            albumService.addSongToAlbum(ALBUM_ID, SONG_ID);

            assertEquals(album, song.getAlbum());
            verify(songRepository).save(song);
        }

        @Test
        @DisplayName("song already in this album — throws BadRequestException")
        void addSongToAlbum_songAlreadyInThisAlbum_throwsBadRequestException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Song song = buildSong(album); // same album
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> albumService.addSongToAlbum(ALBUM_ID, SONG_ID));

            assertTrue(ex.getMessage().contains("already part of this album"));
            verify(songRepository, never()).save(any());
        }

        @Test
        @DisplayName("song in a different album — throws BadRequestException with album name")
        void addSongToAlbum_songInDifferentAlbum_throwsBadRequestExceptionWithAlbumName() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Album other = new Album(); other.setId(999L); other.setName("Other Album");
            Song song = buildSong(other);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> albumService.addSongToAlbum(ALBUM_ID, SONG_ID));

            assertTrue(ex.getMessage().contains("Other Album"));
        }

        @Test
        @DisplayName("song owned by different artist — throws BadRequestException")
        void addSongToAlbum_songOwnedByDifferentArtist_throwsBadRequestException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Artist other = new Artist(); other.setId(999L);
            Song song = new Song(); song.setId(SONG_ID); song.setArtist(other); song.setAlbum(null);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> albumService.addSongToAlbum(ALBUM_ID, SONG_ID));

            assertTrue(ex.getMessage().contains("does not belong to the logged-in artist"));
        }

        @Test
        @DisplayName("song not found — throws ResourceNotFoundException")
        void addSongToAlbum_songNotFound_throwsResourceNotFoundException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> albumService.addSongToAlbum(ALBUM_ID, SONG_ID));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // removeSongFromAlbum
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeSongFromAlbum")
    class RemoveSongFromAlbum {

        @Test
        @DisplayName("song in album — sets album null and saves")
        void removeSongFromAlbum_validOwnership_setsAlbumNull() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Song song = new Song(); song.setId(SONG_ID); song.setArtist(artist); song.setAlbum(album);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            albumService.removeSongFromAlbum(ALBUM_ID, SONG_ID);

            assertNull(song.getAlbum());
            verify(songRepository).save(song);
        }

        @Test
        @DisplayName("song not in any album — throws BadRequestException")
        void removeSongFromAlbum_songNotInAlbum_throwsBadRequestException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Song song = new Song(); song.setId(SONG_ID); song.setArtist(artist); song.setAlbum(null);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> albumService.removeSongFromAlbum(ALBUM_ID, SONG_ID));

            assertTrue(ex.getMessage().contains("does not belong to this album"));
        }

        @Test
        @DisplayName("song in a different album — throws BadRequestException")
        void removeSongFromAlbum_songInDifferentAlbum_throwsBadRequestException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Album other = new Album(); other.setId(999L);
            Song song = new Song(); song.setId(SONG_ID); song.setArtist(artist); song.setAlbum(other);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            assertThrows(BadRequestException.class,
                    () -> albumService.removeSongFromAlbum(ALBUM_ID, SONG_ID));
        }

        @Test
        @DisplayName("song owned by different artist — throws BadRequestException")
        void removeSongFromAlbum_songOwnedByDifferentArtist_throwsBadRequestException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            Artist other = new Artist(); other.setId(999L);
            Song song = new Song(); song.setId(SONG_ID); song.setArtist(other); song.setAlbum(album);
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));

            assertThrows(BadRequestException.class,
                    () -> albumService.removeSongFromAlbum(ALBUM_ID, SONG_ID));
        }

        @Test
        @DisplayName("song not found — throws ResourceNotFoundException")
        void removeSongFromAlbum_songNotFound_throwsResourceNotFoundException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> albumService.removeSongFromAlbum(ALBUM_ID, SONG_ID));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getMyAlbums
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMyAlbums")
    class GetMyAlbums {

        @Test
        @DisplayName("returns paged album DTOs for current artist")
        void getMyAlbums_returnsPagedAlbumDTOs() {
            wireCurrentArtist();
            Pageable pageable = PageRequest.of(0, 20);
            when(albumRepository.findByArtistId(ARTIST_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of(album)));
            when(albumRepository.countSongsByAlbumId(ALBUM_ID)).thenReturn(0);

            Page<AlbumDTO> result = albumService.getMyAlbums(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(ALBUM_ID, result.getContent().get(0).getId());
            verify(albumRepository).findByArtistId(ARTIST_ID, pageable);
        }

        @Test
        @DisplayName("no albums — returns empty page")
        void getMyAlbums_noAlbums_returnsEmptyPage() {
            wireCurrentArtist();
            Pageable pageable = PageRequest.of(0, 20);
            when(albumRepository.findByArtistId(ARTIST_ID, pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            assertTrue(albumService.getMyAlbums(pageable).isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getMySongs
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMySongs")
    class GetMySongs {

        @Test
        @DisplayName("maps songs via SongMapper and returns page")
        void getMySongs_mapsSongsViaSongMapper() {
            wireCurrentArtist();
            Pageable pageable = PageRequest.of(0, 20);
            Song song = new Song(); song.setId(SONG_ID); song.setArtist(artist);
            when(songRepository.findByArtistId(ARTIST_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of(song)));
            SongDTO dto = SongDTO.builder().id(SONG_ID).title("Track").build();
            when(songMapper.toDTO(song)).thenReturn(dto);

            Page<SongDTO> result = albumService.getMySongs(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(SONG_ID, result.getContent().get(0).getId());
            verify(songMapper).toDTO(song);
        }

        @Test
        @DisplayName("no songs — returns empty page")
        void getMySongs_noSongs_returnsEmptyPage() {
            wireCurrentArtist();
            Pageable pageable = PageRequest.of(0, 20);
            when(songRepository.findByArtistId(ARTIST_ID, pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            assertTrue(albumService.getMySongs(pageable).isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getMyAlbumById
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMyAlbumById")
    class GetMyAlbumById {

        @Test
        @DisplayName("owned album — returns DTO with songCount")
        void getMyAlbumById_ownedAlbum_returnsDTOWithSongCount() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
            when(albumRepository.countSongsByAlbumId(ALBUM_ID)).thenReturn(3);
            when(songRepository.findByAlbumId(eq(ALBUM_ID), any(Pageable.class))).thenReturn(Page.empty());

            AlbumDTO result = albumService.getMyAlbumById(ALBUM_ID);

            assertNotNull(result);
            assertEquals(ALBUM_ID,  result.getId());
            assertEquals(3,         result.getSongCount());
            assertEquals(ARTIST_ID, result.getArtistId());
        }

        @Test
        @DisplayName("album not found — throws ResourceNotFoundException")
        void getMyAlbumById_albumNotFound_throwsResourceNotFoundException() {
            wireCurrentArtist();
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> albumService.getMyAlbumById(ALBUM_ID));
        }

        @Test
        @DisplayName("album owned by other artist — throws BadRequestException")
        void getMyAlbumById_albumOwnedByOtherArtist_throwsBadRequestException() {
            wireCurrentArtist();
            Artist other = new Artist(); other.setId(999L);
            Album  otherAlbum = new Album(); otherAlbum.setId(ALBUM_ID); otherAlbum.setArtist(other);
            when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(otherAlbum));

            assertThrows(BadRequestException.class,
                    () -> albumService.getMyAlbumById(ALBUM_ID));
        }
    }
}