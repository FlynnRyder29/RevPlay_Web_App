package com.revplay.controller;

import com.revplay.dto.SongDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;
import org.springframework.data.web.PageableDefault;


@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated  // ✅ Required to activate @Min/@Max on @RequestParam
public class SongController {

    private static final Logger log = LoggerFactory.getLogger(SongController.class);

    // ✅ Whitelist of allowed sort fields — prevents arbitrary column injection
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("title", "releaseDate", "playCount", "createdAt", "duration");

    private final SongService songService;

    // GET /api/songs?page=0&size=20&sort=title,asc
    @GetMapping
    public ResponseEntity<Page<SongDTO>> getAllSongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,  // ✅ max 100 per page
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // ✅ Sanitize sortBy — fall back to "createdAt" if not in whitelist
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/songs - page={}, size={}, sortBy={}", page, size, safeSortBy);

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

    // GET /api/songs/search?q=keyword&page=0&size=20
    @GetMapping("/search")
    public ResponseEntity<Page<SongDTO>> searchSongs(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        // ✅ Throws structured exception instead of bare 400
        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("Search keyword cannot be blank");
        }

        log.info("GET /api/songs/search - keyword='{}'", keyword);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(songService.searchSongs(keyword.trim(), pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<SongDTO>> filterSongs(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Integer year,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /api/songs/filter");
        return ResponseEntity.ok(songService.filterSongs(genre, artist, album, year, pageable));
    }

}
