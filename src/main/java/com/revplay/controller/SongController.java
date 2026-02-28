package com.revplay.controller;

import com.revplay.dto.SongDTO;
import com.revplay.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private static final Logger log = LoggerFactory.getLogger(SongController.class);

    private final SongService songService;

    // GET /api/songs?page=0&size=20&sort=title,asc
    @GetMapping
    public ResponseEntity<Page<SongDTO>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/songs - page={}, size={}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SongDTO> songs = songService.getAllSongs(pageable);
        return ResponseEntity.ok(songs);
    }

    // GET /api/songs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Long id) {
        log.info("GET /api/songs/{}", id);
        SongDTO song = songService.getSongById(id);
        return ResponseEntity.ok(song);
    }

    // GET /api/songs/search?q=keyword&page=0&size=20
    @GetMapping("/search")
    public ResponseEntity<Page<SongDTO>> searchSongs(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/songs/search - keyword={}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<SongDTO> results = songService.searchSongs(keyword, pageable);
        return ResponseEntity.ok(results);
    }
}
