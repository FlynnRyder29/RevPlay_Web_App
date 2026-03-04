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
    private final SongMapper songMapper;

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

        // 🔴 FIX: Only show PUBLIC songs in album tracklist
        // UNLISTED/PRIVATE songs should not appear in public album view
        List<SongDTO> tracks = songRepository
                .findByAlbumId(album.getId(), Pageable.unpaged())
                .stream()
                .filter(song -> song.getVisibility() == Song.Visibility.PUBLIC)
                .map(song -> mapSongToDTO(song, album.getName()))
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
                .artistId(album.getArtist() != null
                        ? album.getArtist().getId() : null)
                .artistName(album.getArtist() != null
                        ? album.getArtist().getArtistName() : "Unknown")
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
                .visibility(song.getVisibility() != null
                        ? song.getVisibility().name() : null)
                .artistId(song.getArtist() != null
                        ? song.getArtist().getId() : null)
                .artistName(song.getArtist() != null
                        ? song.getArtist().getArtistName() : "Unknown")
                .albumId(song.getAlbum() != null
                        ? song.getAlbum().getId() : null)
                .albumName(albumName)
                .createdAt(song.getCreatedAt())
                .build();
    }
}