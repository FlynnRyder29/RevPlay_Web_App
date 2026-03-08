package com.revplay.controller;

import com.revplay.dto.SongDTO;
import com.revplay.model.Genre;
import com.revplay.repository.GenreRepository;
import com.revplay.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LibraryController {

    private static final Logger log = LoggerFactory.getLogger(LibraryController.class);

    private final SongService songService;
    private final GenreRepository genreRepository;

    /**
     * GET /library — Song browsing with search + multi-filter (genre, artist, album, year).
     */
    @GetMapping("/library")
    public String showLibrary(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        log.info("GET /library — q='{}', genre='{}', artist='{}', album='{}', year={}, page={}",
                q, genre, artist, album, year, page);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SongDTO> songs;

        boolean hasSearch = q != null && !q.isBlank();
        boolean hasFilters = (genre != null && !genre.isBlank())
                || (artist != null && !artist.isBlank())
                || (album != null && !album.isBlank())
                || year != null;

        if (hasSearch && !hasFilters) {
            // Search-only mode
            songs = songService.searchSongs(q.trim(), pageable);
        } else if (hasFilters) {
            // Filter mode (may also have search — filter takes priority)
            songs = songService.filterSongs(genre, artist, album, year, pageable);
        } else {
            // Browse all
            songs = songService.getAllSongs(pageable);
        }

        // Genres for the filter dropdown
        List<Genre> genres = genreRepository.findAll();

        model.addAttribute("songs", songs);
        model.addAttribute("genres", genres);
        model.addAttribute("currentQuery", q);
        model.addAttribute("currentGenre", genre);
        model.addAttribute("currentArtist", artist);
        model.addAttribute("currentAlbum", album);
        model.addAttribute("currentYear", year);

        return "library";
    }

    /**
     * GET /player — Dedicated full-screen player page.
     */
    @GetMapping("/player")
    public String showPlayer() {
        return "player";
    }
}