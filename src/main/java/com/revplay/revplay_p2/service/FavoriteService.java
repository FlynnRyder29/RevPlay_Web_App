package com.revplay.revplay_p2.service;

import com.revplay.revplay_p2.model.Favorite;
import com.revplay.revplay_p2.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    public Favorite addFavorite(Long userId, Long songId) {
        if (favoriteRepository.findByUserIdAndSongId(userId, songId).isPresent()) {
            throw new RuntimeException("Song already favorited");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setSongId(songId);
        fav.setCreatedAt(LocalDateTime.now());
        return favoriteRepository.save(fav);
    }

    public void removeFavorite(Long userId, Long songId) {
        Favorite fav = favoriteRepository.findByUserIdAndSongId(userId, songId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));
        favoriteRepository.delete(fav);
    }

    public List<Favorite> getMyFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }
}