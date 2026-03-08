package com.revplay.service;

import com.revplay.dto.ArtistRequestDTO;
import com.revplay.dto.UserDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.*;
import com.revplay.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private SongRepository songRepository;
    @Mock private PlaylistRepository playlistRepository;
    @Mock private FavoriteRepository favoriteRepository;
    @Mock private HistoryRepository historyRepository;
    @Mock private ArtistRequestRepository artistRequestRepository;

    @InjectMocks
    private AdminService adminService;

    private User listenerUser;
    private User artistUser;
    private User adminUser;
    private ArtistRequest pendingRequest;
    private ArtistRequest approvedRequest;

    @BeforeEach
    void setUp() {
        listenerUser = User.builder()
                .id(10L)
                .email("listener@revplay.com")
                .username("listener_one")
                .displayName("Listener One")
                .passwordHash("$2b$10$hash")
                .role(Role.LISTENER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        artistUser = User.builder()
                .id(20L)
                .email("artist@revplay.com")
                .username("artist_one")
                .displayName("Artist One")
                .passwordHash("$2b$10$hash")
                .role(Role.ARTIST)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .id(1L)
                .email("admin@revplay.com")
                .username("admin")
                .displayName("Admin")
                .passwordHash("$2b$10$hash")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        pendingRequest = ArtistRequest.builder()
                .id(100L)
                .userId(10L)
                .artistName("Listener One")
                .genre("Pop")
                .reason("I make music")
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        approvedRequest = ArtistRequest.builder()
                .id(101L)
                .userId(10L)
                .artistName("Listener One")
                .genre("Pop")
                .reason("I make music")
                .status(RequestStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── getDashboardStats ─────────────────────────────────────────

    @Test
    @DisplayName("getDashboardStats - returns map with all expected keys")
    void getDashboardStats_returnsAllExpectedKeys() {
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countByRole(Role.ARTIST)).thenReturn(10L);
        when(userRepository.countByRole(Role.LISTENER)).thenReturn(35L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(5L);
        when(songRepository.count()).thenReturn(200L);
        when(playlistRepository.count()).thenReturn(30L);
        when(artistRequestRepository.countByStatus(RequestStatus.PENDING)).thenReturn(3L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(2L);

        Map<String, Object> stats = adminService.getDashboardStats();

        assertTrue(stats.containsKey("totalUsers"));
        assertTrue(stats.containsKey("totalArtists"));
        assertTrue(stats.containsKey("totalListeners"));
        assertTrue(stats.containsKey("totalAdmins"));
        assertTrue(stats.containsKey("totalSongs"));
        assertTrue(stats.containsKey("totalPlaylists"));
        assertTrue(stats.containsKey("pendingArtistRequests"));
        assertTrue(stats.containsKey("newUsersThisWeek"));
        assertTrue(stats.containsKey("newUsersToday"));
    }

    @Test
    @DisplayName("getDashboardStats - totalUsers value is correct")
    void getDashboardStats_totalUsersIsCorrect() {
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countByRole(any())).thenReturn(0L);
        when(songRepository.count()).thenReturn(0L);
        when(playlistRepository.count()).thenReturn(0L);
        when(artistRequestRepository.countByStatus(any())).thenReturn(0L);
        when(userRepository.countByCreatedAtAfter(any())).thenReturn(0L);

        Map<String, Object> stats = adminService.getDashboardStats();

        assertEquals(50L, stats.get("totalUsers"));
    }

    @Test
    @DisplayName("getDashboardStats - pendingArtistRequests value is correct")
    void getDashboardStats_pendingArtistRequestsIsCorrect() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByRole(any())).thenReturn(0L);
        when(songRepository.count()).thenReturn(0L);
        when(playlistRepository.count()).thenReturn(0L);
        when(artistRequestRepository.countByStatus(RequestStatus.PENDING)).thenReturn(7L);
        when(userRepository.countByCreatedAtAfter(any())).thenReturn(0L);

        Map<String, Object> stats = adminService.getDashboardStats();

        assertEquals(7L, stats.get("pendingArtistRequests"));
    }

    // ── getRoleDistribution ───────────────────────────────────────

    @Test
    @DisplayName("getRoleDistribution - returns map with role name keys")
    void getRoleDistribution_returnsMappedRoles() {
        List<Object[]> grouped = new java.util.ArrayList<>();
        grouped.add(new Object[]{Role.LISTENER, 35L});
        grouped.add(new Object[]{Role.ARTIST, 10L});
        grouped.add(new Object[]{Role.ADMIN, 5L});
        when(userRepository.countByRoleGrouped()).thenReturn(grouped);

        Map<String, Long> dist = adminService.getRoleDistribution();

        assertEquals(35L, dist.get("LISTENER"));
        assertEquals(10L, dist.get("ARTIST"));
        assertEquals(5L, dist.get("ADMIN"));
    }

    // ── getNewUsersPerDay ─────────────────────────────────────────

    @Test
    @DisplayName("getNewUsersPerDay - delegates to repository with correct date range")
    void getNewUsersPerDay_delegatesToRepository() {
        List<Object[]> fakeData = new java.util.ArrayList<>();
        fakeData.add(new Object[]{"2024-01-01", 3L});
        when(userRepository.countNewUsersPerDay(any(LocalDateTime.class))).thenReturn(fakeData);

        List<Object[]> result = adminService.getNewUsersPerDay(7);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).countNewUsersPerDay(any(LocalDateTime.class));
    }

    // ── getUsers ──────────────────────────────────────────────────

    @Test
    @DisplayName("getUsers - no filters - returns all users paged")
    void getUsers_noFilters_returnsAllUsers() {
        Page<User> page = new PageImpl<>(List.of(listenerUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<UserDTO> result = adminService.getUsers(null, null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("listener_one", result.getContent().get(0).getUsername());
    }

    @Test
    @DisplayName("getUsers - search only - calls searchUsers")
    void getUsers_searchOnly_callsSearchUsers() {
        Page<User> page = new PageImpl<>(List.of(listenerUser));
        when(userRepository.searchUsers(eq("listener"), any(Pageable.class))).thenReturn(page);

        Page<UserDTO> result = adminService.getUsers("listener", null, 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).searchUsers(eq("listener"), any(Pageable.class));
    }

    @Test
    @DisplayName("getUsers - role filter only - calls findByRole")
    void getUsers_roleFilterOnly_callsFindByRole() {
        Page<User> page = new PageImpl<>(List.of(artistUser));
        when(userRepository.findByRole(eq(Role.ARTIST), any(Pageable.class))).thenReturn(page);

        Page<UserDTO> result = adminService.getUsers(null, "ARTIST", 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByRole(eq(Role.ARTIST), any(Pageable.class));
    }

    @Test
    @DisplayName("getUsers - search and role filter - calls searchUsersByRole")
    void getUsers_searchAndRoleFilter_callsSearchUsersByRole() {
        Page<User> page = new PageImpl<>(List.of(artistUser));
        when(userRepository.searchUsersByRole(eq("artist"), eq(Role.ARTIST), any(Pageable.class))).thenReturn(page);

        Page<UserDTO> result = adminService.getUsers("artist", "ARTIST", 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).searchUsersByRole(eq("artist"), eq(Role.ARTIST), any(Pageable.class));
    }

    // ── getUserById ───────────────────────────────────────────────

    @Test
    @DisplayName("getUserById - existing id - returns mapped UserDTO")
    void getUserById_existingId_returnsUserDTO() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));

        UserDTO result = adminService.getUserById(10L);

        assertEquals(10L, result.getId());
        assertEquals("listener@revplay.com", result.getEmail());
        assertEquals(Role.LISTENER, result.getRole());
    }

    @Test
    @DisplayName("getUserById - unknown id - throws ResourceNotFoundException")
    void getUserById_unknownId_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(999L));
    }

    // ── changeUserRole ────────────────────────────────────────────

    @Test
    @DisplayName("changeUserRole - listener to ADMIN - updates role")
    void changeUserRole_listenerToAdmin_updatesRole() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));
        when(userRepository.save(any(User.class))).thenReturn(listenerUser);

        adminService.changeUserRole(10L, Role.ADMIN);

        verify(userRepository).save(any(User.class));
        assertEquals(Role.ADMIN, listenerUser.getRole());
    }

    @Test
    @DisplayName("changeUserRole - listener to ARTIST - creates artist profile if not exists")
    void changeUserRole_listenerToArtist_createsArtistProfile() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));
        when(artistRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(listenerUser);

        adminService.changeUserRole(10L, Role.ARTIST);

        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    @DisplayName("changeUserRole - listener to ARTIST - does not create duplicate artist profile")
    void changeUserRole_listenerToArtist_doesNotCreateDuplicateProfile() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));
        when(artistRepository.findByUserId(10L)).thenReturn(Optional.of(new Artist()));
        when(userRepository.save(any(User.class))).thenReturn(listenerUser);

        adminService.changeUserRole(10L, Role.ARTIST);

        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    @DisplayName("changeUserRole - already ARTIST to LISTENER - no artist profile created")
    void changeUserRole_artistToListener_noArtistProfileCreated() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(artistUser));
        when(userRepository.save(any(User.class))).thenReturn(artistUser);

        adminService.changeUserRole(20L, Role.LISTENER);

        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    @DisplayName("changeUserRole - unknown id - throws ResourceNotFoundException")
    void changeUserRole_unknownId_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.changeUserRole(999L, Role.LISTENER));
    }

    // ── deleteUser ────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser - listener - deletes user and cleans up")
    void deleteUser_listener_deletesUserAndCleansUp() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));
        when(artistRepository.findByUserId(10L)).thenReturn(Optional.empty());

        adminService.deleteUser(10L);

        verify(historyRepository).deleteByUser_Id(10L);
        verify(favoriteRepository).deleteByUser_Id(10L);
        verify(userRepository).delete(listenerUser);
    }

    @Test
    @DisplayName("deleteUser - artist user - also deletes artist profile")
    void deleteUser_artistUser_deletesArtistProfile() {
        Artist artistProfile = new Artist();
        when(userRepository.findById(20L)).thenReturn(Optional.of(artistUser));
        when(artistRepository.findByUserId(20L)).thenReturn(Optional.of(artistProfile));

        adminService.deleteUser(20L);

        verify(artistRepository).delete(artistProfile);
        verify(userRepository).delete(artistUser);
    }

    @Test
    @DisplayName("deleteUser - admin user - throws IllegalArgumentException")
    void deleteUser_adminUser_throwsIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class, () -> adminService.deleteUser(1L));

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser - unknown id - throws ResourceNotFoundException")
    void deleteUser_unknownId_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteUser(999L));
    }

    // ── getArtistRequestStats ─────────────────────────────────────

    @Test
    @DisplayName("getArtistRequestStats - returns pending, approved, rejected counts")
    void getArtistRequestStats_returnsAllStatusCounts() {
        when(artistRequestRepository.countByStatus(RequestStatus.PENDING)).thenReturn(4L);
        when(artistRequestRepository.countByStatus(RequestStatus.APPROVED)).thenReturn(10L);
        when(artistRequestRepository.countByStatus(RequestStatus.REJECTED)).thenReturn(2L);

        Map<String, Object> stats = adminService.getArtistRequestStats();

        assertEquals(4L, stats.get("pending"));
        assertEquals(10L, stats.get("approved"));
        assertEquals(2L, stats.get("rejected"));
    }

    // ── getArtistRequests ─────────────────────────────────────────

    @Test
    @DisplayName("getArtistRequests - no status filter - returns all requests")
    void getArtistRequests_noFilter_returnsAllRequests() {
        Page<ArtistRequest> page = new PageImpl<>(List.of(pendingRequest));
        when(artistRequestRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));

        Page<ArtistRequestDTO> result = adminService.getArtistRequests(null, 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(artistRequestRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("getArtistRequests - status filter PENDING - calls findByStatus")
    void getArtistRequests_pendingFilter_callsFindByStatus() {
        Page<ArtistRequest> page = new PageImpl<>(List.of(pendingRequest));
        when(artistRequestRepository.findByStatus(eq(RequestStatus.PENDING), any(Pageable.class))).thenReturn(page);
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));

        Page<ArtistRequestDTO> result = adminService.getArtistRequests("PENDING", 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(artistRequestRepository).findByStatus(eq(RequestStatus.PENDING), any(Pageable.class));
    }

    // ── approveArtistRequest ──────────────────────────────────────

    @Test
    @DisplayName("approveArtistRequest - pending request - updates status to APPROVED")
    void approveArtistRequest_pendingRequest_setsStatusApproved() {
        when(artistRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));
        when(artistRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(artistRequestRepository.save(any())).thenReturn(pendingRequest);
        when(userRepository.save(any())).thenReturn(listenerUser);

        ArtistRequestDTO result = adminService.approveArtistRequest(100L, 1L, "Great!");

        assertEquals(RequestStatus.APPROVED, pendingRequest.getStatus());
        verify(artistRequestRepository).save(pendingRequest);
    }

    @Test
    @DisplayName("approveArtistRequest - pending request - promotes user to ARTIST")
    void approveArtistRequest_pendingRequest_promotesUserToArtist() {
        when(artistRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));
        when(artistRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(artistRequestRepository.save(any())).thenReturn(pendingRequest);
        when(userRepository.save(any())).thenReturn(listenerUser);

        adminService.approveArtistRequest(100L, 1L, "Great!");

        assertEquals(Role.ARTIST, listenerUser.getRole());
        verify(userRepository).save(listenerUser);
    }

    @Test
    @DisplayName("approveArtistRequest - pending request - creates artist profile")
    void approveArtistRequest_pendingRequest_createsArtistProfile() {
        when(artistRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findById(10L)).thenReturn(Optional.of(listenerUser));
        when(artistRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(artistRequestRepository.save(any())).thenReturn(pendingRequest);
        when(userRepository.save(any())).thenReturn(listenerUser);

        adminService.approveArtistRequest(100L, 1L, "Great!");

        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    @DisplayName("approveArtistRequest - already approved request - throws IllegalStateException")
    void approveArtistRequest_alreadyApproved_throwsIllegalStateException() {
        when(artistRequestRepository.findById(101L)).thenReturn(Optional.of(approvedRequest));

        assertThrows(IllegalStateException.class,
                () -> adminService.approveArtistRequest(101L, 1L, "note"));
    }

    @Test
    @DisplayName("approveArtistRequest - unknown request id - throws ResourceNotFoundException")
    void approveArtistRequest_unknownId_throwsResourceNotFoundException() {
        when(artistRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.approveArtistRequest(999L, 1L, "note"));
    }

    // ── rejectArtistRequest ───────────────────────────────────────

    @Test
    @DisplayName("rejectArtistRequest - pending request - updates status to REJECTED")
    void rejectArtistRequest_pendingRequest_setsStatusRejected() {
        when(artistRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(artistRequestRepository.save(any())).thenReturn(pendingRequest);

        adminService.rejectArtistRequest(100L, 1L, "Not suitable");

        assertEquals(RequestStatus.REJECTED, pendingRequest.getStatus());
        verify(artistRequestRepository).save(pendingRequest);
    }

    @Test
    @DisplayName("rejectArtistRequest - null adminNote - uses default note")
    void rejectArtistRequest_nullAdminNote_usesDefaultNote() {
        when(artistRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(artistRequestRepository.save(any())).thenReturn(pendingRequest);

        adminService.rejectArtistRequest(100L, 1L, null);

        assertEquals("Request declined by admin", pendingRequest.getAdminNote());
    }

    @Test
    @DisplayName("rejectArtistRequest - blank adminNote - uses default note")
    void rejectArtistRequest_blankAdminNote_usesDefaultNote() {
        when(artistRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(artistRequestRepository.save(any())).thenReturn(pendingRequest);

        adminService.rejectArtistRequest(100L, 1L, "   ");

        assertEquals("Request declined by admin", pendingRequest.getAdminNote());
    }

    @Test
    @DisplayName("rejectArtistRequest - already approved - throws IllegalStateException")
    void rejectArtistRequest_alreadyApproved_throwsIllegalStateException() {
        when(artistRequestRepository.findById(101L)).thenReturn(Optional.of(approvedRequest));

        assertThrows(IllegalStateException.class,
                () -> adminService.rejectArtistRequest(101L, 1L, "note"));
    }

    @Test
    @DisplayName("rejectArtistRequest - unknown request id - throws ResourceNotFoundException")
    void rejectArtistRequest_unknownId_throwsResourceNotFoundException() {
        when(artistRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.rejectArtistRequest(999L, 1L, "note"));
    }

    // ── deleteSong ────────────────────────────────────────────────

    @Test
    @DisplayName("deleteSong - existing song - deletes by id")
    void deleteSong_existingSong_deletesById() {
        when(songRepository.existsById(50L)).thenReturn(true);

        adminService.deleteSong(50L);

        verify(songRepository).deleteById(50L);
    }

    @Test
    @DisplayName("deleteSong - unknown song - throws ResourceNotFoundException")
    void deleteSong_unknownSong_throwsResourceNotFoundException() {
        when(songRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteSong(999L));

        verify(songRepository, never()).deleteById(any());
    }

    // ── deletePlaylist ────────────────────────────────────────────

    @Test
    @DisplayName("deletePlaylist - existing playlist - deletes by id")
    void deletePlaylist_existingPlaylist_deletesById() {
        when(playlistRepository.existsById(5L)).thenReturn(true);

        adminService.deletePlaylist(5L);

        verify(playlistRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deletePlaylist - unknown playlist - throws ResourceNotFoundException")
    void deletePlaylist_unknownPlaylist_throwsResourceNotFoundException() {
        when(playlistRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> adminService.deletePlaylist(999L));

        verify(playlistRepository, never()).deleteById(any());
    }

    // ── getTopSongs ───────────────────────────────────────────────

    @Test
    @DisplayName("getTopSongs - returns list with id, title, playCount, genre")
    void getTopSongs_returnsListWithExpectedFields() {
        com.revplay.model.Song song = new com.revplay.model.Song();
        song.setId(1L);
        song.setTitle("Test Song");
        song.setPlayCount(500L);
        song.setGenre("Pop");
        song.setArtist(null);

        Page<com.revplay.model.Song> page = new PageImpl<>(List.of(song));
        when(songRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<Map<String, Object>> result = adminService.getTopSongs(5);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals("Test Song", result.get(0).get("title"));
        assertEquals(500L, ((Number) result.get(0).get("playCount")).longValue());
    }

    @Test
    @DisplayName("getTopSongs - song with null artist - shows Unknown")
    void getTopSongs_songWithNullArtist_showsUnknown() {
        com.revplay.model.Song song = new com.revplay.model.Song();
        song.setId(2L);
        song.setTitle("No Artist Song");
        song.setPlayCount(100L);
        song.setGenre("Rock");
        song.setArtist(null);

        Page<com.revplay.model.Song> page = new PageImpl<>(List.of(song));
        when(songRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<Map<String, Object>> result = adminService.getTopSongs(5);

        assertEquals("Unknown", result.get(0).get("artist"));
    }

    @Test
    @DisplayName("getTopSongs - song with null playCount - defaults to 0")
    void getTopSongs_nullPlayCount_defaultsToZero() {
        com.revplay.model.Song song = new com.revplay.model.Song();
        song.setId(3L);
        song.setTitle("Old Song");
        song.setPlayCount(null);
        song.setGenre("Jazz");
        song.setArtist(null);

        Page<com.revplay.model.Song> page = new PageImpl<>(List.of(song));
        when(songRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<Map<String, Object>> result = adminService.getTopSongs(5);

        assertEquals(0L, ((Number) result.get(0).get("playCount")).longValue());
    }
}