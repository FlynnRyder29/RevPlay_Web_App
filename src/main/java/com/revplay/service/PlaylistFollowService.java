package com.revplay.service;

import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Playlist;
import com.revplay.model.PlaylistFollow;
import com.revplay.model.User;
import com.revplay.repository.PlaylistFollowRepository;
import com.revplay.repository.PlaylistRepository;
import com.revplay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistFollowService {

    private static final Logger log =
            LoggerFactory.getLogger(PlaylistFollowService.class);

    private final PlaylistFollowRepository followRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    public PlaylistFollowService(PlaylistFollowRepository followRepository,
                                 PlaylistRepository playlistRepository,
                                 UserRepository userRepository) {
        this.followRepository = followRepository;
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
    }

    // -------------------------
    // Helper: Current User
    // -------------------------

    private User getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "username",
                                username
                        ));
    }

    // -------------------------
    // FOLLOW PLAYLIST
    // -------------------------

    @Transactional
    public void followPlaylist(Long playlistId) {

        User currentUser = getCurrentUser();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Playlist",
                                "id",
                                playlistId
                        ));

        if (followRepository.existsByUser_IdAndPlaylist_Id(
                currentUser.getId(),
                playlistId)) {

            log.debug("User {} already follows playlist {}",
                    currentUser.getId(), playlistId);
            return;
        }

        PlaylistFollow follow = new PlaylistFollow();
        follow.setUser(currentUser);
        follow.setPlaylist(playlist);

        followRepository.save(follow);

        log.debug("User {} followed playlist {}",
                currentUser.getId(), playlistId);
    }

    // -------------------------
    // UNFOLLOW PLAYLIST
    // -------------------------

    @Transactional
    public void unfollowPlaylist(Long playlistId) {

        User currentUser = getCurrentUser();

        if (!followRepository.existsByUser_IdAndPlaylist_Id(
                currentUser.getId(),
                playlistId)) {

            throw new ResourceNotFoundException(
                    "PlaylistFollow",
                    "playlistId",
                    playlistId
            );
        }

        followRepository.deleteByUser_IdAndPlaylist_Id(
                currentUser.getId(),
                playlistId
        );

        log.debug("User {} unfollowed playlist {}",
                currentUser.getId(), playlistId);
    }

    // -------------------------
    // GET MY FOLLOWED PLAYLISTS
    // -------------------------

    public List<PlaylistFollow> getMyFollowedPlaylists() {

        User currentUser = getCurrentUser();

        log.debug("Fetching followed playlists for user {}",
                currentUser.getId());

        return followRepository.findByUser_Id(currentUser.getId());
    }
}