package com.revplay.mapper;

import com.revplay.dto.SongDTO;
import com.revplay.model.Song;
import org.springframework.stereotype.Component;

/**
 * Shared Song mapper component.
 * Eliminates duplicate mapToDTO logic across SongService,
 * AlbumServiceImpl, and AlbumCatalogService.
 *
 * Usage: Inject via @RequiredArgsConstructor and call songMapper.toDTO(song)
 */
@Component
public class SongMapper {

    /**
     * Maps a Song entity to SongDTO.
     * Safe to call inside any @Transactional context.
     * All lazy-loaded fields (artist, album) are accessed here —
     * ensure this is called within an active transaction.
     */
    public SongDTO toDTO(Song song) {
        return SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .genre(song.getGenre())
                .duration(song.getDuration())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .releaseDate(song.getReleaseDate())
                .playCount(song.getPlayCount())
                .visibility(song.getVisibility() != null
                        ? song.getVisibility().name() : null)
                .artistId(song.getArtist() != null
                        ? song.getArtist().getId() : null)
                .artistName(song.getArtist() != null
                        ? song.getArtist().getArtistName() : "Unknown")
                .albumId(song.getAlbum() != null
                        ? song.getAlbum().getId() : null)
                .albumName(song.getAlbum() != null
                        ? song.getAlbum().getName() : null)
                .createdAt(song.getCreatedAt())
                .build();
    }
}