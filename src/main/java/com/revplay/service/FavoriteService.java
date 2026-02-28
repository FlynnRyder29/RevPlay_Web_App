package com.revplay.service;

import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Favorite;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.FavoriteRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private static final Logger log =
            LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           SongRepository songRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
    }

    // -------------------------
    // Helper: Current User
    // -------------------------

    private User getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User", "username", username));
    }

    // -------------------------
    // ADD FAVORITE
    // -------------------------

    @Transactional
    public void addFavorite(Long songId) {

        User currentUser = getCurrentUser();

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Song", "id", songId));

        if (favoriteRepository.existsByUser_IdAndSong_Id(
                currentUser.getId(), songId)) {

            log.debug("Song {} already favorited by user {}",
                    songId, currentUser.getId());
            return; // Prevent duplicate favorites
        }

        Favorite favorite = new Favorite();
        favorite.setUser(currentUser);
        favorite.setSong(song);

        favoriteRepository.save(favorite);

        log.debug("Added favorite: user={}, song={}",
                currentUser.getId(), songId);
    }

    // -------------------------
    // REMOVE FAVORITE
    // -------------------------

    @Transactional
    public void removeFavorite(Long songId) {

        User currentUser = getCurrentUser();

        if (!favoriteRepository.existsByUser_IdAndSong_Id(
                currentUser.getId(), songId)) {

            throw new ResourceNotFoundException(
                    "Favorite", "songId", songId);
        }

        favoriteRepository.deleteByUser_IdAndSong_Id(
                currentUser.getId(), songId);

        log.debug("Removed favorite: user={}, song={}",
                currentUser.getId(), songId);
    }

    // -------------------------
    // GET MY FAVORITES
    // -------------------------

    public List<Favorite> getMyFavorites() {

        User currentUser = getCurrentUser();

        log.debug("Fetching favorites for user {}",
                currentUser.getId());

        return favoriteRepository
                .findByUser_Id(currentUser.getId());
    }
}