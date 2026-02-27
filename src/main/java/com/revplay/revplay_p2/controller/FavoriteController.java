package com.revplay.revplay_p2.controller;

import com.revplay.revplay_p2.model.Favorite;
import com.revplay.revplay_p2.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{songId}")
    public ResponseEntity<Favorite> add(@PathVariable Long songId, @RequestParam Long userId) {
        return ResponseEntity.ok(favoriteService.addFavorite(userId, songId));
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> remove(@PathVariable Long songId, @RequestParam Long userId) {
        favoriteService.removeFavorite(userId, songId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Favorite>> getAll(@RequestParam Long userId) {
        return ResponseEntity.ok(favoriteService.getMyFavorites(userId));
    }
}