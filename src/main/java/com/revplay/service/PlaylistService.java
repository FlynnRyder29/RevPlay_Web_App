package com.revplay.service;

import com.revplay.dto.PlaylistDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.model.Playlist;
import com.revplay.repository.PlaylistRepository;
import com.revplay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    // Get currently logged-in user's ID
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    // CREATE
    public PlaylistDTO createPlaylist(PlaylistDTO dto) {
        Long userId = getCurrentUserId();
        log.debug("Creating playlist for userId: {}", userId);
        Playlist playlist = new Playlist();
        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setPublic(dto.isPublic());
        playlist.setUserId(userId);
        return toDTO(playlistRepository.save(playlist));
    }

    // GET MY PLAYLISTS
    public List<PlaylistDTO> getMyPlaylists() {
        Long userId = getCurrentUserId();
        log.debug("Fetching playlists for userId: {}", userId);
        return playlistRepository.findByUserId(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // GET BY ID
    public PlaylistDTO getPlaylistById(Long id) {
        log.debug("Fetching playlist: {}", id);
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
        Long userId = getCurrentUserId();
        if (!playlist.isPublic() && !playlist.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied to playlist: " + id);
        }
        return toDTO(playlist);
    }

    // UPDATE
    public PlaylistDTO updatePlaylist(Long id, PlaylistDTO dto) {
        Long userId = getCurrentUserId();
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
        if (!playlist.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't own this playlist");
        }
        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setPublic(dto.isPublic());
        log.debug("Updated playlist: {}", id);
        return toDTO(playlistRepository.save(playlist));
    }

    // DELETE
    @Transactional
    public void deletePlaylist(Long id) {
        Long userId = getCurrentUserId();
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
        if (!playlist.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't own this playlist");
        }
        playlistRepository.deleteById(id);
        log.debug("Deleted playlist: {}", id);
    }

    // Entity → DTO
    private PlaylistDTO toDTO(Playlist p) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPublic(p.isPublic());
        dto.setUserId(p.getUserId());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}