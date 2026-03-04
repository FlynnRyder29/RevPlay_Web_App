package com.revplay.controller;

import com.revplay.dto.AnalyticsDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    // ── FANS WHO FAVORITED (Day 7) ────────────────────────────────────────────

    // GET /api/artists/analytics/songs/{id}/fans
    // Returns users who favorited a specific song of the logged-in artist
    @GetMapping("/songs/{id}/fans")
    public ResponseEntity<AnalyticsDTO> getSongFans(@PathVariable Long id) {
        log.info("GET /api/artists/analytics/songs/{}/fans", id);
        return ResponseEntity.ok(analyticsService.getSongFans(id));
    }

    // ── LISTENING TRENDS (Day 7) ──────────────────────────────────────────────

    // GET /api/artists/analytics/trends?period=daily|weekly|monthly
    // Returns play count grouped by time period for logged-in artist
    @GetMapping("/trends")
    public ResponseEntity<AnalyticsDTO> getTrends(
            @RequestParam String period) {

        if (period == null || period.isBlank()) {
            throw new BadRequestException(
                    "Period cannot be blank. Allowed values: daily, weekly, monthly");
        }

        log.info("GET /api/artists/analytics/trends?period={}", period);

        return ResponseEntity.ok(analyticsService.getTrends(period));
    }
}