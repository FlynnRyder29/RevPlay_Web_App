package com.revplay.service;

import com.revplay.dto.SongDTO;
import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Song;
import com.revplay.model.Artist;
import com.revplay.model.Album;
import com.revplay.model.User;
import com.revplay.repository.SongRepository;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.AlbumRepository;
import com.revplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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

    /* =========================================================
       EXISTING METHODS (UNCHANGED)
       ========================================================= */

    @Transactional(readOnly = true)
    public Page<SongDTO> getAllSongs(Pageable pageable) {
        log.debug("Fetching all songs, page={}", pageable.getPageNumber());
        return songRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public SongDTO getSongById(Long id) {
        log.debug("Fetching song by id={}", id);
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));
        return mapToDTO(song);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO> searchSongs(String keyword, Pageable pageable) {
        log.debug("Searching songs by keyword='{}'", keyword);
        return songRepository.searchByKeyword(keyword, pageable).map(this::mapToDTO);
    }


    @Transactional
    public SongDTO createSong(SongCreateRequest request) {

        User user = getCurrentUser();

        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artist", "userId", user.getId()));

        Song song = new Song();
        song.setTitle(request.getTitle());
        song.setGenre(request.getGenre());
        song.setDuration(request.getDuration());
        song.setAudioUrl(request.getAudioUrl());
        song.setCoverImageUrl(request.getCoverImageUrl());
        song.setReleaseDate(request.getReleaseDate());
        song.setArtist(artist);

        if (request.getAlbumId() != null) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Album", "id", request.getAlbumId()));
            song.setAlbum(album);
        }

        log.info("Artist {} created song '{}'", user.getUsername(), song.getTitle());

        return mapToDTO(songRepository.save(song));
    }

    @Transactional
    public SongDTO updateSong(Long id, SongUpdateRequest request) {

        User user = getCurrentUser();

        Song song = songRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Song", "id", id));

        validateOwnership(song, user);

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

        log.info("Artist {} updated song id={}", user.getUsername(), id);

        return mapToDTO(songRepository.save(song));
    }

    @Transactional
    public void deleteSong(Long id) {

        User user = getCurrentUser();

        Song song = songRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Song", "id", id));

        validateOwnership(song, user);

        songRepository.delete(song);

        log.info("Artist {} deleted song id={}", user.getUsername(), id);
    }

    @Transactional
    public SongDTO updateVisibility(Long id, String visibility) {

        User user = getCurrentUser();

        Song song = songRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Song", "id", id));

        validateOwnership(song, user);

        song.setVisibility(Song.Visibility.valueOf(visibility.toUpperCase()));

        log.info("Artist {} changed visibility of song id={}", user.getUsername(), id);

        return mapToDTO(songRepository.save(song));
    }

    /* =========================================================
       HELPER METHODS
       ========================================================= */

    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "username", username));
    }

    private void validateOwnership(Song song, User user) {
        if (!song.getArtist().getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to modify this song");
        }
    }

    // Existing mapping method unchanged
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