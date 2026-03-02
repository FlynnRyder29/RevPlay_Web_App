package com.revplay.service;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.mapper.SongMapper;
import com.revplay.model.Album;
import com.revplay.model.Song;
import com.revplay.repository.AlbumRepository;
import com.revplay.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumCatalogService {

    private static final Logger log = LoggerFactory.getLogger(AlbumCatalogService.class);

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;

    // SongMapper injected for shared mapping — available for future use
    // Note: mapSongToDTO(Song, String albumName) kept intentionally below
    // to avoid lazy-loading album.getName() per song in tracklist queries
    private final SongMapper songMapper;
    // ArtistRepository removed — not needed, we navigate via album.getArtist()

    @Transactional(readOnly = true)
    public Page<AlbumDTO> getAllAlbums(Pageable pageable) {
        log.info("Fetching all albums, page={}", pageable.getPageNumber());
        return albumRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public AlbumDTO getAlbumById(Long id) {
        log.info("Fetching album detail for id={}", id);

        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album", "id", id));

        AlbumDTO dto = mapToDTO(album);

        // Pass album.getName() directly — avoids extra DB call per song
        List<SongDTO> tracks = songRepository
                .findByAlbumId(album.getId(), Pageable.unpaged())
                .stream()
                .map(song -> mapSongToDTO(song, album.getName()))  // ← lambda, not method reference
                .toList();

        dto.setTracks(tracks);
        return dto;
    }

    private AlbumDTO mapToDTO(Album album) {
        return AlbumDTO.builder()
                .id(album.getId())
                .name(album.getName())
                .description(album.getDescription())
                .coverImageUrl(album.getCoverImageUrl())
                .releaseDate(album.getReleaseDate())
                .artistId(album.getArtist() != null ? album.getArtist().getId() : null)        // ← fixed
                .artistName(album.getArtist() != null ? album.getArtist().getArtistName() : "Unknown") // ← fixed
                .build();
    }

    private SongDTO mapSongToDTO(Song song, String albumName) {
        return SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .genre(song.getGenre())
                .duration(song.getDuration())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .releaseDate(song.getReleaseDate())
                .playCount(song.getPlayCount())
                .visibility(song.getVisibility() != null ? song.getVisibility().name() : null)
                .artistId(song.getArtist() != null ? song.getArtist().getId() : null)
                .artistName(song.getArtist() != null ? song.getArtist().getArtistName() : "Unknown")
                .albumId(song.getAlbum() != null ? song.getAlbum().getId() : null)
                .albumName(albumName)
                .createdAt(song.getCreatedAt())
                .build();
    }
}
