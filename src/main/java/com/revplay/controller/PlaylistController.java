package com.revplay.controller;

import com.revplay.dto.PlaylistDTO;
import com.revplay.service.PlaylistService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private static final Logger log = LoggerFactory.getLogger(PlaylistController.class);

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // -------------------------
    // CREATE
    // -------------------------

    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(
            @Valid @RequestBody PlaylistDTO dto) {

        log.info("POST /api/playlists");

        PlaylistDTO created = playlistService.createPlaylist(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // -------------------------
    // GET MY PLAYLISTS
    // -------------------------

    @GetMapping("/me")
    public ResponseEntity<List<PlaylistDTO>> getMyPlaylists() {

        log.info("GET /api/playlists/me");

        return ResponseEntity.ok(
                playlistService.getMyPlaylists()
        );
    }

    // -------------------------
    // GET BY ID
    // -------------------------

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getById(@PathVariable Long id) {

        log.info("GET /api/playlists/{}", id);

        return ResponseEntity.ok(
                playlistService.getPlaylistById(id)
        );
    }

    // -------------------------
    // UPDATE
    // -------------------------

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistDTO dto) {

        log.info("PUT /api/playlists/{}", id);

        return ResponseEntity.ok(
                playlistService.updatePlaylist(id, dto)
        );
    }

    // -------------------------
    // DELETE
    // -------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        log.info("DELETE /api/playlists/{}", id);

        playlistService.deletePlaylist(id);

        return ResponseEntity.noContent().build();
    }
}