package com.revplay.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// ── OVERVIEW DTO ─────────────────────────────────────────────────────────────
// Used for GET /api/artists/analytics/overview
// Returns total songs, total plays, total favorites for logged-in artist

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
    // Null in overview response — only populated in per-song endpoint
    private List<SongPlayCountDTO> songPlayCounts;

    // ── Top Listeners ────────────────────────────────────────────────────────
    // Used for GET /api/artists/analytics/top-listeners
    // Null in overview response — only populated in top-listeners endpoint
    private List<TopListenerDTO> topListeners;

    // ── Fans ─────────────────────────────────────────────────────────────────
    // Used for GET /api/artists/analytics/songs/{id}/fans
    // Null in all other responses — only populated in fans endpoint
    private List<FanDTO> fans;

    // ── Listening Trends ─────────────────────────────────────────────────────
    // Used for GET /api/artists/analytics/trends?period=daily|weekly|monthly
    // Null in all other responses — only populated in trends endpoint
    private String period;
    private List<TrendDTO> trends;


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

    // ── Nested: Fan (Day 7) ──────────────────────────────────────────────────
    // Represents a user who favorited a specific song
    // Used for GET /api/artists/analytics/songs/{id}/fans
    @Data
    @Builder
    public static class FanDTO {
        private Long userId;
        private String username;
        private String displayName;
        private String favoritedAt;  // formatted datetime string
    }

    // ── Nested: Trend (Day 7) ────────────────────────────────────────────────
    // Represents play count for a specific time period bucket
    // Used for GET /api/artists/analytics/trends?period=daily|weekly|monthly
    @Data
    @Builder
    public static class TrendDTO {
        private String period;    // e.g. "2026-03-04" / "2026-W10" / "2026-03"
        private long playCount;
    }
}