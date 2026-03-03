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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

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

        long totalSongs = songRepository.findAllByArtistId(artist.getId()).size();
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
    // Returns users who played this artist's songs the most
    // Default top 10 listeners

    @Transactional(readOnly = true)
    public AnalyticsDTO getTopListeners() {

        Artist artist = getCurrentArtist();

        log.info("Fetching top listeners for artistId={}", artist.getId());

        List<Object[]> results = playEventRepository
                .findTopListenersByArtistId(artist.getId());

        // Limit to top 10 listeners
        List<AnalyticsDTO.TopListenerDTO> topListeners = results.stream()
                .limit(10)
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

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    // Get currently logged-in user via SecurityUtils
    private Artist getCurrentArtist() {
        User user = securityUtils.getCurrentUser();

        return artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artist", "userId", user.getId()));
    }
}