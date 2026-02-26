package com.revplay.revplay_p2.service;

import com.revplay.revplay_p2.model.Playlist;
import com.revplay.revplay_p2.model.PlaylistFollow;
import com.revplay.revplay_p2.model.PlaylistSong;
import com.revplay.revplay_p2.repository.PlaylistFollowRepository;
import com.revplay.revplay_p2.repository.PlaylistRepository;
import com.revplay.revplay_p2.repository.PlaylistSongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistSongRepository playlistSongRepository;

    @Mock
    private PlaylistFollowRepository playlistFollowRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private Playlist testPlaylist;

    @BeforeEach
    void setUp() {
        testPlaylist = new Playlist();
        testPlaylist.setId(1L);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setDescription("Test Description");
        testPlaylist.setPublic(true);
        testPlaylist.setUserId(1L);
    }

    @Test
    void createPlaylist_ShouldSaveAndReturnPlaylist() {
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        Playlist result = playlistService.createPlaylist(testPlaylist);

        assertNotNull(result);
        assertEquals("Test Playlist", result.getName());
        verify(playlistRepository, times(1)).save(testPlaylist);
    }

    @Test
    void getMyPlaylists_ShouldReturnList() {
        when(playlistRepository.findByUserId(1L)).thenReturn(List.of(testPlaylist));

        List<Playlist> result = playlistService.getMyPlaylists(1L);

        assertEquals(1, result.size());
        assertEquals(testPlaylist, result.get(0));
    }

    @Test
    void getPlaylistById_WhenExists_ShouldReturnPlaylist() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        Playlist result = playlistService.getPlaylistById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPlaylistById_WhenNotExists_ShouldThrowException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            playlistService.getPlaylistById(99L);
        });

        assertTrue(exception.getMessage().contains("Playlist not found"));
    }

    @Test
    void updatePlaylist_ShouldUpdateFieldsAndSave() {
        Playlist existing = new Playlist();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setDescription("Old Desc");
        existing.setPublic(false);
        existing.setUserId(1L);

        Playlist updated = new Playlist();
        updated.setName("New Name");
        updated.setDescription("New Desc");
        updated.setPublic(true);
        updated.setUserId(1L);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Playlist result = playlistService.updatePlaylist(1L, updated);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertTrue(result.isPublic());
        assertNotNull(result.getUpdatedAt());
        verify(playlistRepository, times(1)).save(existing);
    }

    @Test
    void deletePlaylist_ShouldDeleteSongsAndFollowsAndPlaylist() {
        doNothing().when(playlistSongRepository).deleteByPlaylistId(1L);
        doNothing().when(playlistFollowRepository).deleteByPlaylistId(1L);
        doNothing().when(playlistRepository).deleteById(1L);

        playlistService.deletePlaylist(1L);

        verify(playlistSongRepository, times(1)).deleteByPlaylistId(1L);
        verify(playlistFollowRepository, times(1)).deleteByPlaylistId(1L);
        verify(playlistRepository, times(1)).deleteById(1L);
    }

    @Test
    void getPublicPlaylists_ShouldReturnList() {
        when(playlistRepository.findByIsPublicTrue()).thenReturn(List.of(testPlaylist));

        List<Playlist> result = playlistService.getPublicPlaylists();

        assertEquals(1, result.size());
    }

    @Test
    void addSongToPlaylist_ShouldCreatePlaylistSongWithNextPosition() {
        when(playlistSongRepository.countByPlaylistId(1L)).thenReturn(3);
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenAnswer(invocation -> invocation.getArgument(0));

        playlistService.addSongToPlaylist(1L, 101L);

        verify(playlistSongRepository, times(1)).save(argThat(ps ->
                ps.getPlaylistId().equals(1L) &&
                        ps.getSongId().equals(101L) &&
                        ps.getPosition().equals(4) &&
                        ps.getAddedAt() != null
        ));
    }

    @Test
    void removeSongFromPlaylist_ShouldCallDeleteMethod() {
        doNothing().when(playlistSongRepository).deleteByPlaylistIdAndSongId(1L, 101L);

        playlistService.removeSongFromPlaylist(1L, 101L);

        verify(playlistSongRepository, times(1)).deleteByPlaylistIdAndSongId(1L, 101L);
    }

    @Test
    void reorderSongs_ShouldDeleteAllAndInsertInOrder() {
        doNothing().when(playlistSongRepository).deleteByPlaylistId(1L);
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Long> newOrder = List.of(102L, 101L, 103L);

        playlistService.reorderSongs(1L, newOrder);

        verify(playlistSongRepository, times(1)).deleteByPlaylistId(1L);
        verify(playlistSongRepository, times(3)).save(any(PlaylistSong.class));
        // Optionally verify positions
        verify(playlistSongRepository).save(argThat(ps -> ps.getPosition().equals(1) && ps.getSongId().equals(102L)));
        verify(playlistSongRepository).save(argThat(ps -> ps.getPosition().equals(2) && ps.getSongId().equals(101L)));
        verify(playlistSongRepository).save(argThat(ps -> ps.getPosition().equals(3) && ps.getSongId().equals(103L)));
    }

    @Test
    void followPlaylist_WhenNotAlreadyFollowing_ShouldSaveFollow() {
        when(playlistFollowRepository.existsByUserIdAndPlaylistId(1L, 1L)).thenReturn(false);
        when(playlistFollowRepository.save(any(PlaylistFollow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        playlistService.followPlaylist(1L, 1L);

        verify(playlistFollowRepository, times(1)).save(argThat(follow ->
                follow.getUserId().equals(1L) &&
                        follow.getPlaylistId().equals(1L) &&
                        follow.getFollowedAt() != null
        ));
    }

    @Test
    void followPlaylist_WhenAlreadyFollowing_ShouldNotSave() {
        when(playlistFollowRepository.existsByUserIdAndPlaylistId(1L, 1L)).thenReturn(true);

        playlistService.followPlaylist(1L, 1L);

        verify(playlistFollowRepository, never()).save(any());
    }

    @Test
    void unfollowPlaylist_ShouldDelete() {
        doNothing().when(playlistFollowRepository).deleteByUserIdAndPlaylistId(1L, 1L);

        playlistService.unfollowPlaylist(1L, 1L);

        verify(playlistFollowRepository, times(1)).deleteByUserIdAndPlaylistId(1L, 1L);
    }

    @Test
    void getFollowers_ShouldReturnList() {
        PlaylistFollow follow = new PlaylistFollow();
        follow.setUserId(2L);
        follow.setPlaylistId(1L);
        when(playlistFollowRepository.findByPlaylistId(1L)).thenReturn(List.of(follow));

        List<PlaylistFollow> result = playlistService.getFollowers(1L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getUserId());
    }
}