package com.revplay.repository;

import com.revplay.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // All favorites of a user
    List<Favorite> findByUser_Id(Long userId);

    // Find specific favorite
    Optional<Favorite> findByUser_IdAndSong_Id(Long userId, Long songId);

    // Check if already favorited
    boolean existsByUser_IdAndSong_Id(Long userId, Long songId);

    // Remove favorite
    void deleteByUser_IdAndSong_Id(Long userId, Long songId);

    // ── ADDED FOR DAY 6 ANALYTICS ─────────────────────────────────────────────

    // Total favorites across ALL songs of a specific artist
    // Used in overview endpoint — total times artist's songs were favorited
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.song.artist.id = :artistId")
    long countByArtistId(@Param("artistId") Long artistId);

    List<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // ── ADDED FOR DAY 7 ANALYTICS ─────────────────────────────────────────────

    // Fans who favorited a specific song
    // Returns Object[] {userId, username, displayName, createdAt}
    // Used for GET /api/artists/analytics/songs/{id}/fans
    @Query("SELECT f.user.id, f.user.username, f.user.displayName, f.createdAt " +
            "FROM Favorite f " +
            "WHERE f.song.id = :songId " +
            "ORDER BY f.createdAt DESC")
    List<Object[]> findFansBySongId(@Param("songId") Long songId);

    // ── ADDED FOR DAY 7 ANALYTICS ─────────────────────────────────────────────

    // Daily trends — play counts grouped by date (YYYY-MM-DD)
    // Returns Object[] {date, playCount}
    // Used for GET /api/artists/analytics/trends?period=daily
    @Query("SELECT FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-%m-%d'), COUNT(pe) " +
            "FROM PlayEvent pe " +
            "WHERE pe.song.artist.id = :artistId " +
            "GROUP BY FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-%m-%d') ASC")
    List<Object[]> findDailyTrendsByArtistId(@Param("artistId") Long artistId);

    // Weekly trends — play counts grouped by year-week (YYYY-Www)
    // Returns Object[] {yearWeek, playCount}
    // Used for GET /api/artists/analytics/trends?period=weekly
    @Query("SELECT FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-W%u'), COUNT(pe) " +
            "FROM PlayEvent pe " +
            "WHERE pe.song.artist.id = :artistId " +
            "GROUP BY FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-W%u') " +
            "ORDER BY FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-W%u') ASC")
    List<Object[]> findWeeklyTrendsByArtistId(@Param("artistId") Long artistId);

    // Monthly trends — play counts grouped by year-month (YYYY-MM)
    // Returns Object[] {yearMonth, playCount}
    // Used for GET /api/artists/analytics/trends?period=monthly
    @Query("SELECT FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-%m'), COUNT(pe) " +
            "FROM PlayEvent pe " +
            "WHERE pe.song.artist.id = :artistId " +
            "GROUP BY FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', pe.playedAt, '%Y-%m') ASC")
    List<Object[]> findMonthlyTrendsByArtistId(@Param("artistId") Long artistId);
}