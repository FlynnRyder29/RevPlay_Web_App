package com.revplay.repository;

import com.revplay.model.PlaylistFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistFollowRepository extends JpaRepository<PlaylistFollow, Long> {
    List<PlaylistFollow> findByUserId(Long userId);
    Optional<PlaylistFollow> findByUserIdAndPlaylistId(Long userId, Long playlistId);
    boolean existsByUserIdAndPlaylistId(Long userId, Long playlistId);
    void deleteByUserIdAndPlaylistId(Long userId, Long playlistId);
}