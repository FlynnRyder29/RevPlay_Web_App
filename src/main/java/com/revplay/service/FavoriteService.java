package com.revplay.service;

import com.revplay.dto.FavoriteDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.BadRequestException;
import com.revplay.model.Favorite;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.FavoriteRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private static final Logger log =
            LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final SongRepository songRepository;
    private final SecurityUtils securityUtils;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           SongRepository songRepository,
                           SecurityUtils securityUtils) {
        this.favoriteRepository = favoriteRepository;
        this.songRepository = songRepository;
        this.securityUtils = securityUtils;
    }

    // -------------------------
    // ADD FAVORITE
    // -------------------------

    // Inside FavoriteService.java — verify these guards exist

    @Transactional
    public void addFavorite(Long songId) {

        User currentUser = securityUtils.getCurrentUser();

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Song", "id", songId));

        // ── DUPLICATE GUARD — must exist ──
        if (favoriteRepository.existsByUser_IdAndSong_Id(
                currentUser.getId(), songId)) {
            throw new BadRequestException(
                    "Song " + songId + " is already in your favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(currentUser);
        favorite.setSong(song);
        favorite.setCreatedAt(LocalDateTime.now());

        favoriteRepository.save(favorite);

        log.debug("User {} favorited song {}",
                currentUser.getId(), songId);
    }

    @Transactional
    public void removeFavorite(Long songId) {

        User currentUser = securityUtils.getCurrentUser();

        Favorite favorite = favoriteRepository
                .findByUser_IdAndSong_Id(currentUser.getId(), songId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Favorite", "songId", songId));

        favoriteRepository.delete(favorite);

        log.debug("User {} unfavorited song {}",
                currentUser.getId(), songId);
    }

    // -------------------------
    // GET MY FAVORITES
    // -------------------------

    @Transactional(readOnly = true)
    public List<FavoriteDTO> getMyFavorites() {

        User currentUser = securityUtils.getCurrentUser();

        log.debug("Fetching favorites for user {}",
                currentUser.getId());

        return favoriteRepository
                .findByUser_Id(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // -------------------------
    // Mapping
    // -------------------------

    private FavoriteDTO toDTO(Favorite favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        dto.setSongId(favorite.getSong().getId());
        dto.setSongTitle(favorite.getSong().getTitle());
        dto.setArtistName(favorite.getSong().getArtist().getArtistName());
        dto.setCoverImageUrl(favorite.getSong().getCoverImageUrl());
        dto.setDuration(favorite.getSong().getDuration());
        dto.setCreatedAt(favorite.getCreatedAt());
        return dto;
    }
}