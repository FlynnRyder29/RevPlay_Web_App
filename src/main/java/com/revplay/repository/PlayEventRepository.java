package com.revplay.repository;

import com.revplay.model.PlayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {

    List<PlayEvent> findBySongId(Long songId);

    List<PlayEvent> findByUserId(Long userId);

    long countBySongId(Long songId);

    // ── ADDED FOR DAY 6 ANALYTICS ─────────────────────────────────────────────

    // Total play events across ALL songs of a specific artist
    @Query("SELECT COUNT(pe) FROM PlayEvent pe WHERE pe.song.artist.id = :artistId")
    long countByArtistId(@Param("artistId") Long artistId);

    // Per-song play counts for all songs of a specific artist
    // Returns Object[] {songId, songTitle, coverImageUrl, playCount}
    @Query("SELECT pe.song.id, pe.song.title, pe.song.coverImageUrl, COUNT(pe) " +
            "FROM PlayEvent pe " +
            "WHERE pe.song.artist.id = :artistId " +
            "GROUP BY pe.song.id, pe.song.title, pe.song.coverImageUrl " +
            "ORDER BY COUNT(pe) DESC")
    List<Object[]> findSongPlayCountsByArtistId(@Param("artistId") Long artistId);

    // Top listeners — users who played this artist's songs the most
    // Returns Object[] {userId, username, displayName, playCount}
    // user_id is nullable in play_events — only include non-null users
    @Query("SELECT pe.user.id, pe.user.username, pe.user.displayName, COUNT(pe) " +
            "FROM PlayEvent pe " +
            "WHERE pe.song.artist.id = :artistId " +
            "AND pe.user IS NOT NULL " +
            "GROUP BY pe.user.id, pe.user.username, pe.user.displayName " +
            "ORDER BY COUNT(pe) DESC")
    List<Object[]> findTopListenersByArtistId(@Param("artistId") Long artistId);
}