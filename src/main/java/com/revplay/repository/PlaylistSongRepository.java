package com.revplay.repository;

import com.revplay.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    // Get all songs in a playlist ordered by position
    List<PlaylistSong> findByPlaylist_IdOrderByPosition(Long playlistId);

    // Remove a specific song from a playlist
    @Transactional
    void deleteByPlaylist_IdAndSong_Id(Long playlistId, Long songId);

    // Check if a song already exists in a playlist
    boolean existsByPlaylist_IdAndSong_Id(Long playlistId, Long songId);
}