package com.revplay.controller;

import com.revplay.dto.SongDTO;
import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.exception.BadRequestException;
import com.revplay.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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

    /* =========================================================
       EXISTING ENDPOINTS (UNCHANGED)
       ========================================================= */

    @GetMapping
    public ResponseEntity<Page<SongDTO>> getAllSongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/songs - page={}, size={}, sortBy={}", page, size, safeSortBy);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(songService.getAllSongs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Long id) {
        log.info("GET /api/songs/{}", id);
        return ResponseEntity.ok(songService.getSongById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SongDTO>> searchSongs(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("Search keyword cannot be blank");
        }

        log.info("GET /api/songs/search - keyword='{}'", keyword);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(songService.searchSongs(keyword.trim(), pageable));
    }

    /* =========================================================
       DAY 4 ENDPOINTS (ARTIST ONLY)
       ========================================================= */

    // CREATE SONG
    @PostMapping
    public ResponseEntity<SongDTO> createSong(
            @Valid @RequestBody SongCreateRequest request) {

        log.info("POST /api/songs - Creating new song");

        SongDTO createdSong = songService.createSong(request);
        return new ResponseEntity<>(createdSong, HttpStatus.CREATED);
    }

    // UPDATE SONG
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(
            @PathVariable Long id,
            @Valid @RequestBody SongUpdateRequest request) {

        log.info("PUT /api/songs/{} - Updating song", id);

        return ResponseEntity.ok(songService.updateSong(id, request));
    }

    // DELETE SONG
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {

        log.info("DELETE /api/songs/{} - Deleting song", id);

        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    // UPDATE VISIBILITY
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<SongDTO> updateVisibility(
            @PathVariable Long id,
            @RequestParam String visibility) {

        if (visibility == null || visibility.isBlank()) {
            throw new BadRequestException("Visibility cannot be blank");
        }

        log.info("PATCH /api/songs/{}/visibility - New visibility={}", id, visibility);

        return ResponseEntity.ok(songService.updateVisibility(id, visibility));
    }
}