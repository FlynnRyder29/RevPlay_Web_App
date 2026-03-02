package com.revplay.controller;

import com.revplay.service.PlaylistFollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistFollowController {

    private static final Logger log =
            LoggerFactory.getLogger(PlaylistFollowController.class);

    private final PlaylistFollowService playlistFollowService;

    public PlaylistFollowController(PlaylistFollowService playlistFollowService) {
        this.playlistFollowService = playlistFollowService;
    }

    // -------------------------
    // FOLLOW PLAYLIST
    // -------------------------
    @PostMapping("/{playlistId}/follow")
    public ResponseEntity<Void> followPlaylist(
            @PathVariable Long playlistId) {

        log.info("POST /api/playlists/{}/follow", playlistId);

        playlistFollowService.followPlaylist(playlistId);

        return ResponseEntity.ok().build();
    }

    // -------------------------
    // UNFOLLOW PLAYLIST
    // -------------------------
    @DeleteMapping("/{playlistId}/follow")
    public ResponseEntity<Void> unfollowPlaylist(
            @PathVariable Long playlistId) {

        log.info("DELETE /api/playlists/{}/follow", playlistId);

        playlistFollowService.unfollowPlaylist(playlistId);

        return ResponseEntity.noContent().build();
    }
}