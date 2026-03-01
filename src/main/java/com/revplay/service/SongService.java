package com.revplay.service;

import com.revplay.dto.SongDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.exception.UnauthorizedAccessException;
import com.revplay.model.Song;
import com.revplay.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongService {

    private static final Logger log = LoggerFactory.getLogger(SongService.class);

    private final SongRepository songRepository;

    // ✅ FIXED — Removed duplicate return
    @Transactional(readOnly = true)
    public Page<SongDTO> getAllSongs(Pageable pageable) {
        log.debug("Fetching all songs, page={}", pageable.getPageNumber());
        return songRepository.findAll(pageable).map(this::mapToDTO);
    }

    // ✅ ADDED (Was missing per TL review)
    @Transactional(readOnly = true)
    public SongDTO getSongById(Long id) {
        log.debug("Fetching song by id={}", id);

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", "id", id));

        return mapToDTO(song);
    }

    // ✅ ADDED (Was missing per TL review)
    @Transactional(readOnly = true)
    public Page<SongDTO> searchSongs(String keyword, Pageable pageable) {
        log.debug("Searching songs by keyword='{}'", keyword);
        return songRepository.searchByKeyword(keyword, pageable).map(this::mapToDTO);
    }

    // ✅ FIXED — Ensure all required fields are mapped
    private SongDTO mapToDTO(Song song) {
        return SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .genre(song.getGenre())
                .duration(song.getDuration())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .releaseDate(song.getReleaseDate())
                .playCount(song.getPlayCount())
                .visibility(song.getVisibility() != null ? song.getVisibility().name() : null)
                .artistId(song.getArtist().getId())
                .artistName(song.getArtist().getArtistName())
                .albumId(song.getAlbum() != null ? song.getAlbum().getId() : null)
                .albumName(song.getAlbum() != null ? song.getAlbum().getName() : null)
                .createdAt(song.getCreatedAt())
                .build();
    }

    // ✅ Replace AccessDeniedException if used elsewhere in this class
    private void validateOwnership(Long artistUserId, Long currentUserId) {
        if (!artistUserId.equals(currentUserId)) {
            throw new UnauthorizedAccessException(
                    "You are not allowed to modify this song"
            );
        }
    }
}