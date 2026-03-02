package com.revplay.service;

import com.revplay.dto.AlbumDTO;
import com.revplay.dto.SongDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlbumService {

    // Create a new album for the logged-in artist
    AlbumDTO createAlbum(AlbumDTO request);

    // Update an existing album owned by the logged-in artist
    AlbumDTO updateAlbum(Long albumId, AlbumDTO request);

    // Delete an album owned by the logged-in artist
    void deleteAlbum(Long albumId);

    // Add a song to an album (both must belong to the logged-in artist)
    void addSongToAlbum(Long albumId, Long songId);

    // Remove a song from an album (sets song's album_id to null)
    void removeSongFromAlbum(Long albumId, Long songId);

    // List all albums of the logged-in artist (paginated)
    Page<AlbumDTO> getMyAlbums(Pageable pageable);

    // List all songs of the logged-in artist (paginated)
    Page<SongDTO> getMySongs(Pageable pageable);

    // Get a single album by ID (must belong to logged-in artist)
    AlbumDTO getMyAlbumById(Long albumId);
}