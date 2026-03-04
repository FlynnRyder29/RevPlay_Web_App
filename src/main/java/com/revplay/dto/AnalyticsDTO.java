package com.revplay.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

// ── ANALYTICS DTO ─────────────────────────────────────────────────────────────
// Unified response DTO for all 5 artist analytics endpoints:
//   1. GET /api/artists/analytics/overview       → artistId, artistName, totalSongs, totalPlays, totalFavorites
//   2. GET /api/artists/analytics/songs          → artistId, artistName, songPlayCounts
//   3. GET /api/artists/analytics/top-listeners  → artistId, artistName, topListeners
//   4. GET /api/artists/analytics/trends         → artistId, artistName, trendPeriod, trends
//   5. GET /api/artists/analytics/fans           → artistId, artistName, fans

@Data
@Builder
public class AnalyticsDTO {

    // ── Artist Overview ──────────────────────────────────────────────────────
    private Long artistId;
    private String artistName;

    // Total songs uploaded by this artist
    private long totalSongs;

    // Total play events across all songs of this artist
    private long totalPlays;

    // Total times any song of this artist was favorited
    private long totalFavorites;

    // ── Per-Song Play Counts ─────────────────────────────────────────────────
    // Used for GET /api/artists/analytics/songs
    // Null in all other responses — only populated in per-song endpoint
    private List<SongPlayCountDTO> songPlayCounts;

    // ── Top Listeners ────────────────────────────────────────────────────────
    // Used for GET /api/artists/analytics/top-listeners
    // Null in all other responses — only populated in top-listeners endpoint
    private List<TopListenerDTO> topListeners;

    // ── Listening Trends ─────────────────────────────────────────────────────
    // Used for GET /api/artists/analytics/trends?period=daily|weekly|monthly
    // trendPeriod → "daily" | "weekly" | "monthly"
    // trends      → list of {period, playCount} data points
    // Null in all other responses — only populated in trends endpoint
    private String trendPeriod;
    private List<TrendPointDTO> trends;

    // ── Fans Who Favorited ───────────────────────────────────────────────────
    // Used for GET /api/artists/analytics/fans
    // Null in all other responses — only populated in fans endpoint
    private List<FanDTO> fans;

    // ── Nested: Per-Song Play Count ──────────────────────────────────────────
    @Data
    @Builder
    public static class SongPlayCountDTO {
        private Long songId;
        private String songTitle;
        private String coverImageUrl;
        private long playCount;
    }

    // ── Nested: Top Listener ─────────────────────────────────────────────────
    @Data
    @Builder
    public static class TopListenerDTO {
        private Long userId;
        private String username;
        private String displayName;
        private long playCount;
    }

    // ── Nested: Trend Data Point ─────────────────────────────────────────────
    // period format depends on granularity:
    //   daily   → "2025-03-04"
    //   weekly  → "2025-W10"
    //   monthly → "2025-03"
    @Data
    @Builder
    public static class TrendPointDTO {
        private String period;
        private long playCount;
    }

    // ── Nested: Fan ──────────────────────────────────────────────────────────
    // A user who favorited at least one song of this artist
    // favoriteCount = how many of this artist's songs they favorited
    @Data
    @Builder
    public static class FanDTO {
        private Long userId;
        private String username;
        private String displayName;
        private String profilePictureUrl;
        private long favoriteCount;
    }
}