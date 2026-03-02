package com.revplay.controller;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;

@RestController
@RequestMapping("/api/artist/albums")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ARTIST')")
public class ArtistAlbumController {

    private static final Logger log = LoggerFactory.getLogger(ArtistAlbumController.class);

    private static final Set<String> ALLOWED_ALBUM_SORT_FIELDS =
            Set.of("name", "releaseDate", "createdAt");

    private static final Set<String> ALLOWED_SONG_SORT_FIELDS =
            Set.of("title", "releaseDate", "playCount", "createdAt", "duration");

    private final AlbumService albumService;

    // ── CREATE ───────────────────────────────────────────────────────────────

    // POST /api/artist/albums
    @PostMapping
    public ResponseEntity<AlbumDTO> createAlbum(@RequestBody AlbumDTO request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Album name cannot be blank");
        }

        log.info("POST /api/artist/albums - name='{}'", request.getName());

        AlbumDTO created = albumService.createAlbum(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    // PUT /api/artist/albums/{albumId}
    @PutMapping("/{albumId}")
    public ResponseEntity<AlbumDTO> updateAlbum(
            @PathVariable Long albumId,
            @RequestBody AlbumDTO request) {

        log.info("PUT /api/artist/albums/{}", albumId);

        AlbumDTO updated = albumService.updateAlbum(albumId, request);

        return ResponseEntity.ok(updated);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    // DELETE /api/artist/albums/{albumId}
    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId) {

        log.info("DELETE /api/artist/albums/{}", albumId);

        albumService.deleteAlbum(albumId);

        return ResponseEntity.noContent().build();
    }

    // ── ADD SONG TO ALBUM ────────────────────────────────────────────────────

    // POST /api/artist/albums/{albumId}/songs/{songId}
    @PostMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<Void> addSongToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId) {

        log.info("POST /api/artist/albums/{}/songs/{}", albumId, songId);

        albumService.addSongToAlbum(albumId, songId);

        return ResponseEntity.noContent().build();
    }

    // ── REMOVE SONG FROM ALBUM ───────────────────────────────────────────────

    // DELETE /api/artist/albums/{albumId}/songs/{songId}
    @DeleteMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId) {

        log.info("DELETE /api/artist/albums/{}/songs/{}", albumId, songId);

        albumService.removeSongFromAlbum(albumId, songId);

        return ResponseEntity.noContent().build();
    }

    // ── LIST MY ALBUMS ───────────────────────────────────────────────────────

    // GET /api/artist/albums
    @GetMapping
    public ResponseEntity<Page<AlbumDTO>> getMyAlbums(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_ALBUM_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/artist/albums - page={}, size={}, sortBy={}", page, size, safeSortBy);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(albumService.getMyAlbums(pageable));
    }

    // ── LIST MY SONGS ────────────────────────────────────────────────────────

    // GET /api/artist/albums/songs
    @GetMapping("/songs")
    public ResponseEntity<Page<SongDTO>> getMySongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SONG_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/artist/albums/songs - page={}, size={}, sortBy={}", page, size, safeSortBy);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(albumService.getMySongs(pageable));
    }

    // ── GET MY ALBUM BY ID ───────────────────────────────────────────────────

    // GET /api/artist/albums/{albumId}
    @GetMapping("/{albumId}")
    public ResponseEntity<AlbumDTO> getMyAlbumById(@PathVariable Long albumId) {

        log.info("GET /api/artist/albums/{}", albumId);

        return ResponseEntity.ok(albumService.getMyAlbumById(albumId));
    }
}