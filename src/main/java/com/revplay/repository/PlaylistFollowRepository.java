package com.revplay.repository;

import com.revplay.model.PlaylistFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistFollowRepository
        extends JpaRepository<PlaylistFollow, Long> {

    // All playlists followed by a user
    List<PlaylistFollow> findByUser_Id(Long userId);

    // Find a specific follow record
    Optional<PlaylistFollow> findByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

    // Check if already following (more efficient than loading the full entity)
    boolean existsByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

    // Unfollow — remove the join record for a specific user/playlist pair
    @Modifying
    void deleteByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

    // Remove ALL follow records for a playlist — used when deleting the playlist.
    // Must run before playlistRepository.delete() to avoid FK constraint violation.
    @Modifying
    void deleteByPlaylist_Id(Long playlistId);
}