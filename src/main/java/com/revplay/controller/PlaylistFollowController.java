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
    // POST /api/playlists/{id}/follow
    // -------------------------

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followPlaylist(@PathVariable Long id) {

        log.info("POST /api/playlists/{}/follow", id);

        playlistFollowService.followPlaylist(id);

        return ResponseEntity.ok().build();
    }

    // -------------------------
    // UNFOLLOW PLAYLIST
    // DELETE /api/playlists/{id}/follow
    // -------------------------

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollowPlaylist(@PathVariable Long id) {

        log.info("DELETE /api/playlists/{}/follow", id);

        playlistFollowService.unfollowPlaylist(id);

        return ResponseEntity.noContent().build();
    }

    // -------------------------
    // CHECK IF FOLLOWING
    // GET /api/playlists/{id}/follow
    // -------------------------

    @GetMapping("/{id}/follow")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long id) {

        log.info("GET /api/playlists/{}/follow", id);

        return ResponseEntity.ok(
                playlistFollowService.isFollowing(id)
        );
    }
}