package com.revplay.service;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.mapper.SongMapper;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private static final Logger log = LoggerFactory.getLogger(AlbumServiceImpl.class);

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final SongMapper songMapper;

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AlbumDTO createAlbum(AlbumDTO request) {

        Artist artist = getCurrentArtist();

        log.info("Creating album '{}' for artistId={}", request.getName(), artist.getId());

        Album album = new Album();
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        album.setCoverImageUrl(request.getCoverImageUrl());
        album.setReleaseDate(request.getReleaseDate());
        album.setArtist(artist);

        Album saved = albumRepository.save(album);

        log.info("Album created with id={}", saved.getId());

        return mapToDTO(saved);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AlbumDTO updateAlbum(Long albumId, AlbumDTO request) {

        Artist artist = getCurrentArtist();

        log.info("Updating albumId={} for artistId={}", albumId, artist.getId());

        Album album = getOwnedAlbum(albumId, artist.getId());

        // Null-safe updates — only update fields that are provided
        if (request.getName() != null)
            album.setName(request.getName());

        if (request.getDescription() != null)
            album.setDescription(request.getDescription());

        if (request.getCoverImageUrl() != null)
            album.setCoverImageUrl(request.getCoverImageUrl());

        if (request.getReleaseDate() != null)
            album.setReleaseDate(request.getReleaseDate());

        Album saved = albumRepository.save(album);

        log.info("Album updated id={}", saved.getId());

        return mapToDTO(saved);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteAlbum(Long albumId) {

        Artist artist = getCurrentArtist();

        log.info("Deleting albumId={} for artistId={}", albumId, artist.getId());

        Album album = getOwnedAlbum(albumId, artist.getId());

        // 🔴 HIGH — Guard: only delete if album has no songs
        // Per project plan: "Delete album only if empty"
        // DB schema has ON DELETE SET NULL — but we block it explicitly
        // to prevent silent orphaning of songs
        int songCount = albumRepository.countSongsByAlbumId(albumId);
        if (songCount > 0) {
            throw new BadRequestException(
                    "Cannot delete album with " + songCount +
                            " song(s). Remove all songs from the album first.");
        }

        albumRepository.delete(album);

        log.info("Album deleted id={}", albumId);
    }

    // ── ADD SONG TO ALBUM ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void addSongToAlbum(Long albumId, Long songId) {

        Artist artist = getCurrentArtist();

        log.info("Adding songId={} to albumId={} for artistId={}", songId, albumId, artist.getId());

        // Verify album belongs to this artist
        Album album = getOwnedAlbum(albumId, artist.getId());

        // Verify song exists and belongs to this artist
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        if (!song.getArtist().getId().equals(artist.getId())) {
            throw new BadRequestException("Song does not belong to the logged-in artist");
        }

        // 🟡 MEDIUM — Option A: Block if song belongs to ANY album (same or different)
        // Artist must explicitly remove song from current album before adding to another
        if (song.getAlbum() != null) {
            if (song.getAlbum().getId().equals(albumId)) {
                throw new BadRequestException(
                        "Song is already part of this album");
            } else {
                throw new BadRequestException(
                        "Song is already in album '" + song.getAlbum().getName() +
                                "'. Remove it first before adding to another album.");
            }
        }

        song.setAlbum(album);
        songRepository.save(song);

        log.info("Song id={} added to album id={}", songId, albumId);
    }

    // ── REMOVE SONG FROM ALBUM ───────────────────────────────────────────────

    @Override
    @Transactional
    public void removeSongFromAlbum(Long albumId, Long songId) {

        Artist artist = getCurrentArtist();

        log.info("Removing songId={} from albumId={} for artistId={}", songId, albumId, artist.getId());

        // Verify album belongs to this artist
        getOwnedAlbum(albumId, artist.getId());

        // Verify song exists and belongs to this artist
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        if (!song.getArtist().getId().equals(artist.getId())) {
            throw new BadRequestException("Song does not belong to the logged-in artist");
        }

        // Verify song is actually in this album
        if (song.getAlbum() == null || !song.getAlbum().getId().equals(albumId)) {
            throw new BadRequestException("Song does not belong to this album");
        }

        // Set album_id to null — song is preserved (matches DB schema: SET NULL)
        song.setAlbum(null);
        songRepository.save(song);

        log.info("Song id={} removed from album id={}", songId, albumId);
    }

    // ── LIST MY ALBUMS ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AlbumDTO> getMyAlbums(Pageable pageable) {

        Artist artist = getCurrentArtist();

        log.info("Fetching albums for artistId={}, page={}", artist.getId(), pageable.getPageNumber());

        return albumRepository.findByArtistId(artist.getId(), pageable)
                .map(this::mapToDTO);
    }

    // ── LIST MY SONGS ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<SongDTO> getMySongs(Pageable pageable) {

        Artist artist = getCurrentArtist();

        log.info("Fetching songs for artistId={}, page={}", artist.getId(), pageable.getPageNumber());

        // ✅ Using shared SongMapper — eliminates duplicate mapping logic
        return songRepository.findByArtistId(artist.getId(), pageable)
                .map(songMapper::toDTO);
    }

    // ── GET MY ALBUM BY ID ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AlbumDTO getMyAlbumById(Long albumId) {

        Artist artist = getCurrentArtist();

        log.info("Fetching albumId={} for artistId={}", albumId, artist.getId());

        Album album = getOwnedAlbum(albumId, artist.getId());

        AlbumDTO dto = mapToDTO(album);

        // Include ALL tracks — artist's own view (no visibility filter)
        List<SongDTO> tracks = songRepository.findByAlbumId(albumId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(songMapper::toDTO)
                .toList();
        dto.setTracks(tracks);

        return dto;
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────────────────

    // Get currently logged-in user from security context
    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));
    }

    // Get artist profile of the currently logged-in user
    private Artist getCurrentArtist() {
        User user = getCurrentUser();

        return artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artist", "userId", user.getId()));
    }

    // Fetch album and verify it belongs to the given artist (ownership check)
    private Album getOwnedAlbum(Long albumId, Long artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Album", "id", albumId));

        if (!album.getArtist().getId().equals(artistId)) {
            throw new BadRequestException(
                    "Album does not belong to the logged-in artist");
        }

        return album;
    }

    // Map Album entity → AlbumDTO
    // Note: album.getArtist() is LAZY — safe here as all callers are @Transactional
    private AlbumDTO mapToDTO(Album album) {
        int songCount = albumRepository.countSongsByAlbumId(album.getId());

        return AlbumDTO.builder()
                .id(album.getId())
                .name(album.getName())
                .description(album.getDescription())
                .coverImageUrl(album.getCoverImageUrl())
                .releaseDate(album.getReleaseDate())
                .artistId(album.getArtist() != null ? album.getArtist().getId() : null)
                .artistName(album.getArtist() != null ? album.getArtist().getArtistName() : "Unknown")
                .songCount(songCount)
                .createdAt(album.getCreatedAt())
                .build();
    }
}