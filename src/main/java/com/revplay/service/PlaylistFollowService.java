package com.revplay.service;

import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Playlist;
import com.revplay.model.PlaylistFollow;
import com.revplay.model.User;
import com.revplay.repository.PlaylistFollowRepository;
import com.revplay.repository.PlaylistRepository;
import com.revplay.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistFollowService {

    private static final Logger log =
            LoggerFactory.getLogger(PlaylistFollowService.class);

    private final PlaylistFollowRepository playlistFollowRepository;
    private final PlaylistRepository playlistRepository;
    private final SecurityUtils securityUtils;

    public PlaylistFollowService(PlaylistFollowRepository playlistFollowRepository,
                                 PlaylistRepository playlistRepository,
                                 SecurityUtils securityUtils) {
        this.playlistFollowRepository = playlistFollowRepository;
        this.playlistRepository = playlistRepository;
        this.securityUtils = securityUtils;
    }

    // -------------------------
    // FOLLOW PLAYLIST
    // -------------------------

    @Transactional
    public void followPlaylist(Long playlistId) {

        User currentUser = securityUtils.getCurrentUser();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Playlist", "id", playlistId));

        // Idempotent — silently skip if already following
        if (playlistFollowRepository.existsByUser_IdAndPlaylist_Id(
                currentUser.getId(), playlistId)) {
            log.debug("User {} already follows playlist {} — no-op",
                    currentUser.getId(), playlistId);
            return;
        }

        PlaylistFollow follow = new PlaylistFollow();
        follow.setUser(currentUser);
        follow.setPlaylist(playlist);

        playlistFollowRepository.save(follow);

        log.debug("User {} followed playlist {}",
                currentUser.getId(), playlistId);
    }

    // -------------------------
    // UNFOLLOW PLAYLIST
    // -------------------------

    @Transactional
    public void unfollowPlaylist(Long playlistId) {

        User currentUser = securityUtils.getCurrentUser();

        // Verify the relationship exists before deleting
        playlistFollowRepository
                .findByUser_IdAndPlaylist_Id(currentUser.getId(), playlistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "PlaylistFollow",
                                "userId/playlistId",
                                currentUser.getId() + "/" + playlistId));

        playlistFollowRepository.deleteByUser_IdAndPlaylist_Id(
                currentUser.getId(), playlistId);

        log.debug("User {} unfollowed playlist {}",
                currentUser.getId(), playlistId);
    }

    // -------------------------
    // CHECK IF FOLLOWING
    // -------------------------

    @Transactional(readOnly = true)
    public boolean isFollowing(Long playlistId) {

        User currentUser = securityUtils.getCurrentUser();

        return playlistFollowRepository.existsByUser_IdAndPlaylist_Id(
                currentUser.getId(), playlistId);
    }
}