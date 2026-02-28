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

        // NOTE FOR MEMBER 3:
// Song.genre is a plain String (VARCHAR in songs table), NOT a FK to the Genre entity.
// Use findByGenreIgnoreCase for ?genre= filter in SongController — plain string match only.
// Do NOT attempt to join or resolve to Genre objects from this field.

    Page<Song> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Song> findByGenreIgnoreCase(String genre, Pageable pageable);
}