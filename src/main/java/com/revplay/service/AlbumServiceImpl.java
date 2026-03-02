package com.revplay.service;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
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
public class AlbumServiceImpl implements AlbumService {

    private static final Logger log = LoggerFactory.getLogger(AlbumServiceImpl.class);

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;

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

        // Verify song belongs to this artist
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        if (!song.getArtist().getId().equals(artist.getId())) {
            throw new BadRequestException("Song does not belong to the logged-in artist");
        }

        // Prevent adding song that is already in this album
        if (song.getAlbum() != null && song.getAlbum().getId().equals(albumId)) {
            throw new BadRequestException("Song is already part of this album");
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

        // Verify song belongs to this artist
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

        return songRepository.findByArtistId(artist.getId(), pageable)
                .map(this::mapSongToDTO);
    }

    // ── GET MY ALBUM BY ID ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AlbumDTO getMyAlbumById(Long albumId) {

        Artist artist = getCurrentArtist();

        log.info("Fetching albumId={} for artistId={}", albumId, artist.getId());

        Album album = getOwnedAlbum(albumId, artist.getId());

        return mapToDTO(album);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────────────────

    // Get currently logged-in user from security context
    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
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

    // Map Song entity → SongDTO
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
                .visibility(song.getVisibility() != null ? song.getVisibility().name() : null)
                .artistId(song.getArtist() != null ? song.getArtist().getId() : null)
                .artistName(song.getArtist() != null ? song.getArtist().getArtistName() : "Unknown")
                .albumId(song.getAlbum() != null ? song.getAlbum().getId() : null)
                .albumName(song.getAlbum() != null ? song.getAlbum().getName() : null)
                .createdAt(song.getCreatedAt())
                .build();
    }
}