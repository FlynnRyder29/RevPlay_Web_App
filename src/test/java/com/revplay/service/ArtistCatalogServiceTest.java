package com.revplay.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.revplay.model.Artist;
import com.revplay.model.Song;
import com.revplay.model.Album;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.AlbumRepository;
import com.revplay.dto.ArtistDTO;
import com.revplay.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistCatalogService Unit Tests")
class ArtistCatalogServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private ArtistCatalogService artistCatalogService;

    // ================================================================
    // getAllArtists
    // ================================================================

    @Test
    @DisplayName("getAllArtists - pagination returns paged artist list")
    void getAllArtists_validPageable_returnsPagedArtists() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Artist> mockPage = new PageImpl<>(List.of(createMockArtist(1L)));
        when(artistRepository.findAll(pageable)).thenReturn(mockPage);

        // When
        Page<ArtistDTO> result = artistCatalogService.getAllArtists(pageable);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(artistRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getAllArtists - maps all DTO fields correctly")
    void getAllArtists_mapsAllFields() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Artist> mockPage = new PageImpl<>(List.of(createMockArtist(1L)));
        when(artistRepository.findAll(pageable)).thenReturn(mockPage);

        // When
        ArtistDTO dto = artistCatalogService.getAllArtists(pageable).getContent().get(0);

        // Then
        assertEquals(1L,             dto.getId());
        assertEquals("Test Artist",  dto.getArtistName());
        assertEquals("Indie",        dto.getGenre());
        assertEquals("Indie pop singer-songwriter.", dto.getBio());
        assertEquals("https://picsum.photos/seed/aria-pfp/300/300",     dto.getProfilePictureUrl());
        assertEquals("https://picsum.photos/seed/aria-banner/1200/400", dto.getBannerImageUrl());
    }

    @Test
    @DisplayName("getAllArtists - songs and albums not populated in list view")
    void getAllArtists_songsAndAlbumsNotPopulated() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Artist> mockPage = new PageImpl<>(List.of(createMockArtist(1L)));
        when(artistRepository.findAll(pageable)).thenReturn(mockPage);

        // When
        Page<ArtistDTO> result = artistCatalogService.getAllArtists(pageable);

        // Then — mapToDTO (used by list view) does NOT populate songs/albums
        assertNull(result.getContent().get(0).getSongs());
        assertNull(result.getContent().get(0).getAlbums());
        verifyNoInteractions(songRepository, albumRepository);
    }

    @Test
    @DisplayName("getAllArtists - empty database returns empty page")
    void getAllArtists_emptyDatabase_returnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        when(artistRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        // When
        Page<ArtistDTO> result = artistCatalogService.getAllArtists(pageable);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllArtists - passes pageable argument directly to repository")
    void getAllArtists_respectsPageable() {
        // Given
        Pageable pageable = PageRequest.of(2, 5);
        when(artistRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // When
        artistCatalogService.getAllArtists(pageable);

        // Then
        verify(artistRepository).findAll(pageable);
    }

    // ================================================================
    // getArtistById
    // ================================================================

    @Test
    @DisplayName("getArtistById - existing artist returns full profile")
    void getArtistById_existingId_returnsArtistProfile() {
        // Given
        Long artistId = 1L;
        Artist mockArtist = createMockArtist(artistId);
        List<Song> mockSongs = List.of(createMockSong(10L));
        List<Album> mockAlbums = List.of(createMockAlbum(20L));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(mockArtist));
        when(songRepository.findAllByArtistId(artistId)).thenReturn(mockSongs);
        when(albumRepository.findAllByArtistId(artistId)).thenReturn(mockAlbums);

        // When
        ArtistDTO result = artistCatalogService.getArtistById(artistId);

        // Then
        assertNotNull(result);
        assertEquals("Test Artist", result.getArtistName());
        assertEquals(1, result.getAlbums().size());
        assertEquals(1, result.getSongs().size());
        assertEquals("Mock Song", result.getSongs().get(0).getTitle());
        verify(artistRepository).findById(artistId);
        verify(songRepository).findAllByArtistId(artistId);
        verify(albumRepository).findAllByArtistId(artistId);
    }

    @Test
    @DisplayName("getArtistById - maps artist base fields correctly")
    void getArtistById_mapsArtistFields() {
        // Given
        when(artistRepository.findById(1L)).thenReturn(Optional.of(createMockArtist(1L)));
        when(songRepository.findAllByArtistId(1L)).thenReturn(List.of());
        when(albumRepository.findAllByArtistId(1L)).thenReturn(List.of());

        // When
        ArtistDTO result = artistCatalogService.getArtistById(1L);

        // Then
        assertEquals(1L,            result.getId());
        assertEquals("Test Artist", result.getArtistName());
        assertEquals("Indie",       result.getGenre());
        assertEquals("Indie pop singer-songwriter.", result.getBio());
    }

    @Test
    @DisplayName("getArtistById - maps song fields including artistId and artistName")
    void getArtistById_mapsSongFields() {
        // Given
        when(artistRepository.findById(1L)).thenReturn(Optional.of(createMockArtist(1L)));
        when(songRepository.findAllByArtistId(1L)).thenReturn(List.of(createMockSong(10L)));
        when(albumRepository.findAllByArtistId(1L)).thenReturn(List.of());

        // When
        ArtistDTO result = artistCatalogService.getArtistById(1L);

        // Then
        assertEquals(10L,          result.getSongs().get(0).getId());
        assertEquals("Mock Song",  result.getSongs().get(0).getTitle());
        assertEquals(10L,          result.getSongs().get(0).getArtistId());
        assertEquals("Mock Artist",result.getSongs().get(0).getArtistName());
    }

    @Test
    @DisplayName("getArtistById - maps album fields including artistId and artistName")
    void getArtistById_mapsAlbumFields() {
        // Given
        when(artistRepository.findById(1L)).thenReturn(Optional.of(createMockArtist(1L)));
        when(songRepository.findAllByArtistId(1L)).thenReturn(List.of());
        when(albumRepository.findAllByArtistId(1L)).thenReturn(List.of(createMockAlbum(20L)));

        // When
        ArtistDTO result = artistCatalogService.getArtistById(1L);

        // Then
        assertEquals(20L,          result.getAlbums().get(0).getId());
        assertEquals("Mock Album", result.getAlbums().get(0).getName());
    }

    @Test
    @DisplayName("getArtistById - artist with no songs or albums returns empty lists")
    void getArtistById_noSongsOrAlbums_returnsEmptyLists() {
        // Given
        when(artistRepository.findById(1L)).thenReturn(Optional.of(createMockArtist(1L)));
        when(songRepository.findAllByArtistId(1L)).thenReturn(List.of());
        when(albumRepository.findAllByArtistId(1L)).thenReturn(List.of());

        // When
        ArtistDTO result = artistCatalogService.getArtistById(1L);

        // Then
        assertTrue(result.getSongs().isEmpty());
        assertTrue(result.getAlbums().isEmpty());
    }

    @Test
    @DisplayName("getArtistById - song with no album has null albumId and albumName")
    void getArtistById_songWithNoAlbum_albumFieldsAreNull() {
        // Given
        when(artistRepository.findById(1L)).thenReturn(Optional.of(createMockArtist(1L)));
        when(songRepository.findAllByArtistId(1L))
                .thenReturn(List.of(createMockSongWithoutAlbum(10L)));
        when(albumRepository.findAllByArtistId(1L)).thenReturn(List.of());

        // When
        ArtistDTO result = artistCatalogService.getArtistById(1L);

        // Then
        assertNull(result.getSongs().get(0).getAlbumId());
        assertNull(result.getSongs().get(0).getAlbumName());
    }

    @Test
    @DisplayName("getArtistById - non-existing artist throws ResourceNotFoundException")
    void getArtistById_nonExistingId_throwsResourceNotFoundException() {
        // Given
        Long artistId = 999L;
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> artistCatalogService.getArtistById(artistId)
        );
        assertTrue(exception.getMessage().contains("Artist not found"));
        verify(artistRepository).findById(artistId);
        verifyNoInteractions(songRepository, albumRepository);
    }

    // ================================================================
    // Helper factories
    // ================================================================

    private Artist createMockArtist(Long id) {
        Artist artist = new Artist();
        artist.setId(id);
        artist.setArtistName("Test Artist");
        artist.setBio("Indie pop singer-songwriter.");
        artist.setGenre("Indie");
        artist.setProfilePictureUrl("https://picsum.photos/seed/aria-pfp/300/300");
        artist.setBannerImageUrl("https://picsum.photos/seed/aria-banner/1200/400");
        return artist;
    }

    private Song createMockSong(Long id) {
        Artist mockArtist = new Artist();
        mockArtist.setId(10L);
        mockArtist.setArtistName("Mock Artist");

        Album mockAlbum = new Album();
        mockAlbum.setId(20L);
        mockAlbum.setName("Mock Album");

        Song song = new Song();
        song.setId(id);
        song.setTitle("Mock Song");
        song.setGenre("Rock");
        song.setDuration(214);
        song.setReleaseDate(LocalDate.of(2023, 3, 15));
        song.setPlayCount(1000L);
        song.setVisibility(Song.Visibility.PUBLIC);
        song.setArtist(mockArtist);  // prevents getArtist() NPE in mapSongToDTO
        song.setAlbum(mockAlbum);    // prevents getAlbum() NPE in mapSongToDTO
        return song;
    }

    /** Standalone single — no album. Tests null-safe album mapping in mapSongToDTO. */
    private Song createMockSongWithoutAlbum(Long id) {
        Artist mockArtist = new Artist();
        mockArtist.setId(10L);
        mockArtist.setArtistName("Mock Artist");

        Song song = new Song();
        song.setId(id);
        song.setTitle("Standalone Single");
        song.setGenre("Indie");
        song.setDuration(205);
        song.setVisibility(Song.Visibility.PUBLIC);
        song.setArtist(mockArtist);
        song.setAlbum(null); // standalone — no album
        return song;
    }

    private Album createMockAlbum(Long id) {
        Album album = new Album();
        album.setId(id);
        album.setName("Mock Album");
        return album;
    }
}