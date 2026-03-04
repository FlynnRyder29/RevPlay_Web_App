package com.revplay.service;

import com.revplay.dto.AnalyticsDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.FavoriteRepository;
import com.revplay.repository.PlayEventRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    // Default limit for top listeners — DB-level, not stream-level
    private static final int TOP_LISTENERS_LIMIT = 10;

    // Lookback windows for trend queries
    private static final int DAILY_LOOKBACK_DAYS    = 30;  // last 30 days
    private static final int WEEKLY_LOOKBACK_WEEKS  = 12;  // last 12 weeks
    private static final int MONTHLY_LOOKBACK_MONTHS = 12; // last 12 months

    private final PlayEventRepository playEventRepository;
    private final FavoriteRepository favoriteRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final SecurityUtils securityUtils;

    // ── OVERVIEW ──────────────────────────────────────────────────────────────
    // GET /api/artists/analytics/overview
    // Returns total songs, total plays, total favorites for logged-in artist

    @Transactional(readOnly = true)
    public AnalyticsDTO getOverview() {

        Artist artist = getCurrentArtist();

        log.info("Fetching analytics overview for artistId={}", artist.getId());

        // 🔴 FIX: DB-level COUNT — avoids loading all songs into memory
        // Previously: songRepository.findAllByArtistId(artist.getId()).size()
        long totalSongs = songRepository.countByArtistId(artist.getId());
        long totalPlays = playEventRepository.countByArtistId(artist.getId());
        long totalFavorites = favoriteRepository.countByArtistId(artist.getId());

        log.info("Overview — artistId={}, totalSongs={}, totalPlays={}, totalFavorites={}",
                artist.getId(), totalSongs, totalPlays, totalFavorites);

        return AnalyticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getArtistName())
                .totalSongs(totalSongs)
                .totalPlays(totalPlays)
                .totalFavorites(totalFavorites)
                .build();
    }

    // ── PER-SONG PLAY COUNTS ──────────────────────────────────────────────────
    // GET /api/artists/analytics/songs
    // Returns play count for each song of the logged-in artist

    @Transactional(readOnly = true)
    public AnalyticsDTO getSongPlayCounts() {

        Artist artist = getCurrentArtist();

        log.info("Fetching per-song play counts for artistId={}", artist.getId());

        List<Object[]> results = playEventRepository
                .findSongPlayCountsByArtistId(artist.getId());

        List<AnalyticsDTO.SongPlayCountDTO> songPlayCounts = results.stream()
                .map(row -> AnalyticsDTO.SongPlayCountDTO.builder()
                        .songId(((Number) row[0]).longValue())
                        .songTitle((String) row[1])
                        .coverImageUrl((String) row[2])
                        .playCount(((Number) row[3]).longValue())
                        .build())
                .toList();

        log.info("Per-song play counts fetched — artistId={}, songCount={}",
                artist.getId(), songPlayCounts.size());

        return AnalyticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getArtistName())
                .songPlayCounts(songPlayCounts)
                .build();
    }

    // ── TOP LISTENERS ─────────────────────────────────────────────────────────
    // GET /api/artists/analytics/top-listeners
    // Returns top 10 users who played this artist's songs the most
    // 🟡 FIX: Limit moved to DB level via PageRequest — removed stream .limit(10)

    @Transactional(readOnly = true)
    public AnalyticsDTO getTopListeners() {

        Artist artist = getCurrentArtist();

        log.info("Fetching top listeners for artistId={}", artist.getId());

        // DB-level limit — only fetches top 10 rows from database
        List<Object[]> results = playEventRepository
                .findTopListenersByArtistId(
                        artist.getId(),
                        PageRequest.of(0, TOP_LISTENERS_LIMIT));

        List<AnalyticsDTO.TopListenerDTO> topListeners = results.stream()
                .map(row -> AnalyticsDTO.TopListenerDTO.builder()
                        .userId(((Number) row[0]).longValue())
                        .username((String) row[1])
                        .displayName((String) row[2])
                        .playCount(((Number) row[3]).longValue())
                        .build())
                .toList();

        log.info("Top listeners fetched — artistId={}, listenerCount={}",
                artist.getId(), topListeners.size());

        return AnalyticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getArtistName())
                .topListeners(topListeners)
                .build();
    }

    // ── LISTENING TRENDS ──────────────────────────────────────────────────────
    // GET /api/artists/analytics/trends?period=daily|weekly|monthly
    //
    // daily   → last 30 days,   grouped by date     (e.g. "2025-03-04")
    // weekly  → last 12 weeks,  grouped by ISO week (e.g. "2025-W10")
    // monthly → last 12 months, grouped by month    (e.g. "2025-03")

    @Transactional(readOnly = true)
    public AnalyticsDTO getListeningTrends(String period) {

        Artist artist = getCurrentArtist();

        log.info("Fetching listening trends — artistId={}, period={}", artist.getId(), period);

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from;
        List<Object[]> results;

        switch (period.toLowerCase()) {
            case "weekly" -> {
                from    = to.minusWeeks(WEEKLY_LOOKBACK_WEEKS);
                results = playEventRepository.findWeeklyTrendsByArtistId(artist.getId(), from, to);
            }
            case "monthly" -> {
                from    = to.minusMonths(MONTHLY_LOOKBACK_MONTHS);
                results = playEventRepository.findMonthlyTrendsByArtistId(artist.getId(), from, to);
            }
            default -> {
                // "daily" is the default fallback — safe for unknown values
                from    = to.minusDays(DAILY_LOOKBACK_DAYS);
                results = playEventRepository.findDailyTrendsByArtistId(artist.getId(), from, to);
            }
        }

        List<AnalyticsDTO.TrendPointDTO> trends = results.stream()
                .map(row -> AnalyticsDTO.TrendPointDTO.builder()
                        .period((String) row[0])
                        .playCount(((Number) row[1]).longValue())
                        .build())
                .toList();

        log.info("Trends fetched — artistId={}, period={}, points={}",
                artist.getId(), period, trends.size());

        return AnalyticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getArtistName())
                .trendPeriod(period.toLowerCase())
                .trends(trends)
                .build();
    }

    // ── FANS WHO FAVORITED ────────────────────────────────────────────────────
    // GET /api/artists/analytics/fans
    // Returns all users who favorited ≥1 of this artist's songs
    // Ordered by favoriteCount DESC — biggest fans appear first

    @Transactional(readOnly = true)
    public AnalyticsDTO getFans() {

        Artist artist = getCurrentArtist();

        log.info("Fetching fans for artistId={}", artist.getId());

        List<Object[]> results = favoriteRepository.findFansByArtistId(artist.getId());

        List<AnalyticsDTO.FanDTO> fans = results.stream()
                .map(row -> AnalyticsDTO.FanDTO.builder()
                        .userId(((Number) row[0]).longValue())
                        .username((String) row[1])
                        .displayName((String) row[2])
                        .profilePictureUrl((String) row[3])
                        .favoriteCount(((Number) row[4]).longValue())
                        .build())
                .toList();

        log.info("Fans fetched — artistId={}, fanCount={}", artist.getId(), fans.size());

        return AnalyticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getArtistName())
                .fans(fans)
                .build();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    // Get currently logged-in user via SecurityUtils
    private Artist getCurrentArtist() {
        User user = securityUtils.getCurrentUser();

        return artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artist", "userId", user.getId()));
    }
}