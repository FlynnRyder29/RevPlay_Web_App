package com.revplay.repository;

import com.revplay.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // All playlists created by a specific user
    List<Playlist> findByUser_Id(Long userId);

    // All public playlists
    List<Playlist> findByIsPublicTrue();

    // Public playlists of a specific user
    List<Playlist> findByUser_IdAndIsPublicTrue(Long userId);
}