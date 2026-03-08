package com.revplay.repository;

import com.revplay.model.PlayEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {

    // ── EXISTING ─────────────────────────────────────────────────────────────

    List<PlayEvent> findBySongId(Long songId);

    List<PlayEvent> findByUserId(Long userId);

    long countBySongId(Long songId);

    // ── DAY 6 ANALYTICS ───────────────────────────────────────────────────────

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
    // Pageable allows DB-level limiting — e.g. PageRequest.of(0, 10) for top 10
    // user_id is nullable in play_events — only include non-null users
    @Query("SELECT pe.user.id, pe.user.username, pe.user.displayName, COUNT(pe) " +
            "FROM PlayEvent pe " +
            "WHERE pe.song.artist.id = :artistId " +
            "AND pe.user IS NOT NULL " +
            "GROUP BY pe.user.id, pe.user.username, pe.user.displayName " +
            "ORDER BY COUNT(pe) DESC")
    List<Object[]> findTopListenersByArtistId(@Param("artistId") Long artistId,
                                              Pageable pageable);

    // ── DAY 7: LISTENING TRENDS (native queries — MySQL + H2 MODE=MySQL) ──────

    // Daily play counts — grouped by calendar date
    // Returns Object[] {dateString ("yyyy-MM-dd"), playCount}
    // Lookback window controlled by :from and :to from service layer (last 30 days)
    @Query(value = "SELECT DATE_FORMAT(pe.played_at, '%Y-%m-%d') AS period, COUNT(*) AS play_count " +
            "FROM play_events pe " +
            "JOIN songs s ON pe.song_id = s.id " +
            "WHERE s.artist_id = :artistId " +
            "AND pe.played_at >= :from " +
            "AND pe.played_at <= :to " +
            "GROUP BY period " +
            "ORDER BY period ASC",
            nativeQuery = true)
    List<Object[]> findDailyTrendsByArtistId(@Param("artistId") Long artistId,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    // Weekly play counts — grouped by ISO year-week
    // Returns Object[] {weekString ("yyyy-Www"), playCount}
    // Uses %x-%v (ISO week, Monday start) instead of %Y-%u (Sunday start, can give W00)
    // Lookback window controlled by :from and :to from service layer (last 12 weeks)
    @Query(value = "SELECT DATE_FORMAT(pe.played_at, '%x-W%v') AS period, COUNT(*) AS play_count " +
            "FROM play_events pe " +
            "JOIN songs s ON pe.song_id = s.id " +
            "WHERE s.artist_id = :artistId " +
            "AND pe.played_at >= :from " +
            "AND pe.played_at <= :to " +
            "GROUP BY period " +
            "ORDER BY period ASC",
            nativeQuery = true)
    List<Object[]> findWeeklyTrendsByArtistId(@Param("artistId") Long artistId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    // Monthly play counts — grouped by year-month
    // Returns Object[] {monthString ("yyyy-MM"), playCount}
    // Lookback window controlled by :from and :to from service layer (last 12 months)
    @Query(value = "SELECT DATE_FORMAT(pe.played_at, '%Y-%m') AS period, COUNT(*) AS play_count " +
            "FROM play_events pe " +
            "JOIN songs s ON pe.song_id = s.id " +
            "WHERE s.artist_id = :artistId " +
            "AND pe.played_at >= :from " +
            "AND pe.played_at <= :to " +
            "GROUP BY period " +
            "ORDER BY period ASC",
            nativeQuery = true)
    List<Object[]> findMonthlyTrendsByArtistId(@Param("artistId") Long artistId,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);
}