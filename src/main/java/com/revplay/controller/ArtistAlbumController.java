package com.revplay.controller;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.BadRequestException;
import com.revplay.service.AlbumService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

import java.util.Set;

@RestController
@RequestMapping("/api/artists/albums")
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

    // POST /api/artists/albums
    @PostMapping
    public ResponseEntity<AlbumDTO> createAlbum(
            @Valid @RequestBody AlbumDTO request) {

        log.info("POST /api/artists/albums - name='{}'", request.getName());

        AlbumDTO created = albumService.createAlbum(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    // PUT /api/artists/albums/{albumId}
    @PutMapping("/{albumId}")
    public ResponseEntity<AlbumDTO> updateAlbum(
            @PathVariable Long albumId,
            @RequestBody AlbumDTO request) {

        log.info("PUT /api/artists/albums/{}", albumId);

        AlbumDTO updated = albumService.updateAlbum(albumId, request);

        return ResponseEntity.ok(updated);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    // DELETE /api/artists/albums/{albumId}
    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId) {

        log.info("DELETE /api/artists/albums/{}", albumId);

        albumService.deleteAlbum(albumId);

        return ResponseEntity.noContent().build();
    }

    // ── ADD SONG TO ALBUM ────────────────────────────────────────────────────

    // POST /api/artists/albums/{albumId}/songs/{songId}
    @PostMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<Void> addSongToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId) {

        log.info("POST /api/artists/albums/{}/songs/{}", albumId, songId);

        albumService.addSongToAlbum(albumId, songId);

        return ResponseEntity.noContent().build();
    }

    // ── REMOVE SONG FROM ALBUM ───────────────────────────────────────────────

    // DELETE /api/artists/albums/{albumId}/songs/{songId}
    @DeleteMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId) {

        log.info("DELETE /api/artists/albums/{}/songs/{}", albumId, songId);

        albumService.removeSongFromAlbum(albumId, songId);

        return ResponseEntity.noContent().build();
    }

    // ── LIST MY ALBUMS ───────────────────────────────────────────────────────

    // GET /api/artists/albums
    @GetMapping
    public ResponseEntity<Page<AlbumDTO>> getMyAlbums(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_ALBUM_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/artists/albums - page={}, size={}, sortBy={}", page, size, safeSortBy);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(albumService.getMyAlbums(pageable));
    }

    // ── LIST MY SONGS ────────────────────────────────────────────────────────

    // GET /api/artists/albums/songs
    @GetMapping("/songs")
    public ResponseEntity<Page<SongDTO>> getMySongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SONG_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        log.info("GET /api/artists/albums/songs - page={}, size={}, sortBy={}", page, size, safeSortBy);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(albumService.getMySongs(pageable));
    }

    // ── GET MY ALBUM BY ID ───────────────────────────────────────────────────

    // GET /api/artists/albums/{albumId}
    @GetMapping("/{albumId}")
    public ResponseEntity<AlbumDTO> getMyAlbumById(@PathVariable Long albumId) {

        log.info("GET /api/artists/albums/{}", albumId);

        return ResponseEntity.ok(albumService.getMyAlbumById(albumId));
    }
}