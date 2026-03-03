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
import com.revplay.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    private final PlaylistService playlistService;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongService songService;
    private final ArtistCatalogService artistCatalogService;
    private final ArtistService artistService;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;

    // ═══════════════════════════════════════════
    //  PLAYLISTS PAGE
    // ═══════════════════════════════════════════

    /**
     * GET /playlists — Show user's playlists list
     */
    @GetMapping("/playlists")
    public String showPlaylists(Model model) {
        log.info("GET /playlists");

        List<PlaylistDTO> myPlaylists = playlistService.getMyPlaylists();
        model.addAttribute("playlists", myPlaylists);
        model.addAttribute("viewMode", "list");

        return "playlist";
    }

    /**
     * GET /playlists/{id} — Show a specific playlist with its songs
     */
    @GetMapping("/playlists/{id}")
    public String showPlaylistDetail(@PathVariable Long id, Model model) {
        log.info("GET /playlists/{}", id);

        PlaylistDTO playlist = playlistService.getPlaylistById(id);

        // Fetch songs in this playlist ordered by position
        List<PlaylistSong> playlistSongs = playlistSongRepository
                .findByPlaylist_IdOrderByPosition(id);

        // Map to SongDTOs for the template
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

        model.addAttribute("playlist", playlist);
        model.addAttribute("songs", songs);
        model.addAttribute("viewMode", "detail");

        return "playlist";
    }

    // ═══════════════════════════════════════════
    //  SEARCH PAGE
    // ═══════════════════════════════════════════

    /**
     * GET /search?q=keyword — Search results page
     */
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
    //  PUBLIC ARTIST PROFILE PAGE
    // ═══════════════════════════════════════════

    /**
     * GET /artist/{id} — Public artist profile
     */
    @GetMapping("/artist/{id}")
    public String showArtistProfile(@PathVariable Long id, Model model) {
        log.info("GET /artist/{}", id);

        ArtistDTO artist = artistCatalogService.getArtistById(id);
        model.addAttribute("artist", artist);

        return "artist-profile";
    }

    // ═══════════════════════════════════════════
    //  ARTIST DASHBOARD (ROLE_ARTIST only)
    // ═══════════════════════════════════════════

    /**
     * GET /artist/dashboard — Artist's own dashboard
     */
    @GetMapping("/artist/dashboard")
    public String showArtistDashboard(Model model) {
        log.info("GET /artist/dashboard");

        // Get current user's artist profile
        var profile = artistService.getMyProfile();
        model.addAttribute("profile", profile);

        // Get artist's songs
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        var user = userRepository.findByEmailOrUsername(username, username).orElse(null);

        if (user != null) {
            var artist = artistRepository.findByUserId(user.getId()).orElse(null);
            if (artist != null) {
                ArtistDTO artistDetail = artistCatalogService.getArtistById(artist.getId());
                model.addAttribute("songs", artistDetail.getSongs());
                model.addAttribute("albums", artistDetail.getAlbums());

                // Calculate stats
                long totalPlays = 0;
                if (artistDetail.getSongs() != null) {
                    totalPlays = artistDetail.getSongs().stream()
                            .mapToLong(s -> s.getPlayCount() != null ? s.getPlayCount() : 0)
                            .sum();
                }
                model.addAttribute("totalSongs",
                        artistDetail.getSongs() != null ? artistDetail.getSongs().size() : 0);
                model.addAttribute("totalAlbums",
                        artistDetail.getAlbums() != null ? artistDetail.getAlbums().size() : 0);
                model.addAttribute("totalPlays", totalPlays);
            }
        }

        return "artist-dashboard";
    }
}
