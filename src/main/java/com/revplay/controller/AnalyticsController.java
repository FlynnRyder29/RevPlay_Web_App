package com.revplay.controller;

import com.revplay.dto.AnalyticsDTO;
import com.revplay.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ARTIST')")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    // ── OVERVIEW ──────────────────────────────────────────────────────────────
    // GET /api/artists/analytics/overview
    // Returns total songs, total plays, total favorites for logged-in artist

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsDTO> getOverview() {
        log.info("GET /api/artists/analytics/overview");
        return ResponseEntity.ok(analyticsService.getOverview());
    }

    // ── PER-SONG PLAY COUNTS ──────────────────────────────────────────────────
    // GET /api/artists/analytics/songs
    // Returns play count breakdown per song for logged-in artist

    @GetMapping("/songs")
    public ResponseEntity<AnalyticsDTO> getSongPlayCounts() {
        log.info("GET /api/artists/analytics/songs");
        return ResponseEntity.ok(analyticsService.getSongPlayCounts());
    }

    // ── TOP LISTENERS ─────────────────────────────────────────────────────────
    // GET /api/artists/analytics/top-listeners
    // Returns top 10 users who played this artist's songs the most

    @GetMapping("/top-listeners")
    public ResponseEntity<AnalyticsDTO> getTopListeners() {
        log.info("GET /api/artists/analytics/top-listeners");
        return ResponseEntity.ok(analyticsService.getTopListeners());
    }

    // ── LISTENING TRENDS ──────────────────────────────────────────────────────
    // GET /api/artists/analytics/trends?period=daily|weekly|monthly
    //
    // Query param:
    //   period = "daily"   → last 30 days  (default if not provided)
    //   period = "weekly"  → last 12 weeks
    //   period = "monthly" → last 12 months
    //
    // Returns: trendPeriod + list of {period, playCount} data points

    @GetMapping("/trends")
    public ResponseEntity<AnalyticsDTO> getListeningTrends(
            @RequestParam(name = "period", defaultValue = "daily") String period) {
        log.info("GET /api/artists/analytics/trends?period={}", period);
        return ResponseEntity.ok(analyticsService.getListeningTrends(period));
    }

    // ── FANS WHO FAVORITED ────────────────────────────────────────────────────
    // GET /api/artists/analytics/fans
    // Returns all users who favorited ≥1 of this artist's songs
    // Ordered by favoriteCount DESC — biggest fans appear first

    @GetMapping("/fans")
    public ResponseEntity<AnalyticsDTO> getFans() {
        log.info("GET /api/artists/analytics/fans");
        return ResponseEntity.ok(analyticsService.getFans());
    }
}