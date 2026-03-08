package com.revplay.controller;

import com.revplay.dto.SongDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.model.Song;
import com.revplay.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongController {

    private static final Logger log = LoggerFactory.getLogger(SongController.class);

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("title", "releaseDate", "playCount", "createdAt", "duration");

    private final SongService songService;

    // GET /api/songs
    @GetMapping
    public ResponseEntity<Page<SongDTO>> getAllSongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/songs - page={}, size={}, sortBy={}, sortDir={}", page, size, safeSortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(songService.getAllSongs(pageable));
    }

    // GET /api/songs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Long id) {
        log.info("GET /api/songs/{}", id);
        return ResponseEntity.ok(songService.getSongById(id));
    }

    // GET /api/songs/search
    @GetMapping("/search")
    public ResponseEntity<Page<SongDTO>> searchSongs(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("Search keyword cannot be blank");
        }

        log.info("GET /api/songs/search - keyword='{}'", keyword.trim());

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(songService.searchSongs(keyword.trim(), pageable));
    }

    // GET /api/songs/filter
    @GetMapping("/filter")
    public ResponseEntity<Page<SongDTO>> filterSongs(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/songs/filter - genre={}, artist={}, album={}, year={}", genre, artist, album, year);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(songService.filterSongs(genre, artist, album, year, pageable));
    }

    // PATCH /api/songs/{id}/visibility
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<SongDTO> updateVisibility(
            @PathVariable Long id,
            @RequestParam String visibility) {

        if (visibility == null || visibility.isBlank()) {
            throw new BadRequestException("Visibility cannot be blank");
        }

        try {
            Song.Visibility.valueOf(visibility.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid visibility value: " + visibility +
                    ". Allowed values: PUBLIC, UNLISTED, PRIVATE");
        }

        log.info("PATCH /api/songs/{}/visibility -> {}", id, visibility.toUpperCase());

        return ResponseEntity.ok(songService.updateVisibility(id, visibility.toUpperCase()));
    }
}
