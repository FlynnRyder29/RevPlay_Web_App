package com.revplay.controller;

import com.revplay.dto.ArtistDTO;
import com.revplay.dto.PlaylistDTO;
import com.revplay.dto.SongDTO;
import com.revplay.model.PlaylistSong;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.PlaylistSongRepository;
import com.revplay.repository.UserRepository;
import com.revplay.service.ArtistCatalogService;
import com.revplay.service.ArtistService;
import com.revplay.service.PlaylistService;
import com.revplay.service.PlaylistFollowService;
import com.revplay.service.SongService;
import com.revplay.service.FavoriteService;
import com.revplay.service.HistoryService;
import com.revplay.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    private final PlaylistService playlistService;
    private final PlaylistFollowService playlistFollowService;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongService songService;
    private final ArtistCatalogService artistCatalogService;
    private final ArtistService artistService;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final FavoriteService favoriteService;
    private final HistoryService historyService;
    private final SecurityUtils securityUtils;

    // ═══════════════════════════════════════════
    // HOME PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("GET /");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        if (isAuthenticated) {
            // Recently added songs (latest 10)
            Pageable recentPageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<SongDTO> recentPage = songService.getAllSongs(recentPageable);
            model.addAttribute("recentSongs", recentPage.getContent());

            // Trending songs (top 10 by play count)
            List<SongDTO> trendingSongs = songService.getTrendingSongs(10);
            model.addAttribute("trendingSongs", trendingSongs);

            // User's playlists (up to 6)
            try {
                List<PlaylistDTO> myPlaylists = playlistService.getMyPlaylists();
                model.addAttribute("myPlaylists",
                        myPlaylists.size() > 6 ? myPlaylists.subList(0, 6) : myPlaylists);
            } catch (Exception e) {
                log.debug("Could not load playlists for home: {}", e.getMessage());
                model.addAttribute("myPlaylists", Collections.emptyList());
            }

            // Public playlists (up to 6)
            try {
                List<PlaylistDTO> publicPlaylists = playlistService.getPublicPlaylists(null);
                model.addAttribute("publicPlaylists",
                        publicPlaylists.size() > 6 ? publicPlaylists.subList(0, 6) : publicPlaylists);
            } catch (Exception e) {
                log.debug("Could not load public playlists: {}", e.getMessage());
                model.addAttribute("publicPlaylists", Collections.emptyList());
            }

            // Featured artists (up to 8)
            List<ArtistDTO> featuredArtists = artistCatalogService.getFeaturedArtists(8);
            model.addAttribute("featuredArtists", featuredArtists);
        }

        return "index";
    }

    // ═══════════════════════════════════════════
    // PLAYLISTS PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/playlists")
    public String showPlaylists(
            @RequestParam(defaultValue = "mine") String tab,
            @RequestParam(required = false) String search,
            Model model) {
        log.info("GET /playlists tab={} search={}", tab, search);

        List<PlaylistDTO> myPlaylists = playlistService.getMyPlaylists();
        model.addAttribute("playlists", myPlaylists);

        List<PlaylistDTO> publicPlaylists = playlistService.getPublicPlaylists(search);
        model.addAttribute("publicPlaylists", publicPlaylists);

        model.addAttribute("viewMode", "list");
        model.addAttribute("activeTab", tab);
        model.addAttribute("searchQuery", search);

        return "playlist";
    }

    @GetMapping("/playlists/{id}")
    public String showPlaylistDetail(@PathVariable Long id, Model model) {
        log.info("GET /playlists/{}", id);

        PlaylistDTO playlist = playlistService.getPlaylistById(id);

        List<PlaylistSong> playlistSongs = playlistSongRepository
                .findByPlaylist_IdOrderByPosition(id);

        List<SongDTO> songs = playlistSongs.stream()
                .map(ps -> SongDTO.builder()
                        .id(ps.getSong().getId())
                        .title(ps.getSong().getTitle())
                        .genre(ps.getSong().getGenre())
                        .duration(ps.getSong().getDuration())
                        .audioUrl(ps.getSong().getAudioUrl())
                        .coverImageUrl(ps.getSong().getCoverImageUrl())
                        .releaseDate(ps.getSong().getReleaseDate())
                        .playCount(ps.getSong().getPlayCount())
                        .artistId(ps.getSong().getArtist().getId())
                        .artistName(ps.getSong().getArtist().getArtistName())
                        .albumId(ps.getSong().getAlbum() != null ? ps.getSong().getAlbum().getId() : null)
                        .albumName(ps.getSong().getAlbum() != null ? ps.getSong().getAlbum().getName() : null)
                        .createdAt(ps.getSong().getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        Long currentUserId = securityUtils.getCurrentUser().getId();
        boolean isOwner = playlist.getUserId().equals(currentUserId);

        boolean isFollowing = false;
        if (!isOwner) {
            try {
                isFollowing = playlistFollowService.isFollowing(id);
            } catch (Exception e) {
                log.debug("Could not check follow status: {}", e.getMessage());
            }
        }

        model.addAttribute("playlist", playlist);
        model.addAttribute("songs", songs);
        model.addAttribute("viewMode", "detail");
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isFollowing", isFollowing);

        return "playlist";
    }

    // ═══════════════════════════════════════════
    // SEARCH PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/search")
    public String showSearch(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        log.info("GET /search — q='{}'", q);

        Page<SongDTO> results = null;

        if (q != null && !q.isBlank()) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            results = songService.searchSongs(q.trim(), pageable);
        }

        model.addAttribute("results", results);
        model.addAttribute("query", q);

        return "search";
    }

    // ═══════════════════════════════════════════
    // PUBLIC ARTIST PROFILE PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/artist/{id}")
    public String showArtistProfile(@PathVariable Long id, Model model) {
        log.info("GET /artist/{}", id);

        ArtistDTO artist = artistCatalogService.getArtistById(id);
        model.addAttribute("artist", artist);

        return "artist-profile";
    }

    // ═══════════════════════════════════════════
    // ARTIST DASHBOARD & PAGES (ROLE_ARTIST only)
    // ═══════════════════════════════════════════

    @GetMapping("/artist/dashboard")
    public String showArtistDashboard(Model model) {
        log.info("GET /artist/dashboard");

        var profile = artistService.getMyProfile();
        model.addAttribute("profile", profile);

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepository.findByEmailOrUsername(username, username).orElse(null);

        if (user != null) {
            var artist = artistRepository.findByUserId(user.getId()).orElse(null);
            if (artist != null) {
                ArtistDTO artistDetail = artistCatalogService.getArtistById(artist.getId());
                model.addAttribute("songs", artistDetail.getSongs());
                model.addAttribute("albums", artistDetail.getAlbums());

                List<SongDTO> allSongs = songService.getArtistAllSongs(artist.getId());
                model.addAttribute("allSongs", allSongs);

                long totalPlays = 0;
                if (allSongs != null) {
                    totalPlays = allSongs.stream()
                            .mapToLong(s -> s.getPlayCount() != null ? s.getPlayCount() : 0)
                            .sum();
                }
                model.addAttribute("totalSongs", allSongs != null ? allSongs.size() : 0);
                model.addAttribute("totalAlbums",
                        artistDetail.getAlbums() != null ? artistDetail.getAlbums().size() : 0);
                model.addAttribute("totalPlays", totalPlays);
                model.addAttribute("artistId", artist.getId());
            }
        }

        return "artist-dashboard";
    }

    @GetMapping("/artist/songs")
    public String showArtistSongs(Model model) {
        log.info("GET /artist/songs");

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepository.findByEmailOrUsername(username, username).orElse(null);

        if (user != null) {
            var artist = artistRepository.findByUserId(user.getId()).orElse(null);
            if (artist != null) {
                List<SongDTO> allSongs = songService.getArtistAllSongs(artist.getId());
                model.addAttribute("songs", allSongs);

                ArtistDTO artistDetail = artistCatalogService.getArtistById(artist.getId());
                model.addAttribute("albums", artistDetail.getAlbums());
            }
        }

        return "artist-songs";
    }

    @GetMapping("/artist/albums")
    public String showArtistAlbums(Model model) {
        log.info("GET /artist/albums");

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepository.findByEmailOrUsername(username, username).orElse(null);

        if (user != null) {
            var artist = artistRepository.findByUserId(user.getId()).orElse(null);
            if (artist != null) {
                ArtistDTO artistDetail = artistCatalogService.getArtistById(artist.getId());
                model.addAttribute("albums", artistDetail.getAlbums());

                List<SongDTO> allSongs = songService.getArtistAllSongs(artist.getId());
                model.addAttribute("allSongs", allSongs);
            }
        }

        return "artist-albums";
    }

    // ═══════════════════════════════════════════
    // ADMIN, FAVORITES, HISTORY
    // ═══════════════════════════════════════════

    @GetMapping("/admin")
    public String showAdminPanel(Model model) {
        log.info("GET /admin");
        return "admin";
    }

    @GetMapping("/favorites")
    public String showFavoritesPage(Model model) {
        log.info("GET /favorites");

        List<SongDTO> songs = favoriteService.getMyFavorites().stream()
                .map(f -> SongDTO.builder()
                        .id(f.getSongId())
                        .title(f.getSongTitle())
                        .audioUrl(f.getAudioUrl())
                        .artistName(f.getArtistName())
                        .coverImageUrl(f.getCoverImageUrl())
                        .duration(f.getDuration())
                        .build())
                .collect(Collectors.toList());

        model.addAttribute("songs", songs);
        return "favorites";
    }

    @GetMapping("/history")
    public String showHistoryPage(Model model) {
        log.info("GET /history");

        List<com.revplay.dto.HistoryDTO> historyEntries = historyService.getAllHistory();
        model.addAttribute("historyEntries", historyEntries);

        List<SongDTO> songs = historyEntries.stream()
                .map(h -> SongDTO.builder()
                        .id(h.getSongId())
                        .title(h.getSongTitle())
                        .audioUrl(h.getAudioUrl())
                        .artistName(h.getArtistName())
                        .coverImageUrl(h.getCoverImageUrl())
                        .build())
                .collect(Collectors.toList());
        model.addAttribute("songs", songs);

        return "history";
    }
}