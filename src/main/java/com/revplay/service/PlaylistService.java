package com.revplay.service;

import com.revplay.dto.PlaylistDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.model.Playlist;
import com.revplay.model.PlaylistFollow;
import com.revplay.model.PlaylistSong;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.PlaylistFollowRepository;
import com.revplay.repository.PlaylistRepository;
import com.revplay.repository.PlaylistSongRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepository       playlistRepository;
    private final PlaylistSongRepository   playlistSongRepository;
    private final PlaylistFollowRepository playlistFollowRepository;
    private final SongRepository           songRepository;
    private final SecurityUtils            securityUtils;

    // -------------------------
    // CREATE
    // -------------------------

    @Transactional
    public PlaylistDTO createPlaylist(PlaylistDTO dto) {

        User currentUser = securityUtils.getCurrentUser();

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

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getMyPlaylists() {

        User currentUser = securityUtils.getCurrentUser();

        log.debug("Fetching playlists for userId: {}", currentUser.getId());

        return playlistRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // -------------------------
    // GET PUBLIC PLAYLISTS (browse)
    // Returns all public playlists sorted newest-first.
    // If a keyword is provided, filters by playlist name (case-insensitive).
    // GET /api/playlists/public
    // GET /api/playlists/public?search=chill
    // -------------------------

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getPublicPlaylists(String search) {

        log.debug("Browsing public playlists, search='{}'", search);

        List<Playlist> results;

        if (search != null && !search.isBlank()) {
            results = playlistRepository.searchPublicByName(search.trim());
        } else {
            results = playlistRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        }

        return results.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // -------------------------
    // GET BY ID
    // -------------------------

    @Transactional(readOnly = true)
    public PlaylistDTO getPlaylistById(Long id) {

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Playlist", "id", id));

        User currentUser = securityUtils.getCurrentUser();

        if (!playlist.isPublic()
                && !playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException(
                    "Access denied to playlist: " + id);
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
                        new ResourceNotFoundException("Playlist", "id", id));

        User currentUser = securityUtils.getCurrentUser();

        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You don't own this playlist");
        }

        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setPublic(dto.isPublic());

        log.debug("Updated playlist: {}", id);

        return toDTO(playlistRepository.save(playlist));
    }

    // -------------------------
    // DELETE
    // Edge case: playlist_song and playlist_follow rows hold FK references to
    // playlist.id. They must be deleted first or the DB throws a constraint
    // violation. Enforced order: songs → follows → playlist.
    // -------------------------

    @Transactional
    public void deletePlaylist(Long id) {

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Playlist", "id", id));

        User currentUser = securityUtils.getCurrentUser();

        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You don't own this playlist");
        }

        // Step 1 — remove all songs from the playlist first
        playlistSongRepository.deleteByPlaylist_Id(id);

        // Step 2 — remove all follow relationships for this playlist
        playlistFollowRepository.deleteByPlaylist_Id(id);

        // Step 3 — now safe to delete the playlist itself
        playlistRepository.delete(playlist);

        log.debug("Deleted playlist {} (songs and follows cleaned up first)", id);
    }

    // -------------------------
    // ADD SONG TO PLAYLIST
    // -------------------------

    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Playlist", "id", playlistId));

        User currentUser = securityUtils.getCurrentUser();

        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You don't own this playlist");
        }

        // Idempotent — prevent duplicates silently
        if (playlistSongRepository.existsByPlaylist_IdAndSong_Id(playlistId, songId)) {
            log.debug("Song {} already in playlist {}", songId, playlistId);
            return;
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Song", "id", songId));

        // Position = current count so new song goes to the end
        List<PlaylistSong> existing = playlistSongRepository
                .findByPlaylist_IdOrderByPosition(playlistId);

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        playlistSong.setPosition(existing.size()+1);

        playlistSongRepository.save(playlistSong);

        log.debug("Added song {} to playlist {} at position {}",
                songId, playlistId, existing.size()+1);
    }

    // -------------------------
    // REMOVE SONG FROM PLAYLIST
    // -------------------------

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Playlist", "id", playlistId));

        User currentUser = securityUtils.getCurrentUser();

        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You don't own this playlist");
        }

        if (!playlistSongRepository.existsByPlaylist_IdAndSong_Id(playlistId, songId)) {
            throw new ResourceNotFoundException("Song", "id", songId);
        }

        playlistSongRepository.deleteByPlaylist_IdAndSong_Id(playlistId, songId);

        log.debug("Removed song {} from playlist {}", songId, playlistId);
    }

    // -------------------------
    // REORDER SONGS
    // orderedSongIds: full list of songIds in the desired new order.
    // e.g. [3, 1, 5, 2] → song 3 gets position 1, song 1 gets position 2, etc.
    // The list must contain exactly the same song IDs as the playlist —
    // no additions, no omissions, checked via size + containsAll.
    // -------------------------

    @Transactional
    public void reorderSongs(Long playlistId, List<Long> orderedSongIds) {

        User currentUser = securityUtils.getCurrentUser();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Playlist", "id", playlistId));

        if (!playlist.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException(
                    "You can only reorder songs in your own playlist");
        }

        List<PlaylistSong> currentSongs =
                playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(playlistId);

        List<Long> currentSongIds = currentSongs.stream()
                .map(ps -> ps.getSong().getId())
                .toList();

        if (orderedSongIds.size() != currentSongIds.size()
                || !orderedSongIds.containsAll(currentSongIds)) {
            throw new BadRequestException(
                    "Reorder list must contain exactly the same song IDs "
                            + "as the playlist. Expected: " + currentSongIds
                            + ", Got: " + orderedSongIds);
        }

        Map<Long, PlaylistSong> songMap = currentSongs.stream()
                .collect(Collectors.toMap(
                        ps -> ps.getSong().getId(),
                        ps -> ps
                ));

        for (int i = 0; i < orderedSongIds.size(); i++) {
            PlaylistSong ps = songMap.get(orderedSongIds.get(i));
            ps.setPosition(i + 1); // 1-based positioning
        }

        playlistSongRepository.saveAll(currentSongs);

        log.debug("Reordered {} songs in playlist {}",
                orderedSongIds.size(), playlistId);
    }

    // -------------------------
    // FOLLOW PLAYLIST
    // -------------------------

    @Transactional
    public void followPlaylist(Long playlistId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Playlist", "id", playlistId));

        if (!playlist.isPublic()) {
            throw new BadRequestException("Cannot follow a private playlist");
        }

        User currentUser = securityUtils.getCurrentUser();

        if (playlist.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot follow your own playlist");
        }

        if (playlistFollowRepository.existsByUser_IdAndPlaylist_Id(
                currentUser.getId(), playlistId)) {
            log.debug("User {} already follows playlist {}", currentUser.getId(), playlistId);
            return;
        }

        PlaylistFollow follow = new PlaylistFollow();
        follow.setUser(currentUser);
        follow.setPlaylist(playlist);

        playlistFollowRepository.save(follow);

        log.debug("User {} followed playlist {}", currentUser.getId(), playlistId);
    }

    // -------------------------
    // UNFOLLOW PLAYLIST
    // -------------------------

    @Transactional
    public void unfollowPlaylist(Long playlistId) {

        User currentUser = securityUtils.getCurrentUser();

        if (!playlistFollowRepository.existsByUser_IdAndPlaylist_Id(
                currentUser.getId(), playlistId)) {
            throw new ResourceNotFoundException("Follow", "playlistId", playlistId);
        }

        playlistFollowRepository.deleteByUser_IdAndPlaylist_Id(
                currentUser.getId(), playlistId);

        log.debug("User {} unfollowed playlist {}", currentUser.getId(), playlistId);
    }

    // -------------------------
    // MAPPER
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