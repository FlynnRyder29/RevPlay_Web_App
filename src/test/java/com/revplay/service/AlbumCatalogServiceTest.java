package com.revplay.service;

import com.revplay.dto.AlbumDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.mapper.SongMapper;
import com.revplay.model.Album;
import com.revplay.model.Artist;
import com.revplay.model.Song;
import com.revplay.repository.AlbumRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.TestDataBuilder;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.revplay.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlbumCatalogService.
 *
 * getAllAlbums  — list view (no tracks)
 * getAlbumById — detail view (with tracks populated from SongRepository)
 *
 * Note: AlbumCatalogService injects SongMapper but uses it only for
 * potential future shared mapping. Current mapSongToDTO is private.
 * SongMapper is mocked here to satisfy constructor injection.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumCatalogService Unit Tests")
class AlbumCatalogServiceTest {

    @Mock private AlbumRepository albumRepository;
    @Mock private SongRepository songRepository;
    @Mock private SongMapper songMapper;  // injected but not called — private mapping used

    @InjectMocks
    private AlbumCatalogService albumCatalogService;

    // ── Helpers ───────────────────────────────────────────────────

    private Artist createArtist() {
        return TestDataBuilder.anArtist()
                .withId(TEST_ARTIST_ID)
                .withArtistName(TEST_ARTIST_NAME)
                .build();
    }

    private Album createAlbum(Long id, String name, Artist artist) {
        return TestDataBuilder.anAlbum()
                .withId(id)
                .withName(name)
                .withDescription("Description for " + name)
                .withArtist(artist)
                .withReleaseDate(LocalDate.of(2024, 6, 15))
                .build();
    }

    private Song createSong(Long id, String title, Artist artist, Album album) {
        return TestDataBuilder.aSong()
                .withId(id)
                .withTitle(title)
                .withArtist(artist)
                .withAlbum(album)
                .withPlayCount(100L)
                .build();
    }

    // =================================================================
    // getAllAlbums
    // =================================================================

    @Nested
    @DisplayName("getAllAlbums")
    class GetAllAlbums {

        @Test
        @DisplayName("returns paginated album list with all DTO fields mapped")
        void returnsPaginatedAlbums_allFieldsMapped() {
            // Given
            Artist artist = createArtist();
            Album album = createAlbum(1L, "Echoes", artist);
            Pageable pageable = PageRequest.of(0, 20);
            Page<Album> mockPage = new PageImpl<>(List.of(album));

            when(albumRepository.findAll(pageable)).thenReturn(mockPage);

            // When
            Page<AlbumDTO> result = albumCatalogService.getAllAlbums(pageable);

            // Then
            assertEquals(1, result.getContent().size());
            AlbumDTO dto = result.getContent().get(0);
            assertEquals(1L, dto.getId());
            assertEquals("Echoes", dto.getName());
            assertEquals("Description for Echoes", dto.getDescription());
            assertEquals(TEST_ARTIST_ID, dto.getArtistId());
            assertEquals(TEST_ARTIST_NAME, dto.getArtistName());
        }

        @Test
        @DisplayName("list view does not populate tracks")
        void listView_tracksNotPopulated() {
            // Given
            Artist artist = createArtist();
            Album album = createAlbum(1L, "Echoes", artist);
            Pageable pageable = PageRequest.of(0, 20);
            when(albumRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(List.of(album)));

            // When
            Page<AlbumDTO> result = albumCatalogService.getAllAlbums(pageable);

            // Then
            assertNull(result.getContent().get(0).getTracks(),
                    "Tracks should be null in list view — only populated in getAlbumById");
            verifyNoInteractions(songRepository);
        }

        @Test
        @DisplayName("empty database — returns empty page")
        void emptyDatabase_returnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            when(albumRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            // When
            Page<AlbumDTO> result = albumCatalogService.getAllAlbums(pageable);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("passes pageable directly to repository")
        void respectsPageable() {
            // Given
            Pageable pageable = PageRequest.of(3, 10);
            when(albumRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            // When
            albumCatalogService.getAllAlbums(pageable);

            // Then
            verify(albumRepository).findAll(pageable);
        }
    }

    // =================================================================
    // getAlbumById
    // =================================================================

    @Nested
    @DisplayName("getAlbumById")
    class GetAlbumById {

        @Test
        @DisplayName("existing album — returns detail with tracks populated")
        void existingId_returnsDetailWithTracks() {
            // Given
            Artist artist = createArtist();
            Album album = createAlbum(1L, "Echoes", artist);
            Song song1 = createSong(10L, "Track 1", artist, album);
            Song song2 = createSong(11L, "Track 2", artist, album);

            when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
            when(songRepository.findByAlbumId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(song1, song2)));

            // When
            AlbumDTO result = albumCatalogService.getAlbumById(1L);

            // Then
            assertEquals("Echoes", result.getName());
            assertNotNull(result.getTracks());
            assertEquals(2, result.getTracks().size());
            assertEquals("Track 1", result.getTracks().get(0).getTitle());
            assertEquals("Track 2", result.getTracks().get(1).getTitle());

            // Tracks should have album name set via the lambda parameter
            assertEquals("Echoes", result.getTracks().get(0).getAlbumName());
            assertEquals("Echoes", result.getTracks().get(1).getAlbumName());
        }

        @Test
        @DisplayName("album with no songs — returns empty tracklist")
        void albumWithNoSongs_returnsEmptyTracks() {
            // Given
            Artist artist = createArtist();
            Album album = createAlbum(1L, "Empty Album", artist);

            when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
            when(songRepository.findByAlbumId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            // When
            AlbumDTO result = albumCatalogService.getAlbumById(1L);

            // Then
            assertNotNull(result.getTracks());
            assertTrue(result.getTracks().isEmpty());
        }

        @Test
        @DisplayName("non-existing album — throws ResourceNotFoundException")
        void nonExistingId_throwsResourceNotFoundException() {
            // Given
            when(albumRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> albumCatalogService.getAlbumById(999L));

            verifyNoInteractions(songRepository);
        }

        @Test
        @DisplayName("song without album reference — albumId/albumName mapped via parameter")
        void songTrack_albumNameFromParameter() {
            // Given
            Artist artist = createArtist();
            Album album = createAlbum(1L, "Echoes", artist);

            // Song belongs to album but we test that albumName comes from
            // the lambda parameter (album.getName()), not from song.getAlbum()
            Song song = TestDataBuilder.aSong()
                    .withId(10L)
                    .withTitle("Standalone")
                    .withArtist(artist)
                    .withAlbum(null)  // song's album field is null
                    .build();

            when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
            when(songRepository.findByAlbumId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(song)));

            // When
            AlbumDTO result = albumCatalogService.getAlbumById(1L);

            // Then — albumName comes from the parameter, not song.getAlbum()
            assertEquals("Echoes", result.getTracks().get(0).getAlbumName());
            // albumId comes from song.getAlbum() which is null
            assertNull(result.getTracks().get(0).getAlbumId());
        }

        @Test
        @DisplayName("album with null artist — artistName defaults to 'Unknown'")
        void albumWithNullArtist_artistNameDefaultsToUnknown() {
            // Given — edge case: album entity has null artist
            Album album = new Album();
            album.setId(1L);
            album.setName("Orphan Album");
            album.setArtist(null);

            when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
            when(songRepository.findByAlbumId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            // When
            AlbumDTO result = albumCatalogService.getAlbumById(1L);

            // Then
            assertNull(result.getArtistId());
            assertEquals("Unknown", result.getArtistName());
        }
    }
}