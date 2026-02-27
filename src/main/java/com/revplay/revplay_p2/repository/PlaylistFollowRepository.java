package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.PlaylistFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaylistFollowRepository extends JpaRepository<PlaylistFollow, Long> {
    List<PlaylistFollow> findByPlaylistId(Long playlistId);
    void deleteByPlaylistId(Long playlistId);
    void deleteByUserIdAndPlaylistId(Long userId, Long playlistId);
    boolean existsByUserIdAndPlaylistId(Long userId, Long playlistId);
}