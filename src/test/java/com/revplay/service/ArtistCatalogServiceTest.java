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
import com.revplay.model.Artist;
import com.revplay.model.Song;
import com.revplay.model.Album;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.AlbumRepository;
import com.revplay.dto.ArtistDTO;
import com.revplay.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        assertEquals(1, result.getSongs().size());                          // ← ADD
        assertEquals("Mock Song", result.getSongs().get(0).getTitle());
        verify(artistRepository).findById(artistId);
        verify(songRepository).findAllByArtistId(artistId);
        verify(albumRepository).findAllByArtistId(artistId);
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

    private Artist createMockArtist(Long id) {
        Artist artist = new Artist();
        artist.setId(id);
        artist.setArtistName("Test Artist");
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
        song.setArtist(mockArtist);  // ✅ prevents getArtist() NPE in mapSongToDTO
        song.setAlbum(mockAlbum);    // ✅ prevents getAlbum() NPE
        return song;
    }


    private Album createMockAlbum(Long id) {
        Album album = new Album();
        album.setId(id);
        album.setName("Mock Album");
        return album;
    }
}
