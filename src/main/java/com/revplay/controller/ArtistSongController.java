package com.revplay.controller;

import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongDTO;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.model.Song;
import com.revplay.service.FileStorageService;
import com.revplay.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/artists/songs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ARTIST')")
public class ArtistSongController {

    private static final Logger log = LoggerFactory.getLogger(ArtistSongController.class);

    private final SongService songService;
    private final FileStorageService fileStorageService;

    // ── UPLOAD (multipart) ───────────────────────────────────────────────────
    // POST /api/artists/songs
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongDTO> uploadSong(
            @RequestParam("title") String title,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "duration", required = false, defaultValue = "200") Integer duration,
            @RequestParam(value = "albumId", required = false) String albumIdStr,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile) {

        log.info("POST /api/artists/songs (multipart) — title='{}'", title);

        // Store audio file
        String audioUrl = "/uploads/" + fileStorageService.storeFile(audioFile, "audio");

        // Store cover if provided
        String coverImageUrl = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            coverImageUrl = "/uploads/" + fileStorageService.storeFile(coverFile, "covers");
        }

        // Parse optional albumId (form sends empty string for "No Album")
        Long albumId = null;
        if (albumIdStr != null && !albumIdStr.isBlank()) {
            try {
                albumId = Long.parseLong(albumIdStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid albumId: '{}'", albumIdStr);
            }
        }

        SongCreateRequest request = new SongCreateRequest();
        request.setTitle(title);
        request.setGenre(genre);
        request.setDuration(duration != null && duration > 0 ? duration : 200);
        request.setAudioUrl(audioUrl);
        request.setCoverImageUrl(coverImageUrl);
        request.setAlbumId(albumId);
        request.setVisibility(visibility != null && !visibility.isBlank()
                ? Song.Visibility.valueOf(visibility)
                : Song.Visibility.PUBLIC);

        SongDTO created = songService.createSong(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── CREATE (JSON) ────────────────────────────────────────────────────────
    // POST /api/artists/songs (Content-Type: application/json)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongDTO> createSongJson(@RequestBody SongCreateRequest request) {
        log.info("POST /api/artists/songs (JSON) — title='{}'", request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(songService.createSong(request));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    // PUT /api/artists/songs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(
            @PathVariable Long id,
            @RequestBody SongUpdateRequest request) {
        log.info("PUT /api/artists/songs/{}", id);
        return ResponseEntity.ok(songService.updateSong(id, request));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    // DELETE /api/artists/songs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        log.info("DELETE /api/artists/songs/{}", id);
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    // ── VISIBILITY ───────────────────────────────────────────────────────────
    // PUT /api/artists/songs/{id}/visibility?visibility=PUBLIC|UNLISTED|PRIVATE
    @PutMapping("/{id}/visibility")
    public ResponseEntity<SongDTO> updateVisibility(
            @PathVariable Long id,
            @RequestParam String visibility) {
        log.info("PUT /api/artists/songs/{}/visibility — {}", id, visibility);
        return ResponseEntity.ok(songService.updateVisibility(id, visibility));
    }
}