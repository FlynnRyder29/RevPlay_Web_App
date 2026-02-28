package com.revplay.service;

import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Favorite;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.FavoriteRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import com.revplay.util.SecurityUtils;
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
                           SongRepository songRepository, SecurityUtils securityUtils) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.securityUtils = securityUtils;
    }

    // -------------------------
    // Helper: Current User
    // -------------------------

    private final SecurityUtils securityUtils;

    // usage:
    User currentUser = securityUtils.getCurrentUser();

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

    public List<FavoriteDTO> getMyFavorites() {
        User currentUser = getCurrentUser();
        return favoriteRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private FavoriteDTO toDTO(Favorite f) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(f.getId());
        dto.setSongId(f.getSong().getId());
        dto.setSongTitle(f.getSong().getTitle());
        dto.setArtistName(f.getSong().getArtist().getArtistName());
        dto.setCoverImageUrl(f.getSong().getCoverImageUrl());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }
}