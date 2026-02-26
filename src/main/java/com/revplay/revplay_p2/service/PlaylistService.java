package com.revplay.revplay_p2.service;

import com.revplay.revplay_p2.model.Playlist;
import com.revplay.revplay_p2.model.PlaylistSong;
import com.revplay.revplay_p2.model.PlaylistFollow;
import com.revplay.revplay_p2.repository.PlaylistRepository;
import com.revplay.revplay_p2.repository.PlaylistSongRepository;
import com.revplay.revplay_p2.repository.PlaylistFollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistSongRepository playlistSongRepository;

    @Autowired
    private PlaylistFollowRepository playlistFollowRepository;

    // ---------- Playlist CRUD ----------
    public Playlist createPlaylist(Playlist playlist) {
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());
        return playlistRepository.save(playlist);
    }

    public List<Playlist> getMyPlaylists(Long userId) {
        return playlistRepository.findByUserId(userId);
    }

    public Playlist getPlaylistById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found with id: " + id));
    }

    public Playlist updatePlaylist(Long id, Playlist updated) {
        Playlist existing = getPlaylistById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPublic(updated.isPublic());
        existing.setUpdatedAt(LocalDateTime.now());
        return playlistRepository.save(existing);
    }

    @Transactional
    public void deletePlaylist(Long id) {
        playlistSongRepository.deleteByPlaylistId(id);
        playlistFollowRepository.deleteByPlaylistId(id);
        playlistRepository.deleteById(id);
    }

    public List<Playlist> getPublicPlaylists() {
        return playlistRepository.findByIsPublicTrue();
    }

    // ---------- Playlist Songs ----------
    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) {
        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylistId(playlistId);
        ps.setSongId(songId);
        int nextPos = playlistSongRepository.countByPlaylistId(playlistId) + 1;
        ps.setPosition(nextPos);
        ps.setAddedAt(LocalDateTime.now());
        playlistSongRepository.save(ps);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    @Transactional
    public void reorderSongs(Long playlistId, List<Long> songIdsInOrder) {
        playlistSongRepository.deleteByPlaylistId(playlistId);
        int pos = 1;
        for (Long songId : songIdsInOrder) {
            PlaylistSong ps = new PlaylistSong();
            ps.setPlaylistId(playlistId);
            ps.setSongId(songId);
            ps.setPosition(pos++);
            ps.setAddedAt(LocalDateTime.now());
            playlistSongRepository.save(ps);
        }
    }

    // ---------- Follow/Unfollow ----------
    public void followPlaylist(Long userId, Long playlistId) {
        if (!playlistFollowRepository.existsByUserIdAndPlaylistId(userId, playlistId)) {
            PlaylistFollow follow = new PlaylistFollow();
            follow.setUserId(userId);
            follow.setPlaylistId(playlistId);
            follow.setFollowedAt(LocalDateTime.now());
            playlistFollowRepository.save(follow);
        }
    }

    public void unfollowPlaylist(Long userId, Long playlistId) {
        playlistFollowRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
    }

    public List<PlaylistFollow> getFollowers(Long playlistId) {
        return playlistFollowRepository.findByPlaylistId(playlistId);
    }
}