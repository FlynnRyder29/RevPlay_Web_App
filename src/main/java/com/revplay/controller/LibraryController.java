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
     * GET / — Home page with recently added songs.
     */
//    @GetMapping("/")
//    public String showHome(Model model) {
//        log.info("GET / — Home page");
//
//        // Fetch the 6 most recently added songs for the "Recently Added" section
//        Pageable pageable = PageRequest.of(0, 6, Sort.by("createdAt").descending());
//        Page<SongDTO> recentSongs = songService.getAllSongs(pageable);
//
//        model.addAttribute("recentSongs", recentSongs.getContent());
//
//        return "index";
//    }

    /**
     * GET /library — Song browsing page with search, genre filter, and pagination.
     *
     * Query params:
     *   q     — search keyword (optional)
     *   genre — filter by genre name (optional)
     *   page  — page number, 0-indexed (default 0)
     *   size  — page size (default 20)
     */
    @GetMapping("/library")
    public String showLibrary(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        log.info("GET /library — q='{}', genre='{}', page={}, size={}", q, genre, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SongDTO> songs;

        if (q != null && !q.isBlank()) {
            // Search mode
            songs = songService.searchSongs(q.trim(), pageable);
        } else if (genre != null && !genre.isBlank()) {
            // Filter by genre
            songs = songService.filterSongs(genre, null, null, null, pageable);
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

        return "library";
    }

    /**
     * GET /player — Dedicated full-screen player page (optional).
     */
    @GetMapping("/player")
    public String showPlayer() {
        return "player";
    }
}
