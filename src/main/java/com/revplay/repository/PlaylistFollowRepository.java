package com.revplay.repository;

import com.revplay.model.PlaylistFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistFollowRepository
        extends JpaRepository<PlaylistFollow, Long> {

    // All playlists followed by a user
    List<PlaylistFollow> findByUser_Id(Long userId);

    // Find specific follow record
    Optional<PlaylistFollow> findByUser_IdAndPlaylist_Id(
            Long userId,
            Long playlistId
    );

    // Check if already following
    boolean existsByUser_IdAndPlaylist_Id(
            Long userId,
            Long playlistId
    );

    // Unfollow playlist
    void deleteByUser_IdAndPlaylist_Id(
            Long userId,
            Long playlistId
    );
}