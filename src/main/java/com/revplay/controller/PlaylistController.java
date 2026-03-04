package com.revplay.controller;

import com.revplay.dto.PlaylistDTO;
import com.revplay.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private static final Logger log = LoggerFactory.getLogger(PlaylistController.class);

    private final PlaylistService playlistService;


    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(
            @Valid @RequestBody PlaylistDTO dto) {
        log.info("POST /api/playlists");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.createPlaylist(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PlaylistDTO>> getMyPlaylists() {
        log.info("GET /api/playlists/me");
        return ResponseEntity.ok(playlistService.getMyPlaylists());
    }

    // -------------------------
    // BROWSE PUBLIC PLAYLISTS
    // Returns all public playlists sorted newest-first.
    // Optional ?search= param filters by playlist name (case-insensitive).
    // GET /api/playlists/public
    // GET /api/playlists/public?search=chill
    // -------------------------

    @GetMapping("/public")
    public ResponseEntity<List<PlaylistDTO>> getPublicPlaylists(
            @RequestParam(required = false) String search) {
        log.info("GET /api/playlists/public search='{}'", search);
        return ResponseEntity.ok(playlistService.getPublicPlaylists(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getById(@PathVariable Long id) {
        log.info("GET /api/playlists/{}", id);
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistDTO dto) {
        log.info("PUT /api/playlists/{}", id);
        return ResponseEntity.ok(playlistService.updatePlaylist(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/playlists/{}", id);
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/songs/{songId}")
    public ResponseEntity<Void> addSong(
            @PathVariable Long id,
            @PathVariable Long songId) {
        log.info("POST /api/playlists/{}/songs/{}", id, songId);
        playlistService.addSongToPlaylist(id, songId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<Void> removeSong(
            @PathVariable Long id,
            @PathVariable Long songId) {
        log.info("DELETE /api/playlists/{}/songs/{}", id, songId);
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reorder")
    public ResponseEntity<Void> reorderSongs(
            @PathVariable Long id,
            @RequestBody List<Long> orderedSongIds) {
        log.info("PUT /api/playlists/{}/reorder - {} songs", id, orderedSongIds.size());
        playlistService.reorderSongs(id, orderedSongIds);
        return ResponseEntity.noContent().build();
    }

    // follow/unfollow/isFollowing → handled by PlaylistFollowController
}