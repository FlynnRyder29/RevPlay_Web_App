package com.revplay.service;

import com.revplay.dto.PlaylistDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.model.*;
import com.revplay.repository.*;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    // ── All dependencies that PlaylistService injects ──
    @Mock private PlaylistRepository       playlistRepository;
    @Mock private PlaylistSongRepository   playlistSongRepository;
    @Mock private PlaylistFollowRepository playlistFollowRepository;
    @Mock private SongRepository           songRepository;
    @Mock private UserRepository           userRepository;
    @Mock private SecurityUtils            securityUtils;

    @InjectMocks
    private PlaylistService playlistService;

    // ── Test data ──
    private User     currentUser;
    private User     otherUser;
    private Playlist ownedPlaylist;
    private Playlist otherPublicPlaylist;
    private Playlist otherPrivatePlaylist;
    private Song     testSong;
    private Artist   testArtist;

    @BeforeEach
    void setUp() {
        // ── Users ──
        currentUser = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@revplay.com")
                .passwordHash("hash")
                .role(Role.LISTENER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("bob")
                .email("bob@revplay.com")
                .passwordHash("hash")
                .role(Role.LISTENER)
                .build();

        // ── Artist & Song ──
        testArtist = new Artist();
        testArtist.setId(1L);
        testArtist.setArtistName("Test Artist");

        testSong = Song.builder()
                .id(10L)
                .title("Test Song")
                .duration(210)
                .artist(testArtist)
                .visibility(Song.Visibility.PUBLIC)
                .build();

        // ── Playlists (NO setPlaylistSongs — field doesn't exist) ──
        ownedPlaylist = new Playlist();
        ownedPlaylist.setId(1L);
        ownedPlaylist.setName("My Playlist");
        ownedPlaylist.setDescription("My description");
        ownedPlaylist.setPublic(true);
        ownedPlaylist.setUser(currentUser);
        ownedPlaylist.setCreatedAt(LocalDateTime.now());
        ownedPlaylist.setUpdatedAt(LocalDateTime.now());

        otherPublicPlaylist = new Playlist();
        otherPublicPlaylist.setId(2L);
        otherPublicPlaylist.setName("Bob's Public");
        otherPublicPlaylist.setPublic(true);
        otherPublicPlaylist.setUser(otherUser);
        otherPublicPlaylist.setCreatedAt(LocalDateTime.now());
        otherPublicPlaylist.setUpdatedAt(LocalDateTime.now());

        otherPrivatePlaylist = new Playlist();
        otherPrivatePlaylist.setId(3L);
        otherPrivatePlaylist.setName("Bob's Private");
        otherPrivatePlaylist.setPublic(false);
        otherPrivatePlaylist.setUser(otherUser);
        otherPrivatePlaylist.setCreatedAt(LocalDateTime.now());
        otherPrivatePlaylist.setUpdatedAt(LocalDateTime.now());

        // ══════════════════════════════════════════════════════════
        // CRITICAL: Mock SecurityContextHolder for private getCurrentUser()
        //
        // Most PlaylistService methods call the PRIVATE getCurrentUser()
        // which reads SecurityContextHolder → userRepository.
        // Only reorderSongs() uses securityUtils.getCurrentUser().
        // ══════════════════════════════════════════════════════════

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        lenient().when(authentication.getName())
                .thenReturn("alice@revplay.com");
        lenient().when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock userRepository for private getCurrentUser()
        lenient().when(userRepository.findByEmailOrUsername(
                        "alice@revplay.com", "alice@revplay.com"))
                .thenReturn(Optional.of(currentUser));

        // Mock securityUtils for reorderSongs()
        lenient().when(securityUtils.getCurrentUser())
                .thenReturn(currentUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ══════════════════════════════════════════════════════════
    //  createPlaylist
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createPlaylist")
    class CreatePlaylist {

        @Test
        @DisplayName("valid input — playlist saved with current user as owner")
        void createPlaylist_validInput_savedWithCurrentUser() {
            PlaylistDTO dto = new PlaylistDTO();
            dto.setName("Road Trip");
            dto.setDescription("Highway tunes");
            dto.setPublic(true);

            when(playlistRepository.save(any(Playlist.class)))
                    .thenAnswer(invocation -> {
                        Playlist saved = invocation.getArgument(0);
                        saved.setId(99L);
                        saved.setCreatedAt(LocalDateTime.now());
                        saved.setUpdatedAt(LocalDateTime.now());
                        return saved;
                    });

            PlaylistDTO result = playlistService.createPlaylist(dto);

            assertNotNull(result);
            assertEquals("Road Trip", result.getName());

            verify(playlistRepository).save(argThat(p ->
                    p.getUser().getId().equals(1L)
                            && p.getName().equals("Road Trip")
                            && p.isPublic()
            ));
        }

        @Test
        @DisplayName("private playlist — isPublic set to false")
        void createPlaylist_privatePlaylist_isPublicFalse() {
            PlaylistDTO dto = new PlaylistDTO();
            dto.setName("Secret Jams");
            dto.setPublic(false);

            when(playlistRepository.save(any(Playlist.class)))
                    .thenAnswer(invocation -> {
                        Playlist saved = invocation.getArgument(0);
                        saved.setId(99L);
                        saved.setCreatedAt(LocalDateTime.now());
                        saved.setUpdatedAt(LocalDateTime.now());
                        return saved;
                    });

            playlistService.createPlaylist(dto);

            verify(playlistRepository).save(argThat(p -> !p.isPublic()));
        }

        @Test
        @DisplayName("repository save called exactly once")
        void createPlaylist_repositorySaveCalledOnce() {
            PlaylistDTO dto = new PlaylistDTO();
            dto.setName("Test");

            when(playlistRepository.save(any(Playlist.class)))
                    .thenAnswer(invocation -> {
                        Playlist saved = invocation.getArgument(0);
                        saved.setId(99L);
                        saved.setCreatedAt(LocalDateTime.now());
                        saved.setUpdatedAt(LocalDateTime.now());
                        return saved;
                    });

            playlistService.createPlaylist(dto);

            verify(playlistRepository, times(1)).save(any(Playlist.class));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  getMyPlaylists
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMyPlaylists")
    class GetMyPlaylists {

        @Test
        @DisplayName("returns playlists owned by current user")
        void getMyPlaylists_returnsOwnedPlaylists() {
            when(playlistRepository.findByUser_Id(1L))
                    .thenReturn(List.of(ownedPlaylist));

            List<PlaylistDTO> result = playlistService.getMyPlaylists();

            assertEquals(1, result.size());
            assertEquals("My Playlist", result.get(0).getName());
        }

        @Test
        @DisplayName("empty list when user has no playlists")
        void getMyPlaylists_noPlaylists_returnsEmpty() {
            when(playlistRepository.findByUser_Id(1L))
                    .thenReturn(List.of());

            List<PlaylistDTO> result = playlistService.getMyPlaylists();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("queries with current user ID only")
        void getMyPlaylists_queriesCurrentUserOnly() {
            when(playlistRepository.findByUser_Id(1L))
                    .thenReturn(List.of());

            playlistService.getMyPlaylists();

            verify(playlistRepository).findByUser_Id(1L);
            verify(playlistRepository, never()).findByUser_Id(2L);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  getPublicPlaylists
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPublicPlaylists")
    class GetPublicPlaylists {

        @Test
        @DisplayName("returns only public playlists")
        void getPublicPlaylists_returnsPublicOnly() {
            when(playlistRepository.findByIsPublicTrue())
                    .thenReturn(List.of(ownedPlaylist, otherPublicPlaylist));

            List<PlaylistDTO> result = playlistService.getPublicPlaylists();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("empty when no public playlists exist")
        void getPublicPlaylists_noneExist_returnsEmpty() {
            when(playlistRepository.findByIsPublicTrue())
                    .thenReturn(List.of());

            List<PlaylistDTO> result = playlistService.getPublicPlaylists();

            assertTrue(result.isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  getPlaylistById
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPlaylistById")
    class GetPlaylistById {

        @Test
        @DisplayName("own playlist — returns successfully")
        void getById_ownPlaylist_returnsDTO() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));

            PlaylistDTO result = playlistService.getPlaylistById(1L);

            assertNotNull(result);
            assertEquals("My Playlist", result.getName());
        }

        @Test
        @DisplayName("other user's public playlist — returns successfully")
        void getById_otherPublicPlaylist_returnsDTO() {
            when(playlistRepository.findById(2L))
                    .thenReturn(Optional.of(otherPublicPlaylist));

            PlaylistDTO result = playlistService.getPlaylistById(2L);

            assertNotNull(result);
            assertEquals("Bob's Public", result.getName());
        }

        @Test
        @DisplayName("other user's private playlist — throws UnauthorizedAccessException")
        void getById_otherPrivatePlaylist_throwsUnauthorized() {
            when(playlistRepository.findById(3L))
                    .thenReturn(Optional.of(otherPrivatePlaylist));

            assertThrows(UnauthorizedAccessException.class,
                    () -> playlistService.getPlaylistById(3L));
        }

        @Test
        @DisplayName("playlist not found — throws ResourceNotFoundException")
        void getById_notFound_throwsResourceNotFound() {
            when(playlistRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> playlistService.getPlaylistById(99L));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  updatePlaylist
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updatePlaylist")
    class UpdatePlaylist {

        @Test
        @DisplayName("own playlist — updates name and description")
        void update_ownPlaylist_updatesFields() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistRepository.save(any(Playlist.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            PlaylistDTO dto = new PlaylistDTO();
            dto.setName("Updated Name");
            dto.setDescription("Updated Desc");
            dto.setPublic(false);

            PlaylistDTO result = playlistService.updatePlaylist(1L, dto);

            assertEquals("Updated Name", result.getName());
            verify(playlistRepository).save(argThat(p ->
                    p.getName().equals("Updated Name")
                            && p.getDescription().equals("Updated Desc")
                            && !p.isPublic()
            ));
        }

        @Test
        @DisplayName("other user's playlist — throws UnauthorizedAccessException")
        void update_otherPlaylist_throwsUnauthorized() {
            when(playlistRepository.findById(2L))
                    .thenReturn(Optional.of(otherPublicPlaylist));

            PlaylistDTO dto = new PlaylistDTO();
            dto.setName("Hacked");

            assertThrows(UnauthorizedAccessException.class,
                    () -> playlistService.updatePlaylist(2L, dto));

            verify(playlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("playlist not found — throws ResourceNotFoundException")
        void update_notFound_throwsResourceNotFound() {
            when(playlistRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> playlistService.updatePlaylist(99L, new PlaylistDTO()));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  deletePlaylist
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deletePlaylist")
    class DeletePlaylist {

        @Test
        @DisplayName("own playlist — deleted successfully")
        void delete_ownPlaylist_deletesSuccessfully() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));

            playlistService.deletePlaylist(1L);

            verify(playlistRepository).delete(ownedPlaylist);
        }

        @Test
        @DisplayName("other user's playlist — throws UnauthorizedAccessException")
        void delete_otherPlaylist_throwsUnauthorized() {
            when(playlistRepository.findById(2L))
                    .thenReturn(Optional.of(otherPublicPlaylist));

            assertThrows(UnauthorizedAccessException.class,
                    () -> playlistService.deletePlaylist(2L));

            verify(playlistRepository, never()).delete(any());
        }

        @Test
        @DisplayName("playlist not found — throws ResourceNotFoundException")
        void delete_notFound_throwsResourceNotFound() {
            when(playlistRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> playlistService.deletePlaylist(99L));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  addSongToPlaylist
    //
    //  ACTUAL BEHAVIOR (from PlaylistService.java):
    //  - Duplicate → silent return (NOT exception)
    //  - Position = findByPlaylist_IdOrderByPosition().size()
    //  - Owner check before duplicate check
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addSongToPlaylist")
    class AddSongToPlaylist {

        @Test
        @DisplayName("valid song + own playlist — PlaylistSong saved with correct position")
        void addSong_validInput_savedWithPosition() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 10L))
                    .thenReturn(false);
            when(songRepository.findById(10L))
                    .thenReturn(Optional.of(testSong));

            // 3 songs already exist → new song gets position 3 (0-based)
            PlaylistSong ps1 = new PlaylistSong();
            PlaylistSong ps2 = new PlaylistSong();
            PlaylistSong ps3 = new PlaylistSong();
            when(playlistSongRepository.findByPlaylist_IdOrderByPosition(1L))
                    .thenReturn(List.of(ps1, ps2, ps3));

            playlistService.addSongToPlaylist(1L, 10L);

            verify(playlistSongRepository).save(argThat(ps ->
                    ps.getPlaylist().getId().equals(1L)
                            && ps.getSong().getId().equals(10L)
                            && ps.getPosition() == 3
            ));
        }

        @Test
        @DisplayName("song already in playlist — silently returns (no exception)")
        void addSong_alreadyInPlaylist_silentReturn() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 10L))
                    .thenReturn(true);

            // Should NOT throw — service silently returns
            assertDoesNotThrow(
                    () -> playlistService.addSongToPlaylist(1L, 10L));

            // save() should never be called
            verify(playlistSongRepository, never()).save(any());
        }

        @Test
        @DisplayName("song not found — throws ResourceNotFoundException")
        void addSong_songNotFound_throwsResourceNotFound() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 99L))
                    .thenReturn(false);
            when(songRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> playlistService.addSongToPlaylist(1L, 99L));
        }

        @Test
        @DisplayName("not owner — throws UnauthorizedAccessException")
        void addSong_notOwner_throwsUnauthorized() {
            when(playlistRepository.findById(2L))
                    .thenReturn(Optional.of(otherPublicPlaylist));

            assertThrows(UnauthorizedAccessException.class,
                    () -> playlistService.addSongToPlaylist(2L, 10L));

            verify(playlistSongRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  removeSongFromPlaylist
    //
    //  ACTUAL BEHAVIOR (from PlaylistService.java):
    //  - Uses existsByPlaylist_IdAndSong_Id() for check
    //  - Uses deleteByPlaylist_IdAndSong_Id() to remove
    //  - Does NOT use findByPlaylist_IdAndSong_Id()
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeSongFromPlaylist")
    class RemoveSongFromPlaylist {

        @Test
        @DisplayName("song in playlist — removed successfully via deleteBy")
        void removeSong_songInPlaylist_deleted() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 10L))
                    .thenReturn(true);

            playlistService.removeSongFromPlaylist(1L, 10L);

            verify(playlistSongRepository)
                    .deleteByPlaylist_IdAndSong_Id(1L, 10L);
        }

        @Test
        @DisplayName("song not in playlist — throws ResourceNotFoundException")
        void removeSong_songNotInPlaylist_throwsResourceNotFound() {
            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.existsByPlaylist_IdAndSong_Id(1L, 99L))
                    .thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> playlistService.removeSongFromPlaylist(1L, 99L));

            verify(playlistSongRepository, never())
                    .deleteByPlaylist_IdAndSong_Id(anyLong(), anyLong());
        }

        @Test
        @DisplayName("not owner — throws UnauthorizedAccessException")
        void removeSong_notOwner_throwsUnauthorized() {
            when(playlistRepository.findById(2L))
                    .thenReturn(Optional.of(otherPublicPlaylist));

            assertThrows(UnauthorizedAccessException.class,
                    () -> playlistService.removeSongFromPlaylist(2L, 10L));

            verify(playlistSongRepository, never())
                    .deleteByPlaylist_IdAndSong_Id(anyLong(), anyLong());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  reorderSongs
    //
    //  NOTE: This method uses securityUtils.getCurrentUser()
    //        (NOT the private getCurrentUser() method)
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("reorderSongs")
    class ReorderSongs {

        @Test
        @DisplayName("valid reorder — positions updated correctly")
        void reorder_validInput_positionsUpdated() {
            PlaylistSong ps1 = new PlaylistSong();
            ps1.setId(1L);
            ps1.setPlaylist(ownedPlaylist);
            ps1.setSong(Song.builder().id(10L).build());
            ps1.setPosition(1);

            PlaylistSong ps2 = new PlaylistSong();
            ps2.setId(2L);
            ps2.setPlaylist(ownedPlaylist);
            ps2.setSong(Song.builder().id(20L).build());
            ps2.setPosition(2);

            PlaylistSong ps3 = new PlaylistSong();
            ps3.setId(3L);
            ps3.setPlaylist(ownedPlaylist);
            ps3.setSong(Song.builder().id(30L).build());
            ps3.setPosition(3);

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(1L))
                    .thenReturn(List.of(ps1, ps2, ps3));

            // Reverse order: 30, 20, 10
            List<Long> newOrder = List.of(30L, 20L, 10L);

            playlistService.reorderSongs(1L, newOrder);

            verify(playlistSongRepository).saveAll(argThat(songs -> {
                List<PlaylistSong> list = new ArrayList<>();
                songs.forEach(list::add);
                return list.size() == 3;
            }));
        }

        @Test
        @DisplayName("valid reorder — verify new positions are 1-based")
        void reorder_validInput_positionsAreOneBased() {
            PlaylistSong ps1 = new PlaylistSong();
            ps1.setId(1L);
            ps1.setPlaylist(ownedPlaylist);
            ps1.setSong(Song.builder().id(10L).build());
            ps1.setPosition(1);

            PlaylistSong ps2 = new PlaylistSong();
            ps2.setId(2L);
            ps2.setPlaylist(ownedPlaylist);
            ps2.setSong(Song.builder().id(20L).build());
            ps2.setPosition(2);

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(1L))
                    .thenReturn(List.of(ps1, ps2));

            // Swap: 20, 10
            playlistService.reorderSongs(1L, List.of(20L, 10L));

            // After reorder: song 20 → position 1, song 10 → position 2
            verify(playlistSongRepository).saveAll(argThat(songs -> {
                List<PlaylistSong> list = new ArrayList<>();
                songs.forEach(list::add);

                // Find by song ID and check position
                PlaylistSong forSong20 = list.stream()
                        .filter(ps -> ps.getSong().getId().equals(20L))
                        .findFirst().orElse(null);
                PlaylistSong forSong10 = list.stream()
                        .filter(ps -> ps.getSong().getId().equals(10L))
                        .findFirst().orElse(null);

                return forSong20 != null && forSong20.getPosition() == 1
                        && forSong10 != null && forSong10.getPosition() == 2;
            }));
        }

        @Test
        @DisplayName("mismatched song IDs — throws BadRequestException")
        void reorder_mismatchedIds_throwsBadRequest() {
            PlaylistSong ps1 = new PlaylistSong();
            ps1.setPlaylist(ownedPlaylist);
            ps1.setSong(Song.builder().id(10L).build());

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(ownedPlaylist));
            when(playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(1L))
                    .thenReturn(List.of(ps1));

            // Send IDs that don't match what's in playlist
            List<Long> wrongOrder = List.of(99L, 88L);

            assertThrows(BadRequestException.class,
                    () -> playlistService.reorderSongs(1L, wrongOrder));

            verify(playlistSongRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("not owner — throws UnauthorizedAccessException")
        void reorder_notOwner_throwsUnauthorized() {
            when(playlistRepository.findById(2L))
                    .thenReturn(Optional.of(otherPublicPlaylist));

            assertThrows(UnauthorizedAccessException.class,
                    () -> playlistService.reorderSongs(2L, List.of(10L)));

            verify(playlistSongRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("playlist not found — throws ResourceNotFoundException")
        void reorder_notFound_throwsResourceNotFound() {
            when(playlistRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> playlistService.reorderSongs(99L, List.of()));
        }
    }
}