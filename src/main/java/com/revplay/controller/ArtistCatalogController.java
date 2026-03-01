package com.revplay.controller;

import com.revplay.dto.ArtistDTO;
import com.revplay.service.ArtistCatalogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistCatalogController {

    private static final Logger log = LoggerFactory.getLogger(ArtistCatalogController.class);
    private final ArtistCatalogService artistCatalogService;

    // GET /api/artists — list all artists (paginated)
    @GetMapping
    public ResponseEntity<Page<ArtistDTO>> getAllArtists(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all artists, page={}", pageable.getPageNumber());
        return ResponseEntity.ok(artistCatalogService.getAllArtists(pageable));
    }

    // GET /api/artists/{id} — public artist profile with songs & albums
    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Long id) {
        log.info("Fetching artist profile for id={}", id);
        return ResponseEntity.ok(artistCatalogService.getArtistById(id));
    }
}
