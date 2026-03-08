package com.revplay.service;

import com.revplay.dto.SongDTO;
import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.exception.BadRequestException;
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
import com.revplay.specification.SongSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {

    private static final Logger log = LoggerFactory.getLogger(SongService.class);

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    // ========================= READ =========================

    // 🔴 FIX: Only return PUBLIC songs in browse endpoint
    // UNLISTED songs are accessible by direct link (getSongById) but should
    // NOT appear in browse results. PRIVATE songs are only for the artist.
    @Transactional(readOnly = true)
    public Page<SongDTO> getAllSongs(Pageable pageable) {
        log.debug("Fetching all PUBLIC songs, page={}", pageable.getPageNumber());
        return songRepository.findByVisibility(Song.Visibility.PUBLIC, pageable)
                .map(this::mapToDTO);
    }

    // getSongById — allows UNLISTED (direct link access) but NOT PRIVATE
    // PRIVATE songs are only accessible by the owning artist
    @Transactional(readOnly = true)
    public SongDTO getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));

        // 🔴 FIX: Block PRIVATE songs from non-owners
        if (song.getVisibility() == Song.Visibility.PRIVATE) {
            User currentUser = getCurrentUser();
            if (!song.getArtist().getUserId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Song", "id", id);
                // Return 404 instead of 403 to avoid revealing existence
            }
        }

        return mapToDTO(song);
    }

    // ── ARTIST'S OWN SONGS (all visibilities) ────────────────────────────────
// Used by artist dashboard/songs page — shows PUBLIC + UNLISTED + PRIVATE
    @Transactional(readOnly = true)
    public List<SongDTO> getArtistAllSongs(Long artistId) {
        log.debug("Fetching all songs for artistId={} (all visibilities)", artistId);
        return songRepository.findAllByArtistId(artistId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ── TRENDING SONGS (top by play count) ────────────────────────────────
    @Transactional(readOnly = true)
    public List<SongDTO> getTrendingSongs(int limit) {
        log.debug("Fetching top {} trending songs", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "playCount"));
        return songRepository.findByVisibility(Song.Visibility.PUBLIC, pageable)
                .getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // 🔴 FIX: Only search PUBLIC songs
    @Transactional(readOnly = true)
    public Page<SongDTO> searchSongs(String keyword, Pageable pageable) {
        return songRepository.searchByKeywordAndVisibility(
                        keyword, Song.Visibility.PUBLIC, pageable)
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

            // 🔴 FIX: Verify album belongs to the current artist
            if (!album.getArtist().getId().equals(artist.getId())) {
                throw new BadRequestException(
                        "Cannot add song to another artist's album");
            }
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

            // 🔴 FIX: Verify album belongs to the current artist
            Artist artist = artistRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artist", "userId", currentUser.getId()));

            if (!album.getArtist().getId().equals(artist.getId())) {
                throw new BadRequestException(
                        "Cannot move song to another artist's album");
            }

            song.setAlbum(album);
        }

        log.info("Artist {} updated song id={}", currentUser.getUsername(), id);

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

        log.info("Artist {} changed visibility of song id={} to {}",
                currentUser.getUsername(), id, visibility);

        return mapToDTO(songRepository.save(song));
    }

    // ========================= FILTER =========================

    // 🔴 FIX: Always include visibility=PUBLIC in filter specification
    @Transactional(readOnly = true)
    public Page<SongDTO> filterSongs(String genre, String artist, String album,
                                     Integer year, Pageable pageable) {
        log.info("Filtering songs - genre={}, artist={}, album={}, year={}",
                genre, artist, album, year);

        Specification<Song> spec = Specification
                .where(SongSpecification.isPublic())   // ← FIX: Always filter to PUBLIC
                .and(SongSpecification.hasGenre(genre))
                .and(SongSpecification.hasArtistName(artist))
                .and(SongSpecification.hasAlbumName(album))
                .and(SongSpecification.hasYear(year));

        return songRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    // ========================= HELPERS =========================

    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));
    }

    private void validateOwnership(Long artistUserId, Long currentUserId) {
        if (!artistUserId.equals(currentUserId)) {
            throw new UnauthorizedAccessException(
                    "You are not allowed to modify this song");
        }
    }

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
                .visibility(song.getVisibility() != null
                        ? song.getVisibility().name() : null)
                .artistId(song.getArtist().getId())
                .artistName(song.getArtist().getArtistName())
                .albumId(song.getAlbum() != null ? song.getAlbum().getId() : null)
                .albumName(song.getAlbum() != null ? song.getAlbum().getName() : null)
                .createdAt(song.getCreatedAt())
                .build();
    }
}