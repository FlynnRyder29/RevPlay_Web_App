package com.revplay.controller;

import com.revplay.dto.*;
import com.revplay.model.PlaylistSong;
import com.revplay.model.Song;
import com.revplay.model.Artist;
import com.revplay.model.Album;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.PlaylistSongRepository;
import com.revplay.repository.PlaylistRepository;
import com.revplay.repository.UserRepository;
import com.revplay.service.*;
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
    private final PlaylistRepository playlistRepository;
    private final SongService songService;
    private final ArtistCatalogService artistCatalogService;
    private final AlbumCatalogService albumCatalogService;
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
            Pageable recentPageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<SongDTO> recentPage = songService.getAllSongs(recentPageable);
            model.addAttribute("recentSongs", recentPage.getContent());

            List<SongDTO> trendingSongs = songService.getTrendingSongs(10);
            model.addAttribute("trendingSongs", trendingSongs);

            try {
                List<PlaylistDTO> myPlaylists = playlistService.getMyPlaylists();
                model.addAttribute("myPlaylists",
                        myPlaylists.size() > 6 ? myPlaylists.subList(0, 6) : myPlaylists);
            } catch (Exception e) {
                log.debug("Could not load playlists for home: {}", e.getMessage());
                model.addAttribute("myPlaylists", Collections.emptyList());
            }

            try {
                List<PlaylistDTO> publicPlaylists = playlistService.getPublicPlaylists(null);
                model.addAttribute("publicPlaylists",
                        publicPlaylists.size() > 6 ? publicPlaylists.subList(0, 6) : publicPlaylists);
            } catch (Exception e) {
                log.debug("Could not load public playlists: {}", e.getMessage());
                model.addAttribute("publicPlaylists", Collections.emptyList());
            }

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
                .findByPlaylistIdWithSongDetails(id);

        List<SongDTO> songs = playlistSongs.stream()
                .map(ps -> {
                    Song song = ps.getSong();
                    Artist artist = song.getArtist();
                    Album album = song.getAlbum();

                    return SongDTO.builder()
                            .id(song.getId())
                            .title(song.getTitle())
                            .genre(song.getGenre())
                            .duration(song.getDuration())
                            .audioUrl(song.getAudioUrl())
                            .coverImageUrl(song.getCoverImageUrl())
                            .releaseDate(song.getReleaseDate())
                            .playCount(song.getPlayCount())
                            .artistId(artist.getId())
                            .artistName(artist.getArtistName())
                            .albumId(album != null ? album.getId() : null)
                            .albumName(album != null ? album.getName() : null)
                            .createdAt(song.getCreatedAt())
                            .build();
                })
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
    // SEARCH PAGE (multi-type: songs, artists, albums, playlists)
    // ═══════════════════════════════════════════

    @GetMapping("/search")
    public String showSearch(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        log.info("GET /search — q='{}', type='{}'", q, type);

        if (q != null && !q.isBlank()) {
            String trimmed = q.trim();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Pageable smallPageable = PageRequest.of(0, 6);

            if ("all".equals(type)) {
                // Search all types — show top results from each
                Page<SongDTO> songResults = songService.searchSongs(trimmed, pageable);
                model.addAttribute("songResults", songResults);

                Page<ArtistDTO> artistResults = artistCatalogService.searchArtists(trimmed, smallPageable);
                model.addAttribute("artistResults", artistResults);

                Page<AlbumDTO> albumResults = albumCatalogService.searchAlbums(trimmed, smallPageable);
                model.addAttribute("albumResults", albumResults);

                Page<com.revplay.model.Playlist> playlistPage = playlistRepository
                        .searchPublicByKeyword(trimmed, smallPageable);
                List<PlaylistDTO> playlistResults = playlistPage.getContent().stream()
                        .map(p -> {
                            PlaylistDTO dto = new PlaylistDTO();
                            dto.setId(p.getId());
                            dto.setName(p.getName());
                            dto.setDescription(p.getDescription());
                            dto.setPublicPlaylist(p.isPublicPlaylist());
                            dto.setUserId(p.getUser().getId());
                            dto.setOwnerName(p.getUser().getDisplayName() != null
                                    ? p.getUser().getDisplayName() : p.getUser().getUsername());
                            dto.setCreatedAt(p.getCreatedAt());
                            return dto;
                        })
                        .collect(Collectors.toList());
                model.addAttribute("playlistResults", playlistResults);

                long totalResults = songResults.getTotalElements()
                        + artistResults.getTotalElements()
                        + albumResults.getTotalElements()
                        + playlistPage.getTotalElements();
                model.addAttribute("totalResults", totalResults);

            } else if ("songs".equals(type)) {
                Page<SongDTO> songResults = songService.searchSongs(trimmed, pageable);
                model.addAttribute("songResults", songResults);
            } else if ("artists".equals(type)) {
                Page<ArtistDTO> artistResults = artistCatalogService.searchArtists(trimmed, pageable);
                model.addAttribute("artistResults", artistResults);
            } else if ("albums".equals(type)) {
                Page<AlbumDTO> albumResults = albumCatalogService.searchAlbums(trimmed, pageable);
                model.addAttribute("albumResults", albumResults);
            } else if ("playlists".equals(type)) {
                Page<com.revplay.model.Playlist> playlistPage = playlistRepository
                        .searchPublicByKeyword(trimmed, pageable);
                List<PlaylistDTO> playlistResults = playlistPage.getContent().stream()
                        .map(p -> {
                            PlaylistDTO dto = new PlaylistDTO();
                            dto.setId(p.getId());
                            dto.setName(p.getName());
                            dto.setDescription(p.getDescription());
                            dto.setPublicPlaylist(p.isPublicPlaylist());
                            dto.setUserId(p.getUser().getId());
                            dto.setOwnerName(p.getUser().getDisplayName() != null
                                    ? p.getUser().getDisplayName() : p.getUser().getUsername());
                            dto.setCreatedAt(p.getCreatedAt());
                            return dto;
                        })
                        .collect(Collectors.toList());
                model.addAttribute("playlistResults", playlistResults);
            }
        }

        model.addAttribute("query", q);
        model.addAttribute("activeType", type);

        return "search";
    }

    // ═══════════════════════════════════════════
    // SONG DETAIL PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/songs/{id}")
    public String showSongDetail(@PathVariable Long id, Model model) {
        log.info("GET /songs/{}", id);

        SongDTO song = songService.getSongById(id);
        model.addAttribute("song", song);

        // Format duration
        if (song.getDuration() != null) {
            int mins = song.getDuration() / 60;
            int secs = song.getDuration() % 60;
            model.addAttribute("formattedDuration", String.format("%d:%02d", mins, secs));
        }

        return "song-detail";
    }

    // ═══════════════════════════════════════════
    // ARTISTS BROWSE PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/artists")
    public String showArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        log.info("GET /artists page={}", page);

        Pageable pageable = PageRequest.of(page, size);
        Page<ArtistDTO> artists = artistCatalogService.getAllArtists(pageable);
        model.addAttribute("artists", artists);

        return "artists";
    }

    // ═══════════════════════════════════════════
    // ALBUMS BROWSE PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/albums")
    public String showAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        log.info("GET /albums page={}", page);

        Pageable pageable = PageRequest.of(page, size);
        Page<AlbumDTO> albums = albumCatalogService.getAllAlbums(pageable);
        model.addAttribute("albums", albums);

        return "albums";
    }

    // ═══════════════════════════════════════════
    // ALBUM DETAIL PAGE
    // ═══════════════════════════════════════════

    @GetMapping("/albums/{id}")
    public String showAlbumDetail(@PathVariable Long id, Model model) {
        log.info("GET /albums/{}", id);

        AlbumDTO album = albumCatalogService.getAlbumById(id);
        model.addAttribute("album", album);

        // Calculate total duration
        if (album.getTracks() != null) {
            int totalSeconds = album.getTracks().stream()
                    .filter(t -> t.getDuration() != null)
                    .mapToInt(SongDTO::getDuration)
                    .sum();
            int mins = totalSeconds / 60;
            int secs = totalSeconds % 60;
            model.addAttribute("totalDuration", String.format("%d min %d sec", mins, secs));
        }

        return "album-detail";
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

    @GetMapping("/about")
    public String showAboutPage() {
        log.info("GET /about");
        return "about";
    }

    @GetMapping("/history")
    public String showHistoryPage(Model model) {
        log.info("GET /history");

        List<HistoryDTO> historyEntries = historyService.getAllHistory();
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