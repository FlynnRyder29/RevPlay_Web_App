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
import com.revplay.model.Song;
import com.revplay.model.Artist;
import com.revplay.model.Album;
import com.revplay.repository.SongRepository;
import com.revplay.dto.SongDTO;
import com.revplay.exception.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SongService Unit Tests")
class SongServiceTest {

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private SongService songService;

    @Test
    @DisplayName("getSongById - existing song returns SongDTO")
    void getSongById_existingId_returnsSongDto() {
        // Given
        Long songId = 1L;
        Song mockSong = new Song();
        mockSong.setId(songId);
        mockSong.setTitle("Test Song");
        Artist mockArtist = new Artist();
        mockArtist.setId(10L);
        mockArtist.setArtistName("Test Artist");
        mockSong.setArtist(mockArtist);
        Album mockAlbum = new Album();
        mockAlbum.setId(20L);
        mockAlbum.setName("Test Album");
        mockSong.setAlbum(mockAlbum);
        when(songRepository.findById(songId)).thenReturn(Optional.of(mockSong));

        // When
        SongDTO result = songService.getSongById(songId);

        // Then
        assertNotNull(result);
        assertEquals("Test Song", result.getTitle());
        assertEquals("Test Artist", result.getArtistName());
        assertEquals("Test Album", result.getAlbumName());
        verify(songRepository).findById(songId);
    }

    @Test
    @DisplayName("getSongById - non-existing song throws ResourceNotFoundException")
    void getSongById_nonExistingId_throwsResourceNotFoundException() {
        // Given
        Long songId = 999L;
        when(songRepository.findById(songId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> songService.getSongById(songId)
        );
        assertTrue(exception.getMessage().contains("Song not found"));
        verify(songRepository).findById(songId);
    }

    @Test
    @DisplayName("searchSongs - keyword search returns paged results")
    void searchSongs_validKeyword_returnsPagedSongs() {
        // Given
        String keyword = "test";
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Song> mockPage = new PageImpl<>(List.of(createMockSong(1L)));
        when(songRepository.searchByKeyword(eq(keyword), eq(pageable)))
                .thenReturn(mockPage);

        // When
        Page<SongDTO> result = songService.searchSongs(keyword, pageable);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(songRepository).searchByKeyword(eq(keyword), eq(pageable));
    }

    @Test
    @DisplayName("filterSongs - filter criteria returns paged filtered results")
    void filterSongs_validFilters_returnsFilteredPage() {
        // Given
        String genre = "Rock";
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Song> mockPage = new PageImpl<>(List.of(createMockSong(1L)));
        when(songRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);

        // When
        Page<SongDTO> result = songService.filterSongs(genre, null, null, null, pageable);

        // Then
        assertFalse(result.isEmpty());
        verify(songRepository).findAll(any(Specification.class), eq(pageable));

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
        song.setArtist(mockArtist);  // ✅ prevents getArtist() NPE
        song.setAlbum(mockAlbum);    // ✅ prevents getAlbum() NPE
        return song;
    }
}
