package com.revplay.controller;

import com.revplay.dto.FavoriteDTO;
import com.revplay.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private static final Logger log =
            LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // -------------------------
    // ADD FAVORITE
    // -------------------------
    @PostMapping("/{songId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long songId) {

        log.info("POST /api/favorites/{}", songId);

        favoriteService.addFavorite(songId);

        return ResponseEntity.ok().build();
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

        return ResponseEntity.ok(
                favoriteService.getMyFavorites()
        );
    }
}