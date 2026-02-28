package com.revplay.service;

import com.revplay.dto.PlaylistDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.model.Playlist;
import com.revplay.model.User;
import com.revplay.repository.PlaylistRepository;
import com.revplay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
                           UserRepository userRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
    }

    // -------------------------
    // Helper: Current User
    // -------------------------

    private User getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User", "username", username
                        )
                );
    }

    // -------------------------
    // CREATE
    // -------------------------

    @Transactional
    public PlaylistDTO createPlaylist(PlaylistDTO dto) {

        User currentUser = getCurrentUser();

        log.debug("Creating playlist for userId: {}", currentUser.getId());

        Playlist playlist = new Playlist();
        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setPublic(dto.isPublic());
        playlist.setUser(currentUser);

        return toDTO(playlistRepository.save(playlist));
    }

    // -------------------------
    // GET MY PLAYLISTS
    // -------------------------

    public List<PlaylistDTO> getMyPlaylists() {

        User currentUser = getCurrentUser();

        log.debug("Fetching playlists for userId: {}", currentUser.getId());

        return playlistRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // -------------------------
    // GET BY ID
    // -------------------------

    public PlaylistDTO getPlaylistById(Long id) {

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Playlist", "id", id
                        )
                );

        User currentUser = getCurrentUser();

        if (!playlist.isPublic()
                && !playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException(
                    "Access denied to playlist: " + id
            );
        }

        return toDTO(playlist);
    }

    // -------------------------
    // UPDATE
    // -------------------------

    @Transactional
    public PlaylistDTO updatePlaylist(Long id, PlaylistDTO dto) {

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Playlist", "id", id
                        )
                );

        User currentUser = getCurrentUser();

        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException(
                    "You don't own this playlist"
            );
        }

        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setPublic(dto.isPublic());

        log.debug("Updated playlist: {}", id);

        return toDTO(playlistRepository.save(playlist));
    }

    // -------------------------
    // DELETE
    // -------------------------

    @Transactional
    public void deletePlaylist(Long id) {

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Playlist", "id", id
                        )
                );

        User currentUser = getCurrentUser();

        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException(
                    "You don't own this playlist"
            );
        }

        playlistRepository.delete(playlist);

        log.debug("Deleted playlist: {}", id);
    }

    // -------------------------
    // Mapping
    // -------------------------

    private PlaylistDTO toDTO(Playlist playlist) {

        PlaylistDTO dto = new PlaylistDTO();

        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setPublic(playlist.isPublic());
        dto.setUserId(playlist.getUser().getId());
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setUpdatedAt(playlist.getUpdatedAt());

        return dto;
    }
}