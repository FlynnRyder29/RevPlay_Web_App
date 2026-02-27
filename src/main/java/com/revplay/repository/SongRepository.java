package com.revplay.repository;

import com.revplay.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    Page<Song> findAll(Pageable pageable);

    Page<Song> findByArtistId(Long artistId, Pageable pageable);

    Page<Song> findByAlbumId(Long albumId, Pageable pageable);

    Page<Song> findByVisibility(Song.Visibility visibility, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.genre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.artist.artistName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Song> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}