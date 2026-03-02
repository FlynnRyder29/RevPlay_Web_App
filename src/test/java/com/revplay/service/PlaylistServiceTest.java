package com.revplay.service;

import com.revplay.dto.PlaylistDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.model.Playlist;
import com.revplay.model.PlaylistFollow;
import com.revplay.model.PlaylistSong;
import com.revplay.model.Role;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.PlaylistFollowRepository;
import com.revplay.repository.PlaylistRepository;
import com.revplay.repository.PlaylistSongRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlaylistService.
 *
 * PlaylistService has its own private getCurrentUser() that reads from
 * SecurityContextHolder directly (no SecurityUtils injection).
 * We configure a real SecurityContext in @BeforeEach and clear it in
 * @AfterEach to avoid state leaking between tests.
 *
 * NOTE: Do NOT run these tests in parallel (maven-surefire parallel=methods)
 * because SecurityContextHolder is static global state. Sequential execution
 * (the Maven default) is safe.
 */
@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock private PlaylistRepository       playlistRepository;
    @Mock private PlaylistSongRepository   playlistSongRepository;
    @Mock private PlaylistFollowRepository playlistFollowRepository;
    @Mock private SongRepository           songRepository;
    @Mock private UserRepository           userRepository;

    @InjectMocks
    private PlaylistService playlistService;

    // ── Test fixtures ─────────────────────────────────────────────

    private User     owner;
    private User     otherUser;
    private Playlist publicPlaylist;
    private Playlist privatePlaylist;
    private Song     testSong;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L).username("alice")
                .email("alice@revplay.com")
                .passwordHash("hash")
                .role(Role.LISTENER)
                .build();

        otherUser = User.builder()
                .id(2L).username("bob")
                .email("bob@revplay.com")
                .passwordHash("hash")
                .role(Role.LISTENER)
                .build();

        publicPlaylist = new Playlist();
        publicPlaylist.setId(1L);
        publicPlaylist.setName("My Public Playlist");
        publicPlaylist.setDescription("A description");
        publicPlaylist.setPublic(true);
        publicPlaylist.setUser(owner);
        publicPlaylist.setCreatedAt(LocalDateTime.of(2024, 6, 1, 10, 0));
        publicPlaylist.setUpdatedAt(LocalDateTime.of(2024, 6, 1, 10, 0));

        privatePlaylist = new Playlist();
        privatePlaylist.setId(2L);
        privatePlaylist.setName("My Private Playlist");
        privatePlaylist.setPublic(false);
        privatePlaylist.setUser(owner);
        privatePlaylist.setCreatedAt(LocalDateTime.of(2024, 6, 1, 10, 0));
        privatePlaylist.setUpdatedAt(LocalDateTime.of(2024, 6, 1, 10, 0));

        testSong = Song.builder()
                .id(10L).title("Test Song").duration(200)
                .visibility(Song.Visibility.PUBLIC)
                .build();

        // ✅ FIX: Use lenient() for shared stubs that not every test needs
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("alice");

        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        lenient().when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(owner));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── createPlaylist ────────────────────────────────────────────

    @Test
    @DisplayName("createPlaylist - valid dto - returns saved PlaylistDTO")
    void createPlaylist_validDto_returnsDTO() {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setName("New Playlist");
        dto.setDescription("desc");
        dto.setPublic(true);

        when(playlistRepository.save(any(Playlist.class))).thenReturn(publicPlaylist);

        PlaylistDTO result = playlistService.createPlaylist(dto);

        assertNotNull(result);
        assertEquals("My Public Playlist", result.getName());
        assertEquals(owner.getId(), result.getUserId());
    }

    @Test
    @DisplayName("createPlaylist - sets current user as owner on saved entity")
    void createPlaylist_setsCurrentUserAsOwner() {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setName("Playlist");
        dto.setPublic(false);

        when(playlistRepository.save(any(Playlist.class))).thenReturn(privatePlaylist);

        playlistService.createPlaylist(dto);

        verify(playlistRepository).save(argThat(p ->
                p.getUser().getId().equals(owner.getId())
        ));
    }

    @Test
    @DisplayName("createPlaylist - repository save called exactly once")
    void createPlaylist_savesExactlyOnce() {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setName("Playlist");

        when(playlistRepository.save(any(Playlist.class))).thenReturn(publicPlaylist);

        playlistService.createPlaylist(dto);

        verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    // ── getMyPlaylists ────────────────────────────────────────────

    @Test
    @DisplayName("getMyPlaylists - returns all playlists owned by current user")
    void getMyPlaylists_returnsCurrentUserPlaylists() {
        when(playlistRepository.findByUser_Id(owner.getId()))
                .thenReturn(List.of(publicPlaylist, privatePlaylist));

        List<PlaylistDTO> result = playlistService.getMyPlaylists();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto ->
                dto.getUserId().equals(owner.getId())
        ));
    }

    @Test
    @DisplayName("getMyPlaylists - no playlists - returns empty list")
    void getMyPlaylists_noPlaylists_returnsEmptyList() {
        when(playlistRepository.findByUser_Id(owner.getId()))
                .thenReturn(List.of());

        List<PlaylistDTO> result = playlistService.getMyPlaylists();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getMyPlaylists - queries by current user id, not another user")
    void getMyPlaylists_queriesWithCurrentUserId() {
        when(playlistRepository.findByUser_Id(owner.getId()))
                .thenReturn(List.of());

        playlistService.getMyPlaylists();

        verify(playlistRepository).findByUser_Id(1L);
        verify(playlistRepository, never()).findByUser_Id(2L);
    }

    // ── getPublicPlaylists ────────────────────────────────────────

    @Test
    @DisplayName("getPublicPlaylists - returns only public playlists")
    void getPublicPlaylists_returnsPublicPlaylists() {
        when(playlistRepository.findByIsPublicTrue())
                .thenReturn(List.of(publicPlaylist));

        List<PlaylistDTO> result = playlistService.getPublicPlaylists();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isPublic());
    }

    @Test
    @DisplayName("getPublicPlaylists - no public playlists - returns empty list")
    void getPublicPlaylists_noPublicPlaylists_returnsEmptyList() {
        when(playlistRepository.findByIsPublicTrue()).thenReturn(List.of());

        List<PlaylistDTO> result = playlistService.getPublicPlaylists();

        assertTrue(result.isEmpty());
    }

    // ── getPlaylistById ───────────────────────────────────────────

    @Test
    @DisplayName("getPlaylistById - public playlist - any authenticated user can access")
    void getPlaylistById_publicPlaylist_returnsDTO() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        PlaylistDTO result = playlistService.getPlaylistById(1L);

        assertEquals(1L, result.getId());
        assertTrue(result.isPublic());
    }

    @Test
    @DisplayName("getPlaylistById - private playlist accessed by owner - returns DTO")
    void getPlaylistById_privatePlaylistByOwner_returnsDTO() {
        // privatePlaylist.user == owner, current user is also owner
        when(playlistRepository.findById(2L)).thenReturn(Optional.of(privatePlaylist));

        PlaylistDTO result = playlistService.getPlaylistById(2L);

        assertEquals(2L, result.getId());
        assertFalse(result.isPublic());
    }

    @Test
    @DisplayName("getPlaylistById - private playlist accessed by non-owner - throws UnauthorizedAccessException")
    void getPlaylistById_privatePlaylistNonOwner_throwsUnauthorized() {
        privatePlaylist.setUser(otherUser); // owned by bob, current user is alice
        when(playlistRepository.findById(2L)).thenReturn(Optional.of(privatePlaylist));

        assertThrows(UnauthorizedAccessException.class,
                () -> playlistService.getPlaylistById(2L));
    }

    @Test
    @DisplayName("getPlaylistById - not found - throws ResourceNotFoundException")
    void getPlaylistById_notFound_throwsResourceNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.getPlaylistById(99L));
    }

    // ── updatePlaylist ────────────────────────────────────────────

    @Test
    @DisplayName("updatePlaylist - owner updates - returns updated DTO")
    void updatePlaylist_owner_returnsUpdatedDTO() {
        Playlist updated = new Playlist();
        updated.setId(1L);
        updated.setName("Renamed Playlist");
        updated.setDescription("new desc");
        updated.setPublic(false);
        updated.setUser(owner);
        updated.setCreatedAt(LocalDateTime.now());
        updated.setUpdatedAt(LocalDateTime.now());

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(updated);

        PlaylistDTO dto = new PlaylistDTO();
        dto.setName("Renamed Playlist");
        dto.setDescription("new desc");
        dto.setPublic(false);

        PlaylistDTO result = playlistService.updatePlaylist(1L, dto);

        assertEquals("Renamed Playlist", result.getName());
        assertFalse(result.isPublic());
        verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    @DisplayName("updatePlaylist - non-owner - throws UnauthorizedAccessException")
    void updatePlaylist_nonOwner_throwsUnauthorized() {
        publicPlaylist.setUser(otherUser);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        PlaylistDTO dto = new PlaylistDTO();
        dto.setName("Hacked Name");

        assertThrows(UnauthorizedAccessException.class,
                () -> playlistService.updatePlaylist(1L, dto));

        verify(playlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePlaylist - playlist not found - throws ResourceNotFoundException")
    void updatePlaylist_notFound_throwsResourceNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.updatePlaylist(99L, new PlaylistDTO()));
    }

    // ── deletePlaylist ────────────────────────────────────────────

    @Test
    @DisplayName("deletePlaylist - owner - repository delete is called")
    void deletePlaylist_owner_deletesSuccessfully() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        playlistService.deletePlaylist(1L);

        verify(playlistRepository, times(1)).delete(publicPlaylist);
    }

    @Test
    @DisplayName("deletePlaylist - non-owner - throws UnauthorizedAccessException")
    void deletePlaylist_nonOwner_throwsUnauthorized() {
        publicPlaylist.setUser(otherUser);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        assertThrows(UnauthorizedAccessException.class,
                () -> playlistService.deletePlaylist(1L));

        verify(playlistRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deletePlaylist - not found - throws ResourceNotFoundException")
    void deletePlaylist_notFound_throwsResourceNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.deletePlaylist(99L));
    }

    // ── addSongToPlaylist ─────────────────────────────────────────

    @Test
    @DisplayName("addSongToPlaylist - new song - saved at end of playlist")
    void addSongToPlaylist_newSong_savedAtEndPosition() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 10L)).thenReturn(false);
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));
        when(playlistSongRepository.findByPlaylist_IdOrderByPosition(1L))
                .thenReturn(List.of()); // empty → position 0

        playlistService.addSongToPlaylist(1L, 10L);

        verify(playlistSongRepository).save(argThat(ps ->
                ps.getPosition() == 0
                        && ps.getSong().getId().equals(10L)
                        && ps.getPlaylist().getId().equals(1L)
        ));
    }

    @Test
    @DisplayName("addSongToPlaylist - playlist has 2 songs already - new song gets position 2")
    void addSongToPlaylist_appendsToEnd() {
        PlaylistSong ps1 = new PlaylistSong(); ps1.setPosition(0);
        PlaylistSong ps2 = new PlaylistSong(); ps2.setPosition(1);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 10L)).thenReturn(false);
        when(songRepository.findById(10L)).thenReturn(Optional.of(testSong));
        when(playlistSongRepository.findByPlaylist_IdOrderByPosition(1L))
                .thenReturn(List.of(ps1, ps2));

        playlistService.addSongToPlaylist(1L, 10L);

        verify(playlistSongRepository).save(argThat(ps -> ps.getPosition() == 2));
    }

    @Test
    @DisplayName("addSongToPlaylist - duplicate song - not saved again")
    void addSongToPlaylist_duplicateSong_doesNotSave() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 10L)).thenReturn(true);

        playlistService.addSongToPlaylist(1L, 10L);

        verify(playlistSongRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSongToPlaylist - non-owner - throws UnauthorizedAccessException")
    void addSongToPlaylist_nonOwner_throwsUnauthorized() {
        publicPlaylist.setUser(otherUser);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        assertThrows(UnauthorizedAccessException.class,
                () -> playlistService.addSongToPlaylist(1L, 10L));

        verify(playlistSongRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSongToPlaylist - song not found - throws ResourceNotFoundException")
    void addSongToPlaylist_songNotFound_throwsResourceNotFoundException() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 99L)).thenReturn(false);
        when(songRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.addSongToPlaylist(1L, 99L));
    }

    // ── removeSongFromPlaylist ────────────────────────────────────

    @Test
    @DisplayName("removeSongFromPlaylist - song exists - deleted from repository")
    void removeSongFromPlaylist_songExists_deletedSuccessfully() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 10L)).thenReturn(true);

        playlistService.removeSongFromPlaylist(1L, 10L);

        verify(playlistSongRepository).deleteByPlaylist_IdAndSong_Id(1L, 10L);
    }

    @Test
    @DisplayName("removeSongFromPlaylist - song not in playlist - throws ResourceNotFoundException")
    void removeSongFromPlaylist_songNotInPlaylist_throwsResourceNotFoundException() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.removeSongFromPlaylist(1L, 99L));

        verify(playlistSongRepository, never()).deleteByPlaylist_IdAndSong_Id(anyLong(), anyLong());
    }

    @Test
    @DisplayName("removeSongFromPlaylist - non-owner - throws UnauthorizedAccessException")
    void removeSongFromPlaylist_nonOwner_throwsUnauthorized() {
        publicPlaylist.setUser(otherUser);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        assertThrows(UnauthorizedAccessException.class,
                () -> playlistService.removeSongFromPlaylist(1L, 10L));

        verify(playlistSongRepository, never()).deleteByPlaylist_IdAndSong_Id(anyLong(), anyLong());
    }

    // ── reorderSongs ─────────────────────────────────────────────

    @Test
    @DisplayName("reorderSongs - valid order - positions updated correctly")
    void reorderSongs_validOrder_updatesPositions() {
        Song song1 = Song.builder().id(1L).title("S1").duration(100).build();
        Song song2 = Song.builder().id(2L).title("S2").duration(100).build();

        PlaylistSong ps1 = new PlaylistSong(); ps1.setId(1L); ps1.setSong(song1); ps1.setPosition(0);
        PlaylistSong ps2 = new PlaylistSong(); ps2.setId(2L); ps2.setSong(song2); ps2.setPosition(1);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.findByPlaylist_IdOrderByPosition(1L))
                .thenReturn(List.of(ps1, ps2));

        // Reverse: song2 first, song1 second
        playlistService.reorderSongs(1L, List.of(2L, 1L));

        assertEquals(0, ps2.getPosition());
        assertEquals(1, ps1.getPosition());
        verify(playlistSongRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("reorderSongs - saveAll called with the mutated list")
    void reorderSongs_saveAllCalledWithMutatedList() {
        Song song1 = Song.builder().id(1L).title("S1").duration(100).build();
        PlaylistSong ps1 = new PlaylistSong(); ps1.setId(1L); ps1.setSong(song1); ps1.setPosition(0);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.findByPlaylist_IdOrderByPosition(1L))
                .thenReturn(List.of(ps1));

        playlistService.reorderSongs(1L, List.of(1L));

        verify(playlistSongRepository).saveAll(argThat(list -> {
            List<PlaylistSong> psList = (List<PlaylistSong>) list;
            return psList.size() == 1 && psList.get(0).getPosition() == 0;
        }));
    }

    @Test
    @DisplayName("reorderSongs - wrong list size - throws BadRequestException")
    void reorderSongs_wrongSize_throwsBadRequest() {
        Song song1 = Song.builder().id(1L).title("S1").duration(100).build();
        PlaylistSong ps1 = new PlaylistSong(); ps1.setId(1L); ps1.setSong(song1); ps1.setPosition(0);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.findByPlaylist_IdOrderByPosition(1L))
                .thenReturn(List.of(ps1)); // 1 song in playlist

        assertThrows(BadRequestException.class,
                () -> playlistService.reorderSongs(1L, List.of(1L, 2L))); // 2 ids sent
    }

    @Test
    @DisplayName("reorderSongs - unknown songId in list - throws BadRequestException")
    void reorderSongs_unknownSongId_throwsBadRequest() {
        Song song1 = Song.builder().id(1L).title("S1").duration(100).build();
        PlaylistSong ps1 = new PlaylistSong(); ps1.setId(1L); ps1.setSong(song1); ps1.setPosition(0);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistSongRepository.findByPlaylist_IdOrderByPosition(1L))
                .thenReturn(List.of(ps1));

        assertThrows(BadRequestException.class,
                () -> playlistService.reorderSongs(1L, List.of(99L))); // 99 not in playlist
    }

    @Test
    @DisplayName("reorderSongs - non-owner - throws UnauthorizedAccessException")
    void reorderSongs_nonOwner_throwsUnauthorized() {
        publicPlaylist.setUser(otherUser);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        assertThrows(UnauthorizedAccessException.class,
                () -> playlistService.reorderSongs(1L, List.of(1L)));
    }

    // ── followPlaylist ────────────────────────────────────────────

    @Test
    @DisplayName("followPlaylist - public playlist by other user - saved")
    void followPlaylist_validPublicPlaylist_saved() {
        publicPlaylist.setUser(otherUser); // owned by someone else
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(owner.getId(), 1L))
                .thenReturn(false);

        playlistService.followPlaylist(1L);

        verify(playlistFollowRepository).save(argThat(f ->
                f.getUser().getId().equals(owner.getId())
                        && f.getPlaylist().getId().equals(1L)
        ));
    }

    @Test
    @DisplayName("followPlaylist - private playlist - throws BadRequestException")
    void followPlaylist_privatePlaylist_throwsBadRequest() {
        when(playlistRepository.findById(2L)).thenReturn(Optional.of(privatePlaylist));

        assertThrows(BadRequestException.class,
                () -> playlistService.followPlaylist(2L));

        verify(playlistFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("followPlaylist - own playlist - throws BadRequestException")
    void followPlaylist_ownPlaylist_throwsBadRequest() {
        // publicPlaylist owned by owner (alice) — she tries to follow her own
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));

        assertThrows(BadRequestException.class,
                () -> playlistService.followPlaylist(1L));

        verify(playlistFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("followPlaylist - already following - not saved again")
    void followPlaylist_alreadyFollowing_doesNotSave() {
        publicPlaylist.setUser(otherUser);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(publicPlaylist));
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(owner.getId(), 1L))
                .thenReturn(true);

        playlistService.followPlaylist(1L);

        verify(playlistFollowRepository, never()).save(any());
    }

    @Test
    @DisplayName("followPlaylist - not found - throws ResourceNotFoundException")
    void followPlaylist_notFound_throwsResourceNotFoundException() {
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.followPlaylist(99L));
    }

    // ── unfollowPlaylist ──────────────────────────────────────────

    @Test
    @DisplayName("unfollowPlaylist - following - deletes follow record")
    void unfollowPlaylist_following_deletesRecord() {
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(owner.getId(), 1L))
                .thenReturn(true);

        playlistService.unfollowPlaylist(1L);

        verify(playlistFollowRepository)
                .deleteByUser_IdAndPlaylist_Id(owner.getId(), 1L);
    }

    @Test
    @DisplayName("unfollowPlaylist - not following - throws ResourceNotFoundException")
    void unfollowPlaylist_notFollowing_throwsResourceNotFoundException() {
        when(playlistFollowRepository.existsByUser_IdAndPlaylist_Id(owner.getId(), 1L))
                .thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.unfollowPlaylist(1L));

        verify(playlistFollowRepository, never())
                .deleteByUser_IdAndPlaylist_Id(anyLong(), anyLong());
    }
}