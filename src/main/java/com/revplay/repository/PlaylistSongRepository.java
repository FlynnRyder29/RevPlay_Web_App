package com.revplay.repository;

import com.revplay.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    // Get all songs in a playlist ordered by position (used by addSongToPlaylist)
    List<PlaylistSong> findByPlaylist_IdOrderByPosition(Long playlistId);

    // Get all songs in a playlist ordered by position ascending (used by reorderSongs)
    List<PlaylistSong> findByPlaylist_IdOrderByPositionAsc(Long playlistId);

    // ═══ NEW: JOIN FETCH for playlist detail page — prevents LazyInitializationException ═══
    @Query("SELECT ps FROM PlaylistSong ps " +
            "JOIN FETCH ps.song s " +
            "JOIN FETCH s.artist " +
            "LEFT JOIN FETCH s.album " +
            "WHERE ps.playlist.id = :playlistId " +
            "ORDER BY ps.position")
    List<PlaylistSong> findByPlaylistIdWithSongDetails(@Param("playlistId") Long playlistId);

    // Remove a specific song from a playlist
    @Modifying
    void deleteByPlaylist_IdAndSong_Id(Long playlistId, Long songId);

    // Remove ALL songs from a playlist — used when deleting the playlist itself.
    @Modifying
    void deleteByPlaylist_Id(Long playlistId);

    // Check if a song already exists in a playlist
    boolean existsByPlaylist_IdAndSong_Id(Long playlistId, Long songId);

    // Count songs in a playlist
    long countByPlaylist_Id(Long playlistId);

    List<PlaylistSong> findByPlaylist_IdAndSong_Id(Long playlistId, Long songId);
}