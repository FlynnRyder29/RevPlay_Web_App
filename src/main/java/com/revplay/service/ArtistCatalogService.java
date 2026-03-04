package com.revplay.service;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.ArtistDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Album;
import com.revplay.model.Artist;
import com.revplay.model.Song;
import com.revplay.repository.AlbumRepository;
import com.revplay.repository.ArtistRepository;
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
public class ArtistCatalogService {

    private static final Logger log = LoggerFactory.getLogger(ArtistCatalogService.class);
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;

    @Transactional(readOnly = true)
    public Page<ArtistDTO> getAllArtists(Pageable pageable) {
        log.info("Fetching all artists, page={}", pageable.getPageNumber());
        return artistRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public ArtistDTO getArtistById(Long id) {
        log.info("Fetching artist detail for id={}", id);
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", "id", id));

        ArtistDTO dto = mapToDTO(artist);

        // 🔴 FIX: Only show PUBLIC songs on public artist profile
        // Artists manage their own songs (including UNLISTED/PRIVATE) via
        // the artist dashboard. Public visitors should only see PUBLIC songs.
        dto.setSongs(songRepository.findAllByArtistId(id)
                .stream()
                .filter(song -> song.getVisibility() == Song.Visibility.PUBLIC)
                .map(this::mapSongToDTO)
                .toList());

        dto.setAlbums(albumRepository.findAllByArtistId(id)
                .stream().map(this::mapAlbumToDTO).toList());

        return dto;
    }

    private ArtistDTO mapToDTO(Artist artist) {
        return ArtistDTO.builder()
                .id(artist.getId())
                .artistName(artist.getArtistName())
                .bio(artist.getBio())
                .genre(artist.getGenre())
                .profilePictureUrl(artist.getProfilePictureUrl())
                .bannerImageUrl(artist.getBannerImageUrl())
                .build();
    }

    private SongDTO mapSongToDTO(Song song) {
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
                .artistId(song.getArtist().getId())
                .artistName(song.getArtist().getArtistName())
                .albumId(song.getAlbum() != null ? song.getAlbum().getId() : null)
                .albumName(song.getAlbum() != null ? song.getAlbum().getName() : null)
                .createdAt(song.getCreatedAt())
                .build();
    }

    private AlbumDTO mapAlbumToDTO(Album album) {
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
}