package com.revplay.controller;

import com.revplay.dto.FavoriteDTO;
import com.revplay.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;

    // -------------------------
    // ADD FAVORITE
    // 201 Created — a new resource was created, not just acknowledged.
    // -------------------------

    @PostMapping("/{songId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long songId) {

        log.info("POST /api/favorites/{}", songId);

        favoriteService.addFavorite(songId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // -------------------------
    // REMOVE FAVORITE
    // -------------------------

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long songId) {

        log.info("DELETE /api/favorites/{}", songId);

        favoriteService.removeFavorite(songId);

        return ResponseEntity.noContent().build();
    }

    // -------------------------
    // GET MY FAVORITES
    // -------------------------

    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getMyFavorites() {

        log.info("GET /api/favorites");

        return ResponseEntity.ok(favoriteService.getMyFavorites());
    }
}