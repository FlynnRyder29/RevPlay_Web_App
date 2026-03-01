package com.revplay.service;

import com.revplay.dto.SongDTO;
import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongUpdateRequest;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongService {

    private static final Logger log = LoggerFactory.getLogger(SongService.class);

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    // ========================= READ =========================

    @Transactional(readOnly = true)
    public Page<SongDTO> getAllSongs(Pageable pageable) {
        log.debug("Fetching all songs, page={}", pageable.getPageNumber());
        return songRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public SongDTO getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));
        return mapToDTO(song);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO> searchSongs(String keyword, Pageable pageable) {
        return songRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToDTO);
    }

    // ========================= CREATE =========================

    @Transactional
    public SongDTO createSong(SongCreateRequest request) {

        User currentUser = getCurrentUser();

        Artist artist = artistRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artist", "userId", currentUser.getId()));

        Album album = null;
        if (request.getAlbumId() != null) {
            album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Album", "id", request.getAlbumId()));
        }

        Song song = Song.builder()
                .title(request.getTitle())
                .genre(request.getGenre())
                .duration(request.getDuration())
                .audioUrl(request.getAudioUrl())
                .coverImageUrl(request.getCoverImageUrl())
                .releaseDate(request.getReleaseDate())
                .visibility(request.getVisibility() != null
                        ? request.getVisibility()
                        : Song.Visibility.PUBLIC)
                .artist(artist)
                .album(album)
                .build();

        // ✅ FIX: Map the saved entity so id and createdAt are populated
        Song saved = songRepository.save(song);

        log.info("Artist {} created song '{}'", currentUser.getUsername(), saved.getTitle());

        return mapToDTO(saved);
    }

    // ========================= UPDATE =========================

    @Transactional
    public SongDTO updateSong(Long id, SongUpdateRequest request) {

        User currentUser = getCurrentUser();

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));

        validateOwnership(song.getArtist().getUserId(), currentUser.getId());

        if (request.getTitle() != null)
            song.setTitle(request.getTitle());

        if (request.getGenre() != null)
            song.setGenre(request.getGenre());

        if (request.getDuration() != null)
            song.setDuration(request.getDuration());

        if (request.getAudioUrl() != null)
            song.setAudioUrl(request.getAudioUrl());

        if (request.getCoverImageUrl() != null)
            song.setCoverImageUrl(request.getCoverImageUrl());

        if (request.getReleaseDate() != null)
            song.setReleaseDate(request.getReleaseDate());

        if (request.getVisibility() != null)
            song.setVisibility(request.getVisibility());

        if (request.getAlbumId() != null) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Album", "id", request.getAlbumId()));
            song.setAlbum(album);
        }

        log.info("Artist {} updated song id={}", currentUser.getUsername(), id);

        // ✅ FIX: Explicit save — don't rely on dirty checking alone
        return mapToDTO(songRepository.save(song));
    }

    // ========================= DELETE =========================

    @Transactional
    public void deleteSong(Long id) {

        User currentUser = getCurrentUser();

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));

        validateOwnership(song.getArtist().getUserId(), currentUser.getId());

        songRepository.delete(song);

        log.info("Artist {} deleted song id={}", currentUser.getUsername(), id);
    }

    // ========================= VISIBILITY =========================

    @Transactional
    public SongDTO updateVisibility(Long id, String visibility) {

        User currentUser = getCurrentUser();

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));

        validateOwnership(song.getArtist().getUserId(), currentUser.getId());

        song.setVisibility(Song.Visibility.valueOf(visibility));

        log.info("Artist {} changed visibility of song id={} to {}", currentUser.getUsername(), id, visibility);

        return mapToDTO(songRepository.save(song));
    }

    // ========================= HELPERS =========================

    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));
    }

    private void validateOwnership(Long artistUserId, Long currentUserId) {
        if (!artistUserId.equals(currentUserId)) {
            throw new UnauthorizedAccessException(
                    "You are not allowed to modify this song"
            );
        }
    }

    // ✅ FIX: All fields mapped — title, genre, duration, audioUrl, coverImageUrl restored
    private SongDTO mapToDTO(Song song) {
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
                .artistId(song.getArtist().getId())
                .artistName(song.getArtist().getArtistName())
                .albumId(song.getAlbum() != null ? song.getAlbum().getId() : null)
                .albumName(song.getAlbum() != null ? song.getAlbum().getName() : null)
                .createdAt(song.getCreatedAt())
                .build();
    }
}