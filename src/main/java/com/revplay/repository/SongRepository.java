package com.revplay.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.revplay.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {

    Page<Song> findAll(Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.artist.id = :artistId")
    Page<Song> findByArtistId(@Param("artistId") Long artistId, Pageable pageable);

    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.album WHERE s.artist.id = :artistId")
    List<Song> findAllByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT s FROM Song s WHERE s.album.id = :albumId")
    Page<Song> findByAlbumId(@Param("albumId") Long albumId, Pageable pageable);


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