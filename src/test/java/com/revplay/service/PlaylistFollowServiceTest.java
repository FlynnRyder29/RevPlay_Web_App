package com.revplay.service;

import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Playlist;
import com.revplay.model.PlaylistFollow;
import com.revplay.model.Role;
import com.revplay.model.User;
import com.revplay.repository.PlaylistFollowRepository;
import com.revplay.repository.PlaylistRepository;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistFollowServiceTest {

    @Mock private PlaylistFollowRepository playlistFollowRepository;
    @Mock private PlaylistRepository       playlistRepository;
    @Mock private SecurityUtils            securityUtils;

    @InjectMocks
    private PlaylistFollowService playlistFollowService;

    private User     currentUser;
    private User     otherUser;
    private Playlist publicPlaylist;
    private Playlist privatePlaylist;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L).username("alice").email("alice@mail.com")
                .passwordHash("hash").role(Role.LISTENER).build();

        otherUser = User.builder()
                .id(2L).username("bob").email("bob@mail.com")
                .passwordHash("hash").role(Role.LISTENER).build();

        publicPlaylist = new Playlist();
        publicPlaylist.setId(10L);
        publicPlaylist.setName("Bob's Public Playlist");
        publicPlaylist.setPublicPlaylist(true);
        publicPlaylist.setUser(otherUser);           // owned by someone else
        publicPlaylist.setCreatedAt(LocalDateTime.now());
        publicPlaylist.setUpdatedAt(LocalDateTime.now());

        privatePlaylist = new Playlist();
        privatePlaylist.setId(11L);
        privatePlaylist.setName("Bob's Private Playlist");
        privatePlaylist.setPublicPlaylist(false);
        privatePlaylist.setUser(otherUser);
        privatePlaylist.setCreatedAt(LocalDateTime.now());
        privatePlaylist.setUpdatedAt(LocalDateTime.now());

        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    // ── followPlaylist ────────────────────────────────────────────

    @Test
    @DisplayName("followPlaylist - public playlist by other user - saved")
    void followPlaylist_publicPlaylistByOtherUser_saved() {
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(false);

        playlistFollowService.followPlaylist(10L);

        verify(playlistFollowRepository).save(argThat(f ->
                f.getUser().getId().equals(1L)
                        && f.getPlaylist().getId().equals(10L)
        ));
    }

    @Test
    @DisplayName("followPlaylist - private playlist - throws BadRequestException")
    void followPlaylist_privatePlaylist_throwsBadRequest() {
        when(playlistRepository.findById(11L)).thenReturn(Optional.of(privatePlaylist));

        assertThrows(BadRequestException.class,
                () -> playlistFollowService.followPlaylist(11L));

        verify(playlistFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("followPlaylist - own public playlist - throws BadRequestException")
    void followPlaylist_ownPlaylist_throwsBadRequest() {
        publicPlaylist.setUser(currentUser); // alice tries to follow her own playlist

        when(playlistRepository.findById(10L)).thenReturn(Optional.of(publicPlaylist));

        assertThrows(BadRequestException.class,
                () -> playlistFollowService.followPlaylist(10L));

        verify(playlistFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("followPlaylist - already following - not saved again")
    void followPlaylist_alreadyFollowing_noOp() {
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(true);

        playlistFollowService.followPlaylist(10L);

        verify(playlistFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("followPlaylist - playlist not found - throws ResourceNotFoundException")
    void followPlaylist_notFound_throwsResourceNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playlistFollowService.followPlaylist(99L));

        verify(playlistFollowRepository, never()).save(any());
    }

    // ── unfollowPlaylist ──────────────────────────────────────────

    @Test
    @DisplayName("unfollowPlaylist - following - deletes record")
    void unfollowPlaylist_following_deletesRecord() {
        PlaylistFollow follow = new PlaylistFollow();
        follow.setId(1L);
        follow.setUser(currentUser);
        follow.setPlaylist(publicPlaylist);

        when(playlistFollowRepository.findByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(Optional.of(follow));

        playlistFollowService.unfollowPlaylist(10L);

        verify(playlistFollowRepository).deleteByUser_IdAndPlaylist_Id(1L, 10L);
    }

    @Test
    @DisplayName("unfollowPlaylist - not following - throws ResourceNotFoundException")
    void unfollowPlaylist_notFollowing_throwsResourceNotFoundException() {
        when(playlistFollowRepository.findByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playlistFollowService.unfollowPlaylist(10L));

        verify(playlistFollowRepository, never())
                .deleteByUser_IdAndPlaylist_Id(anyLong(), anyLong());
    }

    @Test
    @DisplayName("unfollowPlaylist - deletes with exact userId and playlistId")
    void unfollowPlaylist_deletesWithCorrectIds() {
        PlaylistFollow follow = new PlaylistFollow();
        follow.setUser(currentUser);
        follow.setPlaylist(publicPlaylist);

        when(playlistFollowRepository.findByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(Optional.of(follow));

        playlistFollowService.unfollowPlaylist(10L);

        verify(playlistFollowRepository).deleteByUser_IdAndPlaylist_Id(1L, 10L);
        verify(playlistFollowRepository, never()).deleteByUser_IdAndPlaylist_Id(2L, 10L);
    }

    // ── isFollowing ───────────────────────────────────────────────

    @Test
    @DisplayName("isFollowing - following - returns true")
    void isFollowing_following_returnsTrue() {
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(true);

        assertTrue(playlistFollowService.isFollowing(10L));
    }

    @Test
    @DisplayName("isFollowing - not following - returns false")
    void isFollowing_notFollowing_returnsFalse() {
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(false);

        assertFalse(playlistFollowService.isFollowing(10L));
    }

    @Test
    @DisplayName("isFollowing - checks for current user only")
    void isFollowing_checksCurrentUserOnly() {
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(1L, 10L))
                .thenReturn(true);

        playlistFollowService.isFollowing(10L);

        verify(playlistFollowRepository).existsByUser_IdAndPlaylist_Id(1L, 10L);
        verify(playlistFollowRepository, never())
                .existsByUser_IdAndPlaylist_Id(eq(2L), anyLong());
    }
}