package com.revplay.revplay_p2.controller;

import com.revplay.revplay_p2.model.Playlist;
import com.revplay.revplay_p2.model.PlaylistFollow;
import com.revplay.revplay_p2.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<Playlist> create(@RequestBody Playlist playlist) {
        Playlist created = playlistService.createPlaylist(playlist);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Playlist>> getMyPlaylists(@RequestParam Long userId) {
        List<Playlist> playlists = playlistService.getMyPlaylists(userId);
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getById(@PathVariable Long id) {
        Playlist playlist = playlistService.getPlaylistById(id);
        return ResponseEntity.ok(playlist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Playlist> update(@PathVariable Long id, @RequestBody Playlist playlist) {
        Playlist updated = playlistService.updatePlaylist(id, playlist);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public")
    public ResponseEntity<List<Playlist>> getPublicPlaylists() {
        List<Playlist> playlists = playlistService.getPublicPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @PostMapping("/{id}/songs/{songId}")
    public ResponseEntity<Void> addSong(@PathVariable Long id, @PathVariable Long songId) {
        playlistService.addSongToPlaylist(id, songId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<Void> removeSong(@PathVariable Long id, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reorder")
    public ResponseEntity<Void> reorderSongs(@PathVariable Long id, @RequestBody List<Long> songIds) {
        playlistService.reorderSongs(id, songIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followPlaylist(@RequestParam Long userId, @PathVariable Long id) {
        playlistService.followPlaylist(userId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollowPlaylist(@RequestParam Long userId, @PathVariable Long id) {
        playlistService.unfollowPlaylist(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<PlaylistFollow>> getFollowers(@PathVariable Long id) {
        List<PlaylistFollow> followers = playlistService.getFollowers(id);
        return ResponseEntity.ok(followers);
    }
}