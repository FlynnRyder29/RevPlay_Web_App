package com.revplay.repository;

import com.revplay.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    // Get all songs in a playlist ordered by position (0-based)
    List<PlaylistSong> findByPlaylist_IdOrderByPosition(Long playlistId);

    // Get all songs in a playlist ordered by position ascending (1-based, used by reorder)
    List<PlaylistSong> findByPlaylist_IdOrderByPositionAsc(Long playlistId);

    // Remove a specific song from a playlist
    void deleteByPlaylist_IdAndSong_Id(Long playlistId, Long songId);

    // Remove ALL songs from a playlist — used when deleting the playlist itself
    void deleteByPlaylist_Id(Long playlistId);

    // Check if a song already exists in a playlist
    boolean existsByPlaylist_IdAndSong_Id(Long playlistId, Long songId);

    // Count songs in a playlist
    long countByPlaylist_Id(Long playlistId);

    List<PlaylistSong> findByPlaylist_IdAndSong_Id(Long playlistId, Long songId);
}