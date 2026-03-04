package com.revplay.service;

import com.revplay.dto.AnalyticsDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.Song;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    // Default limit for top listeners — DB-level, not stream-level
    private static final int TOP_LISTENERS_LIMIT = 10;

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

        // DB-level COUNT — avoids loading all songs into memory
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

    // ── FANS WHO FAVORITED ────────────────────────────────────────────────────
    // GET /api/artists/analytics/songs/{id}/fans
    // Returns users who favorited a specific song of the logged-in artist

    @Transactional(readOnly = true)
    public AnalyticsDTO getSongFans(Long songId) {

        Artist artist = getCurrentArtist();

        log.info("Fetching fans for songId={}, artistId={}", songId, artist.getId());

        // Verify song exists and belongs to this artist — ownership check
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));

        if (!song.getArtist().getId().equals(artist.getId())) {
            throw new BadRequestException(
                    "Song does not belong to the logged-in artist");
        }

        List<Object[]> results = favoriteRepository.findFansBySongId(songId);

        List<AnalyticsDTO.FanDTO> fans = results.stream()
                .map(row -> AnalyticsDTO.FanDTO.builder()
                        .userId(((Number) row[0]).longValue())
                        .username((String) row[1])
                        .displayName((String) row[2])
                        .favoritedAt(row[3] != null ? row[3].toString() : null)
                        .build())
                .toList();

        log.info("Fans fetched — songId={}, fanCount={}", songId, fans.size());

        return AnalyticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getArtistName())
                .fans(fans)
                .build();
    }

    // ── LISTENING TRENDS ──────────────────────────────────────────────────────
    // GET /api/artists/analytics/trends?period=daily|weekly|monthly
    // Returns play count grouped by time period for the logged-in artist

    @Transactional(readOnly = true)
    public AnalyticsDTO getTrends(String period) {

        Artist artist = getCurrentArtist();

        log.info("Fetching {} trends for artistId={}", period, artist.getId());

        // Validate period parameter
        if (period == null || period.isBlank()) {
            throw new BadRequestException(
                    "Period cannot be blank. Allowed values: daily, weekly, monthly");
        }

        List<Object[]> results = switch (period.toLowerCase()) {
            case "daily"   -> playEventRepository.findDailyTrendsByArtistId(artist.getId());
            case "weekly"  -> playEventRepository.findWeeklyTrendsByArtistId(artist.getId());
            case "monthly" -> playEventRepository.findMonthlyTrendsByArtistId(artist.getId());
            default -> throw new BadRequestException(
                    "Invalid period: '" + period + "'. Allowed values: daily, weekly, monthly");
        };

        List<AnalyticsDTO.TrendDTO> trends = results.stream()
                .map(row -> AnalyticsDTO.TrendDTO.builder()
                        .period((String) row[0])
                        .playCount(((Number) row[1]).longValue())
                        .build())
                .toList();

        log.info("{} trends fetched — artistId={}, buckets={}",
                period, artist.getId(), trends.size());

        return AnalyticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getArtistName())
                .period(period.toLowerCase())
                .trends(trends)
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