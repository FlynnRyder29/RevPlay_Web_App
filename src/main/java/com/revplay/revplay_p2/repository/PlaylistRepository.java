package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(Long userId);
    List<Playlist> findByIsPublicTrue();
}