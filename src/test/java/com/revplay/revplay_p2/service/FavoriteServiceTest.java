package com.revplay.revplay_p2.service;

import com.revplay.revplay_p2.model.Favorite;
import com.revplay.revplay_p2.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void addFavorite_WhenNotAlreadyFavorite_ShouldSave() {
        when(favoriteRepository.findByUserIdAndSongId(1L, 101L)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Favorite result = favoriteService.addFavorite(1L, 101L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(101L, result.getSongId());
        assertNotNull(result.getCreatedAt());
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void addFavorite_WhenAlreadyFavorite_ShouldThrowException() {
        Favorite existing = new Favorite();
        existing.setUserId(1L);
        existing.setSongId(101L);
        when(favoriteRepository.findByUserIdAndSongId(1L, 101L)).thenReturn(Optional.of(existing));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.addFavorite(1L, 101L);
        });

        assertEquals("Song already favorited", exception.getMessage());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void removeFavorite_WhenExists_ShouldDelete() {
        Favorite favorite = new Favorite();
        favorite.setId(1L);
        when(favoriteRepository.findByUserIdAndSongId(1L, 101L)).thenReturn(Optional.of(favorite));
        doNothing().when(favoriteRepository).delete(favorite);

        favoriteService.removeFavorite(1L, 101L);

        verify(favoriteRepository, times(1)).delete(favorite);
    }

    @Test
    void removeFavorite_WhenNotExists_ShouldThrowException() {
        when(favoriteRepository.findByUserIdAndSongId(1L, 101L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            favoriteService.removeFavorite(1L, 101L);
        });

        assertEquals("Favorite not found", exception.getMessage());
        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void getMyFavorites_ShouldReturnList() {
        Favorite fav1 = new Favorite();
        fav1.setUserId(1L);
        fav1.setSongId(101L);
        Favorite fav2 = new Favorite();
        fav2.setUserId(1L);
        fav2.setSongId(102L);
        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of(fav1, fav2));

        List<Favorite> result = favoriteService.getMyFavorites(1L);

        assertEquals(2, result.size());
        verify(favoriteRepository, times(1)).findByUserId(1L);
    }
}