package com.revplay.controller;

import com.revplay.dto.PlaylistDTO;
import com.revplay.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private static final Logger log = LoggerFactory.getLogger(PlaylistController.class);

    private final PlaylistService playlistService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaylistDTO> createPlaylist(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {

        log.info("POST /api/playlists (multipart)");

        PlaylistDTO dto = new PlaylistDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setPublicPlaylist(isPublic);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.createPlaylist(dto, coverImage));
    }

    // Keep JSON fallback for backward compatibility (playlist-actions.js "Add to Playlist" modal)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaylistDTO> createPlaylistJson(
            @RequestBody PlaylistDTO dto) {

        log.info("POST /api/playlists (json)");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.createPlaylist(dto, null));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PlaylistDTO>> getMyPlaylists() {
        log.info("GET /api/playlists/me");
        return ResponseEntity.ok(playlistService.getMyPlaylists());
    }

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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaylistDTO> updatePlaylist(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {

        log.info("PUT /api/playlists/{} (multipart)", id);

        PlaylistDTO dto = new PlaylistDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setPublicPlaylist(isPublic);

        return ResponseEntity.ok(playlistService.updatePlaylist(id, dto, coverImage));
    }

    // Keep JSON fallback for backward compatibility
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaylistDTO> updatePlaylistJson(
            @PathVariable Long id,
            @RequestBody PlaylistDTO dto) {

        log.info("PUT /api/playlists/{} (json)", id);

        return ResponseEntity.ok(playlistService.updatePlaylist(id, dto, null));
    }

    @DeleteMapping("/{id}/cover")
    public ResponseEntity<PlaylistDTO> removeCover(@PathVariable Long id) {
        log.info("DELETE /api/playlists/{}/cover", id);
        return ResponseEntity.ok(playlistService.removeCoverImage(id));
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
}