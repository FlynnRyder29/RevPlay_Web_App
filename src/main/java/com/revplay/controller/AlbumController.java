package com.revplay.controller;

import com.revplay.dto.AlbumDTO;
import com.revplay.service.AlbumCatalogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private static final Logger log = LoggerFactory.getLogger(AlbumController.class);

    private final AlbumCatalogService albumCatalogService;

    // GET /api/albums — paginated list of all albums
    @GetMapping
    public ResponseEntity<Page<AlbumDTO>> getAllAlbums(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /api/albums - page={}", pageable.getPageNumber());
        return ResponseEntity.ok(albumCatalogService.getAllAlbums(pageable));
    }

    // GET /api/albums/{id} — album detail with full tracklist
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDTO> getAlbumById(@PathVariable Long id) {
        log.info("GET /api/albums/{}", id);
        return ResponseEntity.ok(albumCatalogService.getAlbumById(id));
    }
}
