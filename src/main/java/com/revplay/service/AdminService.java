package com.revplay.service;

import com.revplay.dto.UserDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.Role;
import com.revplay.model.User;
import com.revplay.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final FavoriteRepository favoriteRepository;
    private final HistoryRepository historyRepository;

    // ═══════════════════════════════════════════
    // DASHBOARD ANALYTICS
    // ═══════════════════════════════════════════

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalArtists", userRepository.countByRole(Role.ARTIST));
        stats.put("totalListeners", userRepository.countByRole(Role.LISTENER));
        stats.put("totalAdmins", userRepository.countByRole(Role.ADMIN));
        stats.put("totalSongs", songRepository.count());
        stats.put("totalAlbums", albumCount());
        stats.put("totalPlaylists", playlistRepository.count());

        // New users this week
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        stats.put("newUsersThisWeek", userRepository.countByCreatedAtAfter(oneWeekAgo));

        // New users today
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        stats.put("newUsersToday", userRepository.countByCreatedAtAfter(today));

        return stats;
    }

    private long albumCount() {
        try {
            // If AlbumRepository exists
            return songRepository.count(); // placeholder — will be overridden
        } catch (Exception e) {
            return 0;
        }
    }

    public Map<String, Long> getRoleDistribution() {
        Map<String, Long> dist = new LinkedHashMap<>();
        List<Object[]> grouped = userRepository.countByRoleGrouped();
        for (Object[] row : grouped) {
            dist.put(((Role) row[0]).name(), (Long) row[1]);
        }
        return dist;
    }

    public List<Object[]> getNewUsersPerDay(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.countNewUsersPerDay(since);
    }

    // ═══════════════════════════════════════════
    // USER MANAGEMENT
    // ═══════════════════════════════════════════

    public Page<UserDTO> getUsers(String search, String roleFilter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;

        if (search != null && !search.isBlank() && roleFilter != null && !roleFilter.isBlank()) {
            Role role = Role.valueOf(roleFilter.toUpperCase());
            users = userRepository.searchUsersByRole(search.trim(), role, pageable);
        } else if (search != null && !search.isBlank()) {
            users = userRepository.searchUsers(search.trim(), pageable);
        } else if (roleFilter != null && !roleFilter.isBlank()) {
            Role role = Role.valueOf(roleFilter.toUpperCase());
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToDTO);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role oldRole = user.getRole();

        // Don't allow changing your own role
        // (handled at controller level)

        // If promoting to ARTIST, create Artist profile if not exists
        if (newRole == Role.ARTIST && oldRole != Role.ARTIST) {
            if (!artistRepository.findByUserId(userId).isPresent()) {
                Artist artist = new Artist();
                artist.setUserId(user.getId());  // ← fixed line
                artist.setArtistName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                artist.setBio("New artist on RevPlay");
                artist.setCreatedAt(LocalDateTime.now());
                artistRepository.save(artist);
                log.info("Created artist profile for user {} (promoted to ARTIST)", userId);
            }
        }

        user.setRole(newRole);
        userRepository.save(user);

        log.info("Changed role for user {} from {} to {}", userId, oldRole, newRole);
        return mapToDTO(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot delete an admin account");
        }

        // Clean up related data
        historyRepository.deleteByUser_Id(userId);
        favoriteRepository.deleteByUser_Id(userId);

        // Delete artist profile if exists
        artistRepository.findByUserId(userId).ifPresent(artist -> {
            artistRepository.delete(artist);
        });

        userRepository.delete(user);
        log.info("Deleted user {} ({})", userId, user.getUsername());
    }

    // ═══════════════════════════════════════════
    // CONTENT MODERATION
    // ═══════════════════════════════════════════

    @Transactional
    public void deleteSong(Long songId) {
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song", "id", songId);
        }
        songRepository.deleteById(songId);
        log.info("Admin deleted song {}", songId);
    }

    @Transactional
    public void deletePlaylist(Long playlistId) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResourceNotFoundException("Playlist", "id", playlistId);
        }
        playlistRepository.deleteById(playlistId);
        log.info("Admin deleted playlist {}", playlistId);
    }

    // ═══════════════════════════════════════════
    // TOP CONTENT
    // ═══════════════════════════════════════════

    public List<Map<String, Object>> getTopSongs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return songRepository.findAll(PageRequest.of(0, limit, Sort.by("playCount").descending()))
                .stream()
                .map(song -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", song.getId());
                    m.put("title", song.getTitle());
                    m.put("artist", song.getArtist() != null ? song.getArtist().getArtistName() : "Unknown");
                    m.put("playCount", song.getPlayCount() != null ? song.getPlayCount() : 0);
                    m.put("genre", song.getGenre());
                    return m;
                })
                .toList();
    }

    // ═══════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}