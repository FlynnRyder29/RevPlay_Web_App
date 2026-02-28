package com.revplay.controller;

import com.revplay.dto.PlaylistDTO;
import com.revplay.service.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private static final Logger log = LoggerFactory.getLogger(PlaylistController.class);

    @Autowired
    private PlaylistService playlistService;

    // POST /api/playlists
    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(@RequestBody PlaylistDTO dto) {
        log.debug("POST /api/playlists");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.createPlaylist(dto));
    }

    // GET /api/playlists/me
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistDTO>> getMyPlaylists() {
        log.debug("GET /api/playlists/me");
        return ResponseEntity.ok(playlistService.getMyPlaylists());
    }

    // GET /api/playlists/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getById(@PathVariable Long id) {
        log.debug("GET /api/playlists/{}", id);
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    // PUT /api/playlists/{id}
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistDTO> update(@PathVariable Long id,
                                              @RequestBody PlaylistDTO dto) {
        log.debug("PUT /api/playlists/{}", id);
        return ResponseEntity.ok(playlistService.updatePlaylist(id, dto));
    }

    // DELETE /api/playlists/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("DELETE /api/playlists/{}", id);
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}