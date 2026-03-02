package com.revplay.service;

import com.revplay.model.Artist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.revplay.model.Album;
import com.revplay.model.Song;
import com.revplay.repository.AlbumRepository;
import com.revplay.repository.SongRepository;
import com.revplay.dto.AlbumDTO;
import com.revplay.exception.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumCatalogService Unit Tests")
class AlbumCatalogServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private AlbumCatalogService albumCatalogService;

    @Test
    @DisplayName("getAllAlbums - pagination returns paged album list")
    void getAllAlbums_validPageable_returnsPagedAlbums() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Album> mockPage = new PageImpl<>(List.of(createMockAlbum(1L)));
        when(albumRepository.findAll(pageable)).thenReturn(mockPage);

        // When
        Page<AlbumDTO> result = albumCatalogService.getAllAlbums(pageable);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(albumRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getAlbumById - existing album returns album with tracklist")
    void getAlbumById_existingId_returnsAlbumWithTracks() {
        // Given
        Long albumId = 1L;
        Album mockAlbum = createMockAlbum(albumId);
        Page<Song> mockTracks = new PageImpl<>(List.of(createMockSong(10L), createMockSong(11L)));

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(mockAlbum));
        when(songRepository.findByAlbumId(eq(albumId), eq(Pageable.unpaged()))).thenReturn(mockTracks);

        // When
        AlbumDTO result = albumCatalogService.getAlbumById(albumId);

        // Then
        assertNotNull(result);
        assertEquals("Test Album", result.getName());
        assertEquals(2, result.getTracks().size());       // ✅ correct assertion — tracklist size
        verify(albumRepository).findById(albumId);
        verify(songRepository).findByAlbumId(eq(albumId), eq(Pageable.unpaged()));
    }




    @Test
    @DisplayName("getAlbumById - non-existing album throws ResourceNotFoundException")
    void getAlbumById_nonExistingId_throwsResourceNotFoundException() {
        // Given
        Long albumId = 999L;
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> albumCatalogService.getAlbumById(albumId)
        );
        assertTrue(exception.getMessage().contains("Album not found"));
        verify(albumRepository).findById(albumId);
        verifyNoInteractions(songRepository);
    }

    // ✅ FIXED
    private Album createMockAlbum(Long id) {
        Artist mockArtist = new Artist();
        mockArtist.setId(10L);
        mockArtist.setArtistName("Mock Artist");

        Album album = new Album();
        album.setId(id);          // ✅ must be here — @Setter exists so this works fine
        album.setName("Test Album");
        album.setArtist(mockArtist);
        return album;
    }



    private Song createMockSong(Long id) {
        Song song = new Song();
        song.setId(id);
        song.setTitle("Mock Track");
        return song;
    }
}
