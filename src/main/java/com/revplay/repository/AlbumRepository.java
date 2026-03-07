package com.revplay.repository;

import com.revplay.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Existing
    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.artist WHERE a.artist.id = :artistId")
    List<Album> findAllByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT a FROM Album a WHERE a.artist.id = :artistId")
    Page<Album> findByArtistId(@Param("artistId") Long artistId, Pageable pageable);

    Page<Album> findAll(Pageable pageable);

    @Query("SELECT COUNT(s) FROM Song s WHERE s.album.id = :albumId")
    int countSongsByAlbumId(@Param("albumId") Long albumId);

    // ═══ NEW: Search albums by name or artist name ═══
    @Query("SELECT a FROM Album a WHERE " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.artist.artistName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Album> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}