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

    // ── EXISTING ──────────────────────────────────────────────────────────────

    // All favorites of a user
    List<Favorite> findByUser_Id(Long userId);

    // Find specific favorite
    Optional<Favorite> findByUser_IdAndSong_Id(Long userId, Long songId);

    // Check if already favorited
    boolean existsByUser_IdAndSong_Id(Long userId, Long songId);

    // Remove favorite
    void deleteByUser_IdAndSong_Id(Long userId, Long songId);

    // All favorites of a user ordered by newest first
    List<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // ── DAY 6 ANALYTICS ───────────────────────────────────────────────────────

    // Total favorites across ALL songs of a specific artist
    // Used in overview endpoint — total times artist's songs were favorited
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.song.artist.id = :artistId")
    long countByArtistId(@Param("artistId") Long artistId);

    // ── DAY 7: FANS WHO FAVORITED ─────────────────────────────────────────────

    // Returns distinct users who favorited at least one song of this artist
    // along with how many of the artist's songs they have favorited
    // Returns Object[] {userId, username, displayName, profilePictureUrl, favoriteCount}
    // Ordered by favoriteCount DESC — biggest fans appear first
    @Query("SELECT f.user.id, f.user.username, f.user.displayName, f.user.profilePictureUrl, COUNT(f) " +
            "FROM Favorite f " +
            "WHERE f.song.artist.id = :artistId " +
            "GROUP BY f.user.id, f.user.username, f.user.displayName, f.user.profilePictureUrl " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> findFansByArtistId(@Param("artistId") Long artistId);

    // ── DAY 7 PR FIX: FANS WHO FAVORITED (specific song) ─────────────────────

    // Returns users who favorited a SPECIFIC song
    // Returns Object[] {userId, username, displayName, profilePictureUrl, favoriteCount (always 1)}
    // Ordered by createdAt DESC — most recent fans appear first
    // Used by GET /api/artists/analytics/songs/{songId}/fans
    @Query("SELECT f.user.id, f.user.username, f.user.displayName, f.user.profilePictureUrl, 1L " +
            "FROM Favorite f " +
            "WHERE f.song.id = :songId " +
            "ORDER BY f.createdAt DESC")
    List<Object[]> findFansBySongId(@Param("songId") Long songId);
}